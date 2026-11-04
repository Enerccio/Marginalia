package com.github.enerccio.marginalia.domain.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The text of a part as a model reads it, next to the text as it is stored ({@code ChatMessage.getResponse()}), and the map between
 * their offsets.
 * <p>
 * An extension may keep the story in another form than the model should read it (the Conlang Translator stores the speech in
 * the constructed language and gives the model the English it was made from). It registers a
 * {@link com.github.enerccio.marginalia.extensions.PartTextRegistry.Transformer transformer} that replaces ranges of the stored
 * text; this class is the result: the replaced text, and what is needed by the code that talks to the model about the <b>stored</b>
 * text, which has offsets of its own:
 * <ul>
 *     <li>a quote the model answers with is looked up in the stored text ({@link #storedRange});</li>
 *     <li>a passage the author selected in the stored text is shown to the model in its own words ({@link #modelRange}).</li>
 * </ul>
 * Text outside the replaced ranges maps one to one. A range that touches a replaced passage grows to the whole passage on the
 * other side, because the inside of a replaced passage has no counterpart.
 * <p>
 * Without a replacement {@link #text()} is {@link #stored()} and the maps are the identity.
 */
public final class ModelText {

    private final String stored;
    private final String text;
    // the replaced passages, in order: [storedStart, storedEnd, modelStart, modelEnd]
    private final int[][] passages;

    private ModelText(String stored, String text, int[][] passages) {
        this.stored = stored;
        this.text = text;
        this.passages = passages;
    }

    /**
     * Nothing is replaced.
     *
     * @param stored the stored text, null is read as empty
     */
    public static ModelText unchanged(String stored) {
        String s = stored == null ? "" : stored;
        return new ModelText(s, s, new int[0][]);
    }

    /**
     * @param stored        the stored text, null is read as empty
     * @param substitutions the ranges of the stored text to replace, in any order; a range outside the text, empty and without a
     *                      replacement, or overlapping an earlier one (in the order given) is ignored
     */
    public static ModelText of(String stored, List<Substitution> substitutions) {
        String s = stored == null ? "" : stored;
        if (substitutions == null || substitutions.isEmpty()) {
            return unchanged(s);
        }
        // the first one given wins an overlap
        List<Substitution> accepted = new ArrayList<>();
        for (Substitution candidate : substitutions) {
            if (candidate == null || candidate.start() < 0 || candidate.end() > s.length() || candidate.end() < candidate.start()) {
                continue;
            }
            if (candidate.start() == candidate.end() && candidate.replacement().isEmpty()) {
                continue;
            }
            boolean overlaps = false;
            for (Substitution other : accepted) {
                if (candidate.start() < other.end() && other.start() < candidate.end()
                        || candidate.start() == candidate.end() && candidate.start() > other.start() && candidate.start() < other.end()
                        || other.start() == other.end() && other.start() > candidate.start() && other.start() < candidate.end()) {
                    overlaps = true;
                    break;
                }
            }
            if (!overlaps) {
                accepted.add(candidate);
            }
        }
        if (accepted.isEmpty()) {
            return unchanged(s);
        }
        accepted.sort((a, b) -> a.start() != b.start() ? Integer.compare(a.start(), b.start()) : Integer.compare(a.end(), b.end()));

        StringBuilder out = new StringBuilder(s.length());
        int[][] passages = new int[accepted.size()][];
        int at = 0;
        for (int i = 0; i < accepted.size(); i++) {
            Substitution sub = accepted.get(i);
            out.append(s, at, sub.start());
            int modelStart = out.length();
            out.append(sub.replacement());
            passages[i] = new int[]{sub.start(), sub.end(), modelStart, out.length()};
            at = sub.end();
        }
        out.append(s, at, s.length());
        return new ModelText(s, out.toString(), passages);
    }

    /**
     * A range of the stored text to replace.
     *
     * @param start       first character of the range
     * @param end         end of the range (exclusive); equal to {@code start} inserts the replacement
     * @param replacement what the model reads in its place
     */
    public record Substitution(int start, int end, String replacement) {

        public Substitution {
            replacement = replacement == null ? "" : replacement;
        }
    }

    /**
     * The part as it is stored.
     */
    public String stored() {
        return stored;
    }

    /**
     * The part as a model reads it.
     */
    public String text() {
        return text;
    }

    /**
     * Whether any passage is replaced.
     */
    public boolean changed() {
        return passages.length > 0;
    }

    /**
     * The range of the stored text that a range of {@link #text()} stands for. A range that starts or ends in a replaced passage
     * takes the whole passage; an empty range at a place that maps to one place stays empty.
     *
     * @return {@code {start, end}} in the stored text
     * @throws IllegalArgumentException when the range is not in the text
     */
    public int[] storedRange(int start, int end) {
        check(start, end, text.length());
        if (passages.length == 0) {
            return new int[]{start, end};
        }
        return new int[]{map(start, 2, 3, 0, 1, true), map(end, 2, 3, 0, 1, false)};
    }

    /**
     * The range of {@link #text()} that a range of the stored text stands for, the other way round of {@link #storedRange}.
     *
     * @return {@code {start, end}} in the text the model reads
     * @throws IllegalArgumentException when the range is not in the stored text
     */
    public int[] modelRange(int start, int end) {
        check(start, end, stored.length());
        if (passages.length == 0) {
            return new int[]{start, end};
        }
        return new int[]{map(start, 0, 1, 2, 3, true), map(end, 0, 1, 2, 3, false)};
    }

    private static void check(int start, int end, int length) {
        if (start < 0 || end < start || end > length) {
            throw new IllegalArgumentException("The range " + start + ".." + end + " is not in a text of " + length + " characters");
        }
    }

    /**
     * Maps an offset from one text to the other. {@code from*} and {@code to*} are the indexes of the start and the end of a
     * passage in the text the offset is in and in the text it goes to.
     *
     * @param isStart a start of a range moves to the start of the passage it is in, an end to its end
     */
    private int map(int offset, int fromStart, int fromEnd, int toStart, int toEnd, boolean isStart) {
        // how far the texts have drifted before the passage: the same for every offset between two passages
        int drift = 0;
        for (int[] passage : passages) {
            if (passage[fromStart] == passage[fromEnd] && offset == passage[fromStart]) {
                // a place where the other text has a passage and this one has none (a deletion, an insertion): a range that starts
                // here begins with what is after the passage, one that ends here ends before it
                return isStart ? passage[toEnd] : passage[toStart];
            }
            if (offset <= passage[fromStart]) {
                // before the passage (or at its start): one to one, after the drift of the passages before it
                return offset + drift;
            }
            if (offset < passage[fromEnd]) {
                // inside the passage: the passage as a whole
                return isStart ? passage[toStart] : passage[toEnd];
            }
            if (offset == passage[fromEnd]) {
                return passage[toEnd];
            }
            drift = passage[toEnd] - passage[fromEnd];
        }
        return offset + drift;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ModelText other && stored.equals(other.stored) && text.equals(other.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stored, text);
    }

    @Override
    public String toString() {
        return changed() ? "ModelText[" + passages.length + " replaced]" : "ModelText[unchanged]";
    }
}
