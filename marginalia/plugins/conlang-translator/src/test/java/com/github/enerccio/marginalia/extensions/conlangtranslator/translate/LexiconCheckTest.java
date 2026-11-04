package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.EntryStatus;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The check that needs no model: a word of the translation that is not in the lexicon, a compound of it, an affixed word, reported, or a
 * name is one the model invented silently.
 */
class LexiconCheckTest {

    private static LexiconCheck check(Language language, String part, String... reported) {
        return new LexiconCheck(language, List.of(reported), part);
    }

    @Test
    void wordsOfTheLexiconAndCompoundsOfThemAreKnown() {
        LexiconCheck check = check(TestLanguages.vrakht(), "");

        assertTrue(check.unknown("Da ja-se vek.").isEmpty());
        assertTrue(check.unknown("Bef drak-karn ge vek").isEmpty());
        assertEquals(List.of("nord"), check.unknown("Da ja-se nord ge vek"));
        assertEquals(List.of("nord-karn"), check.unknown("Da nord-karn"), "a compound is known only when every part is");
    }

    @Test
    void caseAndTheHyphenDoNotMatterUnlessTheLanguageSaysSo() {
        Language language = TestLanguages.vrakht();
        TestLanguages.word(language, null, "drak-karn", "battlefield");

        assertTrue(check(language, "").unknown("DA drakkarn").isEmpty(), "the hyphen is not a part of the word here");
        assertTrue(check(language, "").unknown("da DRAK-KARN").isEmpty());

        language.setHyphenStructural(true);
        TestLanguages.word(language, null, "ek-rokh", "my lord");

        assertTrue(check(language, "").unknown("ek-rokh").isEmpty());
        assertTrue(check(language, "").unknown("drak-karn").isEmpty());
        assertEquals(List.of("ekrokh"), check(language, "").unknown("ekrokh"));
        assertEquals(List.of("drakkarn"), check(language, "").unknown("drakkarn"), "now the hyphen is a part of the word");
        assertTrue(check(language, "").unknown("vek-drak").isEmpty(), "a compound of known parts is known even where the hyphen is structural");
    }

    @Test
    void aReportedWordIsKnownAndMayTakeAffixesToo() {
        LexiconCheck check = check(TestLanguages.aethel(), "", "thalis", "elsar");

        assertTrue(check.unknown("thalis").isEmpty());
        assertTrue(check.unknown("elsaris").isEmpty(), "a reported word takes the plural");
        assertTrue(check.unknown("Va-elsar").isEmpty());
        assertEquals(List.of("thalen"), check.unknown("thalen"));
    }

    @Test
    void affixesAreRecognisedOnWordsOfTheLexicon() {
        LexiconCheck check = check(TestLanguages.aethel(), "");

        assertTrue(check.unknown("silva-is").isEmpty(), "the affix on its own, after a hyphen");
        assertTrue(check.unknown("Vasilva").isEmpty(), "a prefix written without a hyphen");
        assertTrue(check.unknown("Va-silva-is").isEmpty());
        assertTrue(check.unknown("lelyais").isEmpty(), "lelya + is");
        assertTrue(check.unknown("silvais").isEmpty(), "silva + is");
        // the rules of the language may drop the vowel before the plural (silvis): that is prose, and the check does not know it (the trial's rule gap)
        assertEquals(List.of("silvis"), check.unknown("silvis"));
        assertEquals(List.of("is"), check.unknown("is"), "an affix is not a word");
    }

    @Test
    void aNameFromThePartIsKnownButOnlyWhenItIsCapitalised() {
        LexiconCheck check = check(TestLanguages.vrakht(), "Mara met Brog at the gate of Torvak. The vek was near.");

        assertTrue(check.unknown("Da Mara ge Brog Torvak").isEmpty());
        assertEquals(List.of("gate"), check.unknown("Da gate"), "an English word that is not a name is flagged");
        assertEquals(List.of("mara"), check.unknown("Da mara"), "lower case is not the name");
    }

    @Test
    void aRetiredWordIsNotKnown() {
        Language language = TestLanguages.vrakht();
        language.findByForm("vek").getFirst().setStatus(EntryStatus.RETIRED);

        assertEquals(List.of("vek"), check(language, "").unknown("Da ja-se vek"));
    }

    @Test
    void anUnknownWordIsReportedOnceInTheOrderItComes() {
        assertEquals(List.of("xa", "yo"), check(TestLanguages.vrakht(), "").unknown("xa Da yo xa"));
        assertTrue(check(TestLanguages.vrakht(), "").unknown(null).isEmpty());
        assertTrue(check(TestLanguages.vrakht(), "").unknown("... ! ?").isEmpty());
    }
}
