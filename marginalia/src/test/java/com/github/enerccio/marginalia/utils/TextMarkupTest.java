package com.github.enerccio.marginalia.utils;

import com.github.enerccio.marginalia.utils.TextMarkup.Mark;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TextMarkupTest {

    @Test
    void aRangeIsWrapped() {
        assertThat(TextMarkup.apply("Anna met John.", List.of(new Mark(5, 13, "m"))))
                .isEqualTo("Anna <mark class=\"m\">met John</mark>.");
    }

    @Test
    void noRangeLeavesTheTextAlone() {
        assertThat(TextMarkup.apply("Anna", List.of())).isEqualTo("Anna");
        assertThat(TextMarkup.apply("Anna", null)).isEqualTo("Anna");
        assertThat(TextMarkup.apply("", List.of(new Mark(0, 3, "m")))).isEmpty();
        assertThat(TextMarkup.apply("Anna", List.of(new Mark(2, 2, "m"), new Mark(9, 12, "m")))).isEqualTo("Anna");
    }

    @Test
    void extraAttributesAreWritten() {
        assertThat(TextMarkup.apply("Anna met John.", List.of(new Mark(0, 4, "m a", " data-annotation=\"u1\""))))
                .isEqualTo("<mark class=\"m a\" data-annotation=\"u1\">Anna</mark> met John.");
    }

    @Test
    void rangesOutsideTheTextAreClamped() {
        assertThat(TextMarkup.apply("Anna met", List.of(new Mark(5, 99, "m")))).isEqualTo("Anna <mark class=\"m\">met</mark>");
    }

    @Test
    void overlappingRangesShareThePieceTheyCover() {
        // a: 0-8 "Anna met", b: 5-13 "met John"
        String marked = TextMarkup.apply("Anna met John.", List.of(new Mark(0, 8, "a"), new Mark(5, 13, "b")));

        assertThat(marked).isEqualTo("<mark class=\"a\">Anna </mark><mark class=\"a\"><mark class=\"b\">met</mark></mark>"
                + "<mark class=\"b\"> John</mark>.");
    }

    @Test
    void aRangeInsideAnotherIsNested() {
        String marked = TextMarkup.apply("one two three", List.of(new Mark(0, 13, "outer"), new Mark(4, 7, "inner")));

        assertThat(marked).isEqualTo("<mark class=\"outer\">one </mark><mark class=\"outer\"><mark class=\"inner\">two</mark></mark>"
                + "<mark class=\"outer\"> three</mark>");
    }

    @Test
    void everyLineIsWrappedOnItsOwnAndWhiteSpaceStaysOutside() {
        assertThat(TextMarkup.apply("one\n\ntwo three", List.of(new Mark(0, 14, "m"))))
                .isEqualTo("<mark class=\"m\">one</mark>\n\n<mark class=\"m\">two three</mark>");
        assertThat(TextMarkup.apply("a  b  c", List.of(new Mark(1, 6, "m")))).isEqualTo("a  <mark class=\"m\">b</mark>  c");
    }

    @Test
    void theMarkdownThatOpensALineStaysOutside() {
        assertThat(TextMarkup.apply("# Title\n- item one\n> quote", List.of(new Mark(0, 26, "m"))))
                .isEqualTo("# <mark class=\"m\">Title</mark>\n- <mark class=\"m\">item one</mark>\n> <mark class=\"m\">quote</mark>");
    }

    @Test
    void aRangeThatStartsInTheMiddleOfALineKeepsItsFirstWord() {
        // "- " in the middle of a line is not a list
        assertThat(TextMarkup.apply("a - b", List.of(new Mark(2, 5, "m")))).isEqualTo("a <mark class=\"m\">- b</mark>");
    }

    @Test
    void theGeneratedTextTheSearchAndAnAnnotationAreMarkedTogether() {
        String response = "Anna met John at the bar. They talked.";
        List<Mark> marks = new java.util.ArrayList<>();
        for (int[] range : GeneratedTextHighlighter.ranges(response, "Anna met John at the bar. They laughed.")) {
            marks.add(new Mark(range[0], range[1], "g"));
        }
        marks.add(new Mark(response.indexOf("John"), response.indexOf("John") + 4, "s"));
        marks.add(new Mark(response.indexOf("the bar"), response.indexOf("They talked"), "a", " data-annotation=\"x\""));

        String marked = TextMarkup.apply(response, marks);

        // every character of the text is still there once, in order
        assertThat(marked.replaceAll("<[^>]*>", "")).isEqualTo(response);
        assertThat(marked).contains("<mark class=\"s\">John</mark>").contains("data-annotation=\"x\"");
    }
}
