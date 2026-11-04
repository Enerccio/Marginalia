package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

/**
 * What the reader of a published book (the viewer) is shown for the translated speech. A setting of the book, or of a folder above it; the
 * author's own views keep a personal switch and are not affected.
 */
public enum ReaderMode {

    /** The conlang, and a click on a speech shows the English behind it. The default. */
    CLICK("click"),
    /** The conlang only: the English is never sent to the reader's browser. */
    CONLANG("conlang"),
    /** The English only, as if the book was never translated. */
    SOURCE("source");

    private final String id;

    ReaderMode(String id) {
        this.id = id;
    }

    /**
     * The id stored in the settings of a book or a folder.
     */
    public String id() {
        return id;
    }

    /**
     * The mode with the id; {@link #CLICK} for an unknown one or none.
     */
    public static ReaderMode of(String id) {
        for (ReaderMode mode : values()) {
            if (mode.id.equals(id)) {
                return mode;
            }
        }
        return CLICK;
    }
}
