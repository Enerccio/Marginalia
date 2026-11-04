package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

/**
 * What an export of the book holds for the translated speech. Chosen in the export dialog for that export, the default is the text as it is.
 */
public enum ExportMode {

    /** The text as it is stored: the conlang. */
    CONLANG,
    /** The English, as if the book was never translated. */
    SOURCE,
    /** The conlang, the English of each speech in a footnote. */
    CONLANG_NOTES,
    /** The English, the conlang of each speech in a footnote. */
    SOURCE_NOTES;

    /**
     * Whether the speech is replaced by its English in the text.
     */
    public boolean showsSource() {
        return this == SOURCE || this == SOURCE_NOTES;
    }

    /**
     * Whether each speech gets a footnote.
     */
    public boolean hasNotes() {
        return this == CONLANG_NOTES || this == SOURCE_NOTES;
    }
}
