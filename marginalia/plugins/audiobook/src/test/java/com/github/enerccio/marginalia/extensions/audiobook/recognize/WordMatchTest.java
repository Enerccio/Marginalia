package com.github.enerccio.marginalia.extensions.audiobook.recognize;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordMatchTest {

    @Test
    void caseTokensTagsAndPunctuationDoNotCount() {
        assertEquals(List.of("albus", "open", "this", "door", "immediately"),
                WordMatch.words("<|style:shouting|><|prosody:expressive_high|>Albus! Open this door [sigh] immediately!"));
        assertEquals(List.of("peters", "teeth"), WordMatch.words("Peter’s teeth"));
        assertEquals(0.0, WordMatch.errorRate("The banging continued, rhythmic and aggressive.", "the banging continued rhythmic and aggressive"));
    }

    @Test
    void theRateIsTheWrongWordsByTheWordsOfTheText() {
        assertEquals(0.25, WordMatch.errorRate("one two three four", "one two tree four"), 1e-9);
        assertEquals(0.5, WordMatch.errorRate("one two three four", "one two"), 1e-9);
        assertTrue(WordMatch.errorRate("A short line.", "a short line and then a lot more that was never in the text") > 1);
        assertEquals(1.0, WordMatch.errorRate("The banging continued, rhythmic and aggressive.", "drake kumla kund kund kund kund"), 1e-9);
    }

    @Test
    void emptyTexts() {
        assertEquals(0.0, WordMatch.errorRate("", ""));
        assertEquals(1.0, WordMatch.errorRate("", "something"));
        assertEquals(1.0, WordMatch.errorRate("words here", ""));
    }
}
