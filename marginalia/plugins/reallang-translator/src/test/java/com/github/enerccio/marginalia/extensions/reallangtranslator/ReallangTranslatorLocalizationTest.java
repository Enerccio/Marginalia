package com.github.enerccio.marginalia.extensions.reallangtranslator;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every text of the plugin has its Japanese and Chinese translation, with the same values in it, and no translation is left for a text that is
 * not there any more. (Two constants may have the same text, a few do: a name like {@code KIND_ADD} avoids a clash with an enum constant of the
 * class that uses it; they share the one translation, the text is the key.)
 */
class ReallangTranslatorLocalizationTest {

    private static final Pattern FORMAT = Pattern.compile("%(?:\\d+\\$)?[sd]");

    private static List<String> texts() throws Exception {
        List<String> texts = new ArrayList<>();
        for (Field field : ReallangTranslatorLocalization.class.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                texts.add((String) field.get(null));
            }
        }
        return texts;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> translations(Object language) throws Exception {
        Field field = PluginLocalizationBundle.class.getDeclaredField("keys");
        field.setAccessible(true);
        Map<Object, Map<String, String>> all = (Map<Object, Map<String, String>>) field.get(ReallangTranslatorLocalization.T);
        return all.getOrDefault(language, Map.of());
    }

    private static List<String> placeholders(String text) {
        List<String> found = new ArrayList<>();
        Matcher matcher = FORMAT.matcher(text);
        while (matcher.find()) {
            found.add(matcher.group());
        }
        return found;
    }

    @Test
    void everyTextHasAJapaneseAChineseAndAFrenchTranslation() throws Exception {
        List<String> missing = new ArrayList<>();
        for (String text : texts()) {
            if (!translations(PluginLocalizationBundle.JA).containsKey(text)) {
                missing.add("ja: " + text);
            }
            if (!translations(PluginLocalizationBundle.ZH).containsKey(text)) {
                missing.add("zh: " + text);
            }
            if (!translations(PluginLocalizationBundle.FR).containsKey(text)) {
                missing.add("fr: " + text);
            }
            if (!translations(PluginLocalizationBundle.ES).containsKey(text)) {
                missing.add("es: " + text);
            }
            if (!translations(PluginLocalizationBundle.DE).containsKey(text)) {
                missing.add("de: " + text);
            }
            if (!translations(PluginLocalizationBundle.KO).containsKey(text)) {
                missing.add("ko: " + text);
            }
        }
        assertTrue(missing.isEmpty(), "Missing translations:\n" + String.join("\n", missing));
    }

    @Test
    void noTranslationIsLeftForATextThatIsGone() throws Exception {
        Set<String> texts = new HashSet<>(texts());
        for (Object language : new Object[]{PluginLocalizationBundle.JA, PluginLocalizationBundle.ZH, PluginLocalizationBundle.FR, PluginLocalizationBundle.ES, PluginLocalizationBundle.DE, PluginLocalizationBundle.KO}) {
            for (String key : translations(language).keySet()) {
                assertTrue(texts.contains(key), "A translation of a text that is not a constant: " + key);
            }
        }
    }

    @Test
    void aTranslationKeepsTheValuesOfItsText() throws Exception {
        for (Object language : new Object[]{PluginLocalizationBundle.JA, PluginLocalizationBundle.ZH, PluginLocalizationBundle.FR, PluginLocalizationBundle.ES, PluginLocalizationBundle.DE, PluginLocalizationBundle.KO}) {
            for (Map.Entry<String, String> entry : translations(language).entrySet()) {
                assertEquals(placeholders(entry.getKey()), placeholders(entry.getValue()),
                        "The values of the text differ in its translation: " + entry.getKey() + " -> " + entry.getValue());
                assertFalse(entry.getValue().isBlank(), "Empty translation of " + entry.getKey());
            }
        }
    }
}
