package com.github.enerccio.marginalia.extensions.reallangtranslator.translate;

import com.github.enerccio.marginalia.extensions.reallangtranslator.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The word extraction: what the model is told (the author's prompt and specification, the language, the glossary the book has, the format) and how the
 * answer is read.
 */
class TermExtractionTest {

    private static final String PART = "Akane crossed the Mississippi River to find the Familiar of Belmont.";

    private static GlossaryEntry known(String source, String written) {
        return new GlossaryEntry(source, GlossaryKind.WRITTEN, written, "", "");
    }

    @Test
    void theSystemMessageHasThePromptTheSpecificationTheLanguageAndTheFormat() {
        String system = TermExtraction.build(Presets.make("ja-kana-kanji"), "Use kanji for names.", "Leave out the minor characters.", List.of(), PART).system();

        assertTrue(system.contains("translated into Japanese"), system);
        assertTrue(system.contains("=== INSTRUCTIONS ===\nUse kanji for names."), system);
        assertTrue(system.contains("=== SPECIFICATION OF THIS BOOK ===\nLeave out the minor characters."), system);
        assertTrue(system.contains("=== THE LANGUAGE: JAPANESE ==="), system);
        assertTrue(system.contains("=== OUTPUT FORMAT ==="), system);
        assertTrue(system.contains("{\"terms\":["), system);
        assertTrue(system.indexOf("INSTRUCTIONS") < system.indexOf("SPECIFICATION"), "the specification goes after the prompt");
    }

    @Test
    void aMissingPromptOrSpecificationLeavesNoEmptySection() {
        String onlySpecification = TermExtraction.build(Presets.make("ja-kana-kanji"), "", "Names only.", List.of(), PART).system();
        String onlyPrompt = TermExtraction.build(Presets.make("ja-kana-kanji"), "Names.", " ", List.of(), PART).system();

        assertFalse(onlySpecification.contains("=== INSTRUCTIONS ==="));
        assertTrue(onlySpecification.contains("=== SPECIFICATION OF THIS BOOK ===\nNames only."));
        assertTrue(onlyPrompt.contains("=== INSTRUCTIONS ==="));
        assertFalse(onlyPrompt.contains("SPECIFICATION"));
    }

    @Test
    void theManuscriptIsTheUserMessage() {
        TranslationPrompt.Request request = TermExtraction.build(Presets.make("ja-kana-kanji"), "x", "", List.of(), "  " + PART + " ");

        assertEquals("MANUSCRIPT:\n" + PART + "\n", request.user());
        assertFalse(request.system().contains(PART), "the part is not in the system message");
    }

    @Test
    void onlyTheKnownWordsThePartHasAreToldToTheModel() {
        GlossaryEntry akane = known("Akane", "茜");
        akane.setReading("あかね");
        GlossaryEntry keep = new GlossaryEntry("Belmont", GlossaryKind.KEEP, "", "", "");
        GlossaryEntry absent = known("Zorg", "ゾルグ");

        String system = TermExtraction.build(Presets.make("ja-kana-kanji"), "x", "", List.of(akane, keep, absent), PART).system();

        assertTrue(system.contains("- Akane: 茜 (あかね)"), system);
        assertTrue(system.contains("- Belmont: kept as it is"), system);
        assertFalse(system.contains("Zorg"), "a word the part does not have is not sent");
        assertTrue(system.contains("do not list these again"), system);
    }

    @Test
    void noKnownWordNoGlossaryBlock() {
        assertFalse(TermExtraction.build(Presets.make("ja-kana-kanji"), "x", "", List.of(known("Zorg", "ゾルグ")), PART).system().contains("THE GLOSSARY OF THE BOOK"));
    }

    @Test
    void theAnswerIsReadLeniently() {
        TermExtraction.Answer answer = TermExtraction.parse("""
                Here you are:
                ```json
                {"terms":[
                {"source":"Akane","category":"Japanese proper noun","written":"茜","reading":"あかね","keep":false,"note":""},
                {"source":"Familiar bond","category":"gikun","written":"魔力結合","reading":"ファミリア・ボンド","keep":false,"note":"the magic system"}]}
                ```""");

        assertNotNull(answer);
        assertEquals(2, answer.terms().size());
        assertEquals("茜", answer.terms().getFirst().written());
        assertEquals("ファミリア・ボンド", answer.terms().get(1).reading());
    }

    @Test
    void anAnswerWithoutTheListOrWithoutJsonIsNotAnAnswer() {
        assertNull(TermExtraction.parse("I found some names."));
        assertNull(TermExtraction.parse("{\"names\":[]}"));
        assertEquals(0, TermExtraction.parse("{\"terms\":[]}").terms().size());
    }

    @Test
    void aTermWithoutASourceOrAFormIsDropped() {
        TermExtraction.Answer answer = TermExtraction.parse("""
                {"terms":[{"source":"","written":"x"},{"source":"Belmont","written":""},{"source":"Akane","written":"茜"},
                {"source":"UN","written":"","keep":true}]}""");

        assertEquals(List.of("Akane", "UN"), answer.terms().stream().map(TermExtraction.Term::source).toList());
    }

    @Test
    void aTermBecomesAProposedEntryWithItsCategoryInTheNote() {
        GlossaryEntry entry = new TermExtraction.Term("Akane", "Japanese proper noun", "茜", "あかね", false, "a girl").entry();

        assertEquals(GlossaryKind.WRITTEN, entry.getKind());
        assertEquals("茜", entry.getWritten());
        assertEquals("あかね", entry.getReading());
        assertEquals("Japanese proper noun: a girl", entry.getNote());
        assertTrue(entry.isProposed());
        assertTrue(entry.isEnabled());
    }

    @Test
    void aKeptTermHasNoFormAndTheCategoryAloneIsTheNote() {
        GlossaryEntry entry = new TermExtraction.Term("UN", "real-world", "ignored", "ignored", true, "").entry();

        assertEquals(GlossaryKind.KEEP, entry.getKind());
        assertEquals("", entry.getWritten());
        assertEquals("", entry.getReading());
        assertEquals("real-world", entry.getNote());
    }

    @Test
    void theBuiltInJapanesePromptIsThereAndAsksForNoFormat() {
        ExtractionPrompt prompt = ExtractionPrompts.builtIn().getFirst();

        assertEquals(ExtractionPrompts.JAPANESE, prompt.getId());
        assertTrue(ExtractionPrompts.isBuiltIn(prompt.getId()));
        assertFalse(ExtractionPrompts.isBuiltIn("something-else"));
        assertTrue(prompt.getText().contains("gikun"), prompt.getText());
        assertFalse(prompt.getText().contains("->"), "the output format is the plugin's, the prompt does not ask for one");
        assertFalse(prompt.getText().contains("[OUTPUT"), prompt.getText());
    }

    @Test
    void theSchemaListsTheFieldsOfATerm() {
        for (String field : List.of("source", "category", "written", "reading", "keep", "note")) {
            assertTrue(TermExtraction.SCHEMA.contains("\"" + field + "\""), field);
        }
    }
}
