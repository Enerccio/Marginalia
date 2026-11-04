package com.github.enerccio.marginalia.extensions.conlangtranslator.store;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The library of languages in the attributes of the user settings, and the document a language is shared as.
 */
class ConlangStoreTest {

    private static Language vrakht() {
        Language language = new Language("Vrakht");
        language.setDescription("An imperial tongue");
        RuleSection grammar = new RuleSection("Grammar", "Mood particle first, then roles.");
        RuleSection notes = new RuleSection("Notes", "private", Audience.NONE);
        language.getRules().addAll(List.of(grammar, notes));
        Category war = new Category("Military & War");
        war.setNote("the legion and the siege");
        war.setAlwaysSend(true);
        language.getCategories().add(war);
        Register formal = new Register("formal", "court speech");
        language.getRegisters().add(formal);
        LexiconEntry drak = LexiconEntry.ofAuthor("drak", "war");
        drak.getSenses().add(new Sense("fight", "verb"));
        drak.setCategory(war.getId());
        drak.setRegister(formal.getId());
        drak.setStatus(EntryStatus.LOCKED);
        drak.setKind(EntryKind.ROOT);
        language.addEntry(drak);
        LexiconEntry invented = LexiconEntry.ofModel("eiz-blod", "veteran", "iron + lineage", Origin.model("p1", "m1", 42L));
        invented.setKind(EntryKind.COMPOUND);
        invented.setRule("Rule 3");
        invented.setCategory(war.getId());
        invented.getVariants().add(drak.getId());
        language.addEntry(invented);
        CharacterNote mara = new CharacterNote("Mara");
        mara.setGender("a woman");
        mara.setListed(true);
        language.getSpeakers().getCharacters().add(mara);
        language.getSpeakers().setMode(SpeakersMode.ONLY);
        language.setHyphenStructural(true);
        language.setExampleBook("book-uuid");
        language.setExampleLorebook("lore-uuid");
        return language;
    }

    @Test
    void aLanguageSurvivesTheLibraryWithEverythingInIt() {
        JsonObject attributes = new JsonObject();
        Language original = vrakht();

        ConlangStore.save(attributes, original);
        Language read = ConlangStore.find(attributes, original.getId());

        assertEquals("Vrakht", read.getName());
        assertEquals("An imperial tongue", read.getDescription());
        assertEquals(2, read.getRules().size());
        assertEquals(Audience.NONE, read.getRules().get(1).getAudience());
        assertTrue(read.getCategories().getFirst().isAlwaysSend());
        assertEquals("the legion and the siege", read.getCategories().getFirst().getNote());
        assertEquals("formal", read.getRegisters().getFirst().getName());
        LexiconEntry drak = read.getLexicon().getFirst();
        assertEquals(EntryStatus.LOCKED, drak.getStatus());
        assertEquals(2, drak.getSenses().size());
        assertEquals("verb", drak.getSenses().get(1).getPartOfSpeech());
        assertEquals(read.getRegisters().getFirst().getId(), drak.getRegister());
        LexiconEntry invented = read.getLexicon().get(1);
        assertEquals(EntryKind.COMPOUND, invented.getKind());
        assertEquals(EntryStatus.NEW, invented.getStatus());
        assertEquals(OriginKind.MODEL, invented.getOrigin().getKind());
        assertEquals("p1", invented.getOrigin().getPart());
        assertEquals(42L, invented.getOrigin().getTime());
        assertEquals("iron + lineage", invented.getRationale());
        assertEquals("Rule 3", invented.getRule());
        assertEquals(List.of(drak.getId()), invented.getVariants());
        assertEquals(SpeakersMode.ONLY, read.getSpeakers().getMode());
        assertTrue(read.getSpeakers().speaks("mara"));
        assertEquals("a woman", read.getSpeakers().find("Mara").getGender());
        assertTrue(read.isHyphenStructural());
        assertEquals("book-uuid", read.getExampleBook());
        assertEquals("lore-uuid", read.getExampleLorebook());
    }

    @Test
    void savingAgainReplacesTheLanguageInItsPlaceAndANewOneIsAdded() {
        JsonObject attributes = new JsonObject();
        Language first = new Language("One");
        Language second = new Language("Two");
        ConlangStore.save(attributes, first);
        ConlangStore.save(attributes, second);

        first.setName("One, renamed");
        ConlangStore.save(attributes, first);

        List<Language> library = ConlangStore.readLibrary(attributes);
        assertEquals(List.of("One, renamed", "Two"), library.stream().map(Language::getName).toList());
    }

    @Test
    void deleteRemovesTheLanguageAndTellsWhetherItWasThere() {
        JsonObject attributes = new JsonObject();
        Language language = new Language("One");
        ConlangStore.save(attributes, language);

        assertTrue(ConlangStore.delete(attributes, language.getId()));
        assertFalse(ConlangStore.delete(attributes, language.getId()));
        assertNull(ConlangStore.find(attributes, language.getId()));
        assertTrue(ConlangStore.readLibrary(attributes).isEmpty());
    }

    @Test
    void aUserWithNoLibraryHasAnEmptyOne() {
        assertTrue(ConlangStore.readLibrary(null).isEmpty());
        assertTrue(ConlangStore.readLibrary(new JsonObject()).isEmpty());
        assertNull(ConlangStore.find(new JsonObject(), "x"));
        assertNull(ConlangStore.find(new JsonObject(), null));
        JsonObject wrong = new JsonObject();
        wrong.addProperty(ConlangStore.LIBRARY_KEY, "text");
        assertTrue(ConlangStore.readLibrary(wrong).isEmpty());
    }

    @Test
    void aLanguageThatCanNotBeReadDoesNotTakeTheLibraryWithIt() {
        JsonObject attributes = new JsonObject();
        ConlangStore.save(attributes, new Language("Good"));
        JsonArray array = attributes.getAsJsonArray(ConlangStore.LIBRARY_KEY);
        array.add("a string where a language should be");
        JsonObject noId = new JsonObject();
        noId.addProperty("name", "no id");
        noId.add("id", com.google.gson.JsonNull.INSTANCE);
        array.add(noId);

        assertEquals(List.of("Good"), ConlangStore.readLibrary(attributes).stream().map(Language::getName).toList());
    }

    @Test
    void theOtherDataOfTheUserIsLeftAlone() {
        JsonObject attributes = new JsonObject();
        attributes.addProperty("other-plugin", "kept");

        ConlangStore.save(attributes, new Language("One"));

        assertEquals("kept", attributes.get("other-plugin").getAsString());
    }

    @Test
    void anExportedDocumentIsImportedAsANewLanguageWithoutTheLinks() {
        Language original = vrakht();

        String document = ConlangStore.export(original);
        Language imported = ConlangStore.importDocument(document);

        assertFalse(document.contains("book-uuid"));
        assertFalse(document.contains("lore-uuid"));
        assertNotEquals(original.getId(), imported.getId());
        assertNull(imported.getExampleBook());
        assertTrue(imported.isHyphenStructural());
        assertEquals("Vrakht", imported.getName());
        assertEquals(2, imported.getLexicon().size());
        assertEquals("iron + lineage", imported.getLexicon().get(1).getRationale());
        assertEquals("a woman", imported.getSpeakers().find("Mara").getGender());
    }

    @Test
    void textThatIsNotALanguageIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> ConlangStore.importDocument("not json {"));
        assertThrows(IllegalArgumentException.class, () -> ConlangStore.importDocument("{}"));
        assertThrows(IllegalArgumentException.class, () -> ConlangStore.importDocument(""));
    }

    @Test
    void aDocumentOfANewerFormatIsRefused() {
        String newer = "{\"format\": " + (Language.FORMAT + 1) + ", \"name\": \"From the future\"}";

        assertThrows(IllegalArgumentException.class, () -> ConlangStore.importDocument(newer));
    }
}
