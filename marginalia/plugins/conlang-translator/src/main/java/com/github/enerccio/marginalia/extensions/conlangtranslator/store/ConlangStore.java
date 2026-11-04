package com.github.enerccio.marginalia.extensions.conlangtranslator.store;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;

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
