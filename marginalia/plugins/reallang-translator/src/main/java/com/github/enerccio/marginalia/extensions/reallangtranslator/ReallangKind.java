package com.github.enerccio.marginalia.extensions.reallangtranslator;

/**
 * The id of the passage kind this plugin registers for the speech it translated, and its mark. The Conlang Translator has its own, so that a
 * book can hold both kinds of passages.
 */
public final class ReallangKind {

    public static final String ID = "reallang-translator.speech";
    /** The CSS class of the mark over a translated speech. */
    public static final String MARK = "reallang-passage";

    private ReallangKind() {
    }
}
