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
    private long lastNotified;

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
        while (chars > MAX_CHARS && windows.size() > 1) {
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
