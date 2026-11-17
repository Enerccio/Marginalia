package com.github.enerccio.marginalia.extensions.audiobook.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * What the language model said while it labelled a part, window by window: the reasoning and the answer, kept in memory with the job (until the
 * jobs that are over are cleared) so that the user can read how the model got to its labels. The text is capped: the oldest windows go first.
 */
public final class LabelTrace {

    /** The most characters of reasoning and answers kept for a job. */
    static final int MAX_CHARS = 400_000;

    /** One request to the model. */
    public record Window(String title, String reasoning, String output) {
    }

    private static final class Entry {
        private final String title;
        private final StringBuilder reasoning = new StringBuilder();
        private final StringBuilder output = new StringBuilder();

        private Entry(String title) {
            this.title = title;
        }
    }

    private final List<Entry> windows = new ArrayList<>();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private final long maxChars;
    private long lastNotified;

    /** A trace capped at {@value #MAX_CHARS} characters (the one a job shows while it runs). */
    public LabelTrace() {
        this(MAX_CHARS);
    }

    /** A trace that keeps at most {@code maxChars} characters of reasoning and answers ({@link Long#MAX_VALUE}: everything). */
    public LabelTrace(long maxChars) {
        this.maxChars = maxChars;
    }

    /** A trace that holds the given windows (one that was stored, for {@code LabelTraceDialog}); nothing is cut off. */
    public static LabelTrace of(List<Window> stored) {
        LabelTrace trace = new LabelTrace(Long.MAX_VALUE);
        for (Window window : stored) {
            Entry entry = new Entry(window.title() == null ? "" : window.title());
            entry.reasoning.append(window.reasoning() == null ? "" : window.reasoning());
            entry.output.append(window.output() == null ? "" : window.output());
            trace.windows.add(entry);
        }
        return trace;
    }

    /** A new request to the model starts: what follows is about it. */
    public synchronized void startWindow(String title) {
        windows.add(new Entry(title));
        notifyListeners(true);
    }

    /** A piece of what the model writes for the window that was started last. */
    public synchronized void append(boolean reasoning, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        if (windows.isEmpty()) {
            windows.add(new Entry(""));
        }
        Entry last = windows.getLast();
        (reasoning ? last.reasoning : last.output).append(text);
        trim();
        notifyListeners(false);
    }

    /** The windows so far, oldest first. */
    public synchronized List<Window> windows() {
        return windows.stream().map(e -> new Window(e.title, e.reasoning.toString(), e.output.toString())).toList();
    }

    public synchronized boolean isEmpty() {
        return windows.isEmpty();
    }

    /** Called when something is added (the chunks are passed on at most five times a second). */
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private void trim() {
        long chars = 0;
        for (Entry e : windows) {
            chars += e.reasoning.length() + e.output.length();
        }
        while (chars > maxChars && windows.size() > 1) {
            Entry first = windows.removeFirst();
            chars -= first.reasoning.length() + first.output.length();
        }
    }

    private void notifyListeners(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now - lastNotified < 200) {
            return;
        }
        lastNotified = now;
        for (Runnable listener : listeners) {
            try {
                listener.run();
            } catch (RuntimeException e) {
                // a window that is gone must not stop the labelling
            }
        }
    }
}
