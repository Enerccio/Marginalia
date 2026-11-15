package com.github.enerccio.marginalia.extensions.audiobook.recognize;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * How far what the speech recognition heard is from the text a piece was to say: the word error rate (the words to change, add or drop to turn one into
 * the other, by the number of words of the text). The texts are compared as words only: case, punctuation, the Higgs tokens ({@code <|...|>}) and the
 * sound tags ({@code [sigh]}) do not count.
 */
public final class WordMatch {

    private WordMatch() {
    }

    /** The words of a text as they are compared. */
    public static List<String> words(String text) {
        if (text == null) {
            return List.of();
        }
        String plain = text.replaceAll("<\\|[^|]*\\|>", " ").replaceAll("\\[[^\\]]*\\]", " ");
        plain = Normalizer.normalize(plain, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT)
                .replace('’', '\'').replace('‘', '\'').replace("'", "");
        List<String> words = new ArrayList<>();
        for (String word : plain.split("[^\\p{L}\\p{N}]+")) {
            if (!word.isEmpty()) {
                words.add(word);
            }
        }
        return words;
    }

    /**
     * @return 0 for the same words, 1 when every word is wrong; more than 1 when much more was said than the text (babbling)
     */
    public static double errorRate(String expected, String heard) {
        List<String> want = words(expected);
        List<String> got = words(heard);
        if (want.isEmpty()) {
            return got.isEmpty() ? 0 : 1;
        }
        int[] previous = new int[got.size() + 1];
        int[] current = new int[got.size() + 1];
        for (int j = 0; j <= got.size(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= want.size(); i++) {
            current[0] = i;
            for (int j = 1; j <= got.size(); j++) {
                int cost = want.get(i - 1).equals(got.get(j - 1)) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[got.size()] / (double) want.size();
    }

    /** The number of words of the text, as {@link #words} counts them. */
    public static int count(String text) {
        return words(text).size();
    }
}
