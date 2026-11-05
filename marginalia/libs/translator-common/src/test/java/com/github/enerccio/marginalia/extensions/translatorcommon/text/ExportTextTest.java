package com.github.enerccio.marginalia.extensions.translatorcommon.text;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.extensions.ExportTextRegistry;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.ExportMode;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Span;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Reverse.Located;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the exporters get for each choice of the export dialog: the edits of the plugin put into the text by the core's registry (so the text
 * here is what an exporter reads).
 */
class ExportTextTest {

    private static final String TRANSLATION = "Da ja-se vek.";
    private static final String SOURCE = "I see the enemy.";
    private static final String STORED = "Mara said, \"" + TRANSLATION + "\" and left.";

    private static Located speech(String stored, String translated, String source, String speaker) {
        Span span = new Span("lang-1", source, translated);
        span.setSpeaker(speaker);
        int start = stored.indexOf(translated);
        return new Located(start, start + translated.length(), span);
    }

    /** The parts as the registry gives them to the exporters, for one choice of the dialog. */
    private static List<String> exported(ExportMode mode, List<String> stored, List<List<Located>> passages) {
        ExportTextRegistry registry = new ExportTextRegistry();
        int[] index = {0};
        registry.register("test", (part, session) -> ExportText.edits(mode, part.getResponse(), passages.get(Integer.parseInt(part.getUuid()))));
        List<ChatMessage> parts = new java.util.ArrayList<>();
        for (int i = 0; i < stored.size(); i++) {
            ChatMessage part = new ChatMessage();
            part.setUuid(String.valueOf(i));
            part.setResponse(stored.get(i));
            parts.add(part);
        }
        return registry.apply(parts, new ExportTextRegistry.Session() {
            @Override
            public Object source() {
                return null;
            }

            @Override
            public com.github.enerccio.marginalia.domain.model.impl.Manuscript manuscript() {
                return null;
            }
        }).stream().map(ChatMessage::getResponse).toList();
    }

    private static String one(ExportMode mode, String stored, Located... passages) {
        return exported(mode, List.of(stored), List.of(List.of(passages))).getFirst();
    }

    @Test
    void theTranslationChoiceChangesNothing() {
        assertTrue(ExportText.edits(ExportMode.TRANSLATED, STORED, List.of(speech(STORED, TRANSLATION, SOURCE, "Mara"))).isEmpty());
        assertEquals(STORED, one(ExportMode.TRANSLATED, STORED, speech(STORED, TRANSLATION, SOURCE, "Mara")));
    }

    @Test
    void theSourceChoiceReplacesTheSpeechAndKeepsTheQuotationMarks() {
        assertEquals("Mara said, \"" + SOURCE + "\" and left.", one(ExportMode.SOURCE, STORED, speech(STORED, TRANSLATION, SOURCE, "Mara")));
    }

    @Test
    void aFootnoteMarkGoesBehindTheClosingQuotationMark() {
        assertEquals("Mara said, \"" + TRANSLATION + "\"[^1] and left.\n\n[^1]: Mara: " + SOURCE,
                one(ExportMode.TRANSLATED_NOTES, STORED, speech(STORED, TRANSLATION, SOURCE, "Mara")));
    }

    @Test
    void theNoteOfTheSourceTextHoldsTheTranslation() {
        assertEquals("Mara said, \"" + SOURCE + "\"[^1] and left.\n\n[^1]: " + TRANSLATION,
                one(ExportMode.SOURCE_NOTES, STORED, speech(STORED, TRANSLATION, SOURCE, "")));
    }

    @Test
    void theNumbersGoOnOverTheParts() {
        String second = "He said, \"Ke za.\"";
        List<String> result = exported(ExportMode.TRANSLATED_NOTES, List.of(STORED, second),
                List.of(List.of(speech(STORED, TRANSLATION, SOURCE, "")), List.of(speech(second, "Ke za.", "Go.", ""))));

        assertTrue(result.get(1).startsWith("He said, \"Ke za.\"[^2]"), result.get(1));
        assertTrue(result.get(1).endsWith("[^2]: Go."));
    }

    @Test
    void severalSpeechesKeepTheirOrder() {
        String stored = "\"Aa.\" then \"Bb.\"";
        Located one = speech(stored, "Aa.", "One.", "");
        Located two = speech(stored, "Bb.", "Two.", "");

        assertEquals("\"One.\"[^1] then \"Two.\"[^2]\n\n[^1]: Aa.\n\n[^2]: Bb.", one(ExportMode.SOURCE_NOTES, stored, two, one));
    }

    @Test
    void aSpeechTheAuthorEditedStaysAsItIsWithoutANote() {
        String edited = "Mara said, \"Da ja-se vek za.\" and left.";
        int start = edited.indexOf("Da");
        Located passage = new Located(start, edited.indexOf("za.") + 3, new Span("lang-1", SOURCE, TRANSLATION));

        assertEquals(edited, one(ExportMode.SOURCE_NOTES, edited, passage));
    }

    @Test
    void aPassageWhoseTextIsNotWhatItWasMadeForIsSkipped() {
        // another plugin changed the text before this range: the hash of the text at the range does not match, so the passage drops out
        String shifted = "Mara said (changed), \"" + TRANSLATION + "\" and left.";
        Located pointsAtTheOldPlace = speech(STORED, TRANSLATION, SOURCE, "Mara");

        assertEquals(shifted, one(ExportMode.SOURCE, shifted, pointsAtTheOldPlace));
    }

    @Test
    void aNoteIsOneLine() {
        assertEquals("Mara: one two", ExportText.note("Mara", "one\n  two"));
        assertEquals("one", ExportText.note("  ", "one"));
    }

    @Test
    void aPartWithoutPassagesIsUntouched() {
        assertEquals(STORED, one(ExportMode.SOURCE_NOTES, STORED));
        assertTrue(ExportText.edits(ExportMode.SOURCE, null, null).isEmpty());
    }
}
