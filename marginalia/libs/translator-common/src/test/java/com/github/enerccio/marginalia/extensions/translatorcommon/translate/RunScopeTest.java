package com.github.enerccio.marginalia.extensions.translatorcommon.translate;

import com.github.enerccio.marginalia.domain.components.ModelText;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RunScopeTest {

    @Test
    void aRunForAllKeepsNothingAndHasNoWindow() {
        assertFalse(RunScope.ALL.missingOnly());
        assertFalse(RunScope.ALL.hasWindow());
        assertNull(RunScope.ALL.window(ModelText.of("Hello.", List.of())));
        assertTrue(RunScope.admits(null, 0, 5));
    }

    @Test
    void theMissingRunHasAWindowOnlyWhenARangeWasGiven() {
        assertTrue(RunScope.MISSING.missingOnly());
        assertFalse(RunScope.MISSING.hasWindow());
        RunScope in = RunScope.missingIn(10, 20);
        assertTrue(in.missingOnly());
        assertArrayEquals(new int[]{10, 20}, in.window(ModelText.of("x".repeat(30), List.of())));
    }

    @Test
    void aSpeechIsAdmittedWhenItTouchesTheWindow() {
        int[] window = {10, 20};
        assertTrue(RunScope.admits(window, 12, 15));
        assertTrue(RunScope.admits(window, 5, 11));
        assertTrue(RunScope.admits(window, 19, 30));
        assertFalse(RunScope.admits(window, 0, 10));
        assertFalse(RunScope.admits(window, 20, 25));
    }

    // the stored part has one translated speech: "Tell me." is the source of "Sag mir." in the text the model reads
    private static ModelText translated() {
        return ModelText.of("He said \"Sag mir.\" and \"Go.\"", List.of(new ModelText.Substitution(9, 16, "Tell me.")));
    }

    @Test
    void aMissingRunShowsTheModelThePartAsItIsStored() {
        ModelText read = translated();

        assertTrue(RunScope.MISSING.showsStored(read));
        assertEquals(read.stored(), RunScope.MISSING.shown(read));
        assertTrue(RunScope.missingIn(0, 5).showsStored(read));
    }

    @Test
    void aRunForAllOrAPartWithoutTranslationsShowsTheEnglish() {
        ModelText read = translated();

        assertFalse(RunScope.ALL.showsStored(read));
        assertEquals(read.text(), RunScope.ALL.shown(read));
        ModelText plain = ModelText.unchanged("He said \"Go.\"");
        assertFalse(RunScope.MISSING.showsStored(plain));
        assertEquals(plain.text(), RunScope.MISSING.shown(plain));
    }

    @Test
    void aRangeOfTheShownTextIsMovedToTheTextTheModelReads() {
        ModelText read = translated();
        String shown = RunScope.MISSING.shown(read);
        int start = shown.indexOf("Go.");

        int[] range = RunScope.MISSING.inModel(read, start, start + 3);

        assertEquals("Go.", read.text().substring(range[0], range[1]));
        assertNotEquals(start, range[0], "the English is longer than the translation");
        // a range inside the translated speech grows to the whole speech of the text the model reads
        int[] inside = RunScope.MISSING.inModel(read, 10, 13);
        assertEquals("Tell me.", read.text().substring(inside[0], inside[1]));
        // a run for all is not moved
        assertArrayEquals(new int[]{3, 7}, RunScope.ALL.inModel(read, 3, 7));
    }
}
