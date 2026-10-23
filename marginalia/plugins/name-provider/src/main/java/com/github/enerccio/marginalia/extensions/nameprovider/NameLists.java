package com.github.enerccio.marginalia.extensions.nameprovider;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.random.RandomGenerator;

/**
 * The lists of real names of the categories, read from {@code name-lists.json} (written by the build step
 * {@code src/build/ImportPopularNames.java}), and the random choice of names from them.
 */
public class NameLists {

    static final String RESOURCE = "name-lists.json";

    private final Map<String, List<NameEntry>> categories;

    NameLists(Map<String, List<NameEntry>> categories) {
        Map<String, List<NameEntry>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.putAll(categories);
        this.categories = map;
    }

    /** Reads the lists from the extension's own resource; no categories when there are none. */
    public static NameLists load() {
        try (InputStream in = NameLists.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (in == null) {
                return new NameLists(Map.of());
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return parse(reader);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + RESOURCE, e);
        }
    }

    static NameLists parse(Reader reader) {
        JsonObject root = new Gson().fromJson(reader, JsonObject.class);
        Map<String, List<NameEntry>> categories = new LinkedHashMap<>();
        root.getAsJsonArray("categories").forEach(element -> {
            JsonObject category = element.getAsJsonObject();
            List<NameEntry> names = new ArrayList<>();
            category.getAsJsonArray("names").forEach(name -> {
                JsonArray pair = name.getAsJsonArray();
                names.add(new NameEntry(pair.get(0).getAsString(), pair.get(1).getAsString()));
            });
            categories.put(category.get("title").getAsString(), List.copyOf(names));
        });
        return new NameLists(categories);
    }

    /** The titles of the categories, sorted. */
    public List<String> categories() {
        return categories.keySet().stream().sorted().toList();
    }

    /**
     * The titles in which every word of the filter starts a word (ignoring the case; the words of the filter are separated
     * by spaces), in the given order; all of them for a blank filter. So "chad male" finds "Africa - Chad - Male" but not
     * "Africa - Chad - Female".
     */
    static List<String> filter(List<String> titles, String filter) {
        if (filter == null || filter.isBlank()) {
            return List.copyOf(titles);
        }
        List<java.util.regex.Pattern> words = Arrays.stream(filter.strip().split("\\s+"))
                .map(word -> java.util.regex.Pattern.compile("(?<![\\p{L}\\p{N}])" + java.util.regex.Pattern.quote(word),
                        java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE))
                .toList();
        return titles.stream().filter(title -> words.stream().allMatch(word -> word.matcher(title).find())).toList();
    }

    /** The titles of the categories that contain every word of the filter, see {@link #filter}. */
    public List<String> categories(String filter) {
        return filter(categories(), filter);
    }

    public boolean has(String category) {
        return category != null && categories.containsKey(category.strip());
    }

    /** How many names the category has, 0 when there is no such category. */
    public int size(String category) {
        List<NameEntry> names = category == null ? null : categories.get(category.strip());
        return names == null ? 0 : names.size();
    }

    /**
     * Up to {@code count} different names of the category chosen at random (all of them, shuffled, when it has fewer).
     *
     * @throws IllegalArgumentException when there is no such category
     */
    public List<NameEntry> pick(String category, int count, RandomGenerator random) {
        List<NameEntry> names = category == null ? null : categories.get(category.strip());
        if (names == null) {
            throw new IllegalArgumentException("Unknown category " + category);
        }
        List<NameEntry> shuffled = new ArrayList<>(names);
        Collections.shuffle(shuffled, random);
        return List.copyOf(shuffled.subList(0, Math.min(Math.max(count, 0), shuffled.size())));
    }

}
