package com.github.enerccio.marginalia.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Marks the text of a part that is still the text the LLM wrote: diffs the current response against the original one
 * word by word and wraps what both have in common in {@code <mark>} elements of the given CSS class, so the author's
 * additions and rewrites stay unmarked. The result is markdown with inline HTML for the {@code Markdown} component.
 */
public final class GeneratedTextHighlighter {

    // size of the LCS table (cells) above which only the common start and end are matched
    private static final long MAX_TABLE = 4_000_000L;

    private GeneratedTextHighlighter() {
    }

    /**
     * @return response with the parts shared with the original wrapped in {@code <mark class="cssClass">}; the
     * response itself when there is no original (the part was not generated) or nothing is shared
     */
    public static String highlight(String response, String original, String cssClass) {
        List<TextMarkup.Mark> marks = new ArrayList<>();
        for (int[] range : ranges(response, original)) {
            marks.add(new TextMarkup.Mark(range[0], range[1], cssClass));
        }
        return TextMarkup.apply(response, marks);
    }

    /**
     * @return the runs of the response that are still the text of the original, as [start, end) offsets in the response, in order;
     * empty when there is no original (the part was not generated) or nothing is shared
     */
    public static List<int[]> ranges(String response, String original) {
        List<int[]> ranges = new ArrayList<>();
        if (response == null || response.isBlank() || original == null || original.isBlank()) {
            return ranges;
        }

        List<String> tokens = tokenize(response);
        boolean[] generated = matchGenerated(tokens, tokenize(original));
        int position = 0;
        int runStart = -1;
        for (int i = 0; i < tokens.size(); i++) {
            if (generated[i]) {
                if (runStart < 0) {
                    runStart = position;
                }
            } else if (runStart >= 0) {
                ranges.add(new int[]{runStart, position});
                runStart = -1;
            }
            position += tokens.get(i).length();
        }
        if (runStart >= 0) {
            ranges.add(new int[]{runStart, position});
        }
        return ranges;
    }

    private static List<String> tokenize(String text) {
        return TextTokens.tokenize(text);
    }

    private static boolean anyTrue(boolean[] flags) {
        for (boolean flag : flags) {
            if (flag) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSpace(String token) {
        return TextTokens.isSpace(token);
    }

    /**
     * Longest common subsequence of the words (white space is not compared) of both token lists; a white space token
     * between two shared words is shared too.
     */
    private static boolean[] matchGenerated(List<String> current, List<String> original) {
        List<Integer> currentWords = wordIndexes(current);
        List<Integer> originalWords = wordIndexes(original);
        int n = originalWords.size();
        int m = currentWords.size();
        boolean[] sharedWord = new boolean[m];

        int prefix = 0;
        while (prefix < n && prefix < m
                && original.get(originalWords.get(prefix)).equals(current.get(currentWords.get(prefix)))) {
            sharedWord[prefix] = true;
            prefix++;
        }
        int suffix = 0;
        while (suffix < n - prefix && suffix < m - prefix
                && original.get(originalWords.get(n - 1 - suffix)).equals(current.get(currentWords.get(m - 1 - suffix)))) {
            sharedWord[m - 1 - suffix] = true;
            suffix++;
        }

        int rows = n - prefix - suffix;
        int columns = m - prefix - suffix;
        if (rows > 0 && columns > 0 && (long) (rows + 1) * (columns + 1) <= MAX_TABLE) {
            int[][] lcs = new int[rows + 1][columns + 1];
            for (int i = rows - 1; i >= 0; i--) {
                String a = original.get(originalWords.get(prefix + i));
                for (int j = columns - 1; j >= 0; j--) {
                    lcs[i][j] = a.equals(current.get(currentWords.get(prefix + j)))
                            ? lcs[i + 1][j + 1] + 1
                            : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
                }
            }
            int i = 0;
            int j = 0;
            while (i < rows && j < columns) {
                if (original.get(originalWords.get(prefix + i)).equals(current.get(currentWords.get(prefix + j)))) {
                    sharedWord[prefix + j] = true;
                    i++;
                    j++;
                } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
                    i++;
                } else {
                    j++;
                }
            }
        }

        boolean[] shared = new boolean[current.size()];
        for (int w = 0; w < m; w++) {
            shared[currentWords.get(w)] = sharedWord[w];
        }
        for (int w = 0; w + 1 < m; w++) {
            int from = currentWords.get(w);
            int to = currentWords.get(w + 1);
            if (sharedWord[w] && sharedWord[w + 1]) {
                for (int t = from + 1; t < to; t++) {
                    shared[t] = true;
                }
            }
        }
        return shared;
    }

    private static List<Integer> wordIndexes(List<String> tokens) {
        List<Integer> words = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            if (!isSpace(tokens.get(i))) {
                words.add(i);
            }
        }
        return words;
    }
}
