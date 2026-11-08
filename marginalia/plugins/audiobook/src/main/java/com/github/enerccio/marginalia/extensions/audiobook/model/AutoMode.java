package com.github.enerccio.marginalia.extensions.audiobook.model;

/** Whether a new part of a book is made into audio by itself when it is final, and how. The id is what the inherited setting stores. */
public enum AutoMode {
    /** Nothing happens by itself (the default). */
    NO("no"),
    /** The narrator reads the part, one voice. */
    PLAIN("plain"),
    /** The model of the book labels the part first (who speaks, how), the cast gives the voices. */
    LABELLED("labelled");

    private final String id;

    AutoMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    /** @return the mode with that id, {@link #NO} for anything else (also for a mode that a later version added) */
    public static AutoMode of(String id) {
        for (AutoMode mode : values()) {
            if (mode.id.equals(id)) {
                return mode;
            }
        }
        return NO;
    }
}
