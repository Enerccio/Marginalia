import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Shared by the importers of the name generator ({@code ImportMarkovNamegen}, {@code ImportFantasygen}): cloning a
 * repository and writing source files for {@code GenerateTables.java}. The importers are started with
 * {@code java Import*.java}, which compiles this file next to them (source launcher, several files).
 */
final class ImportSupport {

    private static final Gson GSON = new GsonBuilder().create();

    private ImportSupport() {
    }

    /**
     * Shallow clone of the repository unless {@code clone/<marker>} is there already (an existing clone is used as it is,
     * without a pull); false, with a message, when git or the network is not there.
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
            System.out.println("name-generator: cannot run git: " + e.getMessage());
        }
        System.out.println("name-generator: cannot clone " + url + ", not imported");
        return false;
    }

    /** A name as it is trained: the no-break spaces (a web page leftover) are spaces, the ends are trimmed. */
    static String clean(String raw) {
        return raw.replace(' ', ' ').strip();
    }

    /** A name the chains can take: not empty, without the {@code ^} and {@code $} of the tables. */
    static boolean usable(String name) {
        return !name.isEmpty() && name.indexOf('^') < 0 && name.indexOf('$') < 0;
    }

    /** Deletes the {@code *.json} of the folder, which is created when it is not there. */
    static void clearJson(Path dir) throws IOException {
        Files.createDirectories(dir);
        try (Stream<Path> old = Files.list(dir)) {
            for (Path file : (Iterable<Path>) old.filter(p -> p.getFileName().toString().endsWith(".json"))::iterator) {
                Files.delete(file);
            }
        }
    }

    /** Writes {@code {"_id", "title", "values"}} as {@code <dir>/<file>}. */
    static void write(Path dir, String file, String id, String title, Collection<String> names) throws IOException {
        JsonArray values = new JsonArray();
        names.forEach(values::add);
        JsonObject source = new JsonObject();
        source.addProperty("_id", id);
        source.addProperty("title", title);
        source.add("values", values);
        Files.writeString(dir.resolve(file), GSON.toJson(source) + "\n", StandardCharsets.UTF_8);
    }
}
