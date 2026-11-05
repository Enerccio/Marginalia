package com.github.enerccio.marginalia.extensions.reallangtranslator.translate;

import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Presets;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.RealLanguage;
import com.github.enerccio.marginalia.extensions.reallangtranslator.translate.AnswerChecker.Item;
import com.github.enerccio.marginalia.extensions.reallangtranslator.translate.AnswerChecker.Problem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What the check lets into the story and what it drops, with a Japanese language in kana and kanji.
 */
class AnswerCheckerTest {

    private static final String PART = "Mara looked up. \"I see the enemy,\" she said. Ken nodded. \"Then we go.\" \"Wait for me!\" Tomo shouted.";

    private static RealLanguage japanese() {
        return Presets.make("ja-kana-kanji");
    }

    private static TranslationAnswer.Entry entry(String quote, String language, String text) {
        return new TranslationAnswer.Entry(quote, "Mara", language, text, "", "", true, "");
    }

    private static TranslationAnswer answer(TranslationAnswer.Entry... entries) {
        return new TranslationAnswer(List.of(entries));
    }

    @Test
    void aSpeechInTheLanguageWithItsQuoteInThePartIsAccepted() {
        AnswerChecker.Checked checked = AnswerChecker.check(PART, answer(entry("I see the enemy,", "Japanese", "敵が見える。")), japanese());

        assertEquals(1, checked.accepted().size());
        Item item = checked.accepted().getFirst();
        assertEquals(PART.indexOf("I see the enemy,"), item.start());
        assertTrue(item.problems().isEmpty());
        assertEquals(0, checked.wrongScript());
    }

    @Test
    void theLanguageMayBeNamedByTheNameOfTheLibraryToo() {
        RealLanguage language = japanese();
        language.setName("Japanese for the novel");
        language.setTongue("Japanese");

        assertEquals(1, AnswerChecker.check(PART, answer(entry("Then we go.", "japanese", "では行こう。")), language).accepted().size());
        assertEquals(1, AnswerChecker.check(PART, answer(entry("Then we go.", "Japanese for the novel", "では行こう。")), language).accepted().size());
    }

    @Test
    void aQuoteThatIsNotInThePartIsDropped() {
        AnswerChecker.Checked checked = AnswerChecker.check(PART, answer(entry("This was never said.", "Japanese", "そんなことは言っていない。")), japanese());

        assertTrue(checked.accepted().isEmpty());
        assertEquals(List.of(Problem.QUOTE_NOT_FOUND), checked.speeches().getFirst().problems());
    }

    @Test
    void anotherLanguageAndAnEmptyTextAreDropped() {
        AnswerChecker.Checked checked = AnswerChecker.check(PART, answer(entry("Then we go.", "French", "Alors allons-y."), entry("Wait for me!", "Japanese", " ")),
                japanese());

        assertTrue(checked.accepted().isEmpty());
        assertTrue(checked.speeches().get(0).problems().contains(Problem.LANGUAGE_UNKNOWN));
        assertTrue(checked.speeches().get(1).problems().contains(Problem.EMPTY_TEXT));
    }

    @Test
    void aSpeechInEnglishIsNotTranslatedButIsNotAnError() {
        AnswerChecker.Checked checked = AnswerChecker.check(PART, answer(entry("Then we go.", "none", "")), japanese());

        assertTrue(checked.accepted().isEmpty());
        Item item = checked.speeches().getFirst();
        assertTrue(item.accepted());
        assertFalse(item.translated());
        assertTrue(item.problems().isEmpty());
    }

    @Test
    void twoSpeechesThatOverlapKeepTheFirst() {
        AnswerChecker.Checked checked = AnswerChecker.check(PART, answer(entry("I see the enemy,", "Japanese", "敵が見える。"),
                entry("see the enemy", "Japanese", "敵を見る")), japanese());

        assertEquals(1, checked.accepted().size());
        assertTrue(checked.speeches().get(1).problems().contains(Problem.OVERLAP));
    }

    @Test
    void aSpeechInTheWrongScriptIsKeptAndCounted() {
        AnswerChecker.Checked checked = AnswerChecker.check(PART, answer(entry("Then we go.", "Japanese", "Dewa ikou.")), japanese());

        assertEquals(1, checked.accepted().size());
        assertEquals(1, checked.wrongScript());
        assertEquals(ScriptCheck.Problem.NOT_JAPANESE, checked.accepted().getFirst().script());
        assertTrue(checked.accepted().getFirst().problems().contains(Problem.WRONG_SCRIPT));
    }

    @Test
    void theAcceptedSpeechesComeInTheOrderOfThePart() {
        AnswerChecker.Checked checked = AnswerChecker.check(PART, answer(entry("Wait for me!", "Japanese", "待って!"), entry("I see the enemy,", "Japanese", "敵が見える。")),
                japanese());

        assertEquals(List.of("敵が見える。", "待って!"), checked.accepted().stream().map(item -> item.speech().text()).toList());
    }

    @Test
    void noAnswerIsNothing() {
        assertTrue(AnswerChecker.check(PART, null, japanese()).speeches().isEmpty());
    }
}
