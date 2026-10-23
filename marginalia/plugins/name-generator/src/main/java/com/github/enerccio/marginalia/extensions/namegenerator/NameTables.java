package com.github.enerccio.marginalia.extensions.namegenerator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.random.RandomGenerator;

/**
 * The Markov chain probability tables of the categories, read from {@code name-tables.json} (written by the build
 * step {@code src/build/GenerateTables.java}), and the generation of names from them.
 */
public class NameTables {

    static final String RESOURCE = "name-tables.json";
    static final char START = '^';
    static final char END = '$';
    /** The default length of a name, in letters (the space and the hyphen count). */
    public static final int MIN_LENGTH = 3;
    public static final int MAX_LENGTH = 20;
    /** The limits the model can ask for, whatever it sends. */
    public static final int FLOOR_LENGTH = 2;
    public static final int CEIL_LENGTH = 30;
    /** Attempts per wanted name before giving up: a small category runs out of different names. */
    static final int ATTEMPTS_PER_NAME = 100;

    /**
     * One category: the probabilities of the next character after the last {@code order} ones, and the lengths of the
     * names that are usual in it (null: the defaults), which the build step took from the names the chain was trained on.
     */
    record Category(String title, int order, Integer minLength, Integer maxLength, Map<String, Map<Character, Double>> tables) {}

    private final Map<String, Category> categories;

    NameTables(Collection<Category> categories) {
        Map<String, Category> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        categories.forEach(c -> map.put(c.title(), c));
        this.categories = map;
    }

    /** Reads the tables from the extension's own resource; no categories when there are none. */
    public static NameTables load() {
        try (InputStream in = NameTables.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (in == null) {
                return new NameTables(List.of());
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return parse(reader);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + RESOURCE, e);
        }
    }

    static NameTables parse(Reader reader) {
        JsonObject root = new Gson().fromJson(reader, JsonObject.class);
        List<Category> categories = new ArrayList<>();
        root.getAsJsonArray("categories").forEach(element -> {
            JsonObject category = element.getAsJsonObject();
            Map<String, Map<Character, Double>> tables = new HashMap<>();
            category.getAsJsonObject("tables").entrySet().forEach(context -> {
                Map<Character, Double> next = new LinkedHashMap<>();
                context.getValue().getAsJsonObject().entrySet()
                        .forEach(p -> next.put(p.getKey().charAt(0), p.getValue().getAsDouble()));
                tables.put(context.getKey(), next);
            });
            categories.add(new Category(category.get("title").getAsString(), category.get("order").getAsInt(),
                    optionalInt(category, "minLength"), optionalInt(category, "maxLength"), tables));
        });
        return new NameTables(categories);
    }

    private static Integer optionalInt(JsonObject object, String key) {
        return object.has(key) ? object.get(key).getAsInt() : null;
    }

    /** The titles of the categories, sorted. */
    public List<String> categories() {
        return categories.values().stream().map(Category::title).sorted().toList();
    }

    /**
     * The categories in whose title every word of the filter starts a word, see {@link #filter}, in the order of
     * {@link #categories()}.
     */
    public List<String> categories(String filter) {
        return filter(categories(), filter);
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

    public boolean has(String category) {
        return category != null && categories.containsKey(category.strip());
    }

    /**
     * Up to {@code count} different names of the category (fewer when the chain cannot make that many).
     *
     * @throws IllegalArgumentException when there is no such category
     */
    public List<String> generate(String category, int count, RandomGenerator random) {
        return generate(category, count, null, null, random);
    }

    /**
     * Like {@link #generate(String, int, RandomGenerator)} with the length of the names limited (null for the
     * defaults: the usual lengths of the category, else {@link #MIN_LENGTH} and {@link #MAX_LENGTH}). The limits are
     * clamped to {@link #FLOOR_LENGTH}..{@link #CEIL_LENGTH}, swapped when they are the wrong way round. A chain may not be able to make names of a length at all, then there are fewer.
     */
    public List<String> generate(String category, int count, Integer minLength, Integer maxLength, RandomGenerator random) {
        Category chain = category == null ? null : categories.get(category.strip());
        if (chain == null) {
            throw new IllegalArgumentException("Unknown category " + category);
        }
        int min = clampLength(minLength, chain.minLength() != null ? chain.minLength() : MIN_LENGTH);
        int max = clampLength(maxLength, chain.maxLength() != null ? chain.maxLength() : MAX_LENGTH);
        if (min > max) {
            int swap = min;
            min = max;
            max = swap;
        }
        Set<String> names = new LinkedHashSet<>();
        for (int attempt = 0; attempt < count * ATTEMPTS_PER_NAME && names.size() < count; attempt++) {
            String name = name(chain, min, max, random);
            if (name != null) {
                names.add(name);
            }
        }
        return List.copyOf(names);
    }

    private static String name(Category chain, int min, int max, RandomGenerator random) {
        StringBuilder name = new StringBuilder();
        String context = String.valueOf(START).repeat(chain.order());
        while (name.length() <= max) {
            Map<Character, Double> next = chain.tables().get(context);
            if (next == null) {
                return null;
            }
            char c = pick(next, random);
            if (c == END) {
                return name.length() < min ? null : capitalize(name.toString());
            }
            name.append(c);
            context = (context + c).substring(1);
        }
        return null;
    }

    private static int clampLength(Integer requested, int fallback) {
        return requested == null ? fallback : Math.max(FLOOR_LENGTH, Math.min(CEIL_LENGTH, requested));
    }

    /** Roulette selection; the probabilities are rounded in the file, so the last one takes the remainder. */
    private static char pick(Map<Character, Double> probabilities, RandomGenerator random) {
        double roll = random.nextDouble() * probabilities.values().stream().mapToDouble(Double::doubleValue).sum();
        char last = END;
        for (Map.Entry<Character, Double> entry : probabilities.entrySet()) {
            last = entry.getKey();
            roll -= entry.getValue();
            if (roll < 0) {
                return last;
            }
        }
        return last;
    }

    /** Upper case for the first letter and the ones after a space, hyphen or apostrophe. */
    static String capitalize(String name) {
        StringBuilder out = new StringBuilder(name.length());
        boolean upper = true;
        for (char c : name.toCharArray()) {
            out.append(upper ? Character.toUpperCase(c) : c);
            upper = c == ' ' || c == '-' || c == '\'';
        }
        return out.toString();
    }

}
