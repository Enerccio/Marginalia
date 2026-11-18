package com.github.enerccio.marginalia.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextDiffTest {

    @Test
    void identicalTextsHaveNoChanges() {
        TextDiff.Result result = TextDiff.diff("One. Two.", "One. Two.");

        assertThat(result.identical()).isTrue();
        assertThat(result.oldUnits()).isEqualTo(2);
    }

    @Test
    void anEditInsideAParagraphShowsOnlyTheSentenceThatChanged() {
        TextDiff.Result result = TextDiff.diff("He came. He saw it. He left.", "He came. He missed it. He left.");

        assertThat(result.changes()).extracting(TextDiff.Change::added, TextDiff.Change::index, TextDiff.Change::text)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(false, 2, "He saw it."),
                        org.assertj.core.groups.Tuple.tuple(true, 2, "He missed it."));
    }

    @Test
    void linesAreUnitsToo() {
        TextDiff.Result result = TextDiff.diff("first\nsecond\nthird", "first\nthird\nfourth");

        assertThat(result.changes()).extracting(TextDiff.Change::text).containsExactly("second", "fourth");
    }

    @Test
    void emptyAndNullTextsCompare() {
        assertThat(TextDiff.diff(null, "").identical()).isTrue();
        assertThat(TextDiff.diff(null, "Added.").changes()).hasSize(1);
    }

    @Test
    void textsTooLargeToCompareGiveNull() {
        String big = "a.\nb.\n".repeat(1500);

        assertThat(TextDiff.diff(big, big.replace("a.", "x."))).isNull();
    }

}
