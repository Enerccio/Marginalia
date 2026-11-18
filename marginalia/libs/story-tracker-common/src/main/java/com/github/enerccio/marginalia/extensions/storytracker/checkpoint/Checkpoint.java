package com.github.enerccio.marginalia.extensions.storytracker.checkpoint;

/**
 * The whole state of a tracker in a book as a committed run left it, kept so that the author can return the tracker to that point and
 * run again from there (see {@link TrackerCheckpoints}). What is listed: the state itself is read only for a restore.
 *
 * @param id        the uuid of the file that holds the state
 * @param partUuid  the last part the tracker had read (its cursor) when the checkpoint was taken
 * @param time      when it was taken (millis)
 * @param kind      what kind of run it followed
 * @param added     how many things the run added (a tracker that does not tell added from retracted counts all its changes here)
 * @param retracted how many things the run took back or ended
 */
public record Checkpoint(String id, String partUuid, long time, Kind kind, int added, int retracted) {

    public enum Kind {
        /** A run with the model over the next parts. */
        RUN,
        /** A tidy run: the cursor stayed where it was. */
        TIDY,
        /** Parts read without the model. */
        READ
    }
}
