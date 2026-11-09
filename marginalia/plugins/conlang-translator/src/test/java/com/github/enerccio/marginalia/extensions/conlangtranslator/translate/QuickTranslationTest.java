package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import com.github.enerccio.marginalia.extensions.translatorcommon.model.CharacterNote;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.AnswerChecker.Checked;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationAnswer.NewWord;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationPrompt.Request;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The quick translation: a request with the language and the text, an answer with a text and the words it needed.
 */
class QuickTranslationTest {

    @Test
    void theRequestHasTheLanguageTheTextAndTheNotesOfTheCharactersItNames() {
        Language language = TestLanguages.vrakht();
        Speakers speakers = new Speakers();
        CharacterNote mara = new CharacterNote("Mara");
        mara.setVoice("formal court speech");
        speakers.getCharacters().add(mara);
        CharacterNote absent = new CharacterNote("Nobody");
        absent.setVoice("never mentioned");
        speakers.getCharacters().add(absent);

        Request request = QuickTranslation.build(language, speakers, "I see the enemy.", "Mara to the captain", 0);

        assertTrue(request.system().contains("You translate a text from English into a constructed language"));
        assertTrue(request.system().contains("=== THE LANGUAGE: VRAKHT ==="));
        assertTrue(request.system().contains("=== LEXICON ==="));
        assertFalse(request.system().contains("%"), "every placeholder is filled");
        assertTrue(request.user().contains("- Mara: speaks Vrakht; voice: formal court speech\n"), request.user());
        assertFalse(request.user().contains("Nobody"));
        assertTrue(request.user().contains("CONTEXT (who says it, to whom, where): Mara to the captain"));
        assertTrue(request.user().endsWith("TEXT:\nI see the enemy.\n"));
    }

    @Test
    void aRequestWithoutContextOrNotesHasOnlyTheText() {
        Request request = QuickTranslation.build(TestLanguages.vrakht(), new Speakers(), "Hello.", "  ", 0);

        assertEquals("TEXT:\nHello.\n", request.user());
    }

    @Test
    void theAnswerIsReadFromAFenceAndAWordBeforeIt() {
        String raw = "Here it is:\n```json\n{\"text\":\" Da drak. \",\"choice\":\"plain\",\"newWords\":[{\"word\":\"zorg\",\"gloss\":\"sea\",\"category\":\"Nature\","
                + "\"rationale\":\"from zo + org\",\"register\":\"\",\"kind\":\"compound\",\"variantOf\":\"\"}]}\n```";

        QuickTranslation.Answer answer = QuickTranslation.parse(raw);

        assertEquals("Da drak.", answer.text());
        assertEquals("plain", answer.choice());
        assertEquals(1, answer.newWords().size());
        assertEquals("zorg", answer.newWords().getFirst().word());
    }

    @Test
    void anAnswerWithoutATextOrWithoutAnObjectIsNotAnAnswer() {
        assertNull(QuickTranslation.parse(null));
        assertNull(QuickTranslation.parse("I can not."));
        assertNull(QuickTranslation.parse("{\"text\":\"  \",\"choice\":\"\",\"newWords\":[]}"));
        assertNull(QuickTranslation.parse("{\"text\": 12, \"newWords\": \"x\"}"));
    }

    @Test
    void aMissingChoiceAndMissingWordsAreEmpty() {
        QuickTranslation.Answer answer = QuickTranslation.parse("{\"text\":\"Da drak.\"}");

        assertEquals("", answer.choice());
        assertTrue(answer.newWords().isEmpty());
    }

    @Test
    void theNewWordsAreCheckedLikeThoseOfAPart() {
        Language language = TestLanguages.vrakht();
        NewWord known = new NewWord("drak", "war", "War", "known", "", "", "");
        NewWord fresh = new NewWord("zorg", "sea", "Nature", "from zo + org", "", "", "");
        NewWord incomplete = new NewWord("blip", "", "", "", "", "", "");

        Checked checked = AnswerChecker.checkNewWords(List.of(known, fresh, incomplete), language);

        assertEquals(List.of("zorg"), checked.words().stream().map(word -> word.word().word()).toList());
        assertEquals(2, checked.leftOut().size());
        assertTrue(checked.speeches().isEmpty());
    }
}
