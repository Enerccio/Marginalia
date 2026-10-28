package com.github.enerccio.marginalia.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Which parts of an old text are still in the new one and where: the text is cut into tokens ({@link TextTokens}) and the
 * longest common subsequence of both token lists is found, the tokens it keeps are the {@link Segment segments} (runs of
 * the old text that are in the new one unchanged), everything between them is a gap (deleted, inserted or replaced text).
 * <p>
 * Maps an offset of the old text to the new one the way the range markers of a word processor move: an edit inside a range
 * grows or shrinks it, an edit before it moves it, text typed exactly at an edge of the range lands outside of it.
 */
public final class TextAlignment {

    // size of the LCS table (cells) above which only the common start and end are matched
    private static final long MAX_TABLE = 4_000_000L;

    /**
     * A run of the old text that is in the new text unchanged.
     */
    public record Segment(int oldStart, int newStart, int length) {

        public int oldEnd() {
            return oldStart + length;
        }

        public int newEnd() {
            return newStart + length;
        }
    }

    private final List<Segment> segments;
    private final int oldLength;
    private final int newLength;
    private final boolean approximate;

    private TextAlignment(List<Segment> segments, int oldLength, int newLength, boolean approximate) {
        this.segments = segments;
        this.oldLength = oldLength;
        this.newLength = newLength;
        this.approximate = approximate;
    }

    public static TextAlignment of(String oldText, String newText) {
        String before = oldText == null ? "" : oldText;
        String after = newText == null ? "" : newText;
        if (before.equals(after)) {
            List<Segment> whole = new ArrayList<>();
            if (!before.isEmpty()) {
                whole.add(new Segment(0, 0, before.length()));
            }
            return new TextAlignment(whole, before.length(), after.length(), false);
        }

        List<String> a = TextTokens.tokenize(before);
        List<String> b = TextTokens.tokenize(after);
        int[] aOffsets = offsets(a);
        int[] bOffsets = offsets(b);
        int n = a.size();
        int m = b.size();

        int prefix = 0;
        while (prefix < n && prefix < m && a.get(prefix).equals(b.get(prefix))) {
            prefix++;
        }
        int suffix = 0;
        while (suffix < n - prefix && suffix < m - prefix && a.get(n - 1 - suffix).equals(b.get(m - 1 - suffix))) {
            suffix++;
        }

        // pairs of the token indexes that are the same token of both texts, in order
        List<int[]> pairs = new ArrayList<>();
        for (int i = 0; i < prefix; i++) {
            pairs.add(new int[]{i, i});
        }
        int rows = n - prefix - suffix;
        int columns = m - prefix - suffix;
        boolean approximate = false;
        if (rows > 0 && columns > 0) {
            if ((long) (rows + 1) * (columns + 1) <= MAX_TABLE) {
                int[][] lcs = new int[rows + 1][columns + 1];
                for (int i = rows - 1; i >= 0; i--) {
                    String x = a.get(prefix + i);
                    for (int j = columns - 1; j >= 0; j--) {
                        lcs[i][j] = x.equals(b.get(prefix + j))
                                ? lcs[i + 1][j + 1] + 1
                                : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
                    }
                }
                int i = 0;
                int j = 0;
                while (i < rows && j < columns) {
                    if (a.get(prefix + i).equals(b.get(prefix + j))) {
                        pairs.add(new int[]{prefix + i, prefix + j});
                        i++;
                        j++;
                    } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
                        i++;
                    } else {
                        j++;
                    }
                }
            } else {
                approximate = true;
            }
        }
        for (int k = 0; k < suffix; k++) {
            pairs.add(new int[]{n - suffix + k, m - suffix + k});
        }
        slideRight(pairs, a, b);

        List<Segment> segments = new ArrayList<>();
        for (int[] pair : pairs) {
            int oldStart = aOffsets[pair[0]];
            int newStart = bOffsets[pair[1]];
            int length = aOffsets[pair[0] + 1] - oldStart;
            append(segments, new Segment(oldStart, newStart, length));
        }
        return new TextAlignment(refine(before, after, segments), before.length(), after.length(), approximate);
    }

    /**
     * A word that was changed a little ("John" to "Johnny", "cat" to "cart") is a gap of the token match, but the letters it
     * shares at its start and end are the same text still: they become segments too, so that a one word passage survives a
     * fixed typo. Only when the shared letters are at least half of the shorter side of the gap, else the words are other words.
     */
    private static List<Segment> refine(String before, String after, List<Segment> segments) {
        List<Segment> refined = new ArrayList<>();
        int oldFrom = 0;
        int newFrom = 0;
        for (Segment segment : segments) {
            refineGap(before, after, oldFrom, segment.oldStart(), newFrom, segment.newStart(), refined);
            append(refined, segment);
            oldFrom = segment.oldEnd();
            newFrom = segment.newEnd();
        }
        refineGap(before, after, oldFrom, before.length(), newFrom, after.length(), refined);
        return refined;
    }

    private static void refineGap(String before, String after, int oldFrom, int oldTo, int newFrom, int newTo, List<Segment> out) {
        int oldSize = oldTo - oldFrom;
        int newSize = newTo - newFrom;
        if (oldSize <= 0 || newSize <= 0) {
            return;
        }
        int limit = Math.min(oldSize, newSize);
        int prefix = 0;
        while (prefix < limit && before.charAt(oldFrom + prefix) == after.charAt(newFrom + prefix)) {
            prefix++;
        }
        int suffix = 0;
        while (suffix < limit - prefix && before.charAt(oldTo - 1 - suffix) == after.charAt(newTo - 1 - suffix)) {
            suffix++;
        }
        if ((prefix + suffix) * 2 < limit) {
            return;
        }
        if (prefix > 0) {
            append(out, new Segment(oldFrom, newFrom, prefix));
        }
        if (suffix > 0) {
            append(out, new Segment(oldTo - suffix, newTo - suffix, suffix));
        }
    }

    private static void append(List<Segment> segments, Segment segment) {
        Segment last = segments.isEmpty() ? null : segments.getLast();
        if (last != null && last.oldEnd() == segment.oldStart() && last.newEnd() == segment.newStart()) {
            segments.set(segments.size() - 1, new Segment(last.oldStart(), last.newStart(), last.length() + segment.length()));
        } else {
            segments.add(segment);
        }
    }

    /**
     * Where a text was inserted (or deleted) is often not certain: "hours. Then" to "hours. Very much. Then" is the same as
     * "hours" followed by ". Very much" and the old full stop matching the new one. The alignment prefers the block to sit as
     * late as it can, so that the text before it stays together with what was typed after it, not with the end of it (a
     * passage that ends with the full stop does not take the typed sentence in).
     */
    private static void slideRight(List<int[]> pairs, List<String> a, List<String> b) {
        for (int k = 0; k < pairs.size(); k++) {
            int previousOld = k == 0 ? -1 : pairs.get(k - 1)[0];
            int previousNew = k == 0 ? -1 : pairs.get(k - 1)[1];
            int[] pair = pairs.get(k);
            if (pair[0] == previousOld + 1 && pair[1] > previousNew + 1) {
                // tokens only the new text has
                int from = previousNew + 1;
                int to = pair[1];
                for (int t = k; t < pairs.size() && pairs.get(t)[1] == to && b.get(from).equals(b.get(to)); t++) {
                    pairs.get(t)[1] = from;
                    from++;
                    to++;
                }
            } else if (pair[1] == previousNew + 1 && pair[0] > previousOld + 1) {
                // tokens only the old text has
                int from = previousOld + 1;
                int to = pair[0];
                for (int t = k; t < pairs.size() && pairs.get(t)[0] == to && a.get(from).equals(a.get(to)); t++) {
                    pairs.get(t)[0] = from;
                    from++;
                    to++;
                }
            }
        }
    }

    private static int[] offsets(List<String> tokens) {
        int[] offsets = new int[tokens.size() + 1];
        for (int i = 0; i < tokens.size(); i++) {
            offsets[i + 1] = offsets[i] + tokens.get(i).length();
        }
        return offsets;
    }

    /**
     * @return true when the texts were too different to be matched token by token: only their common start and end are
     * known, what is in between is one gap, and an offset in it can't be mapped to something meaningful (see {@link #isRetained})
     */
    public boolean isApproximate() {
        return approximate;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    /**
     * @return true when the range of the old text is, all of it, in the new text unchanged
     */
    public boolean isRetained(int start, int end) {
        for (Segment segment : segments) {
            if (segment.oldStart() <= start && end <= segment.oldEnd()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true when something of the range of the old text is in the new text unchanged, not counting white space
     * (a range whose words were all rewritten is gone, even if the spaces between the words are still there)
     */
    public boolean isPartlyRetained(String oldText, int start, int end) {
        for (Segment segment : segments) {
            int from = Math.max(start, segment.oldStart());
            int to = Math.min(end, segment.oldEnd());
            for (int i = from; i < to; i++) {
                if (!Character.isWhitespace(oldText.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Where the start of a range of the old text is in the new one: text inserted right at the start is before the range,
     * text that replaced the start of the range is in it.
     */
    public int mapStart(int offset) {
        return map(offset, false);
    }

    /**
     * Where the end (exclusive) of a range of the old text is in the new one: text inserted right at the end is after the
     * range, text that replaced the end of the range is in it.
     */
    public int mapEnd(int offset) {
        return map(offset, true);
    }

    private int map(int offset, boolean end) {
        int p = Math.max(0, Math.min(offset, oldLength));
        int previousOldEnd = 0;
        int previousNewEnd = 0;
        for (Segment segment : segments) {
            if (p <= segment.oldStart()) {
                return mapInGap(p, previousOldEnd, segment.oldStart(), previousNewEnd, segment.newStart(), end);
            }
            if (p < segment.oldEnd()) {
                return segment.newStart() + (p - segment.oldStart());
            }
            previousOldEnd = segment.oldEnd();
            previousNewEnd = segment.newEnd();
        }
        return mapInGap(p, previousOldEnd, oldLength, previousNewEnd, newLength, end);
    }

    /**
     * The gap [oldFrom, oldTo) of the old text became [newFrom, newTo) in the new one.
     */
    private static int mapInGap(int p, int oldFrom, int oldTo, int newFrom, int newTo, boolean end) {
        if (end) {
            return p <= oldFrom ? newFrom : newTo;
        }
        return p >= oldTo ? newTo : newFrom;
    }

}
