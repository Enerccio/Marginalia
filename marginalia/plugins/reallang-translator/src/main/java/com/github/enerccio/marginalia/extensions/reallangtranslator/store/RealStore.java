package com.github.enerccio.marginalia.extensions.reallangtranslator.store;

import com.github.enerccio.marginalia.extensions.reallangtranslator.model.*;
import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes what the plugin keeps in attributes: the library of languages and the user level of the inherited settings in the attributes of
 * the user's settings, the settings of a book in the attributes of the book. It touches no service (the caller saves the entity), so it is the part
 * of the storage that has unit tests.
 */
public final class RealStore {

    /** The key in the attributes of the user settings. */
    public static final String LIBRARY_KEY = "com.github.enerccio.marginalia.extensions.reallangtranslator.languages";

    /** The key in the attributes of a folder: its glossary (the entries of the folders and the book are merged with the language's, see {@code Glossary}). */
    public static final String FOLDER_GLOSSARY_KEY = "com.github.enerccio.marginalia.extensions.reallangtranslator.folderglossary";

    /** The key in the attributes of the user settings: the user's own prompts of the word extraction. */
    public static final String PROMPTS_KEY = "com.github.enerccio.marginalia.extensions.reallangtranslator.extractionprompts";
    private static final Gson GSON = new Gson();

    private RealStore() {
    }

    /**
     * The languages of the library, in the order the user keeps them; never null. An entry that cannot be read is left out.
     */
    public static List<RealLanguage> readLibrary(JsonObject attributes) {
        List<RealLanguage> languages = new ArrayList<>();
        if (attributes == null || !attributes.has(LIBRARY_KEY) || !attributes.get(LIBRARY_KEY).isJsonArray()) {
            return languages;
        }
        for (JsonElement element : attributes.getAsJsonArray(LIBRARY_KEY)) {
            try {
                RealLanguage language = GSON.fromJson(element, RealLanguage.class);
                if (language != null && language.getId() != null) {
                    languages.add(language);
                }
            } catch (JsonParseException e) {
                // a language that is not readable must not take the library with it
            }
        }
        return languages;
    }

    public static void writeLibrary(JsonObject attributes, List<RealLanguage> languages) {
        JsonArray array = new JsonArray();
        languages.forEach(language -> array.add(GSON.toJsonTree(language)));
        attributes.add(LIBRARY_KEY, array);
    }

    /**
     * The language with the id, or null (a book may name a language that was deleted).
     */
    public static RealLanguage find(JsonObject attributes, String id) {
        return id == null ? null : readLibrary(attributes).stream().filter(language -> id.equals(language.getId())).findFirst().orElse(null);
    }

    /**
     * Stores the language: replaces the one with the same id (keeping its place), else adds it at the end.
     */
    public static void save(JsonObject attributes, RealLanguage language) {
        List<RealLanguage> languages = readLibrary(attributes);
        for (int i = 0; i < languages.size(); i++) {
            if (languages.get(i).getId().equals(language.getId())) {
                languages.set(i, language);
                writeLibrary(attributes, languages);
                return;
            }
        }
        languages.add(language);
        writeLibrary(attributes, languages);
    }

    /**
     * @return whether the library had the language
     */
    public static boolean delete(JsonObject attributes, String id) {
        List<RealLanguage> languages = readLibrary(attributes);
        boolean removed = languages.removeIf(language -> language.getId().equals(id));
        if (removed) {
            writeLibrary(attributes, languages);
        }
        return removed;
    }

    /**
     * What a book stores, never null (the defaults for a book that has nothing, or has something unreadable).
     */
    public static BookSettings readBook(JsonObject attributes) {
        if (attributes == null || !attributes.has(BookSettings.KEY) || !attributes.get(BookSettings.KEY).isJsonObject()) {
            return new BookSettings();
        }
        try {
            BookSettings settings = GSON.fromJson(attributes.get(BookSettings.KEY), BookSettings.class);
            return settings == null ? new BookSettings() : settings;
        } catch (JsonParseException e) {
            return new BookSettings();
        }
    }

    public static void writeBook(JsonObject attributes, BookSettings settings) {
        attributes.add(BookSettings.KEY, GSON.toJsonTree(settings));
    }

    /**
     * What the user level of the inherited settings holds, never null.
     */
    public static UserDefaults readDefaults(JsonObject attributes) {
        if (attributes == null || !attributes.has(UserDefaults.KEY) || !attributes.get(UserDefaults.KEY).isJsonObject()) {
            return new UserDefaults();
        }
        try {
            UserDefaults defaults = GSON.fromJson(attributes.get(UserDefaults.KEY), UserDefaults.class);
            return defaults == null ? new UserDefaults() : defaults;
        } catch (JsonParseException e) {
            return new UserDefaults();
        }
    }

    public static void writeDefaults(JsonObject attributes, UserDefaults defaults) {
        attributes.add(UserDefaults.KEY, GSON.toJsonTree(defaults));
    }

    /**
     * The glossary a folder holds for a language (the folder's attributes keep an object with a list for each language id), never null.
     */
    public static List<GlossaryEntry> readFolderGlossary(JsonObject attributes, String languageId) {
        List<GlossaryEntry> entries = new ArrayList<>();
        if (attributes == null || !attributes.has(FOLDER_GLOSSARY_KEY) || !attributes.get(FOLDER_GLOSSARY_KEY).isJsonObject()) {
            return entries;
        }
        JsonElement list = attributes.getAsJsonObject(FOLDER_GLOSSARY_KEY).get(languageId == null ? "" : languageId);
        if (list == null || !list.isJsonArray()) {
            return entries;
        }
        for (JsonElement element : list.getAsJsonArray()) {
            try {
                GlossaryEntry entry = GSON.fromJson(element, GlossaryEntry.class);
                if (entry != null) {
                    entries.add(entry);
                }
            } catch (JsonParseException e) {
                // an entry that is not readable must not take the glossary with it
            }
        }
        return entries;
    }

    public static void writeFolderGlossary(JsonObject attributes, String languageId, List<GlossaryEntry> entries) {
        JsonObject all = attributes.has(FOLDER_GLOSSARY_KEY) && attributes.get(FOLDER_GLOSSARY_KEY).isJsonObject()
                ? attributes.getAsJsonObject(FOLDER_GLOSSARY_KEY) : new JsonObject();
        String key = languageId == null ? "" : languageId;
        if (entries.isEmpty()) {
            all.remove(key);
        } else {
            JsonArray array = new JsonArray();
            entries.forEach(entry -> array.add(GSON.toJsonTree(entry)));
            all.add(key, array);
        }
        if (all.size() == 0) {
            attributes.remove(FOLDER_GLOSSARY_KEY);
        } else {
            attributes.add(FOLDER_GLOSSARY_KEY, all);
        }
    }

    /**
     * The extraction prompts of the user, in the order they keep them; never null. An entry that cannot be read is left out.
     */
    public static List<ExtractionPrompt> readPrompts(JsonObject attributes) {
        List<ExtractionPrompt> prompts = new ArrayList<>();
        if (attributes == null || !attributes.has(PROMPTS_KEY) || !attributes.get(PROMPTS_KEY).isJsonArray()) {
            return prompts;
        }
        for (JsonElement element : attributes.getAsJsonArray(PROMPTS_KEY)) {
            try {
                ExtractionPrompt prompt = GSON.fromJson(element, ExtractionPrompt.class);
                if (prompt != null && prompt.getId() != null && !prompt.getId().isBlank()) {
                    prompts.add(prompt);
                }
            } catch (JsonParseException e) {
                // a prompt that is not readable must not take the library with it
            }
        }
        return prompts;
    }

    public static void writePrompts(JsonObject attributes, List<ExtractionPrompt> prompts) {
        if (prompts.isEmpty()) {
            attributes.remove(PROMPTS_KEY);
            return;
        }
        JsonArray array = new JsonArray();
        prompts.forEach(prompt -> array.add(GSON.toJsonTree(prompt)));
        attributes.add(PROMPTS_KEY, array);
    }

    /**
     * An independent copy with the same identity: what an editor works on until the user saves.
     */
    public static RealLanguage copy(RealLanguage language) {
        return GSON.fromJson(GSON.toJsonTree(language), RealLanguage.class);
    }
}
