package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.service.search.FulltextQuery;
import com.github.enerccio.marginalia.domain.service.search.LikePatterns;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FulltextQueryTest {

    @Test
    void everyWordGetsItsPatterns() {
        assertThat(FulltextQuery.parse("key Lighthouse").likePatterns()).containsExactly(
                java.util.List.of("%key%", "%KEY%", "%Key%"),
                java.util.List.of("%Lighthouse%", "%lighthouse%", "%LIGHTHOUSE%"));
    }

    @Test
    void quotesMakeAPhrase() {
        assertThat(FulltextQuery.parse("\"the key\" gone").likePatterns().stream().map(p -> p.getFirst()))
                .containsExactly("%the key%", "%gone%");
    }

    @Test
    void asteriskIsAWildcardAndLikeCharactersAreLiteral() {
        assertThat(FulltextQuery.parse("li*house").likePatterns().getFirst().getFirst()).isEqualTo("%li%house%");
        assertThat(FulltextQuery.parse("100%_a\\b").likePatterns().getFirst().getFirst()).isEqualTo("%100\\%\\_a\\\\b%");
        assertThat(LikePatterns.fromWildcards("a*b_c%d\\e")).isEqualTo("a%b\\_c\\%d\\\\e");
    }

    @Test
    void triesAccentedLettersInTheUsualSpellings() {
        assertThat(FulltextQuery.parse("žluťoučký").likePatterns().getFirst())
                .containsExactly("%žluťoučký%", "%ŽLUŤOUČKÝ%", "%Žluťoučký%");
        assertThat(FulltextQuery.parse("\uD83D\uDE00ab").likePatterns().getFirst()).contains("%\uD83D\uDE00ab%");
    }

    @Test
    void blankQueryHasNoWords() {
        assertThat(FulltextQuery.parse(null).isEmpty()).isTrue();
        assertThat(FulltextQuery.parse("   ").isEmpty()).isTrue();
        assertThat(FulltextQuery.parse("\"  \"").isEmpty()).isTrue();
        assertThat(FulltextQuery.parse("").likePatterns()).isEmpty();
    }

    @Test
    void snippetShowsTheFirstMatchOnOneLine() {
        String text = "xxxxxxxx\nthe  lighthouse\nkey yyyyyyyy";

        assertThat(FulltextQuery.parse("lighthouse").snippet(text, 5)).isEqualTo("…the lighthouse key…");
        assertThat(FulltextQuery.parse("LIGHT*USE").snippet(text, 5)).isEqualTo("…the lighthouse key…");
    }

    @Test
    void snippetHasNoEllipsisWhenItReachesTheEnds() {
        assertThat(FulltextQuery.parse("dawn").snippet("Harbour at dawn", 60)).isEqualTo("Harbour at dawn");
        assertThat(FulltextQuery.parse("nothing").snippet("Harbour at dawn", 60)).isEmpty();
    }
}
