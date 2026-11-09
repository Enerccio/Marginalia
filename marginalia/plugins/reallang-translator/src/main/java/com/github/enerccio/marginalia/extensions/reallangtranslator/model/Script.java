package com.github.enerccio.marginalia.extensions.reallangtranslator.model;

/**
 * The script a translation is written in. It decides what the check of an answer looks for ({@code ScriptCheck}) and what the model is told.
 */
public enum Script {
    /** Japanese in kana and kanji, the way it is written. */
    KANA_KANJI,
    /** Japanese in kana only, no kanji (a book for children, or a style). */
    KANA,
    /** The language in Latin letters (Japanese: Hepburn romaji). */
    ROMAJI,
    /** Any other language or script: the model decides, nothing is checked. */
    NATIVE
}
