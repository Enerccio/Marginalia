import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Build step of the name generator (run by exec-maven-plugin, see the pom): reads every {@code *.json} of the
 * resources folder and its subfolders and writes one file with the Markov chain probability tables of all categories. Run as
 * {@code java GenerateTables.java <resources dir> <output file> [chains file]}.
 * <p>
 * A source file is {@code {"_id": "demons", "title": "Demons", "values": ["azazel", "baal", ...]}}: the title is the category, the values
 * (names) train its chain. The output has only the title and the probabilities, never the names:
 * <pre>
 * {"categories": [{"title": "Demons", "order": 2, "minLength": 4, "maxLength": 9, "tables": {"^^": {"a": 0.25, ...}, "^a": {...}, "az": {"$": 0.5, ...}}}]}
 * </pre>
 * A key is the last {@code order} characters (padded with {@code ^} at the start), its value the probabilities of the
 * next character, {@code $} being the end of the name. Names are lowercased for the training.
 * <p>
 * The length of the chain ({@code order}) and the usual lengths of the names ({@code minLength}, {@code maxLength}, the
 * defaults of {@code generate_names}) of a category come from the descriptor {@code src/build/chains.json} (committed,
 * written by {@code TuneChains.java}) by the title; a category without an entry gets the order {@value #ORDER} and no lengths.
 * <p>
 * Without source files (they are not in the repository) the output is left as it is.
 */
public class GenerateTables {

    static final int ORDER = 2;
    static final char START = '^';
    static final char END = '$';
    static final int DECIMALS = 4;

    public static void main(String[] args) throws IOException {
        Path sources = Path.of(args[0]);
        Path output = Path.of(args[1]);
        JsonObject chains = args.length > 2 ? chains(Path.of(args[2])) : new JsonObject();
        int defaultOrder = chains.has("defaultOrder") ? chains.get("defaultOrder").getAsInt() : ORDER;
        JsonObject tuned = chains.has("categories") ? chains.getAsJsonObject("categories") : new JsonObject();
        Set<String> tunedUsed = new HashSet<>();

        List<Path> files = new ArrayList<>();
        if (Files.isDirectory(sources)) {
            // the whole folder tree: the scraper writes its categories into a folder of their own
            try (Stream<Path> list = Files.walk(sources)) {
                list.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".json")).sorted().forEach(files::add);
            }
        }

        JsonArray categories = new JsonArray();
        int trained = 0;
        Map<String, Path> seen = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Path file : files) {
            if (Files.size(file) == 0) {
                System.out.println("name-generator: skipping empty " + sources.relativize(file));
                continue;
            }
            JsonObject source;
            try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                source = JsonParser.parseReader(in).getAsJsonObject();
            } catch (RuntimeException e) {
                throw new IllegalStateException(sources.relativize(file) + ": not a JSON object: " + e.getMessage(), e);
            }
            String title = text(source, "title", file);
            Path previous = seen.put(title, file);
            if (previous != null) {
                throw new IllegalStateException("Category " + title + " is in " + previous.getFileName() + " and "
                        + sources.relativize(file));
            }
            List<String> names = new ArrayList<>();
            if (!source.has("values") || !source.get("values").isJsonArray()) {
                throw new IllegalStateException(sources.relativize(file) + ": 'values' has to be an array of strings");
            }
            for (JsonElement name : source.getAsJsonArray("values")) {
                names.add(name.getAsString());
            }
            JsonObject setting = tuned.has(title) ? tuned.getAsJsonObject(title) : new JsonObject();
            if (tuned.has(title)) {
                tunedUsed.add(title);
            }
            categories.add(category(title, names, file, setting.has("order") ? setting.get("order").getAsInt() : defaultOrder,
                    setting.has("minLength") ? setting.get("minLength").getAsInt() : null,
                    setting.has("maxLength") ? setting.get("maxLength").getAsInt() : null));
            trained += names.size();
        }

        if (categories.isEmpty()) {
            System.out.println("name-generator: no source names in " + sources + ", " + output + " left as it is");
            return;
        }
        System.out.println("name-generator: " + categories.size() + " categories from " + trained + " names, "
                + tunedUsed.size() + " with their own chain settings");
        if (tunedUsed.size() < tuned.size()) {
            System.out.println("name-generator: chain settings of " + (tuned.size() - tunedUsed.size())
                    + " categories without a source (outdated chains.json, run the tune-chains step)");
        }
        JsonObject root = new JsonObject();
        root.add("categories", categories);
        Gson gson = new GsonBuilder().create();
        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, gson.toJson(root) + "\n", StandardCharsets.UTF_8);
    }

    /** The settings per category (order, minLength, maxLength) written by {@code TuneChains.java}; none without the file. */
    static JsonObject chains(Path file) throws IOException {
        if (!Files.isRegularFile(file)) {
            return new JsonObject();
        }
        try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(in).getAsJsonObject();
        }
    }

    static String text(JsonObject object, String key, Path file) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive() || object.get(key).getAsString().isBlank()) {
            throw new IllegalStateException(file.getFileName() + ": '" + key + "' is missing");
        }
        return object.get(key).getAsString().strip();
    }

    /** @param order the length of the chain; {@code minLength} and {@code maxLength} the usual lengths of the names, null for none */
    static JsonObject category(String title, List<String> names, Path file, int order, Integer minLength, Integer maxLength) {
        Map<String, Map<Character, Integer>> counts = new TreeMap<>();
        int trained = 0;
        for (String raw : names) {
            String name = raw.strip().toLowerCase(Locale.ROOT);
            if (name.isEmpty()) {
                continue;
            }
            if (name.indexOf(START) >= 0 || name.indexOf(END) >= 0) {
                throw new IllegalStateException(file.getFileName() + ": '" + raw + "' contains " + START + " or " + END);
            }
            String padded = String.valueOf(START).repeat(order) + name + END;
            for (int i = order; i < padded.length(); i++) {
                counts.computeIfAbsent(padded.substring(i - order, i), k -> new TreeMap<>())
                        .merge(padded.charAt(i), 1, Integer::sum);
            }
            trained++;
        }
        if (trained == 0) {
            throw new IllegalStateException(file.getFileName() + ": no names");
        }
        JsonObject tables = new JsonObject();
        for (Map.Entry<String, Map<Character, Integer>> context : counts.entrySet()) {
            int total = context.getValue().values().stream().mapToInt(Integer::intValue).sum();
            JsonObject probabilities = new JsonObject();
            for (Map.Entry<Character, Integer> next : context.getValue().entrySet()) {
                probabilities.addProperty(String.valueOf(next.getKey()),
                        BigDecimal.valueOf(next.getValue()).divide(BigDecimal.valueOf(total), DECIMALS, RoundingMode.HALF_UP));
            }
            tables.add(context.getKey(), probabilities);
        }
        JsonObject category = new JsonObject();
        category.addProperty("title", title);
        category.addProperty("order", order);
        if (minLength != null) {
            category.addProperty("minLength", minLength);
        }
        if (maxLength != null) {
            category.addProperty("maxLength", maxLength);
        }
        category.add("tables", tables);
        return category;
    }

}
