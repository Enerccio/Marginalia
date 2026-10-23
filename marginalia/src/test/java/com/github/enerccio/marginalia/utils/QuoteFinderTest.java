package com.github.enerccio.marginalia.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteFinderTest {

    private static final String TEXT = "Anna walked to the harbour. The sea was calm, and the gulls circled above the boats. "
            + "John waited at the end of the pier. He did not say a word. The sea was calm, and the gulls circled above the boats.";

    private static String found(String text, QuoteFinder.Match match) {
        return match == null ? null : text.substring(match.start(), match.end());
    }

    @Test
    void findsTheQuoteLetterForLetter() {
        QuoteFinder.Match match = QuoteFinder.find(TEXT, "John waited at the end of the pier.", null, null, -1);

        assertThat(match.exact()).isTrue();
        assertThat(found(TEXT, match)).isEqualTo("John waited at the end of the pier.");
    }

    @Test
    void theContextPicksTheRightPlaceOfAQuoteThatIsThereTwice() {
        String quote = "The sea was calm, and the gulls circled above the boats.";

        QuoteFinder.Match first = QuoteFinder.find(TEXT, quote, "harbour. ", " John waited", -1);
        QuoteFinder.Match second = QuoteFinder.find(TEXT, quote, "a word. ", null, -1);

        assertThat(first.start()).isEqualTo(TEXT.indexOf(quote));
        assertThat(second.start()).isEqualTo(TEXT.lastIndexOf(quote));
    }

    @Test
    void theHintDecidesWhenThereIsNoContext() {
        String quote = "The sea was calm, and the gulls circled above the boats.";

        assertThat(QuoteFinder.find(TEXT, quote, null, null, TEXT.length()).start()).isEqualTo(TEXT.lastIndexOf(quote));
        assertThat(QuoteFinder.find(TEXT, quote, null, null, 0).start()).isEqualTo(TEXT.indexOf(quote));
    }

    @Test
    void ignoresTheCaseAndTheAmountOfWhiteSpace() {
        QuoteFinder.Match match = QuoteFinder.find(TEXT, "john  waited\nat the END of the pier", null, null, -1);

        assertThat(match.exact()).isFalse();
        assertThat(found(TEXT, match)).isEqualTo("John waited at the end of the pier");
    }

    @Test
    void findsAQuoteThatTheModelQuotedLoosely() {
        // a word is missing and another one changed
        QuoteFinder.Match match = QuoteFinder.find(TEXT, "John waited at the edge of the pier, he did not say a word", null, null, -1);

        assertThat(match).isNotNull();
        assertThat(match.exact()).isFalse();
        assertThat(found(TEXT, match)).startsWith("John waited").endsWith("a word");
    }

    @Test
    void aPassageThatIsNotThereIsNotFound() {
        assertThat(QuoteFinder.find(TEXT, "A dragon landed on the roof of the church", null, null, -1)).isNull();
        assertThat(QuoteFinder.find(TEXT, "pier", "zzz", "zzz", -1)).isNotNull();
        assertThat(QuoteFinder.find(TEXT, "  ", null, null, -1)).isNull();
        assertThat(QuoteFinder.find(null, "pier", null, null, -1)).isNull();
        assertThat(QuoteFinder.find("", "pier", null, null, -1)).isNull();
    }

    @Test
    void exactSearchDoesNotGuess() {
        assertThat(QuoteFinder.findExact(TEXT, "John waited at the end of the pier.", null, null, -1)).isNotNull();
        assertThat(QuoteFinder.findExact(TEXT, "john waited at the end of the pier.", null, null, -1)).isNull();
    }
}
