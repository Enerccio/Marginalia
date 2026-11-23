package com.github.enerccio.marginalia.extensions.arcoutline.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The characters and the relationships of the book by the numbers the commands of the Character Tracker give them
 * ({@code list-characters}, {@code list-character-relations}), read before a call runs. The model may address a character by
 * that number; the author does not know it, so the plot wizard shows the name next to it, in the question before a call and in
 * the row of the call. The plugin is not imported: its results are read as the JSON they are.
 */
public final class CharacterNumbers {

    /** Nothing known: the numbers are shown as they are. */
    public static final CharacterNumbers NONE = new CharacterNumbers(List.of(), Map.of());

    /** The names, the character number n at n - 1. */
    private final List<String> names;
    /** The relationships by their number, described as {@code A -> B (name)}. */
    private final Map<Integer, String> relations;

    CharacterNumbers(List<String> names, Map<Integer, String> relations) {
        this.names = names;
        this.relations = relations;
    }

    /**
     * Reads the results of {@code list-characters} and {@code list-character-relations}; what can't be read is left out.
     */
    public static CharacterNumbers parse(String characters, String relationships) {
        List<String> names = new ArrayList<>();
        JsonArray list = array(characters, "characters");
        for (JsonElement element : list) {
            if (element.isJsonObject()) {
                JsonObject row = element.getAsJsonObject();
                int number = integer(row, "number");
                String name = text(row, "name");
                if (number == names.size() + 1 && name != null) {
                    names.add(name);
                }
            }
        }
        Map<Integer, String> relations = new LinkedHashMap<>();
        for (JsonElement element : array(relationships, "relationships")) {
            if (element.isJsonObject()) {
                JsonObject row = element.getAsJsonObject();
                int number = integer(row, "number");
                String source = text(row, "source");
                String target = text(row, "target");
                if (number > 0 && source != null && target != null) {
                    boolean both = row.has("bidirectional") && row.get("bidirectional").isJsonPrimitive() && row.get("bidirectional").getAsBoolean();
                    String name = text(row, "name");
                    relations.put(number, source + (both ? " <-> " : " -> ") + target + (StringUtils.isBlank(name) ? "" : " (" + name + ")"));
                }
            }
        }
        return names.isEmpty() && relations.isEmpty() ? NONE : new CharacterNumbers(names, relations);
    }

    /**
     * The value of an argument as the author should see it: a character given by its number gets its name
     * ({@code 3 (Bob)}), a relationship its two characters. A name, or a number that is the name of a character, is left as it is.
     */
    public String describe(String parameter, Object value) {
        String text = value == null ? null : String.valueOf(value);
        String named = name(parameter, text);
        return named == null || named.equals(text) ? text : text + " (" + named + ")";
    }

    /**
     * Whom or what a call of a tool of the characters is about, for the row of the call: the name of the character, the
     * relationship; null for the commands that are about the whole cast (or when nothing is known).
     *
     * @param arguments what the model passed, by parameter name
     */
    public String subject(String command, Map<String, ?> arguments) {
        if (command == null || arguments == null) {
            return null;
        }
        return switch (command) {
            case "get-character", "update-character", "delete-character" -> name("character", string(arguments.get("character")));
            case "create-character" -> StringUtils.trimToNull(string(arguments.get("name")));
            case "list-character-relations" -> name("character", string(arguments.get("character")));
            case "add-character-relation" -> {
                String source = name("source", string(arguments.get("source")));
                String target = name("target", string(arguments.get("target")));
                yield source == null || target == null ? null : source + " -> " + target;
            }
            case "update-character-relation", "delete-character-relation" -> name("relationship", string(arguments.get("relationship")));
            default -> null;
        };
    }

    /** What the value names: the character or the relationship of that number, the value itself when it is a name. */
    private String name(String parameter, String value) {
        String text = StringUtils.trimToNull(value);
        if (text == null) {
            return null;
        }
        if ("relationship".equals(parameter)) {
            Integer number = number(text);
            return number == null ? text : relations.getOrDefault(number, text);
        }
        if (!"character".equals(parameter) && !"source".equals(parameter) && !"target".equals(parameter)) {
            return text;
        }
        // a character called "3" is that character, as the command takes it
        if (names.stream().anyMatch(name -> text.equalsIgnoreCase(StringUtils.strip(name)))) {
            return text;
        }
        Integer number = number(text);
        return number != null && number >= 1 && number <= names.size() ? names.get(number - 1) : text;
    }

    private static Integer number(String text) {
        String digits = text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
        try {
            return Integer.parseInt(digits.strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String string(Object value) {
        if (value instanceof JsonElement element) {
            return element.isJsonPrimitive() ? element.getAsString() : element.toString();
        }
        return value == null ? null : String.valueOf(value);
    }

    private static JsonArray array(String json, String key) {
        if (StringUtils.isBlank(json)) {
            return new JsonArray();
        }
        try {
            JsonElement root = JsonParser.parseString(json);
            if (root.isJsonObject() && root.getAsJsonObject().has(key) && root.getAsJsonObject().get(key).isJsonArray()) {
                return root.getAsJsonObject().getAsJsonArray(key);
            }
        } catch (RuntimeException e) {
            // not the JSON of the command: nothing is known
        }
        return new JsonArray();
    }

    private static int integer(JsonObject row, String key) {
        JsonElement value = row.get(key);
        try {
            return value != null && value.isJsonPrimitive() ? value.getAsInt() : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String text(JsonObject row, String key) {
        JsonElement value = row.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
    }

    /** The arguments of a call as the model wrote them (a JSON object); empty when they are not one. */
    public static Map<String, Object> arguments(String json) {
        Map<String, Object> arguments = new LinkedHashMap<>();
        if (StringUtils.isBlank(json)) {
            return arguments;
        }
        try {
            JsonElement root = JsonParser.parseString(json);
            if (root.isJsonObject()) {
                root.getAsJsonObject().entrySet().forEach(entry -> arguments.put(entry.getKey(), entry.getValue()));
            }
        } catch (RuntimeException e) {
            // not JSON: nothing to describe
        }
        return arguments;
    }
}
