package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;
import com.github.enerccio.marginalia.loc.Language;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
 * Every text the plugin shows is a constant of {@link BetaReadersLocalization} and has a Japanese, a Chinese and a French
 * translation; the sources ask for it as {@code T.localized(CONSTANT)}, never with a literal.
 */
class BetaReadersLocalizationTest {

    private static final Pattern LITERAL = Pattern.compile("T\\.localized\\(\"");

    /** Gives the test the translations, which the bundle keeps in a protected field. */
    private static class Probe extends BetaReadersLocalization {
        Map<String, String> of(Language language) {
            return keys.getOrDefault(language, Map.of());
        }
    }

    private static List<String> usedTexts() throws IllegalAccessException {
        TreeSet<String> texts = new TreeSet<>();
        for (Field field : BetaReadersLocalization.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                texts.add((String) field.get(null));
            }
        }
        return new ArrayList<>(texts);
    }

    @Test
    void textsAreAskedForByConstant() throws IOException {
        List<String> literals = new ArrayList<>();
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                if (LITERAL.matcher(Files.readString(file)).find()) {
                    literals.add(file.toString());
                }
            }
        }
        assertEquals(List.of(), literals);
    }

    @Test
    void everyTextHasAJapaneseAChineseAndAFrenchTranslation() throws IllegalAccessException {
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
            if (!probe.of(PluginLocalizationBundle.FR).containsKey(text)) {
                missing.add("fr: " + text);
            }
            if (!probe.of(PluginLocalizationBundle.ES).containsKey(text)) {
                missing.add("es: " + text);
            }
            if (!probe.of(PluginLocalizationBundle.DE).containsKey(text)) {
                missing.add("de: " + text);
            }
            if (!probe.of(PluginLocalizationBundle.KO).containsKey(text)) {
                missing.add("ko: " + text);
            }
            if (!probe.of(PluginLocalizationBundle.IT).containsKey(text)) {
                missing.add("it: " + text);
            }
        }
        assertEquals(List.of(), missing);
    }

    @Test
    void noTranslationIsForATextThatIsGone() throws IllegalAccessException {
        Probe probe = new Probe();
        List<String> texts = usedTexts();

        List<String> unused = new ArrayList<>();
        for (Language language : List.of(PluginLocalizationBundle.JA, PluginLocalizationBundle.ZH, PluginLocalizationBundle.FR, PluginLocalizationBundle.ES, PluginLocalizationBundle.DE, PluginLocalizationBundle.KO, PluginLocalizationBundle.IT)) {
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
        for (Language language : List.of(PluginLocalizationBundle.JA, PluginLocalizationBundle.ZH, PluginLocalizationBundle.FR, PluginLocalizationBundle.ES, PluginLocalizationBundle.DE, PluginLocalizationBundle.KO, PluginLocalizationBundle.IT)) {
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
