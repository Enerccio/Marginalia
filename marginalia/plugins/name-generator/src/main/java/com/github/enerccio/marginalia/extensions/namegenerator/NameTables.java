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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Markov chain probability tables of the categories, read from {@code name-tables.json} (written by the build
 * step {@code src/build/GenerateTables.java}), and the generation of names from them.
 */
public class NameTables {

    static final String RESOURCE = "name-tables.json";
    static final String GROUPS_RESOURCE = "name-groups.json";
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
     * One category: the probabilities of the next character after the last {@code order} ones, the lengths of the
     * names that are usual in it (null: the defaults), which the build step took from the names the chain was trained on, and
     * the number of those names ({@code size}, null when unknown), which weights the members of a group.
     */
    record Category(String title, int order, Integer minLength, Integer maxLength, Integer size,
                    Map<String, Map<Character, Double>> tables) {}

    /**
     * A group as it is in {@code name-groups.json}.
     *
     * @param members the patterns of the members, see {@link #resolve}
     * @param bySize  true: a member is chosen with a chance proportional to the names it was trained on (a list of 600 names
     *                is chosen 60 times as often as one of 10), false: every member has the same chance
     */
    record Group(List<String> members, boolean bySize) {}

    private record ResolvedGroup(List<Category> members, boolean bySize) {}

    private final Map<String, Category> categories;
    /** The groups: the title and the categories each name of it is made from, one picked at random for every name. */
    private final Map<String, ResolvedGroup> groups;

    NameTables(Collection<Category> categories) {
        this(categories, Map.of());
    }

    /**
     * @param groups the groups by title; a group with no member or with the title of a category is left out
     */
    NameTables(Collection<Category> categories, Map<String, Group> groups) {
        Map<String, Category> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        categories.forEach(c -> map.put(c.title(), c));
        this.categories = map;
        Map<String, ResolvedGroup> resolved = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        groups.forEach((title, group) -> {
            List<Category> members = resolve(new ArrayList<>(map.keySet()), group.members()).stream().map(map::get).toList();
            if (!members.isEmpty() && !map.containsKey(title)) {
                resolved.put(title, new ResolvedGroup(members, group.bySize()));
            }
        });
        this.groups = resolved;
    }

    /** Reads the tables and the groups from the extension's own resources; no categories when there are none. */
    public static NameTables load() {
        try (InputStream in = NameTables.class.getClassLoader().getResourceAsStream(RESOURCE);
             InputStream groupsIn = NameTables.class.getClassLoader().getResourceAsStream(GROUPS_RESOURCE)) {
            if (in == null) {
                return new NameTables(List.of());
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                NameTables parsed = parse(reader);
                if (groupsIn == null) {
                    return parsed;
                }
                try (Reader groupsReader = new InputStreamReader(groupsIn, StandardCharsets.UTF_8)) {
                    return new NameTables(parsed.categories.values(), parseGroups(groupsReader));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + RESOURCE, e);
        }
    }

    /**
     * The groups of {@code name-groups.json}, {@code {"groups": [{"title": "Native American - Male", "members": ["America -
     * Northern - Apache - Male", "America - Northern - * - Male"], "weight": "size"}]}}: the title, the patterns of the
     * members and optionally how they are chosen, {@code "equal"} (default) or {@code "size"}.
     */
    static Map<String, Group> parseGroups(Reader reader) {
        JsonObject root = new Gson().fromJson(reader, JsonObject.class);
        Map<String, Group> groups = new LinkedHashMap<>();
        root.getAsJsonArray("groups").forEach(element -> {
            JsonObject group = element.getAsJsonObject();
            List<String> members = new ArrayList<>();
            group.getAsJsonArray("members").forEach(member -> members.add(member.getAsString()));
            String weight = group.has("weight") ? group.get("weight").getAsString() : "equal";
            if (!weight.equals("equal") && !weight.equals("size")) {
                throw new IllegalStateException("Group " + group.get("title").getAsString() + ": weight has to be equal or size, not " + weight);
            }
            groups.put(group.get("title").getAsString(), new Group(members, weight.equals("size")));
        });
        return groups;
    }

    /**
     * The titles that match any of the patterns, in the order of the titles, each once. A pattern is a whole title (not case
     * sensitive) where {@code *} stands for any text, so one without {@code *} names one category and
     * {@code "America - Northern - * - Male"} every male category of the northern America.
     */
    static List<String> resolve(List<String> titles, List<String> patterns) {
        List<java.util.regex.Pattern> compiled = patterns.stream()
                .map(pattern -> java.util.regex.Pattern.compile(
                        Arrays.stream(pattern.strip().split("\\*", -1)).map(java.util.regex.Pattern::quote).collect(Collectors.joining(".*")),
                        java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE | java.util.regex.Pattern.DOTALL))
                .toList();
        return titles.stream().filter(title -> compiled.stream().anyMatch(p -> p.matcher(title).matches())).toList();
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
                    optionalInt(category, "minLength"), optionalInt(category, "maxLength"), optionalInt(category, "size"), tables));
        });
        return new NameTables(categories);
    }

    private static Integer optionalInt(JsonObject object, String key) {
        return object.has(key) ? object.get(key).getAsInt() : null;
    }

    /** The titles of the categories and of the groups, sorted. */
    public List<String> categories() {
        return Stream.concat(categories.values().stream().map(Category::title), groups.keySet().stream()).sorted().toList();
    }

    /** True when the title is a group, which makes every name from a category picked at random among its members. */
    public boolean isGroup(String category) {
        return category != null && groups.containsKey(category.strip());
    }

    /** The titles of the categories a group is made of, sorted; none for anything that is not a group. */
    public List<String> members(String group) {
        ResolvedGroup resolved = group == null ? null : groups.get(group.strip());
        return resolved == null ? List.of() : resolved.members().stream().map(Category::title).sorted().toList();
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

    /** The most categories an "unknown category" error lists. */
    static final int MAX_SUGGESTIONS = 30;

    /**
     * The titles that fit a wrongly written category best: the words of the query (split at anything that is not a letter
     * or digit, so "Chad - Male" gives "chad" and "male") are looked for at the start of the words of the titles, and
     * the titles with the most words found are kept (none when no word is found anywhere), sorted.
     */
    static List<String> search(List<String> titles, String query) {
        if (query == null) {
            return List.of();
        }
        List<java.util.regex.Pattern> words = Arrays.stream(query.strip().split("[^\\p{L}\\p{N}]+"))
                .filter(word -> !word.isEmpty())
                .map(word -> java.util.regex.Pattern.compile("(?<![\\p{L}\\p{N}])" + java.util.regex.Pattern.quote(word),
                        java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE))
                .toList();
        Map<String, Integer> scores = new LinkedHashMap<>();
        int best = 0;
        for (String title : titles) {
            int score = (int) words.stream().filter(word -> word.matcher(title).find()).count();
            scores.put(title, score);
            best = Math.max(best, score);
        }
        int top = best;
        return top == 0 ? List.of() : scores.entrySet().stream().filter(e -> e.getValue() == top).map(Map.Entry::getKey).sorted().toList();
    }

    /**
     * The error for a category that does not exist: it says so and lists the categories the wrong one would hit
     * ({@link #search}), so the model can retry at once.
     *
     * @param titles   the categories that can be used
     * @param listTool how the tool that lists the categories with a filter is called
     */
    static String unknown(String kind, String category, List<String> titles, String listTool) {
        List<String> hits = search(titles, category);
        String message = "Unknown " + kind + " '" + category + "'.";
        if (hits.isEmpty()) {
            return message + " None matches any word of it, call " + listTool + " with a filter to find one.";
        }
        String shown = String.join(", ", hits.subList(0, Math.min(hits.size(), MAX_SUGGESTIONS)));
        return message + " Closest: " + shown + (hits.size() > MAX_SUGGESTIONS
                ? " (and " + (hits.size() - MAX_SUGGESTIONS) + " more, call " + listTool + " with a filter to narrow it)" : "") + ".";
    }

    public boolean has(String category) {
        return category != null && (categories.containsKey(category.strip()) || groups.containsKey(category.strip()));
    }

    /**
     * Up to {@code count} different names of the category or group (fewer when the chain cannot make that many).
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
     * For a group every name is made by a member chosen at random (with the same chance, or by the size of the member when the group says so), which keeps its own usual lengths.
     */
    public List<String> generate(String category, int count, Integer minLength, Integer maxLength, RandomGenerator random) {
        String key = category == null ? null : category.strip();
        ResolvedGroup group = key == null ? null : groups.get(key);
        List<Category> chains = group != null ? group.members()
                : key != null && categories.containsKey(key) ? List.of(categories.get(key)) : null;
        if (chains == null) {
            throw new IllegalArgumentException("Unknown category " + category);
        }
        long[] cumulative = group != null && group.bySize() ? cumulativeSizes(chains) : null;
        int[][] bounds = chains.stream().map(chain -> bounds(chain, minLength, maxLength)).toArray(int[][]::new);
        Set<String> names = new LinkedHashSet<>();
        for (int attempt = 0; attempt < count * ATTEMPTS_PER_NAME && names.size() < count; attempt++) {
            int member = member(chains.size(), cumulative, random);
            String name = name(chains.get(member), bounds[member][0], bounds[member][1], random);
            if (name != null) {
                names.add(name);
            }
        }
        return List.copyOf(names);
    }

    /** The running totals of the sizes of the members (a member of unknown size counts as one name). */
    private static long[] cumulativeSizes(List<Category> chains) {
        long[] cumulative = new long[chains.size()];
        long total = 0;
        for (int i = 0; i < cumulative.length; i++) {
            total += Math.max(1, chains.get(i).size() == null ? 1 : chains.get(i).size());
            cumulative[i] = total;
        }
        return cumulative;
    }

    /** The index of the member that makes the next name: by the sizes when there are any, else at random with the same chance. */
    private static int member(int count, long[] cumulative, RandomGenerator random) {
        if (count == 1) {
            return 0;
        }
        if (cumulative == null) {
            return random.nextInt(count);
        }
        long roll = random.nextLong(cumulative[count - 1]);
        int member = 0;
        while (cumulative[member] <= roll) {
            member++;
        }
        return member;
    }

    /** The shortest and longest name of a category: the requested lengths, else its usual ones, else the defaults. */
    private static int[] bounds(Category chain, Integer minLength, Integer maxLength) {
        int min = clampLength(minLength, chain.minLength() != null ? chain.minLength() : MIN_LENGTH);
        int max = clampLength(maxLength, chain.maxLength() != null ? chain.maxLength() : MAX_LENGTH);
        return min > max ? new int[]{max, min} : new int[]{min, max};
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
