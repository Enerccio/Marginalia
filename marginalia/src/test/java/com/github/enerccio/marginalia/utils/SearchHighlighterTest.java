package com.github.enerccio.marginalia.utils;

import com.github.enerccio.marginalia.domain.service.search.FulltextQuery;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class SearchHighlighterTest {

    private static Pattern pattern(String query) {
        return FulltextQuery.parse(query).highlightPattern();
    }

    private static String highlight(String text, String query, int current) {
        return SearchHighlighter.highlight(text, pattern(query), "m", current, "c");
    }

    @Test
    void everyMatchIsWrappedIgnoringCase() {
        assertThat(highlight("The Key and the key.", "key", -1))
                .isEqualTo("The <mark class=\"m\">Key</mark> and the <mark class=\"m\">key</mark>.");
    }

    @Test
    void theCurrentMatchGetsTheSecondClass() {
        assertThat(highlight("key key", "key", 1))
                .isEqualTo("<mark class=\"m\">key</mark> <mark class=\"m c\">key</mark>");
    }

    @Test
    void everyWordAndPhraseIsMarked() {
        assertThat(highlight("the old key and a lamp", "\"old key\" lamp", -1))
                .isEqualTo("the <mark class=\"m\">old key</mark> and a <mark class=\"m\">lamp</mark>");
    }

    @Test
    void wildcardStaysOnOneLine() {
        assertThat(highlight("light house\nlighthouse", "li*house", -1))
                .isEqualTo("<mark class=\"m\">light house</mark>\n<mark class=\"m\">lighthouse</mark>");
        assertThat(SearchHighlighter.count("li\nhouse", pattern("li*house"))).isZero();
    }

    @Test
    void textWithoutMatchIsReturnedAsItIs() {
        assertThat(highlight("Nothing here", "key", -1)).isEqualTo("Nothing here");
        assertThat(SearchHighlighter.highlight("text", null, "m", -1, "c")).isEqualTo("text");
    }

    @Test
    void tagsOfTheDiffViewAreNotSearched() {
        String text = "<mark class=\"generated-text\">The mark of the key</mark> and a mark";
        assertThat(highlight(text, "mark", -1)).isEqualTo(
                "<mark class=\"generated-text\">The <mark class=\"m\">mark</mark> of the key</mark> and a <mark class=\"m\">mark</mark>");
        assertThat(SearchHighlighter.count(text, pattern("mark"))).isEqualTo(2);
    }

    @Test
    void countAgreesWithTheMarks() {
        String text = "aaa aa a";
        assertThat(SearchHighlighter.count(text, pattern("a"))).isEqualTo(6);
        assertThat(highlight(text, "a", -1).split("<mark", -1)).hasSize(7);
    }

    @Test
    void queryMatchesTextWithEveryWord() {
        FulltextQuery query = FulltextQuery.parse("old KEY");
        assertThat(query.matches("The key is old.")).isTrue();
        assertThat(query.matches("The key is new.")).isFalse();
        assertThat(FulltextQuery.parse("").matches("anything")).isFalse();
    }
}
