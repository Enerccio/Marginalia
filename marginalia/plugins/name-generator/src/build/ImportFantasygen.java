import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Build step of the name generator (run by exec-maven-plugin before {@code GenerateTables.java}, see the pom): clones
 * <a href="https://github.com/alxgiraud/fantasygen">fantasygen</a> and turns the dictionaries of its
 * {@code data/defaultDictionaries.json} (a list of {@code {"_id", "title", "values"}}) into source files of the name
 * generator, one per dictionary, in {@code <output dir>}. Run as
 * {@code java ImportFantasygen.java <repository url> <clone dir> <output dir>}, Gson on the classpath.
 * <p>
 * The clone is made once and not pulled again, without network nothing is imported and nothing fails (see
 * {@link ImportSupport}). Names are cleaned (the no-break spaces of the file are removed) and de-duplicated.
 */
public class ImportFantasygen {

    static final String DICTIONARIES = "data/defaultDictionaries.json";

    public static void main(String[] args) throws IOException, InterruptedException {
        Path clone = Path.of(args[1]);
        Path output = Path.of(args[2]);
        if (!ImportSupport.ensureClone(args[0], clone, DICTIONARIES)) {
            return;
        }
        ImportSupport.clearJson(output);
        int written = 0;
        try (Reader in = Files.newBufferedReader(clone.resolve(DICTIONARIES), StandardCharsets.UTF_8)) {
            for (JsonElement element : JsonParser.parseReader(in).getAsJsonArray()) {
                JsonObject dictionary = element.getAsJsonObject();
                String id = dictionary.get("_id").getAsString();
                String title = ImportSupport.clean(dictionary.get("title").getAsString());
                Set<String> names = new LinkedHashSet<>();
                for (JsonElement value : dictionary.getAsJsonArray("values")) {
                    String name = ImportSupport.clean(value.getAsString());
                    if (ImportSupport.usable(name)) {
                        names.add(name);
                    }
                }
                if (names.isEmpty()) {
                    System.out.println("name-generator: skipping " + title + ", no names");
                    continue;
                }
                ImportSupport.write(output, "fantasygen-" + id + ".json", "fantasygen-" + id, title, names);
                written++;
            }
        }
        System.out.println("name-generator: " + written + " categories imported from fantasygen into " + output);
    }
}
