package com.github.enerccio.marginalia.extensions;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Where a book and a folder keep the values of the {@link InheritedSetting}s of the extensions: an object in their dynamic
 * attributes ({@link #ATTRIBUTE}) with a text for each key. A blank text is not stored: the book or the folder then inherits.
 * The values are not tied to a registered setting, so they stay (and travel with the backups) while the extension is not loaded.
 * Like any extended attribute, a change is stored by the {@code save} of the service of the entity.
 */
public final class InheritedSettingValues {

    /** The name in the attributes of the entity; the values are a JSON object under it. */
    public static final String ATTRIBUTE = "inheritedSettings";

    private InheritedSettingValues() {
    }

    /**
     * What the entity sets itself for the key, null when it sets nothing.
     */
    public static String get(ExtendableEntity entity, String key) {
        JsonObject values = values(entity);
        if (values == null || !values.has(key) || !values.get(key).isJsonPrimitive()) {
            return null;
        }
        String value = values.get(key).getAsString();
        return value.isBlank() ? null : value;
    }

    /**
     * Sets what the entity sets for the key; a blank text clears it (the entity inherits).
     */
    public static void set(ExtendableEntity entity, String key, String value) {
        JsonObject attributes = entity.getAttributes();
        if (attributes == null) {
            attributes = new JsonObject();
            entity.setAttributes(attributes);
        }
        JsonObject values = values(entity);
        if (value == null || value.isBlank()) {
            if (values != null) {
                values.remove(key);
                if (values.isEmpty()) {
                    attributes.remove(ATTRIBUTE);
                }
            }
            return;
        }
        if (values == null) {
            values = new JsonObject();
            attributes.add(ATTRIBUTE, values);
        }
        values.addProperty(key, value);
    }

    /**
     * Everything the entity sets, by key.
     */
    public static Map<String, String> all(ExtendableEntity entity) {
        Map<String, String> all = new LinkedHashMap<>();
        JsonObject values = values(entity);
        if (values != null) {
            for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
                if (entry.getValue().isJsonPrimitive() && !entry.getValue().getAsString().isBlank()) {
                    all.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
        }
        return all;
    }

    private static JsonObject values(ExtendableEntity entity) {
        JsonObject attributes = entity.getAttributes();
        if (attributes == null || !attributes.has(ATTRIBUTE) || !attributes.get(ATTRIBUTE).isJsonObject()) {
            return null;
        }
        return attributes.getAsJsonObject(ATTRIBUTE);
    }
}
