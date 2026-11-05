package com.github.enerccio.marginalia.extensions.reallangtranslator.translate;

import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Speech;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TranslationAnswerTest {

    @Test
    void anAnswerWithAFenceAndAWordBeforeItIsRead() {
        TranslationAnswer answer = TranslationAnswer.parse("Here it is:\n```json\n{\"translations\":[{\"quote\":\"I see the enemy.\",\"speaker\":\"Mara\","
                + "\"language\":\"Japanese\",\"text\":\"敵が見える。\",\"reading\":\"teki ga mieru.\",\"understood\":true,\"register\":\"plain\",\"choice\":\"plain form\"}]}\n```");

        assertNotNull(answer);
        assertEquals(1, answer.translations().size());
        TranslationAnswer.Entry entry = answer.translations().getFirst();
        assertEquals("teki ga mieru.", entry.reading());
        Speech speech = entry.speech();
        assertEquals("敵が見える。", speech.text());
        assertEquals("Mara", speech.speaker());
        assertTrue(speech.isUnderstood());
        assertFalse(speech.isNone());
    }

    @Test
    void noSpeechIsAnEmptyListAndNotAFailure() {
        TranslationAnswer answer = TranslationAnswer.parse("{\"translations\":[]}");

        assertNotNull(answer);
        assertTrue(answer.translations().isEmpty());
    }

    @Test
    void somethingThatIsNotTheAnswerIsNull() {
        assertNull(TranslationAnswer.parse("I could not do it."));
        assertNull(TranslationAnswer.parse("{\"other\":1}"));
        assertNull(TranslationAnswer.parse("{broken"));
        assertNull(TranslationAnswer.parse(null));
    }

    @Test
    void anEntryWithNoUnderstoodIsUnderstood() {
        TranslationAnswer answer = TranslationAnswer.parse("{\"translations\":[{\"quote\":\"Go.\",\"language\":\"none\"}]}");

        assertNotNull(answer);
        assertTrue(answer.translations().getFirst().speech().isNone());
        assertTrue(answer.translations().getFirst().speech().isUnderstood());
    }
}
