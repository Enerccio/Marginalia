package com.github.enerccio.marginalia.ui.shortcuts;

import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The key of every action as the user has it: what they set in the key bindings of the user settings, the default of the
 * action otherwise. The settings store only what differs from the defaults; an empty text is "no key".
 */
public final class KeyBindings {

    private final Map<String, String> keys = new LinkedHashMap<>();

    private KeyBindings() {
    }

    /**
     * The bindings of the user for all registered actions (a stored key that is not a valid combination counts as not set).
     */
    public static KeyBindings of(UserSetting setting) {
        KeyBindings bindings = new KeyBindings();
        JsonObject stored = setting == null ? null : setting.getKeyBindings();
        for (ShortcutAction action : ShortcutRegistry.getActions()) {
            String key = action.getDefaultKey();
            JsonElement override = stored == null ? null : stored.get(action.getId());
            if (override != null && override.isJsonPrimitive()) {
                key = KeyCombo.normalize(override.getAsString());
            }
            bindings.keys.put(action.getId(), key);
        }
        return bindings;
    }

    /**
     * The canonical key combination of the action, null for none.
     */
    public String get(String actionId) {
        return keys.get(actionId);
    }

    /**
     * The key of every registered action (null for none), a copy.
     */
    public Map<String, String> asMap() {
        return new LinkedHashMap<>(keys);
    }

    /**
     * The actions that have a key, as JSON {@code {"id": "key"}} for the browser script.
     */
    public String toJson() {
        JsonObject json = new JsonObject();
        keys.forEach((id, key) -> {
            if (key != null) {
                json.addProperty(id, key);
            }
        });
        return json.toString();
    }

    /**
     * What the setting must store for the keys: the stored bindings, with the ones of the registered actions in {@code keys}
     * replaced by what differs from the default of their action (the bindings of actions that are not registered now, as
     * of an extension that is not loaded, stay).
     *
     * @param existing what the setting stores now
     * @param keys     action id to the key chosen (null or blank for none)
     */
    public static JsonObject toStored(JsonObject existing, Map<String, String> keys) {
        JsonObject stored = existing == null ? new JsonObject() : existing.deepCopy();
        for (ShortcutAction action : ShortcutRegistry.getActions()) {
            if (!keys.containsKey(action.getId())) {
                continue;
            }
            stored.remove(action.getId());
            String key = KeyCombo.normalize(keys.get(action.getId()));
            if (key == null ? action.getDefaultKey() != null : !key.equals(action.getDefaultKey())) {
                stored.addProperty(action.getId(), key == null ? "" : key);
            }
        }
        return stored;
    }
}
