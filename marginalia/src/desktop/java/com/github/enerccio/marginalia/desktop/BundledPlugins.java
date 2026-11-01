package com.github.enerccio.marginalia.desktop;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The plugins of a "full" desktop build: their JARs travel in a {@code plugins/} folder next to {@code server/} and
 * are copied into the extensions folder ({@code <home>/.marginalia/extensions}) when the application starts.
 * <p>
 * The administrator stays in charge of the folder, so a JAR is copied only once: the names that were copied are
 * remembered in a marker file and a plugin that was copied before and is not in the folder any more (removed in
 * <em>Admin → Extensions</em>) is not brought back, also not in a newer version. A newer version of a plugin that is
 * still installed is copied next to the old JAR; the application keeps the newest of the versions and moves the older
 * ones to {@code old-versions/}. A JAR the user put there by hand under the same name is left alone.
 * <p>
 * JDK only, like the launcher.
 */
final class BundledPlugins {

    /** {@code reviewer-1.6.0.jar} → {@code reviewer}; a name without a version is its own plugin name. */
    private static final Pattern NAME = Pattern.compile("^(.+?)-\\d[^/]*\\.jar$");

    private BundledPlugins() {
    }

    /**
     * Copies the plugins that are new for this installation.
     *
     * @param bundled    the {@code plugins/} folder of the application (may not exist: a build without plugins)
     * @param extensions the extensions folder of the data directory
     * @param marker     file that lists the JARs copied so far
     * @param log        receives one line per copied or failed plugin
     * @return the number of JARs copied
     */
    static int install(Path bundled, Path extensions, Path marker, Consumer<String> log) {
        if (!Files.isDirectory(bundled)) {
            return 0;
        }
        try {
            List<Path> jars;
            try (Stream<Path> files = Files.list(bundled)) {
                jars = files.filter(p -> p.getFileName().toString().endsWith(".jar")).sorted().toList();
            }
            if (jars.isEmpty()) {
                return 0;
            }
            Files.createDirectories(extensions);
            Set<String> seeded = readMarker(marker);
            Set<String> seededPlugins = new LinkedHashSet<>();
            seeded.forEach(jar -> seededPlugins.add(pluginName(jar)));
            Set<String> installedPlugins = new LinkedHashSet<>();
            try (Stream<Path> files = Files.list(extensions)) {
                files.map(p -> p.getFileName().toString()).filter(n -> n.endsWith(".jar"))
                        .forEach(n -> installedPlugins.add(pluginName(n)));
            }

            int copied = 0;
            List<String> done = new ArrayList<>(seeded);
            for (Path jar : jars) {
                String name = jar.getFileName().toString();
                if (seeded.contains(name)) {
                    continue;
                }
                String plugin = pluginName(name);
                Path target = extensions.resolve(name);
                if (Files.exists(target)) {
                    // put there by hand (or an earlier run died before it wrote the marker)
                    done.add(name);
                } else if (seededPlugins.contains(plugin) && !installedPlugins.contains(plugin)) {
                    log.accept("Plugin " + plugin + " was removed, not installing it again");
                    done.add(name);
                } else {
                    Files.copy(jar, extensions.resolve(name + ".part"), StandardCopyOption.REPLACE_EXISTING);
                    Files.move(extensions.resolve(name + ".part"), target, StandardCopyOption.REPLACE_EXISTING);
                    log.accept("Installed plugin " + name);
                    done.add(name);
                    copied++;
                }
            }
            writeMarker(marker, done);
            return copied;
        } catch (IOException | RuntimeException e) {
            log.accept("Bundled plugins were not installed: " + e.getMessage());
            return 0;
        }
    }

    static String pluginName(String jarName) {
        Matcher matcher = NAME.matcher(jarName);
        return matcher.matches() ? matcher.group(1) : jarName.replaceFirst("\\.jar$", "");
    }

    private static Set<String> readMarker(Path marker) throws IOException {
        Set<String> names = new LinkedHashSet<>();
        if (Files.exists(marker)) {
            for (String line : Files.readAllLines(marker, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    names.add(line.trim());
                }
            }
        }
        return names;
    }

    private static void writeMarker(Path marker, List<String> names) throws IOException {
        Files.createDirectories(marker.toAbsolutePath().getParent());
        Files.write(marker, names, StandardCharsets.UTF_8);
    }
}
