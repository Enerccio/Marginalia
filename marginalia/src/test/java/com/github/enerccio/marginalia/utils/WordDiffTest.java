package com.github.enerccio.marginalia.utils;

import com.github.enerccio.marginalia.utils.WordDiff.Kind;
import com.github.enerccio.marginalia.utils.WordDiff.Segment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

class WordDiffTest {

    private static String joined(List<Segment> segments, Kind... kinds) {
        List<Kind> wanted = List.of(kinds);
        return segments.stream().filter(s -> wanted.contains(s.kind())).map(Segment::text).collect(Collectors.joining());
    }

    @Test
    void sameTextsAreOneSegmentOfTheSame() {
        List<Segment> segments = WordDiff.diff("He came. He left.", "He came. He left.");

        assertThat(segments).containsExactly(new Segment(Kind.SAME, "He came. He left."));
        assertThat(WordDiff.same(segments)).isTrue();
    }

    @Test
    void aChangedWordIsRemovedAndAdded() {
        List<Segment> segments = WordDiff.diff("He saw the lake today", "He saw the sea today");

        assertThat(segments).extracting(Segment::kind, Segment::text).containsExactly(
                tuple(Kind.SAME, "He saw the "), tuple(Kind.REMOVED, "lake"), tuple(Kind.ADDED, "sea"), tuple(Kind.SAME, " today"));
    }

    @Test
    void aChangedCommaIsASmallChange() {
        List<Segment> segments = WordDiff.diff("Well, no.", "Well; no.");

        assertThat(joined(segments, Kind.REMOVED)).isEqualTo(",");
        assertThat(joined(segments, Kind.ADDED)).isEqualTo(";");
    }

    @Test
    void anInsertionAndADeletionInOneText() {
        List<Segment> segments = WordDiff.diff("one two three four", "one two extra three");

        assertThat(joined(segments, Kind.SAME, Kind.REMOVED)).isEqualTo("one two three four");
        assertThat(joined(segments, Kind.SAME, Kind.ADDED)).isEqualTo("one two extra three");
        assertThat(joined(segments, Kind.ADDED)).contains("extra");
        assertThat(joined(segments, Kind.REMOVED)).contains("four");
    }

    @Test
    void nullIsAnEmptyText() {
        assertThat(WordDiff.diff(null, "new text")).containsExactly(new Segment(Kind.ADDED, "new text"));
        assertThat(WordDiff.diff("old text", "")).containsExactly(new Segment(Kind.REMOVED, "old text"));
        assertThat(WordDiff.diff(null, null)).isEmpty();
    }

    @Test
    void theTwoSidesOfTheScriptGiveBackBothTexts() {
        String before = "The old house stood at the end of the road. Nobody lived there, and the garden had gone wild.\n\n"
                + "Marta came in the evening with a lamp.";
        String after = "The old house stood at the end of a long road. Nobody had lived there for years, and the garden was wild.\n\n"
                + "Marta came late in the evening.";

        List<Segment> segments = WordDiff.diff(before, after);

        assertThat(joined(segments, Kind.SAME, Kind.REMOVED)).isEqualTo(before);
        assertThat(joined(segments, Kind.SAME, Kind.ADDED)).isEqualTo(after);
    }

    @Test
    void textsThatShareNothingFallBackToTheWholeTextWhenTheyDifferTooMuch() {
        String before = java.util.stream.IntStream.range(0, 2000).mapToObj(i -> "a" + i).collect(Collectors.joining(" "));
        String after = java.util.stream.IntStream.range(0, 2000).mapToObj(i -> "b" + i).collect(Collectors.joining(" "));

        List<Segment> segments = WordDiff.diff(before, after);

        assertThat(joined(segments, Kind.SAME, Kind.REMOVED)).isEqualTo(before);
        assertThat(joined(segments, Kind.SAME, Kind.ADDED)).isEqualTo(after);
    }

    @Test
    void aLongTextWithAFewChangesIsFound() {
        String before = java.util.stream.IntStream.range(0, 6000).mapToObj(i -> "word" + i).collect(Collectors.joining(" "));
        String after = before.replace("word100 ", "changed ").replace("word4000 ", "");

        List<Segment> segments = WordDiff.diff(before, after);

        assertThat(joined(segments, Kind.SAME, Kind.REMOVED)).isEqualTo(before);
        assertThat(joined(segments, Kind.SAME, Kind.ADDED)).isEqualTo(after);
        assertThat(joined(segments, Kind.ADDED)).contains("changed");
        assertThat(joined(segments, Kind.ADDED).length()).isLessThan(20);
    }
}
