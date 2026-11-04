package com.github.enerccio.marginalia.extensions.conlangtranslator.store;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.BookSettings;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.UserDefaults;
import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Reads and writes the library of languages in the attributes of the user's settings, and exports and imports a language as a document. It
 * touches no service (the caller saves the entity), so it is the part of the storage that has unit tests.
 * <p>
 * The library is a part of the user's data, so the database backup carries it; the backup of a book does not (a book only names a language).
 * A language is edited as a whole: the writers replace the stored language with the same id.
 */
public final class ConlangStore {

    /** The key in the attributes of the user settings. */
    public static final String LIBRARY_KEY = "com.github.enerccio.marginalia.extensions.conlangtranslator.languages";

    private static final Gson GSON = new Gson();
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private ConlangStore() {
    }

    /**
     * The languages of the library, in the order the user keeps them; never null. An entry that cannot be read is left out.
     */
    public static List<Language> readLibrary(JsonObject attributes) {
        List<Language> languages = new ArrayList<>();
        if (attributes == null || !attributes.has(LIBRARY_KEY) || !attributes.get(LIBRARY_KEY).isJsonArray()) {
            return languages;
        }
        for (JsonElement element : attributes.getAsJsonArray(LIBRARY_KEY)) {
            try {
                Language language = GSON.fromJson(element, Language.class);
                if (language != null && language.getId() != null) {
                    languages.add(language);
                }
            } catch (JsonParseException e) {
                // a language that is not readable must not take the library with it
            }
        }
        return languages;
    }

    public static void writeLibrary(JsonObject attributes, List<Language> languages) {
        JsonArray array = new JsonArray();
        languages.forEach(language -> array.add(GSON.toJsonTree(language)));
        attributes.add(LIBRARY_KEY, array);
    }

    /**
     * The language with the id, or null (a book may name a language that was deleted).
     */
    public static Language find(JsonObject attributes, String id) {
        return id == null ? null : readLibrary(attributes).stream().filter(language -> id.equals(language.getId())).findFirst().orElse(null);
    }

    /**
     * Stores the language: replaces the one with the same id (keeping its place), else adds it at the end.
     */
    public static void save(JsonObject attributes, Language language) {
        List<Language> languages = readLibrary(attributes);
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
        List<Language> languages = readLibrary(attributes);
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
     * An independent copy with the same identity: what an editor works on until the user saves.
     */
    public static Language copy(Language language) {
        return GSON.fromJson(GSON.toJsonTree(language), Language.class);
    }

    /**
     * The library the settings screen stores. A run of the translator adds words to the stored library while the screen is open, so the
     * screen must not write back what it loaded: a language it did not touch is taken from the library as it is now.
     *
     * @param stored the library as it is now
     * @param shown  the languages the screen shows, in its order, with its edits
     * @param loaded the ids of the library when the screen loaded it
     * @param edited the ids of the languages the user changed on the screen
     * @return the shown order; a language the user changed or added is the screen's, any other the stored one; a language that was
     * loaded and is not shown any more is gone; a language that was untouched and is not stored any more is gone (another session deleted it); a language that is stored and was not loaded (another session added it) is kept at the end
     */
    public static List<Language> merge(List<Language> stored, List<Language> shown, Set<String> loaded, Set<String> edited) {
        List<Language> result = new ArrayList<>();
        for (Language language : shown) {
            Language current = stored.stream().filter(other -> other.getId().equals(language.getId())).findFirst().orElse(null);
            boolean touched = edited.contains(language.getId()) || !loaded.contains(language.getId());
            if (touched) {
                result.add(language);
            } else if (current != null) {
                result.add(current);
            }
            // else: untouched on the screen and not stored any more, another session deleted it
        }
        for (Language language : stored) {
            boolean known = loaded.contains(language.getId()) || shown.stream().anyMatch(other -> other.getId().equals(language.getId()));
            if (!known) {
                result.add(language);
            }
        }
        return result;
    }

    /**
     * The document a language is shared as (JSON, without the links to the example book and lorebook).
     */
    public static String export(Language language) {
        return PRETTY.toJson(language.forExport());
    }

    /**
     * The language of a document, ready to be added to the library (its own id, no links).
     *
     * @throws IllegalArgumentException when the text is not a language, or of a newer format
     */
    public static Language importDocument(String document) {
        Language language;
        try {
            language = GSON.fromJson(document, Language.class);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Not a language: " + e.getMessage(), e);
        }
        if (language == null || language.getName().isBlank() && language.getLexicon().isEmpty() && language.getRules().isEmpty()) {
            throw new IllegalArgumentException("Not a language");
        }
        return language.forImport();
    }
}
