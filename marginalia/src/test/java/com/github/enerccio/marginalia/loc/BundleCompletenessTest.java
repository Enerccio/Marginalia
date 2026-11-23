package com.github.enerccio.marginalia.loc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every bundle shipped with the application has all keys, of the platform's {@link L} and of the application's
 * {@link M_L}, and the format placeholders ({@code %s}, {@code %d}) of the translations match the English text,
 * otherwise a translation fails when the text is formatted.
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

    static Stream<Arguments> bundles() {
        return Stream.of(
                Arguments.of("ja", new LocalizationBundleJP(), new MarginaliaLocalizationBundleJP()),
                Arguments.of("fr", new LocalizationBundleFR(), new MarginaliaLocalizationBundleFR()),
                Arguments.of("es", new LocalizationBundleES(), new MarginaliaLocalizationBundleES()),
                Arguments.of("de", new LocalizationBundleDE(), new MarginaliaLocalizationBundleDE()),
                Arguments.of("ko", new LocalizationBundleKO(), new MarginaliaLocalizationBundleKO()),
                Arguments.of("it", new LocalizationBundleIT(), new MarginaliaLocalizationBundleIT()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bundles")
    void hasEveryKeyAndMatchingPlaceholders(String code, LocalizationBundle platform, TextBundle<M_L> application) {
        assertEquals(Language.of(code), platform.getLanguage());
        assertEquals(Language.of(code), application.getLanguage());
        check(new LocalizationBundleEN(), platform);
        check(new MarginaliaLocalizationBundleEN(), application);
    }

    @Test
    void englishHasEveryKey() {
        for (L key : L.values()) {
            assertNotNull(new LocalizationBundleEN().getValue(key), key.name());
        }
        for (M_L key : M_L.values()) {
            assertNotNull(new MarginaliaLocalizationBundleEN().getValue(key), key.name());
        }
    }

    private static <K extends Enum<K> & LocalizationKey> void check(TextBundle<K> en, TextBundle<K> bundle) {
        for (K key : bundle.getKeys().getEnumConstants()) {
            String text = bundle.getValue(key);
            assertNotNull(text, key.name());
            assertFalse(text.isBlank(), key.name());
            assertEquals(placeholders(en.getValue(key)), placeholders(text), key.name());
        }
    }
}
