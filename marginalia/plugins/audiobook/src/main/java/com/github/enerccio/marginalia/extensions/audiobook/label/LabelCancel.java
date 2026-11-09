package com.github.enerccio.marginalia.extensions.audiobook.label;

import java.util.function.BooleanSupplier;

/**
 * Whether the work that asks the language model on this thread was stopped by the user. The script of a part is made deep inside a call that
 * has no room for another argument (the queue asks a {@code ScriptSource}), so the queue says here, for the time of that call, what to ask.
 */
public final class LabelCancel {

    /** What a stopped labelling throws. */
    public static final class Stopped extends RuntimeException {
        public Stopped() {
            super("The labelling was stopped");
        }
    }

    private static final ThreadLocal<BooleanSupplier> CURRENT = new ThreadLocal<>();

    private LabelCancel() {
    }

    /** Runs the work with {@code stop} as the question of this thread. */
    public static <T> T during(BooleanSupplier stop, java.util.concurrent.Callable<T> work) throws Exception {
        BooleanSupplier before = CURRENT.get();
        CURRENT.set(stop);
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

    /** True when the user stopped what this thread does. */
    public static boolean requested() {
        BooleanSupplier stop = CURRENT.get();
        return stop != null && stop.getAsBoolean();
    }
}
