package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

/**
 * Who reads a {@link RuleSection} of a language.
 */
public enum Audience {
    /** Sent to the model that translates (the grammar book, the compact instruction block). */
    MODEL,
    /** Read only by the designer (the history of the language, notes for making new words), never sent to the translator. */
    DESIGNER,
    /** The author's own notes: nobody reads them but the author. */
    NONE
}
