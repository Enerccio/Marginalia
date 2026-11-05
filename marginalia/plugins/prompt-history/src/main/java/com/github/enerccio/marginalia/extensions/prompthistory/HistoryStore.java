package com.github.enerccio.marginalia.extensions.prompthistory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The last requests of one session (a browser session, or a run of an agent), newest first, and the dialogs that show
 * them and want to hear about changes.
 */
final class HistoryStore {

    /** The number of requests kept per session. */
    static final int LIMIT = 20;

    private static final long THROTTLE_MILLIS = 300;

    private final Deque<HistoryEntry> entries = new ArrayDeque<>();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private final AtomicLong lastNotified = new AtomicLong();

    void add(HistoryEntry entry) {
        synchronized (entries) {
            entries.addFirst(entry);
            while (entries.size() > LIMIT) {
                entries.pollLast();
            }
        }
        changed();
    }

    /**
     * @return a copy of the entries, newest first
     */
    List<HistoryEntry> entries() {
        synchronized (entries) {
            return new ArrayList<>(entries);
        }
    }

    void clear() {
        synchronized (entries) {
            entries.clear();
        }
        changed();
    }

    /** A dialog asks to be told when something changes; the call can come from any thread. */
    void addListener(Runnable listener) {
        listeners.add(listener);
    }

    void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    void changed() {
        lastNotified.set(System.currentTimeMillis());
        listeners.forEach(Runnable::run);
    }

    /** For the pieces of an answer that arrive in a stream: tells at most every few hundred milliseconds. */
    void changedSoon() {
        long last = lastNotified.get();
        long now = System.currentTimeMillis();
        if (now - last >= THROTTLE_MILLIS && lastNotified.compareAndSet(last, now)) {
            listeners.forEach(Runnable::run);
        }
    }
}
