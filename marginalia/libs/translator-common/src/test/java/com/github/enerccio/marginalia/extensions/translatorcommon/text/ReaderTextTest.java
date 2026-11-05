package com.github.enerccio.marginalia.extensions.translatorcommon.text;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.ReaderMode;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Span;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What the reader of a published book gets in each mode, above all that the English is not in the page unless the book asks for it.
 */
class ReaderTextTest {

    private static final String SOURCE = "\"I see the enemy.\"";
    private static final String CONLANG = "\"Da ja-se vek.\"";
    private static final String STORED = "Mara said, " + CONLANG + " and left.";

    private static AnchoredPassage passage(String uuid, int start, int end, String source, String conlang) {
        AnchoredPassage passage = new AnchoredPassage();
        passage.setUuid(uuid);
        passage.setPassageKind("test.speech");
        passage.setStart(start);
        passage.setEnd(end);
        JsonObject attributes = new JsonObject();
        new Span("lang-1", source, conlang).write(attributes);
        passage.setAttributes(attributes);
        return passage;
    }

    private static AnchoredPassage theSpeech() {
        int start = STORED.indexOf(CONLANG);
        return passage("p-1", start, start + CONLANG.length(), SOURCE, CONLANG);
    }

    @Test
    void conlangOnlyIsTheStoredTextAndHasNoEnglish() {
        String shown = ReaderText.render(ReaderMode.TRANSLATED, STORED, List.of(theSpeech()), "test-passage");

        assertEquals(STORED, shown);
        assertFalse(shown.contains("enemy"));
    }

    @Test
    void sourceOnlyIsTheEnglish() {
        assertEquals("Mara said, " + SOURCE + " and left.", ReaderText.render(ReaderMode.SOURCE, STORED, List.of(theSpeech()), "test-passage"));
    }

    @Test
    void theClickModeMarksTheSpeechAndCarriesOnlyItsId() {
        String shown = ReaderText.render(ReaderMode.CLICK, STORED, List.of(theSpeech()), "test-passage");

        assertTrue(shown.contains("<mark class=\"test-passage\" data-translated-passage=\"p-1\">" + CONLANG + "</mark>"), shown);
        assertFalse(shown.contains("enemy"), "the English must stay on the server");
    }

    @Test
    void aPartWithoutPassagesIsUntouchedInEveryMode() {
        for (ReaderMode mode : ReaderMode.values()) {
            assertEquals(STORED, ReaderText.render(mode, STORED, List.of(), "test-passage"));
        }
    }

    @Test
    void aPassageOutsideTheTextIsLeftOut() {
        AnchoredPassage outside = passage("p-2", 500, 520, SOURCE, CONLANG);

        assertEquals(STORED, ReaderText.render(ReaderMode.CLICK, STORED, List.of(outside), "test-passage"));
    }

    @Test
    void aSpeechTheAuthorEditedStaysTheConlangInSourceMode() {
        String edited = "Mara said, \"Da ja-se vek za.\" and left.";
        int start = edited.indexOf('"');
        AnchoredPassage passage = passage("p-3", start, edited.indexOf("za.\"") + 4, SOURCE, CONLANG);

        assertEquals(edited, ReaderText.render(ReaderMode.SOURCE, edited, List.of(passage), "test-passage"));
    }

    @Test
    void anUnknownModeIdIsTheClickMode() {
        assertEquals(ReaderMode.CLICK, ReaderMode.of(null));
        assertEquals(ReaderMode.CLICK, ReaderMode.of("nonsense"));
        assertEquals(ReaderMode.TRANSLATED, ReaderMode.of("conlang"));
        assertEquals(ReaderMode.SOURCE, ReaderMode.of("source"));
    }
}
