package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.AnswerChecker.Checked;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.AnswerChecker.Item;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.AnswerChecker.Problem;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationAnswer.NewWord;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationAnswer.Speech;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What the model answered, checked against the part and the language: the quote, the language, overlaps, the words nobody reported, and the
 * new words.
 */
class AnswerCheckerTest {

    private static final String PART = "Mara drew her sword. \"I see the enemy,\" she said. \"Is it war? Fight!\" shouted Brog. \"Good morning,\" said the cook.";

    private final Language language = TestLanguages.vrakht();

    private static Speech speech(String quote, String lang, String text) {
        return new Speech(quote, "someone", lang, text, "", null, "");
    }

    private static NewWord word(String form, String gloss, String rationale) {
        return new NewWord(form, gloss, "War", rationale, "", "", "");
    }

    private Checked check(List<Speech> speeches, List<NewWord> words) {
        return AnswerChecker.check(PART, new TranslationAnswer(speeches, words), language);
    }

    @Test
    void aGoodAnswerIsAcceptedWithTheRangesOfTheQuotes() {
        Checked checked = check(List.of(speech("I see the enemy,", "Vrakht", "Da ja-se vek."), speech("Is it war?", "vrakht", "Frag drak?"),
                speech("Good morning,", "none", "")), List.of());

        assertEquals(3, checked.speeches().size());
        Item first = checked.speeches().getFirst();
        assertTrue(first.translated() && first.accepted());
        assertEquals("I see the enemy,", PART.substring(first.start(), first.end()));
        assertTrue(first.problems().isEmpty());
        assertTrue(checked.speeches().get(1).accepted(), "the name of the language is compared ignoring case");
        Item none = checked.speeches().get(2);
        assertFalse(none.translated());
        assertTrue(none.accepted());
        assertEquals(2, checked.accepted().size());
        assertFalse(checked.needsRepair());
    }

    @Test
    void aQuoteThatIsNotInThePartDropsTheSpeech() {
        Checked checked = check(List.of(speech("I do not see anybody", "Vrakht", "Da ja-se vek.")), List.of());

        Item item = checked.speeches().getFirst();
        assertFalse(item.accepted());
        assertEquals(List.of(Problem.QUOTE_NOT_FOUND), item.problems());
        assertEquals(-1, item.start());
        assertTrue(checked.accepted().isEmpty());
    }

    @Test
    void aQuoteMayBeAPartOfAQuotationAndEntriesMayAdjoin() {
        Checked checked = check(List.of(speech("Is it war?", "Vrakht", "Frag drak?"), speech("Fight!", "Vrakht", "Bef drak!")), List.of());

        assertEquals(2, checked.accepted().size());
        Item war = checked.accepted().get(0);
        Item fight = checked.accepted().get(1);
        assertTrue(war.end() <= fight.start());
        assertEquals("Is it war?", PART.substring(war.start(), war.end()));
        assertEquals("Fight!", PART.substring(fight.start(), fight.end()));
    }

    @Test
    void aSpeechThatOverlapsAnAcceptedOneIsDroppedWhateverTheOrderOfTheAnswer() {
        Checked checked = check(List.of(speech("Is it war? Fight!", "Vrakht", "Frag drak?"), speech("Fight!", "Vrakht", "Bef drak!"),
                speech("Is it war?", "Vrakht", "Frag drak?")), List.of());

        assertTrue(checked.speeches().get(0).accepted());
        assertEquals(List.of(Problem.OVERLAP), checked.speeches().get(1).problems());
        assertEquals(List.of(Problem.OVERLAP), checked.speeches().get(2).problems());
        assertEquals(1, checked.accepted().size());
    }

    @Test
    void aLanguageThatIsNotTheBooksOrAnEmptyTextDropsTheSpeech() {
        Checked checked = check(List.of(speech("I see the enemy,", "Sylvari", "Da ja-se vek."), speech("Is it war?", "Vrakht", "  ")), List.of());

        assertEquals(List.of(Problem.LANGUAGE_UNKNOWN), checked.speeches().get(0).problems());
        assertEquals(List.of(Problem.EMPTY_TEXT), checked.speeches().get(1).problems());
        assertTrue(checked.accepted().isEmpty());
    }

    @Test
    void textForASpeechInTheCommonLanguageIsIgnored() {
        Checked checked = check(List.of(speech("Good morning,", "none", "Da gut")), List.of());

        Item item = checked.speeches().getFirst();
        assertFalse(item.translated());
        assertTrue(item.accepted());
        assertEquals(List.of(Problem.TEXT_FOR_NONE), item.problems());
        assertTrue(checked.accepted().isEmpty());
    }

    @Test
    void aWordNobodyReportedIsFlaggedAndTheSpeechIsKeptForARepair() {
        Checked checked = check(List.of(speech("I see the enemy,", "Vrakht", "Da ja-se nord.")), List.of());

        Item item = checked.speeches().getFirst();
        assertTrue(item.accepted());
        assertEquals(List.of(Problem.UNKNOWN_WORDS), item.problems());
        assertEquals(List.of("nord"), item.unknownWords());
        assertTrue(checked.needsRepair());
    }

    @Test
    void aReportedWordIsNotFlaggedAndIsKept() {
        Checked checked = check(List.of(speech("I see the enemy,", "Vrakht", "Da ja-se nord.")),
                List.of(word("nord", "north", "from the old word for cold")));

        assertTrue(checked.speeches().getFirst().problems().isEmpty());
        assertEquals(1, checked.words().size());
        assertTrue(checked.words().getFirst().problems().isEmpty());
        assertFalse(checked.needsRepair());
    }

    @Test
    void aWordThatIsAlreadyInTheLexiconWithTheSameMeaningIsLeftOutAndAHomophoneIsAdded() {
        Checked checked = check(List.of(), List.of(word("vek", "Enemy", "again"), word("VEK", "foe", "a second sense"),
                word("drak", "fight", "known sense"), word("nord", "north", "r"), word("Nord", "north", "r again")));

        assertEquals(List.of("VEK", "nord"), checked.words().stream().map(w -> w.word().word()).toList());
        assertEquals(List.of("vek", "drak", "Nord"), checked.leftOut().stream().map(w -> w.word().word()).toList());
        assertTrue(checked.leftOut().stream().allMatch(w -> w.problems().equals(List.of(Problem.WORD_KNOWN))));
    }

    @Test
    void aWordWithoutARationaleIsKeptAndFlaggedAndAnIncompleteOneIsLeftOut() {
        Checked checked = check(List.of(), List.of(word("nord", "north", " "), word("", "nothing", "x"), word("sud", "", "x")));

        assertEquals(1, checked.words().size());
        assertEquals(List.of(Problem.NO_RATIONALE), checked.words().getFirst().problems());
        assertEquals(2, checked.leftOut().size());
        assertTrue(checked.leftOut().stream().allMatch(w -> w.problems().equals(List.of(Problem.INCOMPLETE_WORD))));
    }

    @Test
    void theHyphenSettingOfTheLanguageDecidesWhetherTwoNewWordsAreTheSame() {
        language.setHyphenStructural(false);
        assertEquals(1, check(List.of(), List.of(word("nord-seit", "north", "r"), word("nordseit", "north", "r"))).words().size());

        language.setHyphenStructural(true);
        assertEquals(2, check(List.of(), List.of(word("nord-seit", "north", "r"), word("nordseit", "north", "r"))).words().size());
    }

    @Test
    void anAnswerThatIsNullHasNothing() {
        Checked checked = AnswerChecker.check(PART, null, language);

        assertTrue(checked.speeches().isEmpty());
        assertTrue(checked.words().isEmpty());
        assertTrue(checked.accepted().isEmpty());
    }

    @Test
    void aSpeechWithoutAQuoteIsDropped() {
        Checked checked = check(List.of(speech(null, "Vrakht", "Da ja-se vek."), speech(" ", "Vrakht", "Da ja-se vek.")), List.of());

        assertTrue(checked.speeches().stream().allMatch(item -> item.problems().contains(Problem.QUOTE_NOT_FOUND) && !item.accepted()));
    }
}
