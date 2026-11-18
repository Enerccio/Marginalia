package com.github.enerccio.marginalia.extensions.storytracker.checkpoint;

/**
 * The whole state of a tracker in a book as it was just before a run changed it, kept so that the author can undo that run (and
 * everything after it) and read its parts again (see {@link TrackerCheckpoints}). What is listed: the state itself is read only for
 * a restore.
 * <p>
 * Checkpoints of 1.9.0 builds before this one were taken after the run ({@code before} false): restoring one returns to the state
 * the run left.
 *
 * @param id        the uuid of the file that holds the state
 * @param partUuid  the last part the tracker had read (its cursor) in the stored state, {@link TrackerCheckpoints#START} when it had read
 *                  nothing yet
 * @param time      when it was taken (millis)
 * @param kind      what kind of run (or restore) came after it
 * @param added     how many things that run added (a tracker that does not tell added from retracted counts all its changes here)
 * @param retracted how many things that run took back or ended
 * @param before    whether the state is the one before the run (else the one after it, the old kind)
 */
public record Checkpoint(String id, String partUuid, long time, Kind kind, int added, int retracted, boolean before) {

    public enum Kind {
        /** A run with the model over the next parts. */
        RUN,
        /** A tidy run: the cursor stayed where it was. */
        TIDY,
        /** Parts read without the model. */
        READ,
        /** A return to another checkpoint: this one holds what the tracker had before it, so the return can be undone. */
        RESTORE
    }

    /** Whether the tracker had read nothing in the stored state. */
    public boolean isStart() {
        return TrackerCheckpoints.START.equals(partUuid);
    }
}
