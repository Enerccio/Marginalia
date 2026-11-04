package com.github.enerccio.marginalia.extensions.conlangtranslator.text;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.ExportMode;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Span;
import com.github.enerccio.marginalia.extensions.conlangtranslator.text.ExportText.Rendered;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.Reverse.Located;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the exporters get for each choice of the export dialog.
 */
class ExportTextTest {

    private static final String CONLANG = "Da ja-se vek.";
    private static final String SOURCE = "I see the enemy.";
    private static final String STORED = "Mara said, \"" + CONLANG + "\" and left.";

    private static Located speech(String stored, String conlang, String source, String speaker) {
        Span span = new Span("lang-1", source, conlang);
        span.setSpeaker(speaker);
        int start = stored.indexOf(conlang);
        return new Located(start, start + conlang.length(), span);
    }

    @Test
    void theConlangChoiceChangesNothing() {
        Rendered rendered = ExportText.render(ExportMode.CONLANG, STORED, List.of(speech(STORED, CONLANG, SOURCE, "Mara")), 1);

        assertEquals(STORED, rendered.text());
        assertEquals(1, rendered.next());
    }

    @Test
    void theEnglishChoiceReplacesTheSpeechAndKeepsTheQuotationMarks() {
        Rendered rendered = ExportText.render(ExportMode.SOURCE, STORED, List.of(speech(STORED, CONLANG, SOURCE, "Mara")), 1);

        assertEquals("Mara said, \"" + SOURCE + "\" and left.", rendered.text());
        assertEquals(1, rendered.next());
    }

    @Test
    void aFootnoteMarkGoesBehindTheClosingQuotationMark() {
        Rendered rendered = ExportText.render(ExportMode.CONLANG_NOTES, STORED, List.of(speech(STORED, CONLANG, SOURCE, "Mara")), 1);

        assertEquals("Mara said, \"" + CONLANG + "\"[^1] and left.\n\n[^1]: Mara: " + SOURCE, rendered.text());
        assertEquals(2, rendered.next());
    }

    @Test
    void theNoteOfTheEnglishTextHoldsTheConlang() {
        Rendered rendered = ExportText.render(ExportMode.SOURCE_NOTES, STORED, List.of(speech(STORED, CONLANG, SOURCE, "")), 7);

        assertEquals("Mara said, \"" + SOURCE + "\"[^7] and left.\n\n[^7]: " + CONLANG, rendered.text());
        assertEquals(8, rendered.next());
    }

    @Test
    void theNumbersGoOnOverTheParts() {
        String second = "He said, \"Ke za.\"";
        Rendered first = ExportText.render(ExportMode.CONLANG_NOTES, STORED, List.of(speech(STORED, CONLANG, SOURCE, "")), 1);
        Rendered next = ExportText.render(ExportMode.CONLANG_NOTES, second, List.of(speech(second, "Ke za.", "Go.", "")), first.next());

        assertTrue(next.text().startsWith("He said, \"Ke za.\"[^2]"), next.text());
        assertTrue(next.text().endsWith("[^2]: Go."));
    }

    @Test
    void severalSpeechesKeepTheirOrder() {
        String stored = "\"Aa.\" then \"Bb.\"";
        Located one = speech(stored, "Aa.", "One.", "");
        Located two = speech(stored, "Bb.", "Two.", "");

        Rendered rendered = ExportText.render(ExportMode.SOURCE_NOTES, stored, List.of(two, one), 1);

        assertEquals("\"One.\"[^1] then \"Two.\"[^2]\n\n[^1]: Aa.\n\n[^2]: Bb.", rendered.text());
    }

    @Test
    void aSpeechTheAuthorEditedStaysAsItIsWithoutANote() {
        String edited = "Mara said, \"Da ja-se vek za.\" and left.";
        int start = edited.indexOf("Da");
        Located passage = new Located(start, edited.indexOf("za.") + 3, new Span("lang-1", SOURCE, CONLANG));

        Rendered rendered = ExportText.render(ExportMode.SOURCE_NOTES, edited, List.of(passage), 1);

        assertEquals(edited, rendered.text());
        assertEquals(1, rendered.next());
    }

    @Test
    void aNoteIsOneLine() {
        assertEquals("Mara: one two", ExportText.note("Mara", "one\n  two"));
        assertEquals("one", ExportText.note("  ", "one"));
    }

    @Test
    void aPartWithoutPassagesIsUntouched() {
        assertEquals(STORED, ExportText.render(ExportMode.SOURCE_NOTES, STORED, List.of(), 3).text());
        assertEquals("", ExportText.render(ExportMode.SOURCE, null, null, 1).text());
    }
}
