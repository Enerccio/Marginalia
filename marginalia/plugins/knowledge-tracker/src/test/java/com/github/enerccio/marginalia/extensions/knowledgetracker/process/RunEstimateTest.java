package com.github.enerccio.marginalia.extensions.knowledgetracker.process;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunEstimateTest {

    @Test
    void oneRunPerPartByDefault() {
        RunEstimate e = RunEstimate.of(12, 1, 4000);
        assertEquals(12, e.parts());
        assertEquals(12, e.runs());
        assertEquals(1000, e.storyTokens());
    }

    @Test
    void aLastRunWithFewerPartsStillCounts() {
        assertEquals(3, RunEstimate.of(10, 4, 0).runs());
        assertEquals(2, RunEstimate.of(8, 4, 0).runs());
    }

    @Test
    void nothingToReadIsNoRun() {
        assertEquals(0, RunEstimate.of(0, 3, 0).runs());
        assertEquals(0, RunEstimate.of(-2, 3, -5).parts());
    }

    @Test
    void aBadPartsPerRunIsOne() {
        assertEquals(5, RunEstimate.of(5, 0, 0).runs());
    }

    @Test
    void onlyALongSeriesIsConfirmed() {
        assertFalse(RunEstimate.of(3, 1, 0).isLong());
        assertFalse(RunEstimate.of(RunEstimate.CONFIRM_FROM_RUNS, 1, 0).isLong());
        assertTrue(RunEstimate.of(RunEstimate.CONFIRM_FROM_RUNS + 1, 1, 0).isLong());
        assertFalse(RunEstimate.of(40, 20, 0).isLong());
    }
}
