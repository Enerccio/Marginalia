package com.github.enerccio.marginalia.extensions.translatorcommon.translate;

/**
 * One direct speech of a part as the model answered for it.
 *
 * @param quote      the speech copied from the part, without the quotation marks
 * @param speaker    who says it
 * @param language   the name of the language it is spoken in, or {@code none} for the language of the story
 * @param text       the speech in the language; empty for {@code none}
 * @param choice     one sentence on the main choices of the translation; for the author, never used
 * @param understood false when the listener does not understand it; null is read as true
 * @param register   the register the model chose, empty for the neutral one
 */
public record Speech(String quote, String speaker, String language, String text, String choice, Boolean understood, String register) {

    public boolean isNone() {
        return language == null || language.isBlank() || language.strip().equalsIgnoreCase("none");
    }

    public boolean isUnderstood() {
        return understood == null || understood;
    }
}
