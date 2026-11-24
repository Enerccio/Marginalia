package com.github.enerccio.marginalia.annotation;

import com.github.enerccio.marginalia.domain.components.PhraseMatcher;
import com.github.enerccio.marginalia.domain.model.WatchedPhrase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * How the phrases of a watch list are found in a text, without a model.
 */
class PhraseMatcherTest {

    private static WatchedPhrase phrase(String text) {
        return new WatchedPhrase(text, false, null);
    }

    private static WatchedPhrase regex(String text) {
        return new WatchedPhrase(text, true, null);
    }

    private static List<String> matches(String text, WatchedPhrase... phrases) {
        return PhraseMatcher.find(text, List.of(phrases)).stream().map(PhraseMatcher.Hit::match).toList();
    }

    @Test
    void aPhraseIsFoundWithoutRegardToCaseAndOnlyAsWholeWords() {
        String text = "A Tapestry of lies. The tapestryish wall. Tapestries hang. tapestry";

        assertThat(matches(text, phrase("tapestry"))).containsExactly("Tapestry", "tapestry");
    }

    @Test
    void whiteSpaceMatchesAnyWhiteSpaceAndQuotesMatchTheirCurlyForms() {
        String text = "She let out a\n  breath she didn’t know she was holding.";

        assertThat(matches(text, phrase("a breath she didn't know she was holding"))).containsExactly(
                "a\n  breath she didn’t know she was holding");
    }

    @Test
    void thePlacesComeInTheOrderOfTheTextWithTheirOffsets() {
        String text = "shivers down her spine, and then a smirk. Another smirk.";
        List<PhraseMatcher.Hit> hits = PhraseMatcher.find(text, List.of(phrase("smirk"), phrase("shivers down")));

        assertThat(hits).extracting(PhraseMatcher.Hit::match).containsExactly("shivers down", "smirk", "smirk");
        PhraseMatcher.Hit first = hits.getFirst();
        assertThat(text.substring(first.start(), first.end())).isEqualTo("shivers down");
    }

    @Test
    void aRegularExpressionIsUsedAsItIsAndAnEntryThatIsOffIsNotLookedFor() {
        WatchedPhrase off = phrase("eyes");
        off.setDisabled(true);

        assertThat(matches("Her eyes sparkled, his eyes sparkle.", regex("\\bsparkl(ed|e)\\b"), off))
                .containsExactly("sparkled", "sparkle");
    }

    @Test
    void anEntryThatCanNotBeUsedIsReportedAndLeftOut() {
        assertThat(PhraseMatcher.validate(regex("(unclosed"))).isNotNull();
        assertThat(PhraseMatcher.validate(regex("a*"))).as("matches the empty text").isNotNull();
        assertThat(PhraseMatcher.validate(phrase("  "))).isNotNull();
        assertThat(PhraseMatcher.validate(phrase("fine"))).isNull();

        assertThat(matches("fine (unclosed", regex("(unclosed"), phrase("fine"))).containsExactly("fine");
    }

    @Test
    void theHitsOfOnePartAreCapped() {
        String text = "x ".repeat(PhraseMatcher.MAX_HITS * 2);

        assertThat(PhraseMatcher.find(text, List.of(phrase("x")))).hasSize(PhraseMatcher.MAX_HITS);
    }

    @Test
    void anExpressionThatBacktracksForeverIsGivenUpOn() {
        String text = "a".repeat(40) + "!";
        long start = System.nanoTime();

        List<PhraseMatcher.Hit> hits = PhraseMatcher.find(text, List.of(regex("(a+)+b"), phrase("never")));

        assertThat(hits).isEmpty();
        assertThat(System.nanoTime() - start).as("the budget ends the search").isLessThan(10_000_000_000L);
    }
}
