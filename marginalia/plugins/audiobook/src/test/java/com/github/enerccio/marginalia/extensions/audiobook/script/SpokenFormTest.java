package com.github.enerccio.marginalia.extensions.audiobook.script;

import com.github.enerccio.marginalia.extensions.audiobook.model.SpokenSettings;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpokenFormTest {

    private static final SpokenForm FORM = SpokenForm.DEFAULT;

    private static String say(String text) {
        return FORM.apply(text, false);
    }

    private static String heading(String text) {
        return FORM.apply(text, true);
    }

    @Test
    void numbersAreReadAsWords() {
        assertEquals("He counted eleven heads.", say("He counted 11 heads."));
        assertEquals("one hundred twenty-three", say("123"));
        assertEquals("one thousand two hundred", say("1,200"));
        assertEquals("two million five", say("2,000,005"));
        assertEquals("three point one four", say("3.14"));
        assertEquals("zero zero seven", say("007"), "a number with leading zeros is read digit by digit");
        assertEquals("zero", say("0"));
        assertEquals("twenty-five percent of it", say("25% of it"));
    }

    @Test
    void ordinalsCurrencyTimesAndRanges() {
        assertEquals("the first, the second, the twenty-first and the one hundredth", say("the 1st, the 2nd, the 21st and the 100th"));
        assertEquals("the twelfth and the thirty-third", say("the 12th and the 33rd"));
        assertEquals("five dollars", say("$5"));
        assertEquals("one dollar and fifty cents", say("$1.50"));
        assertEquals("one thousand pounds", say("\u00a31,000"));
        assertEquals("nine o'clock", say("9:00"));
        assertEquals("nine thirty", say("9:30"));
        assertEquals("nine oh five", say("9:05"));
        assertEquals("ten to twenty steps", say("10\u201320 steps"));
    }

    @Test
    void aYearIsReadAsAYearOnlyAfterAWordThatAnnouncesOne() {
        assertEquals("It was built in sixteen eighty-eight.", say("It was built in 1688."));
        assertEquals("since nineteen oh five", say("since 1905"));
        assertEquals("in nineteen hundred", say("in 1900"));
        assertEquals("in two thousand five", say("in 2005"));
        assertEquals("in twenty twenty-four", say("in 2024"));
        assertEquals("He climbed one thousand six hundred eighty-eight steps.", say("He climbed 1688 steps."), "not a year here");
    }

    @Test
    void abbreviationsAreSpelledOut() {
        assertEquals("Doctor Smith and Mister Jones, et cetera.", say("Dr. Smith and Mr. Jones, etc."));
        assertEquals("for example, a door", say("e.g., a door"));
        assertEquals("that is, a door", say("i.e. a door"));
        assertEquals("et cetera. Then he left", say("etc. Then he left"));
        assertEquals("Drake", say("Drake"), "a word that only starts like one is left alone");
    }

    @Test
    void aNumberedHeadingIsAChapter() {
        assertEquals("Chapter one. The Bricked Arch", heading("I. The Bricked Arch"));
        assertEquals("Chapter four. The Arch", heading("IV. The Arch"));
        assertEquals("Chapter four. The Arch", heading("IV The Arch"));
        assertEquals("Chapter four.", heading("IV"));
        assertEquals("Chapter four.", heading("Chapter IV"), "the word is there already");
        assertEquals("Chapter twelve. The Arch", heading("Chapter XII: The Arch"));
        assertEquals("Chapter three. Home", heading("3. Home"));
        assertEquals("Part two. Beginnings", heading("Part 2. Beginnings"));
    }

    @Test
    void aHeadingThatIsNotANumberIsLeftAlone() {
        assertEquals("I remember the day", heading("I remember the day"));
        assertEquals("C is for cat", heading("C is for cat"));
        assertEquals("three Men in a Boat", heading("3 Men in a Boat"), "not a chapter, only a number");
        assertEquals("MIX TAPE", heading("MIX TAPE"));
        assertEquals("CIVIC", heading("CIVIC"));
        assertEquals("The Arch", heading("The Arch"));
        // a roman numeral inside a body is not a chapter
        assertEquals("Henry VIII was there", say("Henry VIII was there"));
        assertEquals("IV. The Arch", say("IV. The Arch"), "only a heading is a chapter");
    }

    @Test
    void theWordBeforeTheChapterIsTheSettingOfTheBook() {
        SpokenSettings settings = new SpokenSettings();
        settings.setChapterWord("Kapitola");
        assertEquals("Kapitola four. The Arch", SpokenForm.of(settings).apply("IV. The Arch", true));
        settings.setChapterWord("");
        assertEquals("four. The Arch", SpokenForm.of(settings).apply("IV. The Arch", true));
    }

    @Test
    void markdownMarksAreNotSpoken() {
        assertEquals("a very old wall", say("a *very* **old** wall"));
        assertEquals("a very old wall", say("a _very_ old wall"));
        assertEquals("see the page", say("see [the page](https://example.com/x)"));
        assertEquals("call it run", say("call it `run`"));
        assertEquals("snake_case stays", say("snake_case stays"));
        assertEquals("2 3", SpokenForm.of(settings(SpokenSettings.Rules.NONE)).apply("2 * 3", false), "a stray asterisk is not spoken");
    }

    @Test
    void aSceneBreakIsNothing() {
        assertEquals("", say("***"));
        assertEquals("", say("* * *"));
        assertEquals("", say("---"));
        assertEquals("", say("\u2014 \u2014 \u2014"));
        assertEquals("", say("   "));
        assertEquals("", say(null));
    }

    @Test
    void theSoundTagsOfTheLabellingAreLeftAlone() {
        assertEquals("[sigh] Tomas, it is eleven.", say("[sigh] Tomas, it is 11."));
        assertEquals("[clear throat] Well", say("[clear throat] Well"));
    }

    @Test
    void theDictionaryReplacesWholeWordsAndWhatItPutsInIsNotTouched() {
        SpokenSettings settings = new SpokenSettings();
        settings.getDictionary().add(new SpokenSettings.Entry("Tomáš", "Tomash"));
        settings.getDictionary().add(new SpokenSettings.Entry("R2", "Artoo 2"));
        settings.getDictionary().add(new SpokenSettings.Entry("Mizuki", "ミズキ"));
        settings.getDictionary().add(new SpokenSettings.Entry("  ", "ignored"));
        SpokenForm form = SpokenForm.of(settings);

        assertEquals("Tomash said Tomash's name", form.apply("Tomáš said Tomáš's name", false));
        assertEquals("Tomášek stays", form.apply("Tomášek stays", false), "whole words only");
        assertEquals("Artoo 2 and eleven", form.apply("R2 and 11", false), "what the dictionary says is not turned into words again");
        assertEquals("ミズキ", form.apply("Mizuki", false));
        assertEquals("tomáš", form.apply("tomáš", false), "case matters");
    }

    @Test
    void theLongerWordOfTheDictionaryWins() {
        SpokenSettings settings = new SpokenSettings();
        settings.getDictionary().add(new SpokenSettings.Entry("Mizuki", "Mee-zoo-kee"));
        settings.getDictionary().add(new SpokenSettings.Entry("Mizuki Sato", "Mee-zoo-kee Sah-toe"));

        assertEquals("Mee-zoo-kee Sah-toe", SpokenForm.of(settings).apply("Mizuki Sato", false));
        assertEquals("Mee-zoo-kee", SpokenForm.of(settings).apply("Mizuki", false));
    }

    @Test
    void withoutTheEnglishRulesOnlyTheMarksAndTheDictionaryApply() {
        SpokenSettings settings = settings(SpokenSettings.Rules.NONE);
        settings.getDictionary().add(new SpokenSettings.Entry("Praha", "Prahá"));
        SpokenForm form = SpokenForm.of(settings);

        assertEquals("Měli jsme 5 minut v Prahá", form.apply("Měli jsme **5 minut** v Praha", false));
        assertEquals("IV. Kapitola", form.apply("IV. Kapitola", true));
    }

    @Test
    void aPieceInAnotherScriptGetsNoEnglishNumbers() {
        assertEquals("彼は5人を数えた", say("彼は5人を数えた"));
        assertEquals("Он пришёл в 5", say("Он пришёл в 5"));
    }

    @Test
    void theSettingsSurviveJson() {
        SpokenSettings settings = new SpokenSettings();
        settings.setRules(SpokenSettings.Rules.NONE);
        settings.setChapterWord("Kapitola");
        settings.getDictionary().add(new SpokenSettings.Entry("a", "b"));

        SpokenSettings back = new Gson().fromJson(new Gson().toJson(settings), SpokenSettings.class);

        assertEquals(SpokenSettings.Rules.NONE, back.getRules());
        assertEquals("Kapitola", back.getChapterWord());
        assertEquals("b", back.getDictionary().getFirst().getSpoken());
        // an old book has none of it
        SpokenSettings none = new Gson().fromJson("{}", SpokenSettings.class);
        assertEquals(SpokenSettings.Rules.ENGLISH, none.getRules());
        assertEquals("Chapter", SpokenSettings.DEFAULT_CHAPTER_WORD);
    }

    private static SpokenSettings settings(SpokenSettings.Rules rules) {
        SpokenSettings settings = new SpokenSettings();
        settings.setRules(rules);
        return settings;
    }
}
