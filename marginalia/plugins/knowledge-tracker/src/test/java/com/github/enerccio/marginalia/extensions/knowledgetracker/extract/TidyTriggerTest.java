package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.Part;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * When the tidy pass starts by itself, and the count of parts it is measured by.
 */
class TidyTriggerTest {

    private static KnowledgeState automatic(int every) {
        KnowledgeState state = new KnowledgeState();
        state.getSettings().setExtractWithModel(true);
        state.getSettings().setAutoTidy(true);
        state.getSettings().setTidyEveryParts(every);
        return state;
    }

    @Test
    void itIsDueOnlyAfterEnoughPartsWereRead() {
        KnowledgeState state = automatic(3);
        assertFalse(TidyTrigger.due(state, false));
        state.partsRead(2);
        assertFalse(TidyTrigger.due(state, false));
        state.partsRead(1);
        assertTrue(TidyTrigger.due(state, false));
    }

    @Test
    void aTidyStartsTheCountAgain() {
        KnowledgeState state = automatic(2);
        state.partsRead(5);
        assertTrue(TidyTrigger.due(state, false));
        state.tidied();
        assertEquals(0, state.getPartsSinceTidy());
        assertFalse(TidyTrigger.due(state, false));
    }

    @Test
    void aRunTheUserReviewsNeverStartsATidy() {
        KnowledgeState state = automatic(1);
        state.partsRead(9);
        assertFalse(TidyTrigger.due(state, true));
    }

    @Test
    void itNeedsTheSettingAndTheModel() {
        KnowledgeState state = automatic(1);
        state.partsRead(9);
        state.getSettings().setAutoTidy(false);
        assertFalse(TidyTrigger.due(state, false));
        state.getSettings().setAutoTidy(true);
        state.getSettings().setExtractWithModel(false);
        assertFalse(TidyTrigger.due(state, false));
    }

    @Test
    void theDefaultsAreOffAndTenAndOldDataReadsAsThose() {
        KnowledgeState state = new Gson().fromJson("{\"settings\":{\"partsPerRun\":2}}", KnowledgeState.class);
        assertFalse(state.getSettings().isAutoTidy());
        assertEquals(10, state.getSettings().getTidyEveryParts());
        assertEquals(0, state.getPartsSinceTidy());
    }

    @Test
    void aCommittedRunCountsItsParts() {
        KnowledgeState state = new KnowledgeState();
        List<Part> branch = List.of(new Part("m1", null, "x", "a"), new Part("m2", null, "x", "b"), new Part("m3", null, "x", "c"));
        assertNotNull(RunCommit.commit(state, branch, List.of(branch.get(0), branch.get(1)), new ChangeSet(List.of(), List.of()), null, 1L));
        assertEquals(2, state.getPartsSinceTidy());
        assertNotNull(RunCommit.commit(state, branch, List.of(branch.get(2)), new ChangeSet(List.of(), List.of()), null, 2L));
        assertEquals(3, state.getPartsSinceTidy());
    }
}
