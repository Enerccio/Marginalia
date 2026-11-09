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

    @Test
    void theGlossaryOfALanguageComesBackWithTheLanguage() {
        JsonObject attributes = new JsonObject();
        RealLanguage language = Presets.make("ja-kana-kanji");
        language.getGlossary().add(new GlossaryEntry("witch", GlossaryKind.WRITTEN, "魔女", "ウィッチ", "gikun"));
        RealStore.save(attributes, language);

        GlossaryEntry read = RealStore.find(attributes, language.getId()).getGlossary().getFirst();

        assertEquals("witch", read.getSource());
        assertEquals("ウィッチ", read.getReading());
        assertTrue(read.isEnabled());
    }

    @Test
    void theGlossaryOfABookIsKeptForEachLanguageOfTheBook() {
        JsonObject attributes = new JsonObject();
        BookSettings settings = new BookSettings();
        GlossaryEntry proposed = new GlossaryEntry("Saellen", GlossaryKind.WRITTEN, "サエレン", "", "");
        proposed.setProposed(true);
        settings.getGlossary("ja").add(proposed);
        settings.getGlossary("cs").add(new GlossaryEntry("Saellen", GlossaryKind.KEEP, "", "", ""));
        RealStore.writeBook(attributes, settings);

        BookSettings read = RealStore.readBook(attributes);

        assertEquals("サエレン", read.getGlossary("ja").getFirst().getWritten());
        assertTrue(read.getGlossary("ja").getFirst().isProposed());
        assertEquals(GlossaryKind.KEEP, read.getGlossary("cs").getFirst().getKind());
        assertTrue(read.getGlossary("other").isEmpty());
    }

    @Test
    void theGlossaryOfAFolderIsKeptForEachLanguageAndGoneWhenEmpty() {
        JsonObject attributes = new JsonObject();
        RealStore.writeFolderGlossary(attributes, "ja", java.util.List.of(new GlossaryEntry("Silvakhor", GlossaryKind.WRITTEN, "シルヴァコール", "", "")));
        RealStore.writeFolderGlossary(attributes, "cs", java.util.List.of(new GlossaryEntry("Silvakhor", GlossaryKind.KEEP, "", "", "")));

        assertEquals("シルヴァコール", RealStore.readFolderGlossary(attributes, "ja").getFirst().getWritten());
        assertEquals(GlossaryKind.KEEP, RealStore.readFolderGlossary(attributes, "cs").getFirst().getKind());
        assertTrue(RealStore.readFolderGlossary(attributes, "de").isEmpty());

        RealStore.writeFolderGlossary(attributes, "ja", java.util.List.of());
        RealStore.writeFolderGlossary(attributes, "cs", java.util.List.of());
        assertFalse(attributes.has(RealStore.FOLDER_GLOSSARY_KEY), "nothing is left in the folder when its glossary is empty");
    }
    @Test
    void theExtractionPromptsKeepTheirOrderAndAnUnreadableOneIsLeftOut() {
        JsonObject attributes = new JsonObject();
        RealStore.writePrompts(attributes, java.util.List.of(new ExtractionPrompt("a", "First", "text one"), new ExtractionPrompt("b", "Second", "text two")));
        attributes.getAsJsonArray(RealStore.PROMPTS_KEY).add("not a prompt");

        var read = RealStore.readPrompts(attributes);

        assertEquals(java.util.List.of("a", "b"), read.stream().map(ExtractionPrompt::getId).toList());
        assertEquals("text two", read.get(1).getText());
    }

    @Test
    void noPromptsMeansNoKey() {
        JsonObject attributes = new JsonObject();
        RealStore.writePrompts(attributes, java.util.List.of(new ExtractionPrompt("a", "First", "text")));
        RealStore.writePrompts(attributes, java.util.List.of());

        assertFalse(attributes.has(RealStore.PROMPTS_KEY));
        assertTrue(RealStore.readPrompts(attributes).isEmpty());
        assertTrue(RealStore.readPrompts(null).isEmpty());
    }

    @Test
    void theExtractionOfABookIsKeptWithItsSettings() {
        JsonObject attributes = new JsonObject();
        BookSettings settings = new BookSettings();
        settings.getExtraction().setPromptId(ExtractionPrompts.JAPANESE);
        settings.getExtraction().setSpecification("  Leave out the minor characters.  ");
        RealStore.writeBook(attributes, settings);

        BookSettings read = RealStore.readBook(attributes);

        assertEquals(ExtractionPrompts.JAPANESE, read.getExtraction().getPromptId());
        assertEquals("Leave out the minor characters.", read.getExtraction().getSpecification());
        assertNull(RealStore.readBook(new JsonObject()).getExtraction().getPromptId(), "a book that never extracted has no prompt");
    }
}
