package com.github.enerccio.marginalia.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedTextHighlighterTest {

    private static String highlight(String response, String original) {
        return GeneratedTextHighlighter.highlight(response, original, "g");
    }

    private static String mark(String text) {
        return "<mark class=\"g\">" + text + "</mark>";
    }

    @Test
    void untouchedTextIsHighlightedWhole() {
        assertThat(highlight("The night was dark.", "The night was dark.")).isEqualTo(mark("The night was dark."));
    }

    @Test
    void withoutOriginalNothingIsHighlighted() {
        assertThat(highlight("Written by hand.", null)).isEqualTo("Written by hand.");
        assertThat(highlight("Written by hand.", "")).isEqualTo("Written by hand.");
        assertThat(highlight("Written by hand.", "Something else entirely")).isEqualTo("Written by hand.");
    }

    @Test
    void authorsAdditionsAndRewritesStayUnmarked() {
        assertThat(highlight("The very dark night was cold.", "The dark night was long."))
                .isEqualTo(mark("The") + " very " + mark("dark night was") + " cold" + mark("."));
    }

    @Test
    void removedTextLeavesTheRestHighlighted() {
        assertThat(highlight("The night.", "The dark night.")).isEqualTo(mark("The night."));
    }

    @Test
    void everyLineIsWrappedOnItsOwn() {
        assertThat(highlight("One two.\n\nThree four.", "One two.\n\nThree four."))
                .isEqualTo(mark("One two.") + "\n\n" + mark("Three four."));
    }

    @Test
    void markdownAtTheStartOfALineStaysOutsideTheMark() {
        assertThat(highlight("# Title\n\n- item one\n> quote", "# Title\n\n- item one\n> quote"))
                .isEqualTo("# " + mark("Title") + "\n\n- " + mark("item one") + "\n> " + mark("quote"));
    }

    @Test
    void longRewriteStillWorks() {
        StringBuilder original = new StringBuilder();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < 6000; i++) {
            original.append("a").append(i).append(' ');
            current.append("b").append(i).append(' ');
        }
        assertThat(highlight(current.toString(), original.toString())).isEqualTo(current.toString());
    }
}
