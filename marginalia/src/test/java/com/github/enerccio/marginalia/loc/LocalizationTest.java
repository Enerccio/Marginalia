package com.github.enerccio.marginalia.loc;

import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class LocalizationTest extends MarginaliaTestBase {

    private static final Language CS = Language.of("cs");

    @Autowired
    private Localization loc;

    @Autowired
    private SessionPoint sessionPoint;

    @Autowired
    private SettingService settingService;

    /** Czech bundle with a single text, the others have to come from the default language. */
    private static class PartialBundle extends LocalizationBundleBase {
        private final Language language;

        PartialBundle(Language language) {
            this.language = language;
        }

        @Override
        protected void loadMessages() {
            setValue(L.LABEL_YES, "Ano");
        }

        @Override
        public Language getLanguage() {
            return language;
        }

        @Override
        public String getDisplayName() {
            return "Čeština";
        }

        @Override
        public Locale getLocale() {
            return Locale.of("cs", "CZ");
        }

        @Override
        public SimpleDateFormat getDateFormat() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }

        @Override
        public SimpleDateFormat getHourFormat() {
            return new SimpleDateFormat("HH:mm:ss");
        }

        @Override
        public SimpleDateFormat getDateHourFormat() {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
    }

    @Test
    void sessionWithoutLanguageUsesDefault() {
        assertEquals(Language.EN, loc.getDefaultLanguage());
        assertEquals(Language.EN, loc.getCurrentLanguage());
        assertEquals("Yes", loc.getValue(L.LABEL_YES));
        assertTrue(loc.getAvailableLanguages().contains(Language.EN));
        assertEquals("English", loc.getDisplayName(Language.EN));
    }

    @Test
    void sessionLanguageSelectsBundleAndMissingTextsFallBack() {
        PartialBundle bundle = new PartialBundle(CS);
        loc.registerBundle(bundle);
        try {
            assertTrue(loc.getAvailableLanguages().contains(CS));
            assertEquals(CS, loc.resolveLanguage("CS"));

            sessionPoint.setLanguage(CS);
            assertEquals(CS, loc.getCurrentLanguage());
            assertEquals("Ano", loc.getValue(L.LABEL_YES));
            assertEquals("No", loc.getValue(L.LABEL_NO));
            assertEquals("cs", loc.getLocale().getLanguage());
            assertEquals(L.LABEL_YES, loc.parseValue("Ano"));
        } finally {
            loc.unregisterBundle(bundle);
            sessionPoint.setLanguage(null);
        }
    }

    @Test
    void removedBundleFallsBackToDefault() {
        PartialBundle bundle = new PartialBundle(CS);
        loc.registerBundle(bundle);
        sessionPoint.setLanguage(CS);
        loc.unregisterBundle(bundle);
        try {
            assertFalse(loc.getAvailableLanguages().contains(CS));
            assertEquals(Language.EN, loc.getCurrentLanguage());
            assertEquals("Yes", loc.getValue(L.LABEL_YES));
            assertEquals(Language.EN, loc.resolveLanguage("cs"));
        } finally {
            sessionPoint.setLanguage(null);
        }
    }

    @Test
    void languageWithBundleCantBeRegisteredTwice() {
        assertThrows(IllegalArgumentException.class, () -> loc.registerBundle(new PartialBundle(Language.EN)));

        PartialBundle bundle = new PartialBundle(CS);
        loc.registerBundle(bundle);
        try {
            assertThrows(IllegalArgumentException.class, () -> loc.registerBundle(new PartialBundle(CS)));
        } finally {
            loc.unregisterBundle(bundle);
        }
    }

    @Test
    void defaultLanguageCantBeUnregistered() {
        loc.unregisterBundle(new LocalizationBundleEN());
        assertEquals("Yes", loc.getValue(L.LABEL_YES));
        assertTrue(loc.getAvailableLanguages().contains(Language.EN));
    }

    @Test
    void userLanguageIsSaved() throws Exception {
        login();
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        assertNull(setting.getLanguage());
        setting.setLanguage("cs");
        settingService.save(setting);

        assertEquals("cs", settingService.getOrCreate(UserSetting.class).getLanguage());
    }

    @Test
    void promptLanguageIsEnglishWithoutSetting() {
        assertEquals(Language.EN, loc.getPromptLanguage());
        assertEquals(Language.EN, loc.getDefaults().getLanguage());
        assertEquals(java.util.List.of(Language.EN, Language.of("ja"), Language.of("fr"),
                Language.of("es"), Language.of("de"), Language.of("it"), Language.of("ko")), loc.getPromptLanguages());
        assertEquals(Language.EN, loc.resolvePromptLanguage(null));
        assertEquals(Language.EN, loc.resolvePromptLanguage("cs"));
        assertEquals(Language.of("fr"), loc.resolvePromptLanguage("fr"));
        // Chinese comes with the Chinese localization extension, not with the application
        assertEquals(Language.EN, loc.resolvePromptLanguage("zh"));
    }

    @Test
    void sessionPromptLanguageSelectsDefaultsApartFromTheInterface() {
        try {
            sessionPoint.setPromptLanguage(Language.of("ja"));
            assertEquals(Language.of("ja"), loc.getPromptLanguage());
            assertEquals(new com.github.enerccio.marginalia.DefaultsJA().masterTemplate(), loc.getDefaults().masterTemplate());
            assertEquals(loc.getDefaults().masterTemplate(),
                    com.github.enerccio.marginalia.domain.service.PromptField.MASTER_TEMPLATE.fallback(loc.getDefaults()));
            // the interface stays English
            assertEquals("Yes", loc.getValue(L.LABEL_YES));
            // a language without prompts falls back to English
            sessionPoint.setPromptLanguage(CS);
            assertEquals(Language.EN, loc.getPromptLanguage());
            assertEquals(new com.github.enerccio.marginalia.Defaults().masterTemplate(), loc.getDefaults().masterTemplate());
        } finally {
            sessionPoint.setPromptLanguage(null);
        }
        assertEquals("Français", loc.getDefaults(Language.of("fr")).getDisplayName());
        assertEquals(Language.EN, loc.getDefaults(CS).getLanguage());
    }

    @Test
    void anExtensionAddsAndTakesAwayAPromptLanguage() {
        com.github.enerccio.marginalia.Defaults czech = new com.github.enerccio.marginalia.Defaults() {
            @Override
            public Language getLanguage() {
                return CS;
            }

            @Override
            public String getDisplayName() {
                return "Čeština";
            }
        };
        loc.registerDefaults(czech);
        try {
            assertEquals(CS, loc.resolvePromptLanguage("cs"));
            assertEquals(CS, loc.getPromptLanguages().getLast());
            assertEquals("Čeština", loc.getDefaults(CS).getDisplayName());
            assertTrue(loc.getAllDefaults().contains(czech));
            // a language has one set of prompts
            assertThrows(IllegalArgumentException.class, () -> loc.registerDefaults(czech));
        } finally {
            loc.unregisterDefaults(czech);
        }
        assertEquals(Language.EN, loc.resolvePromptLanguage("cs"));
        assertFalse(loc.getAllDefaults().contains(czech));
        // the prompts of the application can't be taken away
        loc.unregisterDefaults(loc.getDefaults(Language.of("ja")));
        assertEquals(Language.of("ja"), loc.resolvePromptLanguage("ja"));
    }

    @Test
    void userPromptLanguageIsSaved() throws Exception {
        login();
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        assertNull(setting.getPromptLanguage());
        setting.setPromptLanguage("ja");
        settingService.save(setting);

        assertEquals("ja", settingService.getOrCreate(UserSetting.class).getPromptLanguage());
    }

    @Test
    void allKeysHaveEnglishText() {
        LocalizationBundleEN en = new LocalizationBundleEN();
        for (L key : L.values()) {
            assertNotNull(en.getValue(key), key.name());
        }
    }
}
