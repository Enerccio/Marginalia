package com.github.enerccio.marginalia.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The ways to rename many books: a text before or after the name, a text replaced in it, and a numbering.
 */
class MassRenameTest {

    @Test
    void aPrefixAndAPostfixKeepTheirSpaces() {
        assertThat(MassRename.prefix("The ").rename("Lantern Road", 0)).isEqualTo("The Lantern Road");
        assertThat(MassRename.postfix(" (draft)").rename("Lantern Road", 0)).isEqualTo("Lantern Road (draft)");
    }

    @Test
    void theNewNameHasNoSpacesAround() {
        assertThat(MassRename.prefix("  ").rename("Lantern", 0)).isEqualTo("Lantern");
        assertThat(MassRename.postfix(" ").rename("Lantern", 0)).isEqualTo("Lantern");
    }

    @Test
    void everyOccurrenceIsReplacedAndTheTextIsNotAPattern() {
        assertThat(MassRename.replace("a.", "-", true).rename("a.b a.c aXd", 0)).isEqualTo("-b -c aXd");
        assertThat(MassRename.replace("(1)", "$1", true).rename("Book (1)", 0)).isEqualTo("Book $1");
    }

    @Test
    void theCaseOnlyMattersWhenAskedFor() {
        assertThat(MassRename.replace("book", "Tome", false).rename("Book and BOOK and book", 0)).isEqualTo("Tome and Tome and Tome");
        assertThat(MassRename.replace("book", "Tome", true).rename("Book and BOOK and book", 0)).isEqualTo("Book and BOOK and Tome");
    }

    @Test
    void replacingWithNothingRemovesTheText() {
        assertThat(MassRename.replace(" - FDM", null, true).rename("Half Blood Prince - FDM", 0)).isEqualTo("Half Blood Prince");
    }

    @Test
    void anEmptyTextToFindChangesNothing() {
        assertThat(MassRename.replace("", "x", true).rename("Book", 0)).isEqualTo("Book");
        assertThat(MassRename.replace(null, "x", true).rename("Book", 0)).isEqualTo("Book");
    }

    @Test
    void aNumberingCountsFromTheStartByTheStep() {
        MassRename numbering = MassRename.number("{name} {n}", 5, 10, 1);

        assertThat(numbering.rename("Book", 0)).isEqualTo("Book 5");
        assertThat(numbering.rename("Book", 1)).isEqualTo("Book 15");
        assertThat(numbering.rename("Book", 2)).isEqualTo("Book 25");
    }

    @Test
    void theNumberIsPaddedWithZeros() {
        MassRename numbering = MassRename.number("{n}. {name}", 8, 1, 3);

        assertThat(numbering.rename("Prologue", 0)).isEqualTo("008. Prologue");
        assertThat(numbering.rename("Chapter", 3)).isEqualTo("011. Chapter");
        assertThat(MassRename.number("{n}", 998, 1, 2).rename("x", 2)).isEqualTo("1000");
    }

    @Test
    void aNumberingMayGoDownAndKeepsTheSign() {
        MassRename numbering = MassRename.number("{n}", 1, -1, 2);

        assertThat(numbering.rename("x", 0)).isEqualTo("01");
        assertThat(numbering.rename("x", 2)).isEqualTo("-01");
    }

    @Test
    void thePatternMayLeaveTheNameOutOrUseItTwice() {
        assertThat(MassRename.number("Chapter {n}", 1, 1, 1).rename("Old", 1)).isEqualTo("Chapter 2");
        assertThat(MassRename.number("{name}/{name} {n}", 1, 1, 1).rename("Old", 0)).isEqualTo("Old/Old 1");
    }

    @Test
    void aPlaceholderInTheNameIsNotReplacedAgain() {
        assertThat(MassRename.number("{name} {n}", 1, 1, 1).rename("Odd {n} $1 name", 0)).isEqualTo("Odd {n} $1 name 1");
    }

    @Test
    void aPatternNeedsTheNumber() {
        assertThat(MassRename.hasNumber("{name} {n}")).isTrue();
        assertThat(MassRename.hasNumber("{name}")).isFalse();
        assertThat(MassRename.hasNumber(null)).isFalse();
    }
}
