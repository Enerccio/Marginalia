package com.github.enerccio.marginalia.utils;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttributeMergeTest {

    private static JsonObject json(String... pairs) {
        JsonObject object = new JsonObject();
        for (int i = 0; i < pairs.length; i += 2) {
            object.addProperty(pairs[i], pairs[i + 1]);
        }
        return object;
    }

    @Test
    void theHeldKeysWinAndTheKeysOnlyTheFreshOneHasStay() {
        JsonObject result = AttributeMerge.merge(json("a", "held", "b", "held"), json("b", "fresh", "c", "fresh"));

        assertEquals("held", result.get("a").getAsString());
        assertEquals("held", result.get("b").getAsString());
        assertEquals("fresh", result.get("c").getAsString());
    }

    @Test
    void nothingHeldLeavesTheFreshOneAsItIs() {
        JsonObject fresh = json("c", "fresh");

        assertSame(fresh, AttributeMerge.merge(null, fresh));
        assertNull(AttributeMerge.merge(null, null));
    }

    @Test
    void aFreshOneWithoutAttributesGetsTheHeldOnes() {
        JsonObject result = AttributeMerge.merge(json("a", "held"), null);

        assertEquals("held", result.get("a").getAsString());
    }

    @Test
    void theValuesAreCopied() {
        JsonObject held = new JsonObject();
        JsonObject inner = new JsonObject();
        inner.addProperty("x", 1);
        held.add("plugin", inner);

        JsonObject result = AttributeMerge.merge(held, new JsonObject());
        inner.addProperty("x", 2);

        assertEquals(1, result.getAsJsonObject("plugin").get("x").getAsInt());
    }
}
