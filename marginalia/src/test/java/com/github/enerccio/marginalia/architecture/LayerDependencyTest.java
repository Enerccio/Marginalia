package com.github.enerccio.marginalia.architecture;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * The platform (layer 1 of {@code TODO.VN.md}, see {@code TODO.REFACTOR.md}) must not name a class of the application.
 * Every main class gets its layer from {@code layers/layers.txt}; the compiled classes of both modules are read with
 * ASM, every class must be in the module of its layer ({@code marginalia/platform} or the application) and every class
 * a platform class refers to (in signatures, bodies, annotations, constants) is checked. The module border makes the
 * compiler refuse most of these references already; the test still catches a class compiled into the wrong module.
 * <p>
 * The references that exist today are listed in {@code layers/allowed-leaks.txt}. The list may only shrink: a new
 * reference fails, and so does a listed one that is gone (delete its line). {@code -Dlayers.update=true} rewrites the
 * list from the classes, for when a class moves between layers in the map.
 */
class LayerDependencyTest {

    private static final String BASE = "com.github.enerccio.marginalia.";
    private static final String MAP = "/layers/layers.txt";
    private static final String ALLOWED = "/layers/allowed-leaks.txt";
    private static final String TEST_MAP = "/layers/test-layers.txt";
    private static final Path ALLOWED_SOURCE = Path.of("src/test/resources/layers/allowed-leaks.txt");

    @Test
    void platformDoesNotNameApplicationClasses() throws Exception {
        Map<String, String> rules = readRules(MAP);
        Map<String, Set<String>> references = new TreeMap<>();
        Set<String> misplaced = new TreeSet<>();
        for (Map.Entry<String, Path> module : ModuleClasses.roots().entrySet()) {
            Map<String, Set<String>> found = readReferences(module.getValue());
            for (String name : found.keySet()) {
                String layer = layerOf(rules, name);
                if (layer != null && !layer.equals(module.getKey())) {
                    misplaced.add(name + " (" + layer + ") is in the module of the " + module.getKey());
                }
            }
            references.putAll(found);
        }
        if (!misplaced.isEmpty()) {
            fail("Classes in the module of the other layer (move the source to marginalia/platform or back, or fix "
                    + MAP + "):\n  " + String.join("\n  ", misplaced));
        }

        Set<String> unmapped = new TreeSet<>();
        Set<String> leaks = new TreeSet<>();
        for (Map.Entry<String, Set<String>> entry : references.entrySet()) {
            String from = entry.getKey();
            String fromLayer = layerOf(rules, from);
            if (fromLayer == null) {
                unmapped.add(from);
                continue;
            }
            if (!fromLayer.equals("platform")) {
                continue;
            }
            for (String to : entry.getValue()) {
                if ("app".equals(layerOf(rules, to))) {
                    leaks.add(from + " -> " + to);
                }
            }
        }
        if (!unmapped.isEmpty()) {
            fail("Classes without a layer, add them to " + MAP + ":\n  " + String.join("\n  ", unmapped));
        }

        if (Boolean.getBoolean("layers.update")) {
            writeAllowed(leaks);
            return;
        }

        Set<String> allowed = readLines(ALLOWED);
        Set<String> added = new TreeSet<>(leaks);
        added.removeAll(allowed);
        Set<String> gone = new TreeSet<>(allowed);
        gone.removeAll(leaks);
        StringBuilder problems = new StringBuilder();
        if (!added.isEmpty()) {
            problems.append("Platform classes that name application classes (a registration point or an interface "
                    + "of the platform instead, or fix the layer in ").append(MAP).append("):\n  ")
                    .append(String.join("\n  ", added)).append('\n');
        }
        if (!gone.isEmpty()) {
            problems.append("Fixed, delete these lines from ").append(ALLOWED).append(":\n  ")
                    .append(String.join("\n  ", gone)).append('\n');
        }
        if (!problems.isEmpty()) {
            fail(problems.toString());
        }
    }

    /**
     * The shared test code of the platform (the test base, the mock LLM, the CRUD contracts; {@code layers/test-layers.txt})
     * names no class of the application, main or test, and has no allow list.
     */
    @Test
    void platformTestCodeDoesNotNameApplicationClasses() throws Exception {
        Map<String, String> mainRules = readRules(MAP);
        Map<String, String> testRules = readRules(TEST_MAP);
        Map<String, Set<String>> references = readReferences(testClasses());
        Set<String> leaks = new TreeSet<>();
        for (Map.Entry<String, Set<String>> entry : references.entrySet()) {
            String from = entry.getKey();
            if (!"platform".equals(layerOf(testRules, from))) {
                continue;
            }
            for (String to : entry.getValue()) {
                String layer = references.containsKey(to) ? layerOf(testRules, to) : layerOf(mainRules, to);
                if ("app".equals(layer) || (references.containsKey(to) && layer == null)) {
                    leaks.add(from + " -> " + to);
                }
            }
        }
        if (!leaks.isEmpty()) {
            fail("Platform test code that names application classes or unlisted test classes (" + TEST_MAP + "):\n  "
                    + String.join("\n  ", leaks));
        }
    }

    /** The folder the test classes were compiled to. */
    private static Path testClasses() throws URISyntaxException {
        return Path.of(LayerDependencyTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    /** Main class (outer, relative to the base package) to the classes of the base package it refers to. */
    private static Map<String, Set<String>> readReferences(Path root) throws IOException {
        Map<String, Set<String>> references = new TreeMap<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".class"))::iterator) {
                String name = root.relativize(file).toString().replace('\\', '/');
                if (name.endsWith("package-info.class") || name.endsWith("module-info.class")) {
                    continue;
                }
                String from = relative(name.substring(0, name.length() - ".class".length()));
                if (from == null) {
                    continue;
                }
                Set<String> to = references.computeIfAbsent(from, k -> new TreeSet<>());
                Remapper collector = new Remapper() {
                    @Override
                    public String map(String internalName) {
                        String target = relative(internalName);
                        if (target != null && !target.equals(from)) {
                            to.add(target);
                        }
                        return internalName;
                    }
                };
                byte[] bytes = Files.readAllBytes(file);
                new ClassReader(bytes).accept(new ClassRemapper(new ClassNode(), collector), 0);
            }
        }
        return references;
    }

    /** {@code com/github/enerccio/marginalia/a/B$C} to {@code a.B}, null for a class outside the base package. */
    private static String relative(String internalName) {
        String dotted = internalName.replace('/', '.');
        if (!dotted.startsWith(BASE)) {
            return null;
        }
        String name = dotted.substring(BASE.length());
        int nested = name.indexOf('$');
        return nested < 0 ? name : name.substring(0, nested);
    }

    /** The layer of the longest matching name of the map; "." matches the classes of the base package itself. */
    private static String layerOf(Map<String, String> rules, String className) {
        String best = null;
        int length = -1;
        for (Map.Entry<String, String> rule : rules.entrySet()) {
            String name = rule.getKey();
            boolean matches = name.equals(".")
                    ? className.indexOf('.') < 0
                    : className.equals(name) || className.startsWith(name + ".");
            int size = name.equals(".") ? 0 : name.length();
            if (matches && size > length) {
                best = rule.getValue();
                length = size;
            }
        }
        return best;
    }

    private static Map<String, String> readRules(String map) throws IOException {
        Map<String, String> rules = new TreeMap<>();
        for (String line : readLines(map)) {
            String[] parts = line.split("\\s+");
            if (parts.length != 2 || !(parts[0].equals("platform") || parts[0].equals("app"))) {
                fail("Bad line in " + map + ": " + line);
            }
            if (rules.put(parts[1], parts[0]) != null) {
                fail("Listed twice in " + map + ": " + parts[1]);
            }
        }
        return rules;
    }

    /** The lines of a resource without comments and blank lines; empty when it does not exist. */
    private static Set<String> readLines(String resource) throws IOException {
        Set<String> lines = new TreeSet<>();
        try (InputStream in = LayerDependencyTest.class.getResourceAsStream(resource)) {
            if (in == null) {
                return lines;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.strip();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    private static void writeAllowed(Set<String> leaks) throws IOException {
        var out = new ArrayList<String>();
        out.add("# Platform classes that name application classes today (LayerDependencyTest, TODO.REFACTOR.md Phase 1).");
        out.add("# This list may only shrink: fix a reference and delete its line. Rewrite it with -Dlayers.update=true");
        out.add("# only when the layer map changes.");
        out.addAll(leaks);
        Files.createDirectories(Objects.requireNonNull(ALLOWED_SOURCE.getParent()));
        Files.write(ALLOWED_SOURCE, out, StandardCharsets.UTF_8);
    }
}
