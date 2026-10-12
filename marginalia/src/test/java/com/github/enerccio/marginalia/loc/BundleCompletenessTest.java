package com.github.enerccio.marginalia.loc;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every bundle shipped with the application has all keys, and the format placeholders ({@code %s}, {@code %d}) of
 * the translations match the English text, otherwise a translation fails when the text is formatted.
 */
class BundleCompletenessTest {

    private static final Pattern PLACEHOLDER = Pattern.compile("%[sd]");

    private static List<String> placeholders(String text) {
        Matcher matcher = PLACEHOLDER.matcher(text);
        List<String> found = new java.util.ArrayList<>();
        while (matcher.find()) {
            found.add(matcher.group());
        }
        found.sort(String::compareTo);
        return found;
    }

    @Test
    void japaneseHasEveryKeyAndMatchingPlaceholders() {
        LocalizationBundle en = new LocalizationBundleEN();
        LocalizationBundle jp = new LocalizationBundleJP();

        assertEquals(Language.of("ja"), jp.getLanguage());
        for (L key : L.values()) {
            String text = jp.getValue(key);
            assertNotNull(text, key.name());
            assertFalse(text.isBlank(), key.name());
            assertEquals(placeholders(en.getValue(key)), placeholders(text), key.name());
        }
    }
}
