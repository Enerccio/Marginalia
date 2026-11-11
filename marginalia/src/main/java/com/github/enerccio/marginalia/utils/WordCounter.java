package com.github.enerccio.marginalia.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Counts the words of a text in any script. A run of letters and digits is one word (as for Latin, Cyrillic, Greek, Hangul
 * and the like, which are written with spaces), but Chinese and Japanese are written without spaces between words, so every
 * Han ideograph (hanzi, kanji), hiragana and katakana character counts as one word, the usual way to measure such a text
 * (the "character count" of a Word document). The punctuation of every script is not counted, and a mixed text is counted
 * piece by piece: {@code "AIは便利です"} is 1 + 5 = 6.
 */
public final class WordCounter {

    /** Ideographic characters: Han, hiragana, katakana (also half width), the prolonged sound mark and the iteration marks. */
    private static final String IDEOGRAPHIC = "\\p{IsHan}\\p{IsHiragana}\\p{IsKatakana}\\u30FC\\uFF70\\u3005\\u3006\\u3031-\\u3035\\u309D\\u309E\\u30FD\\u30FE";

    private static final Pattern WORD = Pattern.compile(
            "[" + IDEOGRAPHIC + "]|(?:[^" + IDEOGRAPHIC + "\\W])+", Pattern.UNICODE_CHARACTER_CLASS);

    private WordCounter() {
    }

    /**
     * @return the number of words of the text, 0 for {@code null} or a text without any
     */
    public static int count(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        Matcher matcher = WORD.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
