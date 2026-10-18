package com.github.enerccio.marginalia.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Difference of two texts, taken sentence by sentence (a line break ends a sentence too), so an edit inside a long
 * paragraph shows the sentences that changed and not the whole paragraph. The sentences are compared as they are,
 * a longest common subsequence of them is the part that stays.
 */
public final class TextDiff {

    /**
     * The largest product of the numbers of sentences that is compared, the table of the comparison has that many cells.
     */
    public static final long MAX_CELLS = 4_000_000;

    private static final Pattern SEGMENTS = Pattern.compile("(?<=[.!?…。！？][\"'”’»)]{0,2})\\s+|\\s*\\R\\s*");

    private TextDiff() {
    }

    /**
     * One difference.
     *
     * @param added true for a sentence only the new text has, false for one only the old text has
     * @param index number of the sentence in its text, from 1
     * @param text  the sentence
     */
    public record Change(boolean added, int index, String text) {
    }

    /**
     * @param changes the sentences removed from {@code oldText} and added in {@code newText}, in the order of the texts
     * @param units   the sentences of the old and of the new text
     * @return null when the texts are too large to compare, see {@link #MAX_CELLS}
     */
    public record Result(List<Change> changes, int oldUnits, int newUnits) {

        public boolean identical() {
            return changes.isEmpty();
        }

    }

    public static List<String> split(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(SEGMENTS.split(text.strip())).filter(s -> !s.isBlank()).toList();
    }

    public static Result diff(String oldText, String newText) {
        List<String> a = split(oldText);
        List<String> b = split(newText);
        // the common start and end need no table
        int start = 0;
        while (start < a.size() && start < b.size() && a.get(start).equals(b.get(start))) {
            start++;
        }
        int endA = a.size();
        int endB = b.size();
        while (endA > start && endB > start && a.get(endA - 1).equals(b.get(endB - 1))) {
            endA--;
            endB--;
        }
        int n = endA - start;
        int m = endB - start;
        if ((long) n * m > MAX_CELLS) {
            return null;
        }

        int[][] lcs = new int[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                lcs[i][j] = a.get(start + i).equals(b.get(start + j))
                        ? lcs[i + 1][j + 1] + 1 : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
            }
        }
        List<Change> changes = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < n || j < m) {
            if (i < n && j < m && a.get(start + i).equals(b.get(start + j))) {
                i++;
                j++;
            } else if (j >= m || (i < n && lcs[i + 1][j] >= lcs[i][j + 1])) {
                changes.add(new Change(false, start + i + 1, a.get(start + i)));
                i++;
            } else {
                changes.add(new Change(true, start + j + 1, b.get(start + j)));
                j++;
            }
        }
        return new Result(changes, a.size(), b.size());
    }

}
