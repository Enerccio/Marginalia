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
}
