package com.github.enerccio.marginalia.extensions.translatorcommon.service;

import com.github.enerccio.marginalia.extensions.translatorcommon.model.Span;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Reverse;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Speech;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.TranslationPlan;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A part with the speech of two languages: a run for one language leaves the other's speeches translated, and a revert of one language puts back
 * only that one.
 */
class CarriedSpeechesTest {

    private static final String ENGLISH = "Mara said, \"Be welcome.\" Brog growled, \"Out of my way.\" The cook sighed, \"Good day.\"";

    /** The stored text: Elvish for Mara and Dwarvish for Brog, the cook in English. */
    private static final String STORED = "Mara said, \"Ael tuvi.\" Brog growled, \"Ug mor zan.\" The cook sighed, \"Good day.\"";

    private static Reverse.Located located(String stored, String translated, String source, String language, String speaker) {
        int start = stored.indexOf(translated);
        return new Reverse.Located(start, start + translated.length(), spanOf(language, source, translated, speaker));
    }

    private static Span spanOf(String language, String source, String translated, String speaker) {
        Span span = new Span(language, source, translated);
        span.setSpeaker(speaker);
        return span;
    }

    private static List<Reverse.Located> both() {
        return List.of(located(STORED, "Ael tuvi.", "Be welcome.", "elf", "Mara"), located(STORED, "Ug mor zan.", "Out of my way.", "dwarf", "Brog"));
    }

    @Test
    void theOtherLanguagesSpeechesAreCarriedAtTheirPlaceInTheEnglish() {
        Reverse.Result read = Reverse.toModel(STORED, both());
        assertEquals(ENGLISH, read.text());

        List<TranslationPlan.Accepted> carried = TranslationWriter.carried(STORED, both(), read, "elf");

        assertEquals(1, carried.size());
        TranslationPlan.Accepted brog = carried.getFirst();
        assertEquals("Out of my way.", ENGLISH.substring(brog.start(), brog.end()));
        assertEquals("Ug mor zan.", brog.speech().text());
        assertEquals("dwarf", brog.carried().getLanguage());
    }

    @Test
    void aNewSpeechAndTheCarriedOnesMakeTheTextOfThePart() {
        Reverse.Result read = Reverse.toModel(STORED, both());
        List<TranslationPlan.Accepted> accepted = new java.util.ArrayList<>(TranslationWriter.carried(STORED, both(), read, "elf"));
        int cook = ENGLISH.indexOf("Good day.");
        accepted.add(new TranslationPlan.Accepted(new Speech("Good day.", "the cook", "elf", "Ael dan.", "", null, ""), cook, cook + "Good day.".length()));

        TranslationPlan plan = TranslationPlan.make(read.text(), accepted);

        // Mara's Elvish is the model's English again (this run translates it anew), Brog's Dwarvish stays
        assertEquals("Mara said, \"Be welcome.\" Brog growled, \"Ug mor zan.\" The cook sighed, \"Ael dan.\"", plan.stored());
        assertEquals(1, plan.translatedCount());
    }

    @Test
    void anEditedSpeechIsNotCarriedItStaysInTheText() {
        List<Reverse.Located> passages = List.of(located(STORED, "Ael tuvi.", "Be welcome.", "elf", "Mara"),
                new Reverse.Located(STORED.indexOf("Ug mor zan."), STORED.indexOf("Ug mor zan.") + "Ug mor zan.".length(),
                        spanOf("dwarf", "Out of my way.", "Ug mor zan. (what the cook first wrote)", "Brog")));
        Reverse.Result read = Reverse.toModel(STORED, passages);

        assertTrue(TranslationWriter.carried(STORED, passages, read, "elf").isEmpty());
        assertEquals(1, read.edited().size());
    }

    @Test
    void revertingOneLanguageLeavesTheOtherTranslated() {
        Reverse.Result read = Reverse.toModel(STORED, both());
        List<TranslationPlan.Accepted> carried = TranslationWriter.carried(STORED, both(), read, "elf");

        TranslationPlan plan = TranslationPlan.make(read.text(), carried);

        assertEquals("Mara said, \"Be welcome.\" Brog growled, \"Ug mor zan.\" The cook sighed, \"Good day.\"", plan.stored());
        assertEquals(0, plan.translatedCount());
        assertEquals(1, plan.passages().size());
    }

    @Test
    void aSpeechInTheLanguageOfTheRunIsNeverCarried() {
        Reverse.Result read = Reverse.toModel(STORED, both());

        assertEquals(2, TranslationWriter.carried(STORED, both(), read, "orc").size());
        assertEquals(1, TranslationWriter.carried(STORED, both(), read, "dwarf").size());
    }

    @Test
    void speechesThatOverlapACarriedOneAreFound() {
        Reverse.Result read = Reverse.toModel(STORED, both());
        List<TranslationPlan.Accepted> carried = TranslationWriter.carried(STORED, both(), read, "elf");
        int start = ENGLISH.indexOf("Out of my way.");

        assertTrue(TranslationWriter.overlapsAny(carried, start + 4, start + 9));
        assertFalse(TranslationWriter.overlapsAny(carried, ENGLISH.indexOf("Be welcome."), ENGLISH.indexOf("Be welcome.") + 5));
    }
}
