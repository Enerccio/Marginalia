package com.github.enerccio.marginalia.loc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LocalizationOverrideTest {

    private static final Language DE = Language.of("de");

    private LocalizationImpl localization;

    @BeforeEach
    void setUp() {
        localization = new LocalizationImpl();
        localization.setBundles(List.of(new LocalizationBundleEN(), new LocalizationBundleDE()));
        localization.afterPropertiesSet();
    }

    @Test
    void withoutOverridesTheBundlesSpeak() {
        assertThat(localization.getValue(DE, L.LABEL_CLOSE)).isEqualTo("Schließen");
        assertThat(localization.getValue(Language.EN, L.LABEL_CLOSE)).isEqualTo("Close");
    }

    @Test
    void textOfTheLanguageWinsThenEveryLanguageThenBundle() {
        LocalizationOverride override = new LocalizationOverride();
        override.setTexts(Map.of(
                "de", Map.of("L.LABEL_CLOSE", "Zu"),
                "*", Map.of("LABEL_CLOSE", "Shut", "L.LABEL_SAVE_LOGIN", "Keep me")));
        localization.setOverrides(List.of(override));

        assertThat(localization.getValue(DE, L.LABEL_CLOSE)).isEqualTo("Zu");
        assertThat(localization.getValue(Language.EN, L.LABEL_CLOSE)).isEqualTo("Shut");
        assertThat(localization.getValue(DE, L.LABEL_SAVE_LOGIN)).isEqualTo("Keep me");
        // a language without a bundle falls back to the default language, with its override
        assertThat(localization.getValue(Language.of("cs"), L.LABEL_CLOSE)).isEqualTo("Shut");
    }

    @Test
    void overrideOfTheDefaultLanguageIsTheFallback() {
        localization.setOverrides(List.of(new LocalizationOverride().text("en", L.LABEL_CLOSE, "Done")));

        assertThat(localization.getValue(DE, L.LABEL_CLOSE)).isEqualTo("Schließen");
        assertThat(localization.getValue(Language.of("cs"), L.LABEL_CLOSE)).isEqualTo("Done");
    }

    @Test
    void replacementsApplyToEveryTextOfTheLanguage() {
        LocalizationOverride override = new LocalizationOverride()
                .replace("*", "Marginalia", "Joi")
                .replace("de", "außerhalb von", "außer");
        localization.setOverrides(List.of(override));

        assertThat(localization.getValue(Language.EN, L.LABEL_USED_OUTSIDE_MARGINALIA)).isEqualTo("Also used outside Joi");
        assertThat(localization.getValue(DE, L.LABEL_USED_OUTSIDE_MARGINALIA)).isEqualTo("Auch außer Joi genutzt");
        // a language added later is covered by the replacement for every language
        localization.registerBundle(new LocalizationBundleFR());
        assertThat(localization.getValue(Language.of("fr"), L.LABEL_USED_OUTSIDE_MARGINALIA)).contains("Joi")
                .doesNotContain("Marginalia");
    }

    @Test
    void transformersRunLastAndParseValueSeesTheResult() {
        localization.setOverrides(List.of(new LocalizationOverride()
                .text("*", L.LABEL_CLOSE, "Close it")
                .transformer((language, key, text) -> text.toUpperCase())));

        assertThat(localization.getValue(Language.EN, L.LABEL_CLOSE)).isEqualTo("CLOSE IT");
        assertThat(localization.parseValue("CLOSE IT")).isEqualTo(L.LABEL_CLOSE);
    }
}
