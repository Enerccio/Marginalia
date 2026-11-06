package com.github.enerccio.marginalia.extensions.reallangtranslator.model;

/**
 * What a glossary entry says about its source word.
 */
public enum GlossaryKind {
    /** The source (a name, a place, a term of the story, an English word with a special reading) is written exactly as {@link GlossaryEntry#getWritten()}. */
    WRITTEN,
    /** The source is left as it is in the translation (a title, a place, a made-up term that is not translated). */
    KEEP
}
