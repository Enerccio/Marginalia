package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.EntryStatus;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.LexiconEntry;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The lexicon as the translator reads it: grouped under the categories, one line for the forms of a concept, no retired words, and a cut
 * down version for a lexicon that is too big.
 */
class LexiconTextTest {

    @Test
    void theLexiconIsGroupedUnderItsCategoriesInTheOrderOfTheAuthor() {
        Language language = TestLanguages.vrakht();

        String text = LexiconText.render(language, null);

        assertTrue(text.startsWith("[Particles]\nDa - statement mood\nFrag - question mood\nBef - command mood\nge - role: object\n\n[Military & War]\n"), text);
        assertTrue(text.contains("drak - war / fight (verb)\n"), text);
        assertTrue(text.contains("blod - blood / lineage\n"), text);
        assertTrue(text.indexOf("[Military & War]") < text.indexOf("[People & Social]"));
    }

    @Test
    void theFormsOfOneConceptAreOneLineWithTheirRegisters() {
        String text = LexiconText.render(TestLanguages.aethel(), null);

        assertTrue(text.contains("breast: mela (formal), melis (soft), lelya (vulgar)\n"), text);
        assertEquals(1, text.split("breast:", -1).length - 1);
        assertFalse(text.contains("melis - "), "a variant is not listed a second time");
        assertTrue(text.contains("silva - tree"));
    }

    @Test
    void retiredWordsAreNotOfferedAndAWordWithoutACategoryIsUnderOther() {
        Language language = TestLanguages.vrakht();
        LexiconEntry old = TestLanguages.word(language, null, "old", "old thing");
        old.setStatus(EntryStatus.RETIRED);
        TestLanguages.word(language, null, "loose", "a loose word");
        LexiconEntry script = TestLanguages.word(language, null, "zeik", "sign");
        script.setScript("ᚠ");

        String text = LexiconText.render(language, null);

        assertFalse(text.contains("old thing"));
        assertTrue(text.contains("[Other]\nloose - a loose word\nzeik (ᚠ) - sign"), text);
        assertTrue(text.indexOf("[Other]") > text.indexOf("[People & Social]"));
    }

    @Test
    void aSmallLexiconIsSentWholeAndABigOneIsCutDown() {
        Language language = TestLanguages.vrakht();
        String all = LexiconText.render(language, null);

        assertEquals(all, LexiconText.select(language, "anything", 0));
        assertEquals(all, LexiconText.select(language, "anything", all.length()));

        // too big: the always-send categories and the categories of the words the part needs; the author's words are not new, so the rest goes
        String cut = LexiconText.select(language, "The woman was waiting.", all.length() - 1);
        assertTrue(cut.contains("[Particles]"), cut);
        assertTrue(cut.contains("frou - woman"), cut);
        assertFalse(cut.contains("[Military & War]"), cut);
        assertTrue(cut.length() < all.length());
    }

    @Test
    void aNewOrLockedWordBringsItsCategoryEvenWhenThePartDoesNotNeedIt() {
        Language language = TestLanguages.vrakht();
        language.findByForm("karn").getFirst().setStatus(EntryStatus.LOCKED);
        String all = LexiconText.render(language, null);

        String cut = LexiconText.select(language, "Nothing relevant.", all.length() - 1);

        assertTrue(cut.contains("[Military & War]"), cut);
    }

    @Test
    void onlyTheWantedCategoriesAreWritten() {
        Language language = TestLanguages.vrakht();
        String id = language.getCategories().get(2).getId();

        String text = LexiconText.render(language, Set.of(id));

        assertTrue(text.startsWith("[People & Social]"), text);
        assertFalse(text.contains("[Particles]"));
    }
}
