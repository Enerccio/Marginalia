package com.github.enerccio.marginalia.desktop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BundledPluginsTest {

    @TempDir
    Path root;

    Path bundled;
    Path extensions;
    Path marker;
    List<String> messages = new ArrayList<>();

    @BeforeEach
    void folders() throws IOException {
        bundled = Files.createDirectories(root.resolve("plugins"));
        extensions = root.resolve("data/extensions");
        marker = root.resolve("data/desktop/bundled-plugins.txt");
    }

    private void bundle(String... names) throws IOException {
        for (String name : names) {
            Files.writeString(bundled.resolve(name), "jar " + name, StandardCharsets.UTF_8);
        }
    }

    private int install() {
        return BundledPlugins.install(bundled, extensions, marker, messages::add);
    }

    @Test
    void nothingHappensWithoutTheFolder() {
        assertEquals(0, BundledPlugins.install(root.resolve("missing"), extensions, marker, messages::add));
        assertFalse(Files.exists(extensions));
        assertFalse(Files.exists(marker));
    }

    @Test
    void theFirstStartCopiesEveryJar() throws IOException {
        bundle("reviewer-1.6.0.jar", "sidequery-1.6.0.jar", "readme.txt");

        assertEquals(2, install());

        assertTrue(Files.exists(extensions.resolve("reviewer-1.6.0.jar")));
        assertTrue(Files.exists(extensions.resolve("sidequery-1.6.0.jar")));
        assertFalse(Files.exists(extensions.resolve("readme.txt")));
        assertEquals(List.of("reviewer-1.6.0.jar", "sidequery-1.6.0.jar"), Files.readAllLines(marker));
        try (var left = Files.list(extensions)) {
            assertTrue(left.noneMatch(p -> p.getFileName().toString().endsWith(".part")));
        }
    }

    @Test
    void aSecondStartCopiesNothing() throws IOException {
        bundle("reviewer-1.6.0.jar");
        install();

        assertEquals(0, install());
    }

    @Test
    void aPluginTheAdministratorRemovedStaysRemoved() throws IOException {
        bundle("reviewer-1.6.0.jar", "sidequery-1.6.0.jar");
        install();
        Files.delete(extensions.resolve("reviewer-1.6.0.jar"));

        assertEquals(0, install());

        assertFalse(Files.exists(extensions.resolve("reviewer-1.6.0.jar")));
    }

    @Test
    void aRemovedPluginStaysRemovedInANewerVersionToo() throws IOException {
        bundle("reviewer-1.6.0.jar", "sidequery-1.6.0.jar");
        install();
        Files.delete(extensions.resolve("reviewer-1.6.0.jar"));
        bundle("reviewer-1.7.0.jar", "sidequery-1.7.0.jar");

        assertEquals(1, install());

        assertFalse(Files.exists(extensions.resolve("reviewer-1.7.0.jar")));
        assertTrue(Files.exists(extensions.resolve("sidequery-1.7.0.jar")));
        assertTrue(messages.stream().anyMatch(m -> m.contains("reviewer") && m.contains("removed")));
    }

    @Test
    void aNewVersionOfAnInstalledPluginIsCopiedNextToTheOldOne() throws IOException {
        bundle("reviewer-1.6.0.jar");
        install();
        bundle("reviewer-1.7.0.jar");

        assertEquals(1, install());

        assertTrue(Files.exists(extensions.resolve("reviewer-1.6.0.jar")));
        assertTrue(Files.exists(extensions.resolve("reviewer-1.7.0.jar")));
    }

    @Test
    void aNewPluginInANewVersionIsInstalled() throws IOException {
        bundle("reviewer-1.6.0.jar");
        install();
        bundle("beta-readers-1.7.0.jar");

        assertEquals(1, install());

        assertTrue(Files.exists(extensions.resolve("beta-readers-1.7.0.jar")));
    }

    @Test
    void aJarThatIsThereAlreadyIsNotOverwritten() throws IOException {
        Files.createDirectories(extensions);
        Files.writeString(extensions.resolve("reviewer-1.6.0.jar"), "mine");
        bundle("reviewer-1.6.0.jar");

        assertEquals(0, install());

        assertEquals("mine", Files.readString(extensions.resolve("reviewer-1.6.0.jar")));
    }

    @Test
    void pluginNameDropsTheVersion() {
        assertEquals("reviewer", BundledPlugins.pluginName("reviewer-1.6.0.jar"));
        assertEquals("beta-readers", BundledPlugins.pluginName("beta-readers-1.6.0-SNAPSHOT.jar"));
        assertEquals("sidequery", BundledPlugins.pluginName("sidequery.jar"));
    }
}
