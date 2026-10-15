package com.github.enerccio.marginalia.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helpers of {@link Restorable} implementations. The stored state is read defensively: a missing key or a value of
 * another type than expected is the same as no value.
 */
public final class RestorableState {

    private RestorableState() {
    }

    /**
     * @return the nested object, {@code null} when there is none
     */
    public static JsonObject object(JsonObject parent, String key) {
        JsonElement element = parent == null ? null : parent.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    /**
     * @return the string value, {@code null} when there is none
     */
    public static String string(JsonObject parent, String key) {
        JsonElement element = parent == null ? null : parent.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }

    public static String string(JsonObject parent, String key, String defaultValue) {
        String value = string(parent, key);
        return value != null ? value : defaultValue;
    }

    public static boolean bool(JsonObject parent, String key, boolean defaultValue) {
        JsonElement element = parent == null ? null : parent.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()
                ? element.getAsBoolean() : defaultValue;
    }

    /**
     * @return the strings of the array, empty when there is none
     */
    public static List<String> strings(JsonObject parent, String key) {
        List<String> values = new ArrayList<>();
        JsonElement element = parent == null ? null : parent.get(key);
        if (element != null && element.isJsonArray()) {
            for (JsonElement item : element.getAsJsonArray()) {
                if (item.isJsonPrimitive()) {
                    values.add(item.getAsString());
                }
            }
        }
        return values;
    }

    public static JsonArray array(Collection<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    /**
     * Adds the state under the key unless it is empty, so a component with nothing to remember leaves no trace.
     */
    public static void put(JsonObject parent, String key, JsonObject state) {
        if (state != null && state.size() > 0) {
            parent.add(key, state);
        }
    }

}
