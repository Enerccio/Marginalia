package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.AnswerChecker.Problem;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.AnswerChecker.Word;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationAnswer.NewWord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The words the model reported go into the lexicon at once, new, in the category, register and kind it named.
 */
class NewWordsTest {

    private static Word word(String form, String gloss, String category, String register, String kind, String variantOf) {
        return new Word(new NewWord(form, gloss, category, "because of " + form, register, kind, variantOf), List.<Problem>of());
    }

    @Test
    void aReportedWordIsAddedNewWithItsRationaleAndOrigin() {
        Language language = TestLanguages.vrakht();
        Origin origin = Origin.model("part-1", "some-model", 99L);

        List<LexiconEntry> added = NewWords.addTo(language, List.of(word("eiz-blod", " veteran ", "People & Social", "", "compound", "")), origin);

        LexiconEntry entry = added.getFirst();
        assertSame(entry, language.getLexicon().getLast());
        assertEquals("eiz-blod", entry.getWord());
        assertEquals("veteran", entry.getSenses().getFirst().getGloss());
        assertEquals(EntryStatus.NEW, entry.getStatus());
        assertEquals(EntryKind.COMPOUND, entry.getKind());
        assertEquals("because of eiz-blod", entry.getRationale());
        assertEquals("part-1", entry.getOrigin().getPart());
        assertEquals(language.getCategories().get(2).getId(), entry.getCategory());
        assertEquals(3, language.getCategories().size(), "an existing category is found by its name, ignoring case");
    }

    @Test
    void aCategoryNobodyHasIsMadeAndAnUnknownKindIsOther() {
        Language language = TestLanguages.vrakht();

        LexiconEntry entry = NewWords.addTo(language, List.of(word("sud", "south", "Directions", "", "wonder", "")), Origin.model("p", "m", 1L)).getFirst();

        assertEquals(4, language.getCategories().size());
        assertEquals("Directions", language.findCategory(entry.getCategory()).getName());
        assertEquals(EntryKind.OTHER, entry.getKind());
    }

    @Test
    void theRegisterIsFoundByNameAndAnUnknownOneIsNeutral() {
        Language language = TestLanguages.aethel();

        List<LexiconEntry> added = NewWords.addTo(language, List.of(word("lelith", "breast", "Body", "Vulgar", "root", ""),
                word("sael", "blood", "Body", "colloquial", "", "")), Origin.model("p", "m", 1L));

        assertEquals(language.getRegisters().get(2).getId(), added.get(0).getRegister());
        assertNull(added.get(1).getRegister());
        assertEquals(EntryKind.ROOT, added.get(1).getKind());
    }

    @Test
    void aWordThatIsAnotherRegisterOfAWordIsLinkedToIt() {
        Language language = TestLanguages.aethel();
        LexiconEntry mela = language.findByForm("mela").getFirst();

        LexiconEntry added = NewWords.addTo(language, List.of(word("melith", "breast", "Body", "soft", "", "mela")), Origin.model("p", "m", 1L)).getFirst();

        assertEquals(List.of(mela.getId()), added.getVariants());
        assertTrue(language.variantGroup(mela).contains(added), "the link works from either side");
    }
}
