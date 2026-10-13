import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Build step of the name generator (run by exec-maven-plugin before {@code GenerateTables.java}, see the pom): clones
 * <a href="https://github.com/dariusk/corpora">Corpora</a> (CC0) and turns the lists of names in its {@code data} folder
 * into source files of the name generator, one per list, in {@code <output dir>}. Run as
 * {@code java ImportCorpora.java <repository url> <clone dir> <resources dir> <output dir>}, Gson on the classpath.
 * <p>
 * The files of Corpora have no common format, so a list is whatever looks like one:
 * <ul>
 * <li>an array of strings (the file itself, or a property of the object in it: {@code {"description": "...", "dogs": [...]}});</li>
 * <li>an array of objects, the names are the {@code name} (or {@code title}, {@code city}... see {@link #NAME_FIELDS}) of
 * each; objects without such a field are not names.</li>
 * </ul>
 * Everything else (nested objects, numbers, descriptions) is left out, and so are the files in {@link #SKIPPED}. A file that is not valid JSON ({@code humans/firstNames.json}
 * misses a comma) gives the lines that are one quoted string.
 * <ul>
 * <li>The category is the path of the file in {@code data} and its name, {@code animals/dog_names.json} is
 * {@code Animals - Dog Names}; a file with several lists adds the name of the property ({@code Geography - Oceans -
 * Seas}). A title already used by another source file in the resources dir is skipped, and so are lists with fewer
 * than {@value #MIN_NAMES} names.</li>
 * <li>Names that have no letter, are longer than {@value #MAX_LENGTH} characters, have more than {@value #MAX_WORDS}
 * words or are templates, links or sentences are dropped, the rest is cleaned and de-duplicated like in the other
 * importers. A list that has the same names as one imported before (Corpora repeats some) is skipped.</li>
 * </ul>
 */
public class ImportCorpora {

    static final String DATA = "data";
    static final int MIN_NAMES = 10;
    static final int MAX_LENGTH = 40;
    static final int MAX_WORDS = 5;
    /**
     * Folders and files (below {@code data}) that are not names for a story: words (stop words, verbs, clues), numbers,
     * texts (messages, test questions, categories of a streaming service), data about the real world that has no use
     * in fiction (banned games, terrorist groups, weapons, candidates, parties), and the lists of a year.
     */
    static final Set<String> SKIPPED = Set.of("words", "mathematics", "psychology", "instructions", "games/bannedGames",
            "games/dark_souls_iii_messages.json", "games/wrestling_moves.json", "music/xxl_freshman.json",
            "humans/2016_us_presidential_candidates.json", "societies_and_groups/designated_terrorist_groups",
            "technology/guns_n_rifles.json", "governments/uk_political_parties.json", "film-tv/netflix-categories.json",
            "film-tv/extended-netflix-categories.json", "film-tv/iab_categories.json", "foods/combine.json",
            "geography/venues.json", "geography/countries_with_capitals.json");
    /** The property of an object that is its name, the first one found. */
    static final List<String> NAME_FIELDS = List.of("name", "Name", "title", "city", "color", "character", "saint",
            "candidate_name", "neighborhood", "capital", "actor", "player", "member", "gender");
    static final Pattern QUOTED_LINE = Pattern.compile("^\\s*\"([^\"\\\\]+)\"\\s*,?\\s*$");

    public static void main(String[] args) throws IOException, InterruptedException {
        Path clone = Path.of(args[1]);
        Path resources = Path.of(args[2]);
        Path output = Path.of(args[3]);
        if (!ImportSupport.ensureClone(args[0], clone, DATA)) {
            return;
        }
        Path data = clone.resolve(DATA);

        Set<String> taken = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        if (Files.isDirectory(resources)) {
            try (Stream<Path> walk = Files.walk(resources)) {
                for (Path file : (Iterable<Path>) walk.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".json")
                        && !p.startsWith(output) && isNonEmpty(p))::iterator) {
                    try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                        JsonObject source = JsonParser.parseReader(in).getAsJsonObject();
                        if (source.has("title")) {
                            taken.add(source.get("title").getAsString().strip());
                        }
                    } catch (RuntimeException e) {
                        // not ours to judge, GenerateTables reports a malformed file
                    }
                }
            }
        }

        List<Path> files = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(data)) {
            walk.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".json")).sorted().forEach(files::add);
        }
        ImportSupport.clearJson(output);

        int written = 0;
        Set<Set<String>> sets = new HashSet<>();
        for (Path file : files) {
            Path relative = data.relativize(file);
            String path = relative.toString().replace('\\', '/');
            if (SKIPPED.stream().anyMatch(skipped -> path.equals(skipped) || path.startsWith(skipped + "/"))) {
                continue;
            }
            Map<String, List<String>> lists = lists(file);
            for (Map.Entry<String, List<String>> list : lists.entrySet()) {
                String title = title(relative, lists.size() > 1 ? list.getKey() : null);
                Set<String> names = new LinkedHashSet<>();
                for (String raw : list.getValue()) {
                    String name = ImportSupport.clean(raw);
                    if (isName(name)) {
                        names.add(name);
                    }
                }
                if (names.size() < MIN_NAMES) {
                    continue;
                }
                if (!sets.add(normalized(names))) {
                    // Corpora has the same list twice (a copy in an other file, a language of a dictionary...)
                    continue;
                }
                if (!taken.add(title)) {
                    System.out.println("name-generator: skipping " + title + ", the category exists already");
                    continue;
                }
                String id = "corpora-" + title.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
                ImportSupport.write(output, id + ".json", id, title, names);
                written++;
            }
        }
        System.out.println("name-generator: " + written + " categories imported from corpora into " + output);
    }

    /** Not a sentence, a link, a template or a censored text: {@code Mix {0} and {1}.}, {@code **** ahead}. */
    static boolean isName(String name) {
        if (!ImportSupport.usable(name) || name.length() > MAX_LENGTH || name.chars().noneMatch(Character::isLetter)
                || name.contains("://") || name.contains("{") || name.contains("*")) {
            return false;
        }
        int words = name.split("\\s+").length;
        return words <= MAX_WORDS && !(words >= 3 && name.endsWith("."));
    }

    static Set<String> normalized(Set<String> names) {
        Set<String> out = new HashSet<>();
        names.forEach(n -> out.add(n.toLowerCase(Locale.ROOT)));
        return out;
    }

    /** The lists of names of a file by the name of their property ({@code ""} for the file itself), in the order of the file. */
    static Map<String, List<String>> lists(Path file) throws IOException {
        Map<String, List<String>> lists = new LinkedHashMap<>();
        JsonElement root;
        try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            root = JsonParser.parseReader(in);
        } catch (RuntimeException e) {
            // not JSON: the lines with one quoted string
            List<String> lines = new ArrayList<>();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                Matcher m = QUOTED_LINE.matcher(line);
                if (m.matches()) {
                    lines.add(m.group(1));
                }
            }
            lists.put("", lines);
            return lists;
        }
        if (root.isJsonArray()) {
            lists.put("", names(root));
        } else if (root.isJsonObject()) {
            for (Map.Entry<String, JsonElement> property : root.getAsJsonObject().entrySet()) {
                if (property.getValue().isJsonArray()) {
                    List<String> names = names(property.getValue());
                    if (!names.isEmpty()) {
                        lists.put(property.getKey(), names);
                    }
                }
            }
        }
        lists.values().removeIf(List::isEmpty);
        return lists;
    }

    /** The strings of an array, or the names of its objects; nothing when it is neither. */
    static List<String> names(JsonElement array) {
        List<String> names = new ArrayList<>();
        for (JsonElement element : array.getAsJsonArray()) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                names.add(element.getAsString());
            } else if (element.isJsonObject()) {
                for (String field : NAME_FIELDS) {
                    JsonElement value = element.getAsJsonObject().get(field);
                    if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                        names.add(value.getAsString());
                        break;
                    }
                }
            }
        }
        return names;
    }

    /** {@code animals/dog_names.json} to {@code Animals - Dog Names}, with the property of a file that has several lists. */
    static String title(Path relative, String property) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < relative.getNameCount(); i++) {
            String part = relative.getName(i).toString().replaceFirst("\\.json$", "");
            parts.add(words(part));
        }
        if (property != null && !property.isEmpty()) {
            parts.add(words(property));
        }
        // "Fraternities - Fraternities": a part that repeats the one before is dropped
        List<String> unique = new ArrayList<>();
        for (String part : parts) {
            if (unique.isEmpty() || !unique.getLast().equalsIgnoreCase(part)) {
                unique.add(part);
            }
        }
        return String.join(" - ", unique);
    }

    /** {@code dog_names}, {@code dogNames}, {@code dog-names} to {@code Dog Names}. */
    static String words(String name) {
        StringBuilder out = new StringBuilder();
        for (String word : name.replaceAll("([a-z0-9])([A-Z])", "$1 $2").split("[_\\-\\s]+")) {
            if (word.isEmpty()) {
                continue;
            }
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    static boolean isNonEmpty(Path file) {
        try {
            return Files.size(file) > 0;
        } catch (IOException e) {
            return false;
        }
    }
}
