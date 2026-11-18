package com.github.enerccio.marginalia.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextAlignmentTest {

    /**
     * The passage {@code quote} of the old text, as it is in the new one; null when it is gone.
     */
    private static String follow(String oldText, String newText, String quote) {
        int start = oldText.indexOf(quote);
        assertThat(start).as("quote in the old text").isGreaterThanOrEqualTo(0);
        int end = start + quote.length();
        TextAlignment alignment = TextAlignment.of(oldText, newText);
        if (!alignment.isPartlyRetained(oldText, start, end)) {
            return null;
        }
        int newStart = alignment.mapStart(start);
        int newEnd = alignment.mapEnd(end);
        return newEnd <= newStart ? null : newText.substring(newStart, newEnd);
    }

    @Test
    void sameTextKeepsEverything() {
        TextAlignment alignment = TextAlignment.of("Anna met John.", "Anna met John.");

        assertThat(alignment.isApproximate()).isFalse();
        assertThat(alignment.mapStart(5)).isEqualTo(5);
        assertThat(alignment.mapEnd(9)).isEqualTo(9);
        assertThat(alignment.isRetained(0, 14)).isTrue();
    }

    @Test
    void editBeforeThePassageMovesIt() {
        assertThat(follow("Anna met John at the bar.", "Yesterday Anna met John at the bar.", "John at")).isEqualTo("John at");
        assertThat(follow("Anna met John at the bar.", "Met John at the bar.", "John at")).isEqualTo("John at");
    }

    @Test
    void editAfterThePassageLeavesIt() {
        assertThat(follow("Anna met John at the bar.", "Anna met John at the bar. Then she left.", "met John")).isEqualTo("met John");
    }

    @Test
    void editInsideThePassageGrowsAndShrinksIt() {
        assertThat(follow("Anna met John at the bar.", "Anna met John at once the bar.", "John at the")).isEqualTo("John at once the");
        assertThat(follow("Anna met John at the bar.", "Anna met John the bar.", "John at the")).isEqualTo("John the");
        assertThat(follow("Anna met John at the bar.", "Anna met old John at the bar.", "met John at")).isEqualTo("met old John at");
    }

    @Test
    void textTypedExactlyAtAnEdgeLandsOutside() {
        // before the first character
        assertThat(follow("Anna met John", "Anna met big John", "John")).isEqualTo("John");
        // after the last character
        assertThat(follow("Anna met John at the bar", "Anna met John Smith at the bar", "John")).isEqualTo("John");
        assertThat(follow("Anna met John", "Anna met John and Mary", "John")).isEqualTo("John");
    }

    @Test
    void aPassageThatIsDeletedCompletelyIsGone() {
        assertThat(follow("Anna met John at the bar. She left.", "Anna met John at the bar.", "She left.")).isNull();
        assertThat(follow("Anna met John at the bar. She left early.", "Anna left early.", "met John at the bar.")).isNull();
    }

    @Test
    void aPassageWhoseWordsWereAllReplacedIsGone() {
        // the spaces between the words stay, they are not the passage
        assertThat(follow("The old man sat down", "The young boy sat down", "old man")).isNull();
    }

    @Test
    void aPassageThatLostAPartOfItKeepsTheRest() {
        assertThat(follow("The old man sat down slowly", "The old man sat", "man sat down slowly")).isEqualTo("man sat");
    }

    @Test
    void aFixedTypoInAOneWordPassageKeepsThePassage() {
        assertThat(follow("Anna met Jon at the bar", "Anna met John at the bar", "Jon")).isEqualTo("John");
        assertThat(follow("Anna met John at the bar", "Anna met Johnny at the bar", "John")).isEqualTo("John");
        assertThat(follow("The cat sat", "The cart sat", "cat")).isEqualTo("cart");
    }

    @Test
    void anotherWordIsNotATypo() {
        assertThat(follow("She ate an apple", "She ate an avocado", "apple")).isNull();
    }

    @Test
    void severalEditsAreFollowedTogether() {
        String before = "Chapter one. Anna met John at the bar. They talked for hours. Then she left.";
        String after = "Prologue. Chapter one. Anna met old John at the bar. They talked. Then she left alone.";

        assertThat(follow(before, after, "Anna met John at the bar.")).isEqualTo("Anna met old John at the bar.");
        assertThat(follow(before, after, "Then she left.")).isEqualTo("Then she left alone.");
        assertThat(follow(before, after, "They talked for hours.")).isEqualTo("They talked.");
    }

    @Test
    void textsTooDifferentToFollowAreApproximate() {
        StringBuilder before = new StringBuilder("Start of the part. ");
        StringBuilder after = new StringBuilder("Start of the part. ");
        for (int i = 0; i < 2500; i++) {
            before.append("alpha").append(i).append(' ');
            after.append("beta").append(i).append(' ');
        }
        before.append("The end.");
        after.append("The end.");

        TextAlignment alignment = TextAlignment.of(before.toString(), after.toString());

        assertThat(alignment.isApproximate()).isTrue();
        // the common start and end are still known exactly
        assertThat(alignment.isRetained(0, 17)).isTrue();
        assertThat(alignment.isRetained(before.length() - 8, before.length())).isTrue();
        assertThat(alignment.isRetained(30, 60)).isFalse();
    }

    @Test
    void emptyTextsAreHandled() {
        assertThat(TextAlignment.of("", "Something").getSegments()).isEmpty();
        assertThat(TextAlignment.of("Something", "").getSegments()).isEmpty();
        assertThat(TextAlignment.of(null, null).getSegments()).isEmpty();
        assertThat(TextAlignment.of("Something", "").mapEnd(4)).isZero();
    }
}
