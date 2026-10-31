package com.github.enerccio.marginalia.utils;

import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Carries the dynamic attributes of an entity a screen holds over to the entity it loaded again to store it: the extensions put their
 * data into the attributes of the loaded one (their decorators of a save method run before its body), and the body works on a fresh one.
 */
public final class AttributeMerge {

    private AttributeMerge() {
    }

    /**
     * Puts every key of {@code held} into {@code fresh}, over the value it has; the keys only {@code fresh} has stay.
     *
     * @return the attributes {@code fresh} has after it (a new object when it had none and {@code held} has some)
     */
    public static JsonObject merge(JsonObject held, JsonObject fresh) {
        if (held == null) {
            return fresh;
        }
        JsonObject result = fresh != null ? fresh : new JsonObject();
        for (Map.Entry<String, com.google.gson.JsonElement> entry : held.entrySet()) {
            result.add(entry.getKey(), entry.getValue().deepCopy());
        }
        return result;
    }
}
