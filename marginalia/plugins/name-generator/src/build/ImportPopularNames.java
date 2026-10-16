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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Build step of the name generator (run by exec-maven-plugin before {@code GenerateTables.java}, see the pom): clones the
 * <a href="https://github.com/sigpwned/popular-names-by-country-dataset">popular names by country dataset</a> (CC0) and
 * turns the romanized surnames of {@code common-surnames-by-country.csv} into source files of the name generator,
 * {@code {"_id", "title", "values"}}, in {@code <output dir>}, one per country. Run as
 * {@code java ImportPopularNames.java <repository url> <clone dir> <resources dir> <output dir>}, Gson on the classpath.
 * <ul>
 * <li>Only the {@code Country} and the {@code Romanized Name} columns are read. The title is
 * {@code <Country> - Surnames} with the code expanded to the English name of the country ({@code AM} is {@code Armenia}).</li>
 * <li>Alternatives of a cell ({@code Huỳnh/Hoàng}) are separate names. Rows without a romanized name are dropped, names
 * are trimmed and de-duplicated, those with the {@code ^} or {@code $} marker characters of the tables dropped.</li>
 * <li>A country with fewer than {@value #MIN_NAMES} names is skipped, and so is a title that a source file elsewhere in
 * the resources dir uses already.</li>
 * <li>An existing clone is used as it is, without a pull. Without network (and without a clone) nothing is imported and
 * nothing fails: the committed {@code name-tables.json} is enough to build.</li>
 * </ul>
 */
public class ImportPopularNames {

    static final String SURNAMES = "common-surnames-by-country.csv";
    static final int MIN_NAMES = 5;

    public static void main(String[] args) throws IOException, InterruptedException {
        String url = args[0];
        Path clone = Path.of(args[1]);
        Path resources = Path.of(args[2]);
        Path output = Path.of(args[3]);

        if (!ImportSupport.ensureClone(url, clone, SURNAMES)) {
            return;
        }
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

        List<List<String>> rows = parse(Files.readString(clone.resolve(SURNAMES), StandardCharsets.UTF_8));
        List<String> header = rows.get(0);
        int country = column(header, "Country");
        int romanized = column(header, "Romanized Name");
        Map<String, Set<String>> countries = new TreeMap<>();
        for (List<String> row : rows.subList(1, rows.size())) {
            if (row.size() <= Math.max(country, romanized)) {
                continue;
            }
            Set<String> names = countries.computeIfAbsent(row.get(country).strip(), k -> new LinkedHashSet<>());
            for (String alternative : row.get(romanized).split("/")) {
                String name = ImportSupport.clean(alternative);
                if (ImportSupport.usable(name)) {
                    names.add(name);
                }
            }
        }

        ImportSupport.clearJson(output);
        int written = 0;
        for (Map.Entry<String, Set<String>> entry : countries.entrySet()) {
            String title = countryName(entry.getKey()) + " - Surnames";
            if (entry.getValue().size() < MIN_NAMES) {
                System.out.println("name-generator: skipping " + title + ", only " + entry.getValue().size() + " names");
                continue;
            }
            if (!taken.add(title)) {
                System.out.println("name-generator: skipping " + title + ", the category exists already");
                continue;
            }
            ImportSupport.write(output, "surnames-" + entry.getKey().toLowerCase(Locale.ROOT) + ".json",
                    "popular-surnames-" + entry.getKey().toLowerCase(Locale.ROOT), title, entry.getValue());
            written++;
        }
        System.out.println("name-generator: " + written + " categories imported from popular-names-by-country-dataset into " + output);
    }

    static boolean isNonEmpty(Path file) {
        try {
            return Files.size(file) > 0;
        } catch (IOException e) {
            return false;
        }
    }

    /** The English name of a country by its ISO 3166 code ({@code XK} is Kosovo); the code itself when unknown. */
    static String countryName(String code) {
        String name = Locale.of("", code).getDisplayCountry(Locale.ENGLISH);
        return name.isBlank() || name.equalsIgnoreCase(code) ? code : name;
    }

    private static int column(List<String> header, String name) {
        int index = header.indexOf(name);
        if (index < 0) {
            throw new IllegalStateException(SURNAMES + ": no column '" + name + "' in " + header);
        }
        return index;
    }

    /** CSV (RFC 4180: quoted fields with doubled quotes and line breaks); a byte order mark at the start is skipped. */
    static List<List<String>> parse(String text) {
        if (text.startsWith("﻿")) {
            text = text.substring(1);
        }
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < text.length() && text.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                row.add(field.toString());
                field.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                row.add(field.toString());
                field.setLength(0);
                if (!(row.size() == 1 && row.get(0).isEmpty())) {
                    rows.add(row);
                }
                row = new ArrayList<>();
            } else {
                field.append(c);
            }
        }
        if (field.length() > 0 || !row.isEmpty()) {
            row.add(field.toString());
            rows.add(row);
        }
        return rows;
    }

}
