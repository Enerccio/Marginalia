package com.github.enerccio.marginalia.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Matches the words of a text to the words of an older text: the longest common subsequence of the words (white space is not
 * compared), found after the common start and the common end are matched. Used to tell what stayed of a text from what was added to it,
 * see {@link GeneratedTextHighlighter} and {@link AuthorshipTracker}.
 */
public final class WordAlignment {

    // size of the LCS table (cells) above which only the common start and end are matched
    private static final long MAX_TABLE = 4_000_000L;

    private WordAlignment() {
    }

    /**
     * @param current  tokens of the newer text, see {@link TextTokens#tokenize(String)}
     * @param original tokens of the older text
     * @return for every token of the newer text the index in the older one of the token it was matched to, -1 for a token that has no
     * counterpart (a new word, and every white space token: the white space is not matched)
     */
    public static int[] align(List<String> current, List<String> original) {
        int[] currentWords = wordIndexes(current);
        int[] originalWords = wordIndexes(original);
        int n = originalWords.length;
        int m = currentWords.length;
        // for every word of the current text the position among the words of the original one
        int[] matched = new int[m];
        Arrays.fill(matched, -1);

        int prefix = 0;
        while (prefix < n && prefix < m
                && original.get(originalWords[prefix]).equals(current.get(currentWords[prefix]))) {
            matched[prefix] = prefix;
            prefix++;
        }
        int suffix = 0;
        while (suffix < n - prefix && suffix < m - prefix
                && original.get(originalWords[n - 1 - suffix]).equals(current.get(currentWords[m - 1 - suffix]))) {
            matched[m - 1 - suffix] = n - 1 - suffix;
            suffix++;
        }

        int rows = n - prefix - suffix;
        int columns = m - prefix - suffix;
        if (rows > 0 && columns > 0 && (long) (rows + 1) * (columns + 1) <= MAX_TABLE) {
            int[][] lcs = new int[rows + 1][columns + 1];
            for (int i = rows - 1; i >= 0; i--) {
                String a = original.get(originalWords[prefix + i]);
                for (int j = columns - 1; j >= 0; j--) {
                    lcs[i][j] = a.equals(current.get(currentWords[prefix + j]))
                            ? lcs[i + 1][j + 1] + 1
                            : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
                }
            }
            int i = 0;
            int j = 0;
            while (i < rows && j < columns) {
                if (original.get(originalWords[prefix + i]).equals(current.get(currentWords[prefix + j]))) {
                    matched[prefix + j] = prefix + i;
                    i++;
                    j++;
                } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
                    i++;
                } else {
                    j++;
                }
            }
        }

        int[] result = new int[current.size()];
        Arrays.fill(result, -1);
        for (int w = 0; w < m; w++) {
            if (matched[w] >= 0) {
                result[currentWords[w]] = originalWords[matched[w]];
            }
        }
        return result;
    }

    /**
     * @return the indexes of the tokens that are not white space
     */
    public static int[] wordIndexes(List<String> tokens) {
        return IntStream.range(0, tokens.size()).filter(i -> !TextTokens.isSpace(tokens.get(i))).toArray();
    }
}
