package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

import com.github.enerccio.marginalia.extensions.knowledgetracker.scene.PresentCharacters;
import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The tracked characters of a book as a JSON file, and that file brought into a book (the same one or another).
 * <p>
 * The file has the characters with their aliases, items and the people they have met, nothing of the processing: no
 * cursor, no parts, no message of the book, so it fits any book. An import only adds: a character whose name the book tracks
 * already (as a name or an alias) gets the aliases and items it does not have, the others are added; an alias that another
 * tracked character is called by is left out. An imported item has no
 * part of the book behind it, so it is never dropped by a reprocess: an initial item stays initial, the others become
 * the user's (manual); an old item stays old, a retraction becomes one by hand.
 */
public final class KnowledgeTransfer {

    /** The value of {@code format} in the file. */
    public static final String FORMAT = "marginalia-knowledge-tracker";
    public static final int VERSION = 1;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /** What an import did. */
    public record ImportResult(int newCharacters, int updatedCharacters, int itemsAdded, int itemsSkipped) {
    }

    private KnowledgeTransfer() {
    }

    /**
     * The tracked characters of {@code state} as the JSON of an export file.
     */
    public static String export(KnowledgeState state) {
        JsonObject root = new JsonObject();
        root.addProperty("format", FORMAT);
        root.addProperty("version", VERSION);
        JsonArray characters = new JsonArray();
        for (TrackedCharacter character : state.getCharacters()) {
            JsonObject c = new JsonObject();
            c.addProperty("name", character.getName());
            JsonArray aliases = new JsonArray();
            character.getAliases().forEach(aliases::add);
            c.add("aliases", aliases);
            JsonArray items = new JsonArray();
            for (KnowledgeItem item : character.getItems()) {
                JsonObject i = new JsonObject();
                i.addProperty("text", item.getText());
                i.addProperty("kind", item.getKind().name());
                i.addProperty("origin", item.getOrigin().name());
                if (item.getOldReason() != null) {
                    i.addProperty("old", item.getOldReason().name());
                }
                if (item.isLocked()) {
                    i.addProperty("locked", true);
                }
                items.add(i);
            }
            c.add("items", items);
            JsonArray met = new JsonArray();
            character.getMet().forEach(person -> met.add(person.getName()));
            c.add("met", met);
            characters.add(c);
        }
        root.add("characters", characters);
        return GSON.toJson(root);
    }

    /**
     * Adds the characters of an export file to {@code state}. Nothing is changed when the file can't be read.
     *
     * @throws IllegalArgumentException when the text is not an export file, with what is wrong
     */
    public static ImportResult importInto(KnowledgeState state, String json) {
        List<JsonObject> characters = parse(json);
        int created = 0;
        int updated = 0;
        int added = 0;
        int skipped = 0;
        for (JsonObject c : characters) {
            String name = string(c, "name");
            if (name == null || PresentCharacters.normalize(name).isEmpty()) {
                continue;
            }
            List<String> aliases = strings(c, "aliases");
            // by the name only: a character that shares an alias with another one is still another character
            TrackedCharacter target = state.findByName(name);
            boolean isNew = target == null;
            if (isNew) {
                target = new TrackedCharacter(name.strip());
            }
            // an alias that another tracked character is called by stays with that one
            int aliasesBefore = target.getAliases().size();
            for (String alias : aliases) {
                TrackedCharacter owner = state.findByName(alias);
                if (!PresentCharacters.normalize(alias).isEmpty() && (owner == null || owner == target) && !target.isCalled(alias)) {
                    target.getAliases().add(alias.strip());
                }
            }

            int addedHere = 0;
            JsonArray items = c.has("items") && c.get("items").isJsonArray() ? c.getAsJsonArray("items") : new JsonArray();
            for (JsonElement element : items) {
                KnowledgeItem item = item(element);
                if (item == null) {
                    continue;
                }
                if (hasSame(target, item)) {
                    skipped++;
                } else {
                    target.getItems().add(item);
                    addedHere++;
                }
            }
            int metBefore = target.getMet().size();
            for (String person : strings(c, "met")) {
                target.addMet(person, null);
            }
            boolean changed = addedHere > 0 || target.getMet().size() > metBefore || target.getAliases().size() > aliasesBefore;
            added += addedHere;
            if (isNew) {
                state.getCharacters().add(target);
                created++;
            } else if (changed) {
                updated++;
            }
        }
        return new ImportResult(created, updated, added, skipped);
    }

    /** The characters of the file, checked before anything is changed. */
    private static List<JsonObject> parse(String json) {
        JsonElement root;
        try {
            root = JsonParser.parseString(json == null ? "" : json.strip());
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("not JSON: " + e.getMessage(), e);
        }
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException("not a JSON object");
        }
        JsonObject object = root.getAsJsonObject();
        String format = string(object, "format");
        if (format != null && !FORMAT.equals(format)) {
            throw new IllegalArgumentException("format " + format);
        }
        if (!object.has("characters") || !object.get("characters").isJsonArray()) {
            throw new IllegalArgumentException("no characters");
        }
        List<JsonObject> characters = new ArrayList<>();
        for (JsonElement element : object.getAsJsonArray("characters")) {
            if (element.isJsonObject()) {
                characters.add(element.getAsJsonObject());
            }
        }
        return characters;
    }

    /** An item of the file, {@code null} when it has no text. Unknown kinds are <i>Knows</i>. */
    private static KnowledgeItem item(JsonElement element) {
        if (!element.isJsonObject()) {
            return null;
        }
        JsonObject i = element.getAsJsonObject();
        String text = string(i, "text");
        if (text == null || text.isBlank()) {
            return null;
        }
        ItemKind kind = ItemKind.DOES_NOT_KNOW.name().equals(string(i, "kind")) ? ItemKind.DOES_NOT_KNOW : ItemKind.KNOWS;
        boolean initial = ItemOrigin.INITIAL.name().equals(string(i, "origin"));
        boolean locked = i.has("locked") && i.get("locked").isJsonPrimitive() && i.get("locked").getAsBoolean();
        KnowledgeItem item = KnowledgeItem.imported(text.strip(), kind, initial, locked);
        String old = string(i, "old");
        if (OldReason.REJECTED.name().equals(old)) {
            item.reject();
        } else if (OldReason.TIDIED.name().equals(old)) {
            item.tidy();
        } else if (old != null) {
            item.retractByHand();
        }
        return item;
    }

    /** Whether the character has an item of the same kind and text already (case and spaces aside). */
    private static boolean hasSame(TrackedCharacter character, KnowledgeItem item) {
        String text = normalize(item.getText());
        return character.getItems().stream().anyMatch(other -> other.getKind() == item.getKind() && normalize(other.getText()).equals(text));
    }

    private static String normalize(String text) {
        return text == null ? "" : text.strip().replaceAll("\\s+", " ").toLowerCase();
    }

    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
    }

    private static List<String> strings(JsonObject object, String key) {
        List<String> values = new ArrayList<>();
        JsonElement value = object.get(key);
        if (value != null && value.isJsonArray()) {
            for (JsonElement element : value.getAsJsonArray()) {
                if (element.isJsonPrimitive()) {
                    values.add(element.getAsString());
                }
            }
        }
        return values;
    }
}
