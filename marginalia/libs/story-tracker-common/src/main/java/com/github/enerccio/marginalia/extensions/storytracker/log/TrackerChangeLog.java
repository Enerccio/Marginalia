package com.github.enerccio.marginalia.extensions.storytracker.log;

import java.util.List;

/**
 * The log of what automatic runs applied, and taking an entry back. Pure functions over the list a tracker keeps in its state; what
 * an entry did and how it is undone is the tracker's {@link Undo}.
 * <p>
 * An entry is only reverted while the state is still as the run left it; what the user edited, deleted or handled by hand since is
 * not touched, and an entry is reverted once.
 */
public final class TrackerChangeLog {

    /** The log keeps this many entries, the oldest go first. */
    public static final int KEEP = 200;

    private TrackerChangeLog() {
    }

    public enum Outcome {
        REVERTED,
        /** The state is not as the run left it any more (the user dealt with it already), or the entry was reverted. */
        ALREADY_HANDLED,
        /** The item or its character is gone. */
        GONE,
        /** There is no such entry. */
        UNKNOWN
    }

    /**
     * How a tracker takes back one of its entries.
     *
     * @param <E> the entries of the tracker
     */
    public interface Undo<E extends LogEntry> {

        /** {@link Outcome#REVERTED} when {@link #undo} can take the entry back now, else why not. Changes nothing. */
        Outcome check(E entry);

        /** Takes the entry back; called only after {@link #check} said it can. */
        void undo(E entry);
    }

    /** Adds entries, newest last, and drops the oldest over {@link #KEEP}. */
    public static <E extends LogEntry> void add(List<E> log, List<? extends E> entries) {
        log.addAll(entries);
        while (log.size() > KEEP) {
            log.removeFirst();
        }
    }

    /** Whether {@link #revert} would change something now. */
    public static <E extends LogEntry> boolean canRevert(E entry, Undo<E> undo) {
        return !entry.isReverted() && undo.check(entry) == Outcome.REVERTED;
    }

    public static <E extends LogEntry> Outcome revert(List<E> log, String entryId, Undo<E> undo) {
        E entry = log.stream().filter(e -> e.getId().equals(entryId)).findFirst().orElse(null);
        if (entry == null) {
            return Outcome.UNKNOWN;
        }
        if (entry.isReverted()) {
            return Outcome.ALREADY_HANDLED;
        }
        Outcome outcome = undo.check(entry);
        if (outcome != Outcome.REVERTED) {
            return outcome;
        }
        undo.undo(entry);
        entry.setReverted(true);
        return Outcome.REVERTED;
    }
}
