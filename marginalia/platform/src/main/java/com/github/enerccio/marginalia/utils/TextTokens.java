package com.github.enerccio.marginalia.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits a text into tokens that together make up the whole text: runs of white space, words, and single other characters.
 * The tokens keep their offsets, so what is computed over them can be mapped back to the text.
 */
public final class TextTokens {

    private static final Pattern TOKEN = Pattern.compile("\\s+|[\\p{L}\\p{N}_]+|[^\\p{L}\\p{N}_\\s]");

    private TextTokens() {
    }

    public static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(text);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    public static boolean isSpace(String token) {
        return Character.isWhitespace(token.charAt(0));
    }

}
