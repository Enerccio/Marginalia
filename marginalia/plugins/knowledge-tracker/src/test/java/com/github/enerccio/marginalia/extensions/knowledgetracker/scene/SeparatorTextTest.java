package com.github.enerccio.marginalia.extensions.knowledgetracker.scene;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The separators as edited text.
 */
class SeparatorTextTest {

    @Test
    void theDefaultsSurviveTheRoundTrip() {
        String text = SeparatorText.format(PresentCharacters.DEFAULT_SEPARATORS);
        assertEquals(",\n;\n\\n\n/\n|\n、\n，\n；", text);
        assertEquals(PresentCharacters.DEFAULT_SEPARATORS, SeparatorText.parse(text));
    }

    @Test
    void wordsAreStrippedAndEmptyLinesIgnored() {
        assertEquals(List.of(",", "and"), SeparatorText.parse("  ,  \n\n and \r\n"));
    }

    @Test
    void nothingGivesNothing() {
        assertEquals(List.of(), SeparatorText.parse(null));
        assertEquals(List.of(), SeparatorText.parse(" \n "));
        assertEquals("", SeparatorText.format(List.of()));
    }
}
