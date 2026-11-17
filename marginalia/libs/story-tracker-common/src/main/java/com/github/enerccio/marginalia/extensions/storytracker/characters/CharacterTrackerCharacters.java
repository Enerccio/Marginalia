package com.github.enerccio.marginalia.extensions.storytracker.characters;

import com.github.enerccio.marginalia.extensions.storytracker.scene.PresentCharacters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

/**
 * The characters of the Character Tracker plugin in a book, as a tracker imports them.
 * <p>
 * The Character Tracker keeps its characters and relationships in the attributes of the book under {@link #KEY}; they are read as
 * plain JSON, so a tracker does not need the other plugin (installed or not, the data is in the book).
 */
public final class CharacterTrackerCharacters {

    /** Key of the data of the Character Tracker in the attributes of the book. */
    public static final String KEY = "com.github.enerccio.marginalia.extensions.charactertracker";

    /**
     * A character of the Character Tracker.
     *
     * @param name    its name
     * @param tagline its tagline, empty when it has none
     * @param related the names of the characters it has a relationship with, in the order of the relationships
     */
    public record Candidate(String name, String tagline, List<String> related) {
    }

    private CharacterTrackerCharacters() {
    }

    /**
     * The characters the Character Tracker has in a book with these attributes, by name (ignoring case); empty when it has none.
     * Characters without a name are left out.
     */
    public static List<Candidate> read(JsonObject attributes) {
        if (attributes == null || !attributes.has(KEY) || !attributes.get(KEY).isJsonObject()) {
            return List.of();
        }
        JsonObject data = attributes.getAsJsonObject(KEY);
        Map<String, String> names = new LinkedHashMap<>();
        Map<String, String> taglines = new HashMap<>();
        for (JsonObject c : objects(data, "characters")) {
            String uuid = string(c, "uuid");
            String name = string(c, "name");
            if (uuid != null && name != null && !PresentCharacters.normalize(name).isEmpty()) {
                names.put(uuid, name.strip());
                taglines.put(uuid, Objects.toString(string(c, "tagline"), "").strip());
            }
        }
        Map<String, List<String>> related = new HashMap<>();
        for (JsonObject r : objects(data, "relations")) {
            String source = names.get(string(r, "sourceUuid"));
            String target = names.get(string(r, "targetUuid"));
            if (source != null && target != null) {
                related.computeIfAbsent(string(r, "sourceUuid"), _ -> new ArrayList<>()).add(target);
                related.computeIfAbsent(string(r, "targetUuid"), _ -> new ArrayList<>()).add(source);
            }
        }
        List<Candidate> candidates = new ArrayList<>();
        names.forEach((uuid, name) -> candidates.add(new Candidate(name, taglines.get(uuid), related.getOrDefault(uuid, List.of()))));
        candidates.sort(Comparator.comparing(Candidate::name, String.CASE_INSENSITIVE_ORDER));
        return candidates;
    }

    private static List<JsonObject> objects(JsonObject object, String key) {
        List<JsonObject> values = new ArrayList<>();
        JsonElement value = object.get(key);
        if (value != null && value.isJsonArray()) {
            for (JsonElement element : (JsonArray) value) {
                if (element.isJsonObject()) {
                    values.add(element.getAsJsonObject());
                }
            }
        }
        return values;
    }

    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
    }
}
