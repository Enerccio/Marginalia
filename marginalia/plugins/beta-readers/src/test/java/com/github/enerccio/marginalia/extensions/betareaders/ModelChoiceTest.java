package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.extensions.betareaders.service.ModelChoice;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The choice of the chat wins over the chain, and what is gone counts as not set.
 */
class ModelChoiceTest {

    private static AI ai(String name, boolean deleted) {
        AI ai = new AI();
        ai.setName(name);
        ai.setDeleted(deleted);
        return ai;
    }

    private final Map<String, AI> all = new HashMap<>(Map.of(
            "tab", ai("tab", false), "chain", ai("chain", false), "gone", ai("gone", true)));

    private AI pick(String... candidates) throws Exception {
        return ModelChoice.firstUsable(Arrays.asList(candidates), all::get, found -> !found.isDeleted());
    }

    @Test
    void theFirstCandidateWins() throws Exception {
        assertEquals("tab", pick("tab", "chain").getName());
    }

    @Test
    void emptyDeletedAndUnknownCandidatesAreSkipped() throws Exception {
        assertEquals("chain", pick(null, "chain").getName());
        assertEquals("chain", pick("", "chain").getName());
        assertEquals("chain", pick("gone", "chain").getName());
        assertEquals("chain", pick("unknown", "chain").getName());
    }

    @Test
    void nothingUsableIsNull() throws Exception {
        assertNull(pick("gone", null, "unknown"));
        assertNull(ModelChoice.firstUsable(List.of(), all::get, found -> true));
    }
}
