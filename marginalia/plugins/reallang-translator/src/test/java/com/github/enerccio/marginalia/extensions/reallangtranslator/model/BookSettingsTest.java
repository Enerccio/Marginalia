package com.github.enerccio.marginalia.extensions.reallangtranslator.model;

import com.github.enerccio.marginalia.extensions.reallangtranslator.store.RealStore;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.BookLanguage;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.CharacterNote;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The languages of a book: the one it names first and the others, each with its own speakers, and what the book keeps of them.
 */
class BookSettingsTest {

    private static BookSettings elvesAndDwarves() {
        BookSettings settings = new BookSettings();
        settings.setLanguageId("elf");
        settings.getSpeakers().getCharacters().add(new CharacterNote("Mara"));
        BookLanguage dwarf = new BookLanguage("dwarf");
        dwarf.getSpeakers().getCharacters().add(new CharacterNote("Brog"));
        settings.getOtherLanguages().add(dwarf);
        return settings;
    }

    @Test
    void theBookNamesItsFirstLanguageThenTheOthers() {
        BookSettings settings = elvesAndDwarves();
        settings.getOtherLanguages().add(new BookLanguage("elf"));
        settings.getOtherLanguages().add(new BookLanguage(null));
        settings.getOtherLanguages().add(new BookLanguage("orc"));

        assertEquals(java.util.List.of("elf", "dwarf", "orc"), settings.languageIds());
    }

    @Test
    void eachLanguageHasItsOwnSpeakers() {
        BookSettings settings = elvesAndDwarves();

        assertNotNull(settings.speakersOf("elf").find("Mara"));
        assertNull(settings.speakersOf("elf").find("Brog"));
        assertNotNull(settings.speakersOf("dwarf").find("Brog"));
        assertNull(settings.speakersOf("orc"));
        assertNull(settings.speakersOf(null));
    }

    @Test
    void aBookWithoutAnyLanguageHasNone() {
        assertTrue(new BookSettings().languageIds().isEmpty());
        assertTrue(new BookSettings().getOtherLanguages().isEmpty());
    }

    @Test
    void theOtherLanguagesAndTheirSpeakersAreStoredInTheBook() {
        JsonObject attributes = new JsonObject();
        RealStore.writeBook(attributes, elvesAndDwarves());

        BookSettings read = RealStore.readBook(attributes);

        assertEquals(java.util.List.of("elf", "dwarf"), read.languageIds());
        assertNotNull(read.speakersOf("dwarf").find("Brog"));
    }

    @Test
    void aBookStoredBeforeTheOtherLanguagesExistedReadsWithNone() {
        JsonObject attributes = new JsonObject();
        JsonObject old = new JsonObject();
        old.addProperty("languageId", "elf");
        attributes.add(BookSettings.KEY, old);

        BookSettings read = RealStore.readBook(attributes);

        assertEquals(java.util.List.of("elf"), read.languageIds());
        assertTrue(read.getOtherLanguages().isEmpty());
    }
}
