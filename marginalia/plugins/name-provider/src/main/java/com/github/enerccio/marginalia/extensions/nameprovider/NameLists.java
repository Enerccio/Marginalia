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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The lists of real names of the categories, read from {@code name-lists.json} (written by the build step
 * {@code src/build/ImportPopularNames.java}), and the random choice of names from them.
 */
public class NameLists {

    static final String RESOURCE = "name-lists.json";
    static final String GROUPS_RESOURCE = "name-groups.json";

    private final Map<String, List<NameEntry>> categories;
    /** The groups: the title and the lists each pick of it is made from, one chosen at random for every name. */
    private final Map<String, List<String>> groups;

    NameLists(Map<String, List<NameEntry>> categories) {
        this(categories, Map.of());
    }

    /**
     * @param groups the title of every group and the patterns of its members, see {@link #resolve}; a group with no member
     *               or with the title of a list is left out
     */
    NameLists(Map<String, List<NameEntry>> categories, Map<String, List<String>> groups) {
        Map<String, List<NameEntry>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.putAll(categories);
        this.categories = map;
        Map<String, List<String>> resolved = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        groups.forEach((title, patterns) -> {
            List<String> members = resolve(new ArrayList<>(map.keySet()), patterns);
            if (!members.isEmpty() && !map.containsKey(title)) {
                resolved.put(title, members);
            }
        });
        this.groups = resolved;
    }

    /** Reads the lists and the groups from the extension's own resources; no categories when there are none. */
    public static NameLists load() {
        try (InputStream in = NameLists.class.getClassLoader().getResourceAsStream(RESOURCE);
             InputStream groupsIn = NameLists.class.getClassLoader().getResourceAsStream(GROUPS_RESOURCE)) {
            if (in == null) {
                return new NameLists(Map.of());
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                NameLists parsed = parse(reader);
                if (groupsIn == null) {
                    return parsed;
                }
                try (Reader groupsReader = new InputStreamReader(groupsIn, StandardCharsets.UTF_8)) {
                    return new NameLists(parsed.categories, parseGroups(groupsReader));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + RESOURCE, e);
        }
    }

    /**
     * The groups of {@code name-groups.json}, {@code {"groups": [{"title": "Nordic - Surnames", "members": ["Norway -
     * Surnames", "Denmark - *"]}]}}: the title and the patterns of the members.
     */
    static Map<String, List<String>> parseGroups(Reader reader) {
        JsonObject root = new Gson().fromJson(reader, JsonObject.class);
        Map<String, List<String>> groups = new LinkedHashMap<>();
        root.getAsJsonArray("groups").forEach(element -> {
            JsonObject group = element.getAsJsonObject();
            List<String> members = new ArrayList<>();
            group.getAsJsonArray("members").forEach(member -> members.add(member.getAsString()));
            groups.put(group.get("title").getAsString(), members);
        });
        return groups;
    }

    /**
     * The titles that match any of the patterns, in the order of the titles, each once. A pattern is a whole title (not case
     * sensitive) where {@code *} stands for any text, so one without {@code *} names one list and
     * {@code "Denmark - *"} every list of Denmark.
     */
    static List<String> resolve(List<String> titles, List<String> patterns) {
        List<java.util.regex.Pattern> compiled = patterns.stream()
                .map(pattern -> java.util.regex.Pattern.compile(
                        Arrays.stream(pattern.strip().split("\\*", -1)).map(java.util.regex.Pattern::quote).collect(Collectors.joining(".*")),
                        java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE | java.util.regex.Pattern.DOTALL))
                .toList();
        return titles.stream().filter(title -> compiled.stream().anyMatch(p -> p.matcher(title).matches())).toList();
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

    /** The titles of the categories and of the groups, sorted. */
    public List<String> categories() {
        return Stream.concat(categories.keySet().stream(), groups.keySet().stream()).sorted().toList();
    }

    /** True when the title is a group, which makes every pick come from a list chosen at random among its members. */
    public boolean isGroup(String category) {
        return category != null && groups.containsKey(category.strip());
    }

    /** The titles of the lists a group is made of, sorted; none for anything that is not a group. */
    public List<String> members(String group) {
        List<String> members = group == null ? null : groups.get(group.strip());
        return members == null ? List.of() : members.stream().sorted().toList();
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

    /** How many different names the category or group has, 0 when there is no such category. */
    public int size(String category) {
        return pool(category).stream().flatMap(List::stream).collect(Collectors.toSet()).size();
    }

    /** The lists a category or a group is picked from: the one list, or every member of the group; none when unknown. */
    private List<List<NameEntry>> pool(String category) {
        String key = category == null ? null : category.strip();
        if (key == null) {
            return List.of();
        }
        if (groups.containsKey(key)) {
            return groups.get(key).stream().map(categories::get).toList();
        }
        List<NameEntry> names = categories.get(key);
        return names == null ? List.of() : List.of(names);
    }

    /**
     * Up to {@code count} different names of the category chosen at random (all of them, shuffled, when it has fewer).
     * For a group every name is picked from a member chosen at random (each member that still has names with the same
     * chance), so the names mix the lists without any of them being read beyond its length.
     *
     * @throws IllegalArgumentException when there is no such category
     */
    public List<NameEntry> pick(String category, int count, RandomGenerator random) {
        List<List<NameEntry>> pool = pool(category);
        if (pool.isEmpty()) {
            throw new IllegalArgumentException("Unknown category " + category);
        }
        List<Deque<NameEntry>> remaining = new ArrayList<>();
        for (List<NameEntry> names : pool) {
            List<NameEntry> shuffled = new ArrayList<>(names);
            Collections.shuffle(shuffled, random);
            remaining.add(new ArrayDeque<>(shuffled));
        }
        Set<NameEntry> picked = new LinkedHashSet<>();
        while (picked.size() < Math.max(count, 0)) {
            remaining.removeIf(Deque::isEmpty);
            if (remaining.isEmpty()) {
                break;
            }
            picked.add(remaining.get(remaining.size() == 1 ? 0 : random.nextInt(remaining.size())).poll());
        }
        return List.copyOf(picked);
    }

}
