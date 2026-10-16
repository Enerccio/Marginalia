import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
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

/**
 * Build step of the name provider (run by exec-maven-plugin, see the pom): clones the
 * <a href="https://github.com/sigpwned/popular-names-by-country-dataset">popular names by country dataset</a> (CC0) and
 * writes the lists of names the extension ships as {@code name-lists.json}. Run as
 * {@code java ImportPopularNames.java <repository url> <clone folder> <output file>}.
 * <p>
 * Of {@code common-surnames-by-country.csv} and {@code common-forenames-by-country.csv} only the country and the pair
 * of the {@code Localized Name} (the original) and the {@code Romanized Name} is read, the other columns are ignored.
 * One category per country and file, {@code Armenia - Surnames} or {@code Armenia - Forenames} (the country code is
 * expanded to its English name), with the names in the order of the file:
 * <pre>
 * {"categories": [{"title": "Armenia - Surnames", "names": [["Grigoryan", "Գրիգորյան"], ...]}]}
 * </pre>
 * Every name is an array of the romanized name and the original one. A row without a romanized name is dropped (there
 * is nothing to read it by), a row without an original name takes the romanized one (the name is written in Latin
 * script), a pair is kept once per category.
 * <p>
 * Without git or network (or when the files are not in the clone) the output is left as it is, so a clone of the
 * repository builds without them. The clone is made once into the folder and not pulled again.
 */
public class ImportPopularNames {

    static final String SURNAMES = "common-surnames-by-country.csv";
    static final String FORENAMES = "common-forenames-by-country.csv";

    public static void main(String[] args) throws IOException, InterruptedException {
        Path clone = Path.of(args[1]);
        Path output = Path.of(args[2]);
        if (!ensureClone(args[0], clone, SURNAMES) || !Files.exists(clone.resolve(FORENAMES))) {
            System.out.println("name-provider: " + output + " left as it is");
            return;
        }
        Map<String, List<String[]>> categories = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        read(clone.resolve(SURNAMES), "Surnames", categories);
        read(clone.resolve(FORENAMES), "Forenames", categories);

        JsonArray array = new JsonArray();
        int names = 0;
        for (Map.Entry<String, List<String[]>> category : categories.entrySet()) {
            JsonArray list = new JsonArray();
            for (String[] pair : category.getValue()) {
                JsonArray entry = new JsonArray();
                entry.add(pair[0]);
                entry.add(pair[1]);
                list.add(entry);
            }
            JsonObject json = new JsonObject();
            json.addProperty("title", category.getKey());
            json.add("names", list);
            array.add(json);
            names += list.size();
        }
        JsonObject root = new JsonObject();
        root.add("categories", array);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.writeString(output, gson.toJson(root) + "\n", StandardCharsets.UTF_8);
        System.out.println("name-provider: " + array.size() + " lists with " + names + " names");
    }

    /** Adds the names of one CSV file to the categories {@code <Country> - <kind>}. */
    static void read(Path file, String kind, Map<String, List<String[]>> categories) throws IOException {
        List<List<String>> rows = parse(Files.readString(file, StandardCharsets.UTF_8));
        List<String> header = rows.get(0);
        int country = column(header, "Country", file);
        int original = column(header, "Localized Name", file);
        int romanized = column(header, "Romanized Name", file);
        Map<String, Set<String>> seen = new TreeMap<>();
        for (List<String> row : rows.subList(1, rows.size())) {
            if (row.size() <= Math.max(country, Math.max(original, romanized))) {
                continue;
            }
            String roman = clean(row.get(romanized));
            String local = clean(row.get(original));
            if (roman.isEmpty()) {
                continue;
            }
            String title = countryName(row.get(country).strip()) + " - " + kind;
            if (seen.computeIfAbsent(title, k -> new LinkedHashSet<>()).add(roman + "\u0000" + (local.isEmpty() ? roman : local))) {
                categories.computeIfAbsent(title, k -> new ArrayList<>()).add(new String[] {roman, local.isEmpty() ? roman : local});
            }
        }
    }

    /** The English name of a country by its ISO 3166 code ({@code XK} is Kosovo); the code itself when unknown. */
    static String countryName(String code) {
        String name = Locale.of("", code).getDisplayCountry(Locale.ENGLISH);
        return name.isBlank() || name.equalsIgnoreCase(code) ? code : name;
    }

    private static int column(List<String> header, String name, Path file) {
        int index = header.indexOf(name);
        if (index < 0) {
            throw new IllegalStateException(file.getFileName() + ": no column '" + name + "' in " + header);
        }
        return index;
    }

    /** No-break spaces (a web page leftover) are spaces, the ends are trimmed. */
    static String clean(String raw) {
        return raw.replace(' ', ' ').strip();
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

    /**
     * Shallow clone of the repository unless {@code clone/<marker>} is there already; false, with a message, when git or
     * the network is not there.
     */
    static boolean ensureClone(String url, Path clone, String marker) throws InterruptedException {
        if (Files.exists(clone.resolve(marker))) {
            return true;
        }
        try {
            Files.createDirectories(clone.toAbsolutePath().getParent());
            Process git = new ProcessBuilder("git", "clone", "--depth", "1", url, clone.toString()).inheritIO().start();
            if (git.waitFor() == 0 && Files.exists(clone.resolve(marker))) {
                return true;
            }
        } catch (IOException e) {
            System.out.println("name-provider: cannot run git: " + e.getMessage());
        }
        System.out.println("name-provider: cannot clone " + url + ", not imported");
        return false;
    }
}
