package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The language: lookup, the rule that a homophone is normal and only a repeat is a fault, variants of one concept, deleting what others
 * point to, and the copies (duplicate, export, import).
 */
class LanguageTest {

    private Language language;
    private Category people;
    private Category war;

    @BeforeEach
    void setUp() {
        language = new Language("Vrakht");
        people = new Category("People & Social");
        war = new Category("Military & War");
        language.getCategories().addAll(List.of(people, war));
    }

    private LexiconEntry word(String form, String gloss, Category category) {
        LexiconEntry entry = LexiconEntry.ofAuthor(form, gloss);
        entry.setCategory(category == null ? null : category.getId());
        return language.addEntry(entry);
    }

    @Test
    void aFormIsFoundIgnoringCaseSpacesAndHyphens() {
        LexiconEntry compound = word("drak-karn", "battlefield", war);

        assertEquals(List.of(compound), language.findByForm("  Drak-Karn "));
        assertEquals(List.of(compound), language.findByForm("drakkarn"));
        assertTrue(language.findByForm("karn").isEmpty());
        assertTrue(language.findByForm(" ").isEmpty());
    }

    @Test
    void aHyphenCountsOnlyWhenTheLanguageSaysItIsStructural() {
        LexiconEntry compound = word("drak-karn", "battlefield", war);
        LexiconEntry plain = word("drakkarn", "camp", war);

        // off by default: the same word with and without the hyphen
        assertFalse(language.isHyphenStructural());
        assertEquals(List.of(compound, plain), language.findByForm("Drak-Karn"));

        language.setHyphenStructural(true);

        assertEquals(List.of(compound), language.findByForm(" Drak-Karn "));
        assertEquals(List.of(plain), language.findByForm("drakkarn"));
        assertTrue(language.findByForm("drak karn").isEmpty());
    }

    @Test
    void theRepeatsFollowTheHyphenSettingToo() {
        word("drak-karn", "camp", war);
        word("drakkarn", "camp", war);

        assertEquals(1, language.repeats().size());

        language.setHyphenStructural(true);

        assertTrue(language.repeats().isEmpty());
    }

    @Test
    void theSameFormWithAnotherMeaningIsAHomophoneAndNotAFault() {
        word("den", "because", people);
        word("den", "think", people);
        word("blod", "blood", people);
        LexiconEntry sense = word("blod", "lineage", people);
        sense.getSenses().add(new Sense("bleed"));

        assertEquals(2, language.findByForm("den").size());
        assertTrue(language.repeats().isEmpty());
    }

    @Test
    void theSameMeaningTwiceIsARepeat() {
        LexiconEntry first = word("gab", "give", people);
        LexiconEntry second = word("GAB", " Give ", war);
        word("gab", "gap", people);
        word("hab", "have", people);

        List<List<LexiconEntry>> repeats = language.repeats();

        assertEquals(1, repeats.size());
        assertEquals(List.of(first, second), repeats.getFirst());
    }

    @Test
    void entriesAreListedUnderTheirCategoryAndTheOnesWithoutAreTogether() {
        LexiconEntry a = word("mak", "make", people);
        LexiconEntry b = word("drak", "war", war);
        LexiconEntry loose = word("zeik", "sign", null);
        LexiconEntry stray = word("ord", "law", people);
        stray.setCategory("a-category-that-was-deleted");

        assertEquals(List.of(a), language.entriesOf(people.getId()));
        assertEquals(List.of(b), language.entriesOf(war.getId()));
        assertEquals(List.of(loose, stray), language.entriesOf(null));
    }

    @Test
    void variantsOfOneConceptAreFoundFromAnyOfThemAndInEitherDirection() {
        LexiconEntry formal = word("mela", "breast", people);
        LexiconEntry soft = word("melis", "breast", people);
        LexiconEntry vulgar = word("lelya", "breast", people);
        LexiconEntry other = word("dok", "door", people);
        formal.getVariants().add(soft.getId());
        soft.getVariants().add(vulgar.getId());

        assertEquals(List.of(formal, soft, vulgar), language.variantGroup(formal));
        assertEquals(List.of(formal, soft, vulgar), language.variantGroup(vulgar));
        assertEquals(List.of(other), language.variantGroup(other));
    }

    @Test
    void deletingAWordRemovesTheLinksToIt() {
        LexiconEntry formal = word("mela", "breast", people);
        LexiconEntry soft = word("melis", "breast", people);
        formal.getVariants().add(soft.getId());

        assertTrue(language.deleteEntry(soft.getId()));

        assertTrue(formal.getVariants().isEmpty());
        assertFalse(language.deleteEntry(soft.getId()));
        assertNull(language.findEntry(soft.getId()));
    }

    @Test
    void deletingACategoryOrARegisterKeepsTheWords() {
        Register formal = new Register("formal", "court speech");
        language.getRegisters().add(formal);
        LexiconEntry entry = word("mela", "breast", people);
        entry.setRegister(formal.getId());

        assertTrue(language.deleteCategory(people.getId()));
        assertTrue(language.deleteRegister(formal.getId()));

        assertNull(entry.getCategory());
        assertNull(entry.getRegister());
        assertSame(entry, language.findEntry(entry.getId()));
    }

    @Test
    void aNewWordGoesToTheEndAndIsNewUntilTheAuthorLooks() {
        word("mak", "make", people);
        LexiconEntry invented = language.addEntry(LexiconEntry.ofModel("eiz-blod", "veteran", "eiz 'iron' + blod 'lineage', the compound rule",
                Origin.model("part-3", "some-model", 1000L)));

        assertSame(invented, language.getLexicon().getLast());
        assertEquals(List.of(invented), language.newEntries());
        assertEquals(OriginKind.MODEL, invented.getOrigin().getKind());
        assertEquals("part-3", invented.getOrigin().getPart());

        invented.editedByAuthor();

        assertEquals(OriginKind.AUTHOR, invented.getOrigin().getKind());
        assertEquals(EntryStatus.ACCEPTED, invented.getStatus());
        assertTrue(language.newEntries().isEmpty());
    }

    @Test
    void anEditKeepsALockedWordLockedAndARetiredWordIsNotOffered() {
        LexiconEntry locked = word("mak", "make", people);
        locked.setStatus(EntryStatus.LOCKED);
        locked.editedByAuthor();
        LexiconEntry retired = word("old", "old", people);
        retired.setStatus(EntryStatus.RETIRED);

        assertTrue(locked.isLocked());
        assertTrue(locked.isOffered());
        assertFalse(retired.isOffered());
    }

    @Test
    void aDuplicateIsIndependentAndKeepsTheLinks() {
        language.setExampleBook("book-uuid");
        language.setExampleLorebook("lore-uuid");
        LexiconEntry entry = word("mak", "make", people);

        Language copy = language.duplicate("Vrakht 2");

        assertNotEquals(language.getId(), copy.getId());
        assertEquals("Vrakht 2", copy.getName());
        assertEquals("book-uuid", copy.getExampleBook());
        assertEquals("lore-uuid", copy.getExampleLorebook());
        assertEquals(1, copy.getLexicon().size());
        copy.getLexicon().getFirst().setWord("changed");
        copy.findCategory(people.getId()).setName("other");
        assertEquals("mak", entry.getWord());
        assertEquals("People & Social", people.getName());
    }

    @Test
    void anExportDropsTheLinksAndAnImportGetsItsOwnId() {
        language.setExampleBook("book-uuid");
        language.setExampleLorebook("lore-uuid");
        word("mak", "make", people);

        Language exported = language.forExport();
        Language imported = exported.forImport();

        assertNull(exported.getExampleBook());
        assertNull(exported.getExampleLorebook());
        assertEquals(language.getId(), exported.getId());
        assertNotEquals(language.getId(), imported.getId());
        assertEquals("Vrakht", imported.getName());
        assertEquals(1, imported.getLexicon().size());
        // the language itself is untouched
        assertEquals("book-uuid", language.getExampleBook());
    }

    @Test
    void aLanguageFromNothingHasNoNullLists() {
        Language empty = new Language();

        assertTrue(empty.getRules().isEmpty());
        assertTrue(empty.getLexicon().isEmpty());
        assertTrue(empty.getCategories().isEmpty());
        assertTrue(empty.getRegisters().isEmpty());
        assertEquals("", empty.getName());
    }

    @Test
    void ruleSectionsKeepTheirAudience() {
        RuleSection grammar = new RuleSection("Grammar", "SOV");
        RuleSection history = new RuleSection("History", "old", Audience.DESIGNER);
        language.getRules().addAll(List.of(grammar, history));

        assertEquals(Audience.MODEL, grammar.getAudience());
        assertSame(history, language.findRule(history.getId()));
        assertNull(language.findRule("nothing"));
    }
}
