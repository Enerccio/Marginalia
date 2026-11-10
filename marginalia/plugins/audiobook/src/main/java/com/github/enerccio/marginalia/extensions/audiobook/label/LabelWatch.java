package com.github.enerccio.marginalia.extensions.audiobook.label;

import java.util.concurrent.Callable;

/**
 * Who watches the labelling that this thread does: it is told which window of pieces the model is asked about, and the reasoning and the answer as
 * they arrive. Like {@link LabelCancel}, it is set for the time of the call that makes the script of a part (a deep call with no room for another
 * argument): the queue shows it in the window of the conversion.
 */
public final class LabelWatch {

    /** What the labelling reports; called on the thread that labels or on the thread of the request, it must be quick and must not throw. */
    public interface Listener {

        /** The model is asked about the pieces {@code from} to {@code to} (0 based, {@code to} exclusive) of {@code total}. */
        void window(int from, int to, int total);

        /** A piece of what the model wrote: its reasoning, or the answer. */
        void chunk(boolean reasoning, String text);
    }

    private static final ThreadLocal<Listener> CURRENT = new ThreadLocal<>();

    private LabelWatch() {
    }

    /** Runs the work with {@code listener} watching what this thread labels. */
    public static <T> T during(Listener listener, Callable<T> work) throws Exception {
        Listener before = CURRENT.get();
        CURRENT.set(listener);
        try {
            return work.call();
        } finally {
            if (before == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(before);
            }
        }
    }

    /** The watcher of this thread, null when nobody watches. */
    public static Listener current() {
        return CURRENT.get();
    }

    /** Tells the watcher of this thread that a window of pieces is asked about. */
    public static void window(int from, int to, int total) {
        Listener listener = CURRENT.get();
        if (listener != null) {
            listener.window(from, to, total);
        }
    }
}
