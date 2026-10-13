import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Measures how each category of the source files behaves as a Markov chain of order 1 to {@value #MAX_ORDER} and writes
 * the best settings into the descriptor {@code src/build/chains.json}, which {@code GenerateTables.java} applies. Run on
 * demand, not part of the build, after the sources changed:
 * {@code mvn exec:exec@tune-chains} in the plugin folder, or
 * {@code java TuneChains.java <resources dir> <descriptor file> [report file]} with Gson on the classpath.
 * <p>
 * Per category and order two things are measured:
 * <ul>
 * <li><b>bits</b>: how well the order predicts names it was not trained on, in bits per character (lower is better): a
 * {@value #FOLDS}-fold cross-validation, the names of a fold are held out, the chain of the other ones (smoothed with the
 * shorter chains for what it has not seen, so that a long chain is not punished for a gap) scores them.
 * A chain longer than the amount of names supports gets worse here, a short one for long names too;</li>
 * <li><b>copy</b> and <b>novel</b>: the chain trained on all the names walks {@value #ATTEMPTS} times (a fixed seed, the
 * result is repeatable) with the length limits of the category. The share of the names made that are in the training
 * names (too high: the chain only repeats them) and the number of different new names (too few: nothing to choose from).</li>
 * </ul>
 * The order picked starts at {@value #DEFAULT_ORDER}, goes up while the next one has fewer bits by more than
 * {@value #TOLERANCE_PERCENT} %, goes down to 1 when that has that much fewer bits than the order 2, and is lowered
 * while the chain copies more than {@code MAX_COPY} of the names or makes fewer than {@code MIN_NOVEL} new ones.
 * <p>
 * The length limits are the 5th and 95th percentile of the lengths of the names, so that a category of long names does
 * not give fragments and one of short names does not give sentences.
 * <p>
 * The descriptor holds one entry per category title, {@code {"order": 3, "minLength": 4, "maxLength": 14}}; an entry
 * edited by hand stays as it is when it has {@code "keep": true}. Titles that are not in the sources any more are
 * dropped.
 */
public class TuneChains {

    static final int MAX_ORDER = 4;
    static final int ATTEMPTS = 400;
    static final double DISCOUNT = 0.75;
    static final int FOLDS = 5;
    static final int TOLERANCE_PERCENT = 1;
    static final double MAX_COPY = 0.3;
    static final int MIN_NOVEL = 40;
    static final int DEFAULT_ORDER = 2;
    static final int FLOOR_LENGTH = 2;
    static final int CEIL_LENGTH = 30;
    static final char START = '^';
    static final char END = '$';

    record Metrics(int order, double bits, double valid, double copy, int novel) {
        /** @param names the amount of names: a small category cannot make many new ones */
        boolean varied(int names) {
            return copy <= MAX_COPY && novel >= Math.min(MIN_NOVEL, names / 2);
        }
    }

    public static void main(String[] args) throws IOException {
        Path sources = Path.of(args[0]);
        Path descriptorFile = Path.of(args[1]);
        Path reportFile = args.length > 2 ? Path.of(args[2]) : null;

        Map<String, List<String>> categories = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        try (Stream<Path> walk = Files.walk(sources)) {
            for (Path file : (Iterable<Path>) walk.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".json")
                    && isNonEmpty(p)).sorted()::iterator) {
                try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    JsonObject source = JsonParser.parseReader(in).getAsJsonObject();
                    List<String> names = new ArrayList<>(new java.util.LinkedHashSet<>(source.getAsJsonArray("values").asList().stream()
                            .map(JsonElement::getAsString).map(n -> n.strip().toLowerCase(Locale.ROOT)).filter(n -> !n.isEmpty()).toList()));
                    categories.put(source.get("title").getAsString().strip(), names);
                }
            }
        }

        JsonObject old = Files.exists(descriptorFile) ? parse(descriptorFile) : new JsonObject();
        JsonObject oldCategories = old.has("categories") ? old.getAsJsonObject("categories") : new JsonObject();
        JsonObject tuned = new JsonObject();
        StringBuilder report = new StringBuilder("category\tnames\torder\tminLength\tmaxLength\t"
                + "o1 bits/valid/copy/novel\to2\to3\to4\n");
        int changed = 0;
        for (Map.Entry<String, List<String>> category : categories.entrySet()) {
            String title = category.getKey();
            JsonObject kept = oldCategories.has(title) ? oldCategories.getAsJsonObject(title) : null;
            if (kept != null && kept.has("keep") && kept.get("keep").getAsBoolean()) {
                tuned.add(title, kept);
                continue;
            }
            List<String> names = category.getValue();
            int[] limits = limits(names);
            Random random = new Random(title.hashCode());
            List<Metrics> metrics = new ArrayList<>();
            for (int order = 1; order <= MAX_ORDER; order++) {
                metrics.add(measure(names, order, limits[0], limits[1], random));
            }
            // from the default order, a longer one only when it is clearly better than the one before, a shorter one when it is better than the default
            int order = DEFAULT_ORDER;
            while (order < MAX_ORDER && better(metrics.get(order), metrics.get(order - 1))) {
                order++;
            }
            if (order == DEFAULT_ORDER && better(metrics.get(order - 2), metrics.get(order - 1))) {
                order--;
            }
            while (order > 1 && !metrics.get(order - 1).varied(names.size())) {
                order--;
            }
            JsonObject entry = new JsonObject();
            entry.addProperty("order", order);
            entry.addProperty("minLength", limits[0]);
            entry.addProperty("maxLength", limits[1]);
            tuned.add(title, entry);
            if (!entry.equals(kept)) {
                changed++;
            }
            report.append(title).append('\t').append(names.size()).append('\t').append(order).append('\t')
                    .append(limits[0]).append('\t').append(limits[1]);
            for (Metrics m : metrics) {
                report.append(String.format(Locale.ROOT, "\t%.2f/%.2f/%.2f/%d", m.bits(), m.valid(), m.copy(), m.novel()));
            }
            report.append('\n');
        }

        JsonObject root = new JsonObject();
        root.addProperty("defaultOrder", DEFAULT_ORDER);
        root.add("categories", tuned);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Files.writeString(descriptorFile, gson.toJson(root) + "\n", StandardCharsets.UTF_8);
        if (reportFile != null) {
            Files.writeString(reportFile, report.toString(), StandardCharsets.UTF_8);
        }
        System.out.println("name-generator: " + tuned.size() + " categories tuned, " + changed + " changed, written to " + descriptorFile);
    }

    /** The bits of {@code a} are lower than the ones of {@code b} by more than the tolerance. */
    static boolean better(Metrics a, Metrics b) {
        return a.bits() < b.bits() * (1 - TOLERANCE_PERCENT / 100.0);
    }

    static JsonObject parse(Path file) throws IOException {
        try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(in).getAsJsonObject();
        }
    }

    static boolean isNonEmpty(Path file) {
        try {
            return Files.size(file) > 0;
        } catch (IOException e) {
            return false;
        }
    }

    /** The 5th and 95th percentile of the lengths, in the limits of the generator. */
    static int[] limits(List<String> names) {
        List<Integer> lengths = new ArrayList<>(names.stream().map(String::length).toList());
        Collections.sort(lengths);
        int min = lengths.get((int) Math.floor((lengths.size() - 1) * 0.05));
        int max = lengths.get((int) Math.ceil((lengths.size() - 1) * 0.95));
        min = Math.max(FLOOR_LENGTH, Math.min(CEIL_LENGTH, min));
        max = Math.max(min, Math.min(CEIL_LENGTH, max));
        return new int[] {min, max};
    }

    static Metrics measure(List<String> names, int order, int min, int max, Random random) {
        List<String> shuffled = new ArrayList<>(names);
        Collections.shuffle(shuffled, random);

        // cross-validation: the bits per character of the held-out names
        double bits = 0;
        long chars = 0;
        int folds = Math.min(FOLDS, shuffled.size());
        for (int fold = 0; fold < folds; fold++) {
            List<String> train = new ArrayList<>();
            List<String> held = new ArrayList<>();
            for (int i = 0; i < shuffled.size(); i++) {
                (i % folds == fold ? held : train).add(shuffled.get(i));
            }
            List<Map<String, Map<Character, Integer>>> counts = new ArrayList<>();
            for (int o = 0; o <= order; o++) {
                counts.add(count(train, o));
            }
            Set<Character> alphabet = new HashSet<>(List.of(END));
            train.forEach(n -> n.chars().forEach(c -> alphabet.add((char) c)));
            for (String name : held) {
                String padded = String.valueOf(START).repeat(order) + name + END;
                for (int i = order; i < padded.length(); i++) {
                    bits -= Math.log(probability(counts, order, padded.substring(i - order, i), padded.charAt(i), alphabet.size() + 1)) / Math.log(2);
                    chars++;
                }
            }
        }

        // the walks of the chain of all the names
        Map<String, Map<Character, Integer>> counts = count(shuffled, order);
        Set<String> trained = new HashSet<>(shuffled);
        int valid = 0;
        int copies = 0;
        Set<String> novel = new HashSet<>();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            String name = walk(counts, order, min, max, random);
            if (name == null) {
                continue;
            }
            valid++;
            if (trained.contains(name)) {
                copies++;
            } else {
                novel.add(name);
            }
        }
        return new Metrics(order, bits / chars, (double) valid / ATTEMPTS, valid == 0 ? 1 : (double) copies / valid, novel.size());
    }

    static Map<String, Map<Character, Integer>> count(List<String> names, int order) {
        Map<String, Map<Character, Integer>> counts = new HashMap<>();
        for (String name : names) {
            String padded = String.valueOf(START).repeat(order) + name + END;
            for (int i = order; i < padded.length(); i++) {
                counts.computeIfAbsent(padded.substring(i - order, i), k -> new HashMap<>()).merge(padded.charAt(i), 1, Integer::sum);
            }
        }
        return counts;
    }

    /**
     * The chance of {@code next} after {@code context} (its last {@code order} characters): the counts of the chain with
     * {@value #DISCOUNT} taken off each, the taken part goes to the guess of the next shorter chain (a uniform guess for
     * the order 0), the more of it the more different characters followed the context (absolute discounting).
     */
    static double probability(List<Map<String, Map<Character, Integer>>> counts, int order, String context, char next, int alphabet) {
        double lower = order == 0 ? 1.0 / alphabet
                : probability(counts, order - 1, context.substring(1), next, alphabet);
        Map<Character, Integer> seen = counts.get(order).get(context);
        if (seen == null) {
            return lower;
        }
        int total = seen.values().stream().mapToInt(Integer::intValue).sum();
        double own = Math.max(seen.getOrDefault(next, 0) - DISCOUNT, 0) / total;
        return own + DISCOUNT * seen.size() / total * lower;
    }

    static String walk(Map<String, Map<Character, Integer>> counts, int order, int min, int max, Random random) {
        StringBuilder name = new StringBuilder();
        String context = String.valueOf(START).repeat(order);
        while (name.length() <= max) {
            Map<Character, Integer> next = counts.get(context);
            if (next == null) {
                return null;
            }
            int total = next.values().stream().mapToInt(Integer::intValue).sum();
            int pick = random.nextInt(total);
            char c = END;
            for (Map.Entry<Character, Integer> e : next.entrySet()) {
                pick -= e.getValue();
                if (pick < 0) {
                    c = e.getKey();
                    break;
                }
            }
            if (c == END) {
                return name.length() < min ? null : name.toString();
            }
            name.append(c);
            context = (context + c).substring(1);
        }
        return null;
    }
}
