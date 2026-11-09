package com.github.enerccio.marginalia.extensions.translatorcommon.translate;

import com.github.enerccio.marginalia.extensions.translatorcommon.model.Span;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The plan of a translation: the new text of the part and the passages in it, and that the reverse of the plan is the source again.
 */
class TranslationPlanTest {

    private static final String SOURCE = "Mara drew her sword. \"I see the enemy,\" she said. \"Is it war?\" shouted Brog. \"Good morning,\" said the cook.";

    private TranslationPlan plan(String source, Speech... speeches) {
        List<TranslationPlan.Accepted> accepted = new ArrayList<>();
        for (Speech speech : speeches) {
            int start = source.indexOf(speech.quote());
            if (!speech.isNone() && start >= 0) {
                accepted.add(new TranslationPlan.Accepted(speech, start, start + speech.quote().length()));
            }
        }
        return TranslationPlan.make(source, accepted);
    }

    private static Speech speech(String quote, String lang, String text) {
        return new Speech(quote, "someone", lang, text, "", null, "");
    }

    @Test
    void theSpeechesAreReplacedAndTheNarrationAndTheQuotationMarksStay() {
        TranslationPlan plan = plan(SOURCE, speech("I see the enemy,", "Vrakht", "Da ja-se vek,"), speech("Is it war?", "Vrakht", "Frag drak?"),
                speech("Good morning,", "none", ""));

        assertEquals("Mara drew her sword. \"Da ja-se vek,\" she said. \"Frag drak?\" shouted Brog. \"Good morning,\" said the cook.", plan.stored());
        assertEquals(2, plan.passages().size());
        assertEquals(SOURCE, plan.source());
    }

    @Test
    void thePassagesAreInTheNewTextWithTheirEnglish() {
        TranslationPlan plan = plan(SOURCE, speech("Is it war?", "Vrakht", "Frag drak?"), speech("I see the enemy,", "Vrakht", "Da ja-se vek,"));

        for (TranslationPlan.Planned passage : plan.passages()) {
            assertEquals(passage.translated(), plan.stored().substring(passage.start(), passage.end()));
        }
        assertEquals(List.of("I see the enemy,", "Is it war?"), plan.passages().stream().map(TranslationPlan.Planned::english).toList());
        assertTrue(plan.passages().get(0).start() < plan.passages().get(1).start());
    }

    @Test
    void theReverseOfThePlanIsTheSourceAgain() {
        TranslationPlan plan = plan(SOURCE, speech("I see the enemy,", "Vrakht", "Da ja-se vek, you see how long this is"),
                speech("Is it war?", "Vrakht", "Frag drak?"));
        List<Reverse.Located> located = plan.passages().stream()
                .map(p -> new Reverse.Located(p.start(), p.end(), new Span("lang", p.english(), p.translated()))).toList();

        assertEquals(SOURCE, Reverse.sourceText(plan.stored(), located));
    }

    @Test
    void theConlangIsTrimmedAndAnEmptyOneIsSkipped() {
        TranslationPlan plan = plan(SOURCE, speech("I see the enemy,", "Vrakht", "  Da ja-se vek,\n"));

        assertEquals("Da ja-se vek,", plan.passages().getFirst().translated());
        assertTrue(plan.stored().contains("\"Da ja-se vek,\" she said"));
    }

    @Test
    void aPlanWithNothingToDoIsEmptyAndTheTextIsAsItWas() {
        TranslationPlan plan = plan(SOURCE, speech("Good morning,", "none", ""), speech("not in the part", "Vrakht", "Da"));

        assertTrue(plan.isEmpty());
        assertEquals(SOURCE, plan.stored());
    }

    @Test
    void adjoiningSpeechesKeepTheirOrderAndTheirOwnRanges() {
        String source = "\"Is it war?Fight!\" he said.";
        TranslationPlan plan = plan(source, speech("Is it war?", "Vrakht", "Frag drak?"), speech("Fight!", "Vrakht", "Bef drak!"));

        assertEquals("\"Frag drak?Bef drak!\" he said.", plan.stored());
        assertEquals(plan.passages().get(0).end(), plan.passages().get(1).start());
    }

    @Test
    void aSpeechOfAnotherLanguageStaysAsItWasAndIsNotCounted() {
        Span carried = new Span("Dwarvish", "Good morning,", "Guten Tak,");
        carried.setSpeaker("the cook");
        Speech old = new Speech("Good morning,", "the cook", "Dwarvish", "Guten Tak,", "", null, "");
        int start = SOURCE.indexOf("Good morning,");
        Speech fresh = speech("I see the enemy,", "Vrakht", "Da ja-se vek,");
        int freshStart = SOURCE.indexOf("I see the enemy,");

        TranslationPlan plan = TranslationPlan.make(SOURCE, List.of(
                new TranslationPlan.Accepted(old, start, start + "Good morning,".length(), carried),
                new TranslationPlan.Accepted(fresh, freshStart, freshStart + "I see the enemy,".length())));

        assertEquals("Mara drew her sword. \"Da ja-se vek,\" she said. \"Is it war?\" shouted Brog. \"Guten Tak,\" said the cook.", plan.stored());
        assertEquals(2, plan.passages().size());
        assertEquals(1, plan.translatedCount());
        TranslationPlan.Planned kept = plan.passages().stream().filter(p -> p.carried() != null).findFirst().orElseThrow();
        assertEquals(carried, kept.carried());
        assertEquals("Guten Tak,", plan.stored().substring(kept.start(), kept.end()));
        assertEquals("Good morning,", kept.english());
    }

    @Test
    void aPlanOfCarriedSpeechesOnlyTranslatesNothing() {
        Span carried = new Span("Dwarvish", "Good morning,", "Guten Tak,");
        int start = SOURCE.indexOf("Good morning,");
        TranslationPlan plan = TranslationPlan.make(SOURCE, List.of(new TranslationPlan.Accepted(
                new Speech("Good morning,", "the cook", "Dwarvish", "Guten Tak,", "", null, ""), start, start + "Good morning,".length(), carried)));

        assertEquals(0, plan.translatedCount());
        assertEquals(1, plan.passages().size());
    }
}
