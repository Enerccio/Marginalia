package com.github.enerccio.marginalia.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The difference of two texts, word by word: the stretches that are the same in both, the ones that only the first has
 * (removed) and the ones that only the second has (added). The words and the white space between them are the units, so a
 * changed comma is one small change and not a changed line.
 * <p>
 * It is the shortest edit script of Myers. A text that differs from the other in more than {@link #MAX_EDITS} units (two
 * texts that have little in common) is not searched for the shortest script: what the texts do not share at the start and at
 * the end is then shown as removed and added as a whole.
 */
public final class WordDiff {

    public enum Kind {
        SAME, ADDED, REMOVED
    }

    /**
     * A stretch of the text; the stretches of one kind that follow each other are one segment.
     */
    public record Segment(Kind kind, String text) {
    }

    /** The most units that may be different before the shortest script is not looked for any more. */
    static final int MAX_EDITS = 1500;

    private static final Pattern TOKEN = Pattern.compile("[\\p{L}\\p{N}_]+|\\s+|[^\\p{L}\\p{N}_\\s]");

    private WordDiff() {
    }

    /**
     * What turns {@code before} into {@code after}. Null is the same as an empty text.
     */
    public static List<Segment> diff(String before, String after) {
        List<String> a = tokens(before);
        List<String> b = tokens(after);
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
        Builder out = new Builder();
        out.add(Kind.SAME, join(a, 0, start));
        if (start < endA || start < endB) {
            if (!script(a, start, endA, b, start, endB, out)) {
                out.add(Kind.REMOVED, join(a, start, endA));
                out.add(Kind.ADDED, join(b, start, endB));
            }
        }
        out.add(Kind.SAME, join(a, endA, a.size()));
        return out.segments;
    }

    /**
     * Whether the two texts are the same.
     */
    public static boolean same(List<Segment> segments) {
        return segments.stream().allMatch(segment -> segment.kind() == Kind.SAME);
    }

    private static List<String> tokens(String text) {
        List<String> tokens = new ArrayList<>();
        if (text != null) {
            Matcher matcher = TOKEN.matcher(text);
            while (matcher.find()) {
                tokens.add(matcher.group());
            }
        }
        return tokens;
    }

    private static String join(List<String> tokens, int from, int to) {
        return String.join("", tokens.subList(from, to));
    }

    private static final class Builder {

        final List<Segment> segments = new ArrayList<>();

        void add(Kind kind, String text) {
            if (text.isEmpty()) {
                return;
            }
            int last = segments.size() - 1;
            if (last >= 0 && segments.get(last).kind() == kind) {
                segments.set(last, new Segment(kind, segments.get(last).text() + text));
            } else {
                segments.add(new Segment(kind, text));
            }
        }
    }

    /**
     * The shortest edit script of the two ranges (Myers, O(ND) steps), added to the builder.
     *
     * @return false when it needs more than {@link #MAX_EDITS} edits, nothing was added then
     */
    private static boolean script(List<String> a, int a0, int a1, List<String> b, int b0, int b1, Builder out) {
        int n = a1 - a0;
        int m = b1 - b0;
        int limit = Math.min(n + m, MAX_EDITS);
        int offset = limit + 1;
        int[] v = new int[2 * limit + 3];
        List<int[]> trace = new ArrayList<>();
        int found = -1;
        search:
        for (int d = 0; d <= limit; d++) {
            // the state before this step, only the diagonals the step can read
            trace.add(Arrays.copyOfRange(v, offset - d - 1, offset + d + 2));
            for (int k = -d; k <= d; k += 2) {
                int x;
                if (k == -d || (k != d && v[offset + k - 1] < v[offset + k + 1])) {
                    x = v[offset + k + 1];
                } else {
                    x = v[offset + k - 1] + 1;
                }
                int y = x - k;
                while (x < n && y < m && a.get(a0 + x).equals(b.get(b0 + y))) {
                    x++;
                    y++;
                }
                v[offset + k] = x;
                if (x >= n && y >= m) {
                    found = d;
                    break search;
                }
            }
        }
        if (found < 0) {
            return false;
        }
        // walk back from the end, the pieces come last first
        List<Segment> reversed = new ArrayList<>();
        int x = n;
        int y = m;
        for (int d = found; d >= 0; d--) {
            int[] before = trace.get(d);
            int k = x - y;
            int previousK;
            if (k == -d || (k != d && before[k - 1 + d + 1] < before[k + 1 + d + 1])) {
                previousK = k + 1;
            } else {
                previousK = k - 1;
            }
            int previousX = before[previousK + d + 1];
            int previousY = previousX - previousK;
            while (x > previousX && y > previousY) {
                reversed.add(new Segment(Kind.SAME, a.get(a0 + x - 1)));
                x--;
                y--;
            }
            if (d > 0) {
                if (x == previousX) {
                    reversed.add(new Segment(Kind.ADDED, b.get(b0 + previousY)));
                } else {
                    reversed.add(new Segment(Kind.REMOVED, a.get(a0 + previousX)));
                }
            }
            x = previousX;
            y = previousY;
        }
        Collections.reverse(reversed);
        for (Segment piece : reversed) {
            out.add(piece.kind(), piece.text());
        }
        return true;
    }
}
