package com.github.enerccio.marginalia.extensions.translatorcommon.model;

/**
 * What the reader of a published book (the viewer) is shown for the translated speech. A setting of the book, or of a folder above it; the
 * author's own views keep a personal switch and are not affected.
 */
public enum ReaderMode {

    /** The translation, and a click on a speech shows the English behind it. The default. */
    CLICK("click"),
    /** The translation only: the English is never sent to the reader's browser. */
    TRANSLATED("translated"),
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
        // the id the setting had before the mode was named for any translation (it was the Conlang Translator's only)
        if ("conlang".equals(id)) {
            return TRANSLATED;
        }
        return CLICK;
    }
}
