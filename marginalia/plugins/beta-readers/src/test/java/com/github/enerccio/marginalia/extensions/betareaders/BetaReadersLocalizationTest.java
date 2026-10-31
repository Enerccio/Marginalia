package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;
import com.github.enerccio.marginalia.loc.Language;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Every text the plugin shows has a Japanese and a Chinese translation: the texts are found in the sources, as
 * {@code T.localized("...")}.
 */
class BetaReadersLocalizationTest {

    private static final Pattern TEXT = Pattern.compile("T\\.localized\\(\"((?:[^\"\\\\]|\\\\.)*)\"\\)");

    /** Gives the test the translations, which the bundle keeps in a protected field. */
    private static class Probe extends BetaReadersLocalization {
        Map<String, String> of(Language language) {
            return keys.getOrDefault(language, Map.of());
        }
    }

    private static List<String> usedTexts() throws IOException {
        TreeSet<String> texts = new TreeSet<>();
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                Matcher matcher = TEXT.matcher(Files.readString(file));
                while (matcher.find()) {
                    texts.add(matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\"));
                }
            }
        }
        return new ArrayList<>(texts);
    }

    @Test
    void everyTextHasAJapaneseAndAChineseTranslation() throws IOException {
        Probe probe = new Probe();
        List<String> texts = usedTexts();
        assertFalse(texts.isEmpty());

        List<String> missing = new ArrayList<>();
        for (String text : texts) {
            if (!probe.of(PluginLocalizationBundle.JA).containsKey(text)) {
                missing.add("ja: " + text);
            }
            if (!probe.of(PluginLocalizationBundle.ZH).containsKey(text)) {
                missing.add("zh: " + text);
            }
        }
        assertEquals(List.of(), missing);
    }

    @Test
    void noTranslationIsForATextThatIsGone() throws IOException {
        Probe probe = new Probe();
        List<String> texts = usedTexts();

        List<String> unused = new ArrayList<>();
        for (Language language : List.of(PluginLocalizationBundle.JA, PluginLocalizationBundle.ZH)) {
            for (String text : probe.of(language).keySet()) {
                if (!texts.contains(text)) {
                    unused.add(language + ": " + text);
                }
            }
        }
        assertEquals(List.of(), unused);
    }

    @Test
    void aTranslationKeepsThePlaceholdersOfTheText() {
        Probe probe = new Probe();
        for (Language language : List.of(PluginLocalizationBundle.JA, PluginLocalizationBundle.ZH)) {
            probe.of(language).forEach((text, translation) -> {
                assertEquals(placeholders(text), placeholders(translation), language + ": " + text);
            });
        }
    }

    private static List<String> placeholders(String text) {
        List<String> found = new ArrayList<>();
        Matcher matcher = Pattern.compile("%(\\d+\\$)?[sd]|\\{\\{[^}]*}}").matcher(text);
        while (matcher.find()) {
            found.add(matcher.group());
        }
        return found;
    }
}
