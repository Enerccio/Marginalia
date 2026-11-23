package com.github.enerccio.marginalia.domain.templates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Variable storage used by the {@code getvar}/{@code setvar} family of macros and by variable shorthands
 * ({@code {{.name}}}, {@code {{$name}}}).
 * <p>
 * Marginalia maps SillyTavern variable scopes to its own concepts:
 * <ul>
 *     <li><b>local</b> variables follow the story branch - they are stored on every generated message and loaded from
 *     the message the new one continues from, so swiping/regenerating does not apply changes twice,</li>
 *     <li><b>global</b> variables belong to the whole manuscript, regardless of the branch.</li>
 * </ul>
 * Values are always kept as strings, same as SillyTavern does when it renders them.
 */
public class TemplateVariables {

    /**
     * Key under which the variables are stored in {@link com.github.enerccio.marginalia.domain.model.ExtendableEntity#getAttributes()}.
     */
    public static final String ATTRIBUTE_KEY = "templateVariables";

    public enum Scope {
        LOCAL, GLOBAL
    }

    private final Map<String, String> local = new LinkedHashMap<>();
    private final Map<String, String> global = new LinkedHashMap<>();

    private Map<String, String> map(Scope scope) {
        return scope == Scope.GLOBAL ? global : local;
    }

    public String get(Scope scope, String name) {
        return map(scope).get(name);
    }

    public boolean has(Scope scope, String name) {
        return map(scope).containsKey(name);
    }

    public void set(Scope scope, String name, String value) {
        if (name == null || name.isEmpty()) {
            return;
        }
        map(scope).put(name, value == null ? "" : value);
    }

    public void delete(Scope scope, String name) {
        map(scope).remove(name);
    }

    public Map<String, String> getLocal() {
        return local;
    }

    public Map<String, String> getGlobal() {
        return global;
    }

    public TemplateVariables copy() {
        TemplateVariables copy = new TemplateVariables();
        copy.local.putAll(local);
        copy.global.putAll(global);
        return copy;
    }

    public JsonObject toJson(Scope scope) {
        JsonObject object = new JsonObject();
        map(scope).forEach(object::addProperty);
        return object;
    }

    public void loadFrom(Scope scope, JsonObject attributes) {
        if (attributes == null || !attributes.has(ATTRIBUTE_KEY)) {
            return;
        }
        JsonElement element = attributes.get(ATTRIBUTE_KEY);
        if (element == null || !element.isJsonObject()) {
            return;
        }
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            JsonElement value = entry.getValue();
            if (value == null || value.isJsonNull()) {
                continue;
            }
            map(scope).put(entry.getKey(), value.isJsonPrimitive() ? value.getAsString() : value.toString());
        }
    }

    public void storeTo(Scope scope, JsonObject attributes) {
        if (attributes == null) {
            return;
        }
        attributes.add(ATTRIBUTE_KEY, toJson(scope));
    }

    @Override
    public String toString() {
        return "TemplateVariables{local=" + local + ", global=" + global + '}';
    }
}
