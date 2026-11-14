package com.github.enerccio.marginalia.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the merge dialog suggests for two turns that become one.
 */
class TurnDetailsMergeTest {

    @Test
    void textsGoUnderEachOther() {
        assertThat(TurnDetailsMerge.text(" Anna finds the letter. ", "John leaves.\n")).isEqualTo("Anna finds the letter.\n\nJohn leaves.");
    }

    @Test
    void aBlankOrRepeatedTextIsNotJoined() {
        assertThat(TurnDetailsMerge.text("the harbor", null)).isEqualTo("the harbor");
        assertThat(TurnDetailsMerge.text("  ", "the ship")).isEqualTo("the ship");
        assertThat(TurnDetailsMerge.text("the harbor", " the harbor ")).isEqualTo("the harbor");
        assertThat(TurnDetailsMerge.text(null, null)).isEmpty();
    }

    @Test
    void namesOfBothListsAreKeptOnce() {
        assertThat(TurnDetailsMerge.names("Anna, John", "john; Mara, Anna")).isEqualTo("Anna, John, Mara");
        assertThat(TurnDetailsMerge.names("Anna (young, tired)\nJohn", "john\nMara")).isEqualTo("Anna (young, tired)\nJohn\nMara");
        assertThat(TurnDetailsMerge.names(null, " Mara ,")).isEqualTo("Mara");
        assertThat(TurnDetailsMerge.names(null, null)).isEmpty();
    }

    @Test
    void theFirstValueWinsWhenThereIsOne() {
        assertThat(TurnDetailsMerge.one("Anna", "John")).isEqualTo("Anna");
        assertThat(TurnDetailsMerge.one(" ", " John ")).isEqualTo("John");
        assertThat(TurnDetailsMerge.one(null, null)).isEmpty();
    }

    @Test
    void titlesAreJoinedAndCut() {
        assertThat(TurnDetailsMerge.title("The harbor", "The letter", 200)).isEqualTo("The harbor / The letter");
        assertThat(TurnDetailsMerge.title("The harbor", "The harbor", 200)).isEqualTo("The harbor");
        assertThat(TurnDetailsMerge.title("", "The letter", 200)).isEqualTo("The letter");
        assertThat(TurnDetailsMerge.title("a".repeat(150), "b".repeat(150), 200)).hasSize(200).endsWith("…");
    }
}
