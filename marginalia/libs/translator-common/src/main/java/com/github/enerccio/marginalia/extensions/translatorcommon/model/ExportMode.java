package com.github.enerccio.marginalia.extensions.translatorcommon.model;

/**
 * What an export of the book holds for the translated speech. Chosen in the export dialog for that export, the default is the text as it is.
 */
public enum ExportMode {

    /** The text as it is stored: the translation. */
    TRANSLATED,
    /** The English, as if the book was never translated. */
    SOURCE,
    /** The translation, the English of each speech in a footnote. */
    TRANSLATED_NOTES,
    /** The English, the translation of each speech in a footnote. */
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
        return this == TRANSLATED_NOTES || this == SOURCE_NOTES;
    }
}
