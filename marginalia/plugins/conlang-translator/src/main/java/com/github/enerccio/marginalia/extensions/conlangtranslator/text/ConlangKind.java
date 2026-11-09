package com.github.enerccio.marginalia.extensions.conlangtranslator.text;

/**
 * The id of the passage kind this plugin registers for the speech it translated. The other translator plugin has its own, so that a book can hold
 * both kinds of passages.
 */
public final class ConlangKind {

    public static final String ID = "conlang-translator.speech";

    private ConlangKind() {
    }
}
