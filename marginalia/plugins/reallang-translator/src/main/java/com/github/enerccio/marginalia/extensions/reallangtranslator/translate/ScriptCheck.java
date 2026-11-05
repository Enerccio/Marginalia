package com.github.enerccio.marginalia.extensions.reallangtranslator.translate;

import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Script;

/**
 * Checks that a translation is written in the script it was asked for. The model is told, but a model writes kanji where kana was asked for, or
 * kana in a romaji book; the check finds it, the plugin says so and the author decides. Only Japanese scripts are checked (letters of other
 * scripts, and Latin names inside a Japanese sentence, are fine).
 */
public final class ScriptCheck {

    /** What is wrong with a text. */
    public enum Problem {
        /** The text is supposed to be Japanese and has no kana or kanji at all. */
        NOT_JAPANESE,
        /** The text has kanji where only kana was asked for. */
        KANJI_IN_KANA,
        /** The text has kana or kanji where romaji was asked for. */
        JAPANESE_IN_ROMAJI
    }

    private ScriptCheck() {
    }

    /**
     * @return what is wrong with the text for the script; null when nothing is
     */
    public static Problem problem(Script script, String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        boolean kana = false;
        boolean kanji = false;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            switch (Character.UnicodeScript.of(cp)) {
                case HIRAGANA, KATAKANA -> kana = true;
                case HAN -> kanji = true;
                default -> {
                }
            }
        }
        return switch (script) {
            case KANA_KANJI -> kana || kanji ? null : Problem.NOT_JAPANESE;
            case KANA -> kanji ? Problem.KANJI_IN_KANA : kana ? null : Problem.NOT_JAPANESE;
            case ROMAJI -> kana || kanji ? Problem.JAPANESE_IN_ROMAJI : null;
            case NATIVE -> null;
        };
    }
}
