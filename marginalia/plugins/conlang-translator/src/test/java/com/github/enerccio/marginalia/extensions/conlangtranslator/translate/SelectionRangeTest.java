package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The range of a selected sentence: spaces and enclosing quotation marks are not translated.
 */
class SelectionRangeTest {

    private static String cut(String text, int start, int end) {
        int[] range = SelectionRange.trim(text, start, end);
        return text.substring(range[0], range[1]);
    }

    @Test
    void theSpacesAroundTheSelectionAreLeftOut() {
        String text = "Mara said:  I see the enemy.  Then she left.";

        assertEquals("I see the enemy.", cut(text, 10, 30));
    }

    @Test
    void aPairOfQuotationMarksAroundTheWholeSelectionIsLeftOut() {
        String text = "Mara said, \"I see the enemy,\" and left.";

        assertEquals("I see the enemy,", cut(text, 11, 29));
        assertEquals("I see the enemy,", cut("“I see the enemy,”", 0, 18));
    }

    @Test
    void aQuotationMarkThatDoesNotEncloseTheSelectionStays() {
        String text = "She said \"go\" and then \"stay\" twice.";

        assertEquals("go\" and then \"stay", cut(text, 10, 28));
        assertEquals("\"go", cut(text, 9, 12));
    }

    @Test
    void anEmptyOrOutOfRangeSelectionIsEmpty() {
        String text = "  \"  \"  ";
        int[] range = SelectionRange.trim(text, 0, 100);

        assertTrue(range[1] <= range[0]);
        int[] none = SelectionRange.trim("abc", 2, 2);
        assertTrue(none[1] <= none[0]);
    }
}
