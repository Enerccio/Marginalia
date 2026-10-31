package com.github.enerccio.marginalia.extensions.betareaders.service;

import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersSettings;
import com.github.enerccio.marginalia.extensions.betareaders.model.BookData;
import com.github.enerccio.marginalia.extensions.betareaders.model.Persona;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads and writes the data of the plugin in the attributes of the user settings and of a book, and works out the personas a book
 * can use. It touches no service, so it is the part of the storage that has unit tests; {@link BetaReadersService} stores what it
 * produces. The writers replace the whole object of the plugin, the caller saves the entity.
 */
public final class BetaReadersStore {

    private static final Gson GSON = new Gson();

    private BetaReadersStore() {
    }

    /**
     * Where a persona that a book can use comes from.
     */
    public enum Scope {
        /** Made in the book. */
        BOOK,
        /** In the library of the user. */
        LIBRARY
    }

    /**
     * A persona a book can use.
     */
    public record Available(Persona persona, Scope scope) {
    }

    /**
     * The settings stored in the attributes, null when there are none (the user never opened the plugin).
     */
    public static BetaReadersSettings readSettings(JsonObject attributes) {
        JsonElement stored = stored(attributes, BetaReadersSettings.KEY);
        if (stored == null) {
            return null;
        }
        BetaReadersSettings settings = GSON.fromJson(stored, BetaReadersSettings.class);
        return settings == null ? null : settings.normalize();
    }

    public static void writeSettings(JsonObject attributes, BetaReadersSettings settings) {
        attributes.add(BetaReadersSettings.KEY, GSON.toJsonTree(settings));
    }

    /**
     * What the book stores, never null (an empty object for a book that has nothing).
     */
    public static BookData readBook(JsonObject attributes) {
        JsonElement stored = stored(attributes, BookData.KEY);
        BookData data = stored == null ? null : GSON.fromJson(stored, BookData.class);
        return data == null ? new BookData() : data.normalize();
    }

    public static void writeBook(JsonObject attributes, BookData data) {
        attributes.add(BookData.KEY, GSON.toJsonTree(data));
    }

    private static JsonElement stored(JsonObject attributes, String key) {
        if (attributes == null || !attributes.has(key) || attributes.get(key).isJsonNull()) {
            return null;
        }
        return attributes.get(key);
    }

    /**
     * The personas a book can use: its own first, then the ones of the library whose name no persona of the book has (the book wins
     * when the names are the same, ignoring case and spaces around it). A persona without a name is left out.
     *
     * @param settings the settings of the user, null for none
     * @param book     what the book stores, null for nothing
     */
    public static List<Available> available(BetaReadersSettings settings, BookData book) {
        List<Available> available = new ArrayList<>();
        List<String> taken = new ArrayList<>();
        if (book != null) {
            for (Persona persona : book.getPersonas()) {
                String key = nameKey(persona.getName());
                if (!key.isEmpty()) {
                    available.add(new Available(persona, Scope.BOOK));
                    taken.add(key);
                }
            }
        }
        if (settings != null) {
            for (Persona persona : settings.getPersonas()) {
                String key = nameKey(persona.getName());
                if (!key.isEmpty() && !taken.contains(key)) {
                    available.add(new Available(persona, Scope.LIBRARY));
                }
            }
        }
        return available;
    }

    /**
     * What two names are compared by: the text without the spaces around it, in lower case.
     */
    public static String nameKey(String name) {
        return name == null ? "" : name.strip().toLowerCase(Locale.ROOT);
    }
}
