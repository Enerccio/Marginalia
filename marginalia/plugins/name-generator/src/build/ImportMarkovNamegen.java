import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Build step of the name generator (run by exec-maven-plugin before {@code GenerateTables.java}, see the pom): clones
 * the word lists of <a href="https://github.com/Tw1ddle/markov-namegen-lib/">markov-namegen-lib</a> (MIT) and turns
 * every {@code word_lists/*.txt} (one name per line) into a source file of the name generator,
 * {@code {"_id", "title", "values"}}, in {@code <output dir>}. Run as
 * {@code java ImportMarkovNamegen.java <repository url> <clone dir> <resources dir> <output dir>}, Gson on the classpath.
 * <ul>
 * <li>An existing clone is used as it is, without a pull, so the build does not change under one's hands. Delete the
 * clone dir to fetch it again.</li>
 * <li>Without network (and without a clone) nothing is imported and nothing fails: the committed
 * {@code name-tables.json} is enough to build.</li>
 * <li>The title is the file name: {@code theological_angels.txt} is {@code Theological Angels}. A list whose title is
 * already used by a source file elsewhere in the resources dir (the hand made ones, the Onomastikon) is skipped, and
 * so are lists with fewer than {@value #MIN_NAMES} names.</li>
 * <li>Names are trimmed and de-duplicated, those with the {@code ^} or {@code $} marker characters of the tables dropped.</li>
 * </ul>
 */
public class ImportMarkovNamegen {

    static final int MIN_NAMES = 10;
    static final Set<String> SMALL_WORDS = Set.of("a", "an", "and", "in", "of", "on", "the", "to");

    public static void main(String[] args) throws IOException, InterruptedException {
        String url = args[0];
        Path clone = Path.of(args[1]);
        Path resources = Path.of(args[2]);
        Path output = Path.of(args[3]);

        if (!ImportSupport.ensureClone(url, clone, "word_lists")) {
            return;
        }
        Path lists = clone.resolve("word_lists");

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
        try (Stream<Path> list = Files.list(lists)) {
            list.filter(p -> p.getFileName().toString().endsWith(".txt")).sorted().forEach(files::add);
        }
        ImportSupport.clearJson(output);

        int written = 0;
        for (Path file : files) {
            String stem = file.getFileName().toString().replaceFirst("\\.txt$", "");
            String title = title(stem);
            Set<String> names = new LinkedHashSet<>();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String name = ImportSupport.clean(line);
                if (ImportSupport.usable(name)) {
                    names.add(name);
                }
            }
            if (names.size() < MIN_NAMES) {
                System.out.println("name-generator: skipping " + title + ", only " + names.size() + " names");
                continue;
            }
            if (!taken.add(title)) {
                System.out.println("name-generator: skipping " + title + ", the category exists already");
                continue;
            }
            ImportSupport.write(output, stem + ".json", "markov-namegen-" + stem.replace('_', '-'), title, names);
            written++;
        }
        System.out.println("name-generator: " + written + " categories imported from markov-namegen-lib into " + output);
    }

    static boolean isNonEmpty(Path file) {
        try {
            return Files.size(file) > 0;
        } catch (IOException e) {
            return false;
        }
    }

    /** {@code units_of_heroes_3} to {@code Units of Heroes 3}. */
    static String title(String stem) {
        StringBuilder title = new StringBuilder();
        for (String word : stem.split("_")) {
            if (word.isEmpty()) {
                continue;
            }
            if (!title.isEmpty()) {
                title.append(' ');
            }
            if (!title.isEmpty() && SMALL_WORDS.contains(word.toLowerCase(Locale.ROOT))) {
                title.append(word.toLowerCase(Locale.ROOT));
            } else {
                title.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return title.toString();
    }

}
