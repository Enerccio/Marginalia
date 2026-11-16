package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

import com.github.enerccio.marginalia.extensions.storytracker.scene.PresentCharacters;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

/**
 * The characters of the Character Tracker plugin brought into the Knowledge Tracker of the same book.
 * <p>
 * The Character Tracker keeps its characters and relationships in the attributes of the book under {@link #KEY}; they are read as
 * plain JSON, so this plugin does not need the other one (installed or not, the data is in the book). Only the names are taken:
 * the tagline and the note describe a character, they are not something it knows. A relationship can be taken as a meeting:
 * the imported character has then met the people it is related to.
 */
public final class CharacterTrackerImport {

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

    /** What an import did: tracked characters added, and meetings recorded for them. */
    public record ImportResult(int added, int met) {
    }

    private CharacterTrackerImport() {
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

    /**
     * The characters of {@code candidates} that {@code state} does not track yet (by name or alias).
     */
    public static List<Candidate> untracked(KnowledgeState state, List<Candidate> candidates) {
        return candidates.stream().filter(c -> state.findByName(c.name()) == null).toList();
    }

    /**
     * Starts tracking the {@code chosen} characters; one the state tracks already (or one chosen twice) is skipped.
     *
     * @param relationsAsMet whether an added character has met the people it is related to (as written by the user: a
     *                       reprocessing keeps them)
     */
    public static ImportResult importInto(KnowledgeState state, List<Candidate> chosen, boolean relationsAsMet) {
        int added = 0;
        int met = 0;
        for (Candidate candidate : chosen) {
            if (PresentCharacters.normalize(candidate.name()).isEmpty() || state.findByName(candidate.name()) != null) {
                continue;
            }
            TrackedCharacter character = new TrackedCharacter(candidate.name().strip());
            if (relationsAsMet) {
                candidate.related().forEach(person -> character.addMet(person, null));
                met += character.getMet().size();
            }
            state.getCharacters().add(character);
            added++;
        }
        return new ImportResult(added, met);
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
