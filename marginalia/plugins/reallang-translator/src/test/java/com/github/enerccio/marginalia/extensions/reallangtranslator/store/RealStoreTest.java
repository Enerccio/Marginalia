package com.github.enerccio.marginalia.extensions.reallangtranslator.store;

import com.github.enerccio.marginalia.extensions.reallangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.CharacterNote;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What the plugin keeps in attributes comes back as it went in, and something unreadable never takes the rest with it.
 */
class RealStoreTest {

    @Test
    void theLibraryKeepsItsLanguagesAndTheirOrder() {
        JsonObject attributes = new JsonObject();
        RealLanguage first = Presets.make("ja-kana-kanji");
        RealLanguage second = Presets.make("ja-romaji");

        RealStore.save(attributes, first);
        RealStore.save(attributes, second);

        var read = RealStore.readLibrary(attributes);
        assertEquals(2, read.size());
        assertEquals(first.getId(), read.get(0).getId());
        assertEquals(Script.ROMAJI, read.get(1).getScript());
        assertEquals("Japanese", read.get(0).getTongue());
        assertEquals(first.getPoliteness(), read.get(0).getPoliteness());
    }

    @Test
    void savingAgainReplacesTheLanguageAndKeepsItsPlace() {
        JsonObject attributes = new JsonObject();
        RealLanguage first = Presets.make("ja-kana-kanji");
        RealStore.save(attributes, first);
        RealStore.save(attributes, Presets.make("ja-kana"));

        first.setName("Renamed");
        RealStore.save(attributes, first);

        var read = RealStore.readLibrary(attributes);
        assertEquals(2, read.size());
        assertEquals("Renamed", read.getFirst().getName());
        assertEquals("Renamed", RealStore.find(attributes, first.getId()).getName());
    }

    @Test
    void deletingTakesTheLanguageOut() {
        JsonObject attributes = new JsonObject();
        RealLanguage language = Presets.make("ja-kana-kanji");
        RealStore.save(attributes, language);

        assertTrue(RealStore.delete(attributes, language.getId()));
        assertFalse(RealStore.delete(attributes, language.getId()));
        assertNull(RealStore.find(attributes, language.getId()));
        assertNull(RealStore.find(attributes, null));
    }

    @Test
    void anUnreadableLanguageDoesNotTakeTheLibraryWithIt() {
        JsonObject attributes = new JsonObject();
        RealStore.save(attributes, Presets.make("ja-kana-kanji"));
        attributes.getAsJsonArray(RealStore.LIBRARY_KEY).add("not a language");
        attributes.getAsJsonArray(RealStore.LIBRARY_KEY).add(new JsonObject());

        assertEquals(1, RealStore.readLibrary(attributes).stream().filter(language -> language.getName().startsWith("Japanese")).count());
    }

    @Test
    void aBookKeepsItsSettingsAndSpeakers() {
        JsonObject attributes = new JsonObject();
        BookSettings settings = new BookSettings();
        settings.setContextParts(4);
        settings.getSpeakers().getCharacters().add(new CharacterNote("Mara"));

        RealStore.writeBook(attributes, settings);

        BookSettings read = RealStore.readBook(attributes);
        assertEquals(4, read.getContextParts());
        assertEquals("Mara", read.getSpeakers().getCharacters().getFirst().getName());
    }

    @Test
    void aBookWithNothingGetsTheDefaults() {
        assertEquals(2, RealStore.readBook(new JsonObject()).getContextParts());
        assertEquals(2, RealStore.readBook(null).getContextParts());
        JsonObject broken = new JsonObject();
        broken.addProperty(BookSettings.KEY, "text");
        assertEquals(2, RealStore.readBook(broken).getContextParts());
    }

    @Test
    void theDefaultsOfTheUserComeBack() {
        JsonObject attributes = new JsonObject();
        UserDefaults defaults = new UserDefaults();
        defaults.setLanguageId("lang-1");
        defaults.setAuto(true);
        defaults.setAiUuid("ai-1");

        RealStore.writeDefaults(attributes, defaults);

        UserDefaults read = RealStore.readDefaults(attributes);
        assertEquals("lang-1", read.getLanguageId());
        assertTrue(read.isAuto());
        assertEquals("ai-1", read.getAiUuid());
        assertNull(read.getProtocolUuid());
        assertNull(RealStore.readDefaults(new JsonObject()).getLanguageId());
    }

    @Test
    void aCopyIsIndependentButKeepsTheIdentity() {
        RealLanguage language = Presets.make("ja-kana-kanji");

        RealLanguage copy = RealStore.copy(language);
        copy.setName("Other");

        assertEquals(language.getId(), copy.getId());
        assertNotEquals(language.getName(), copy.getName());
    }
}
