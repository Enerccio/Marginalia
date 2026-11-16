package com.github.enerccio.marginalia.extensions.statetracker.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The user's categories, the catalog books copy from: a JSON array in the attributes of the user settings under {@link #KEY}.
 * A user who never saved theirs has the built-in ones, which have fixed ids so that the books that copied them stay linked
 * after the user saves the catalog for the first time.
 */
public final class CategoryCatalog {

    public static final String KEY = StateBook.KEY + ".categories";

    private static final Gson GSON = new Gson();

    private CategoryCatalog() {
    }

    /**
     * The categories stored in {@code attributes}, or {@code defaults} when none were ever stored (an empty list that was stored
     * stays empty: the user deleted them all).
     */
    public static List<Category> read(JsonObject attributes, Supplier<List<Category>> defaults) {
        if (attributes == null || !attributes.has(KEY) || !attributes.get(KEY).isJsonArray()) {
            return new ArrayList<>(defaults.get());
        }
        List<Category> categories = new ArrayList<>();
        for (JsonElement element : attributes.getAsJsonArray(KEY)) {
            Category category = GSON.fromJson(element, Category.class);
            if (category != null && category.getId() != null) {
                categories.add(category);
            }
        }
        return categories;
    }

    /** Stores the categories into {@code attributes}. */
    public static void write(JsonObject attributes, List<Category> categories) {
        JsonArray array = new JsonArray();
        for (Category category : categories) {
            array.add(GSON.toJsonTree(category));
        }
        attributes.add(KEY, array);
    }

    /** Whether the user has never stored a catalog (the built-in categories are shown). */
    public static boolean isDefault(JsonObject attributes) {
        return attributes == null || !attributes.has(KEY);
    }

    /** Removes the stored catalog: the built-in categories apply again. */
    public static void reset(JsonObject attributes) {
        if (attributes != null) {
            attributes.remove(KEY);
        }
    }

    public static Category find(List<Category> catalog, String id) {
        return catalog.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }
}
