package com.github.enerccio.marginalia.extensions.reallangtranslator.translate;

import com.github.enerccio.marginalia.extensions.reallangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.CharacterNote;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.SpeakersMode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What the model is told: the language, the script, the rules and the people; and that the system message does not depend on the part.
 */
class TranslationPromptTest {

    private static Speakers speakers() {
        Speakers speakers = new Speakers();
        CharacterNote mara = new CharacterNote("Mara");
        mara.setGender("woman");
        mara.setVoice("a young officer, polite to her seniors");
        CharacterNote ken = new CharacterNote("Ken");
        ken.setListed(true);
        speakers.getCharacters().add(mara);
        speakers.getCharacters().add(ken);
        return speakers;
    }

    @Test
    void theSystemMessageNamesTheLanguageTheScriptAndTheRules() {
        String system = TranslationPrompt.build(Presets.make("ja-kana-kanji"), new Speakers(), "x", TranslationPrompt.Scene.none()).system();

        assertTrue(system.contains("into Japanese"), system);
        assertTrue(system.contains("kana and kanji"), system);
        assertTrue(system.contains("=== THE LANGUAGE: JAPANESE ==="), system);
        assertTrue(system.contains("Politeness and speech level"), system);
        assertTrue(system.contains("Write the honorific suffixes"), system);
        assertFalse(system.contains("LEXICON"), "a real language has no lexicon");
    }

    @Test
    void theScriptRuleFollowsTheLanguage() {
        RealLanguage language = Presets.make("ja-romaji");

        String system = TranslationPrompt.build(language, new Speakers(), "x", TranslationPrompt.Scene.none()).system();

        assertTrue(system.contains("Hepburn romaji"), system);
        assertTrue(system.contains("\"reading\": always empty."), system);
        language.setScript(Script.KANA);
        assertTrue(TranslationPrompt.build(language, new Speakers(), "x", TranslationPrompt.Scene.none()).system().contains("no kanji"));
    }

    @Test
    void aReadingIsAskedForOnlyWhenTheLanguageWantsIt() {
        RealLanguage language = Presets.make("ja-kana-kanji");
        assertTrue(TranslationPrompt.build(language, new Speakers(), "x", TranslationPrompt.Scene.none()).system().contains("how the text is read"));

        language.setReading(false);
        assertTrue(TranslationPrompt.build(language, new Speakers(), "x", TranslationPrompt.Scene.none()).system().contains("\"reading\": always empty."));
    }

    @Test
    void honorificsOffAreSaidSo() {
        RealLanguage language = Presets.make("ja-kana-kanji");
        language.setHonorifics(false);

        assertTrue(TranslationPrompt.build(language, new Speakers(), "x", TranslationPrompt.Scene.none()).system().contains("Do not write honorific suffixes"));
    }

    @Test
    void theSystemMessageIsTheSameForEveryPartSoTheServerCanCacheIt() {
        RealLanguage language = Presets.make("ja-kana-kanji");

        String first = TranslationPrompt.build(language, speakers(), "Mara ran.", TranslationPrompt.Scene.none()).system();
        String second = TranslationPrompt.build(language, speakers(), "Ken waited.", new TranslationPrompt.Scene("Ken", "", List.of("Before."))).system();

        assertEquals(first, second);
    }

    @Test
    void theUserMessageHasTheCharactersNamedInThePartAndTheScene() {
        TranslationPrompt.Request request = TranslationPrompt.build(Presets.make("ja-kana-kanji"), speakers(), "Mara said, \"Hello.\"",
                new TranslationPrompt.Scene("Mara", "Mara, Tomo", List.of("The story before.")));

        assertTrue(request.user().contains("- Mara: speaks Japanese; woman; voice: a young officer"), request.user());
        assertFalse(request.user().contains("Ken"), "Ken is not in the part");
        assertTrue(request.user().contains("THE STORY BEFORE"));
        assertTrue(request.user().contains("Point of view: Mara"));
        assertTrue(request.user().contains("PART:\nMara said, \"Hello.\""));
    }

    @Test
    void aListedCharacterDoesNotKnowTheLanguageInTheDefaultMode() {
        String user = TranslationPrompt.build(Presets.make("ja-kana-kanji"), speakers(), "Ken said, \"Hello.\"", TranslationPrompt.Scene.none()).user();

        assertTrue(user.contains("- Ken: does NOT know Japanese"), user);
    }

    @Test
    void inTheOnlyModeTheListedSpeakIt() {
        Speakers speakers = speakers();
        speakers.setMode(SpeakersMode.ONLY);

        String request = TranslationPrompt.build(Presets.make("ja-kana-kanji"), speakers, "Ken said, \"Hello.\"", TranslationPrompt.Scene.none()).user();

        assertTrue(request.contains("- Ken: speaks Japanese"), request);
    }

    @Test
    void theQuickTranslationUsesTheSameLanguageBlock() {
        TranslationPrompt.Request request = QuickTranslation.build(Presets.make("ja-kana-kanji"), speakers(), "Good morning.", "Mara to her captain");

        assertTrue(request.system().contains("into Japanese"));
        assertTrue(request.system().contains("=== THE LANGUAGE: JAPANESE ==="));
        assertTrue(request.user().contains("CONTEXT (who says it, to whom, where): Mara to her captain"));
        assertTrue(request.user().contains("TEXT:\nGood morning."));
    }

    @Test
    void theQuickAnswerIsReadLeniently() {
        QuickTranslation.Answer answer = QuickTranslation.parse("```\n{\"text\":\" おはよう \",\"reading\":\"ohayou\",\"choice\":\"plain\"}\n```");

        assertNotNull(answer);
        assertEquals("おはよう", answer.text());
        assertEquals("ohayou", answer.reading());
        assertNull(QuickTranslation.parse("{\"text\":\"\"}"));
        assertNull(QuickTranslation.parse("nothing"));
    }

    private static List<GlossaryEntry> glossary(int extra) {
        List<GlossaryEntry> entries = new java.util.ArrayList<>(List.of(
                new GlossaryEntry("Saellen", GlossaryKind.WRITTEN, "サエレン", "saeren", "the ferrywoman, called -san"),
                new GlossaryEntry("Tomáš", GlossaryKind.KEEP, "", "", "")));
        for (int i = 0; i < extra; i++) {
            entries.add(new GlossaryEntry("Name" + i, GlossaryKind.WRITTEN, "ネーム" + i, "", ""));
        }
        return entries;
    }

    @Test
    void theGlossaryIsInTheSystemMessageAndTheModelIsAskedForTheNamesItChose() {
        String system = TranslationPrompt.build(Presets.make("ja-kana-kanji"), glossary(0), new Speakers(), "Saellen spoke.", TranslationPrompt.Scene.none()).system();

        assertTrue(system.contains("=== GLOSSARY ==="), system);
        assertTrue(system.contains("- Saellen: サエレン (read: saeren) - the ferrywoman, called -san"), system);
        assertTrue(system.contains("- Tomáš: keep it as it is"), system);
        assertTrue(system.contains("\"names\""), "the answer has a list of the names the model chose");
    }

    @Test
    void noGlossaryNoBlock() {
        String system = TranslationPrompt.build(Presets.make("ja-kana-kanji"), List.of(), new Speakers(), "x", TranslationPrompt.Scene.none()).system();

        assertFalse(system.contains("=== GLOSSARY ==="), system);
    }

    @Test
    void aSmallGlossaryGoesWholeEveryTimeSoTheSystemMessageDoesNotChangeWithThePart() {
        List<GlossaryEntry> entries = glossary(10);
        RealLanguage japanese = Presets.make("ja-kana-kanji");

        String one = TranslationPrompt.build(japanese, entries, new Speakers(), "Saellen spoke.", TranslationPrompt.Scene.none()).system();
        String other = TranslationPrompt.build(japanese, entries, new Speakers(), "Nobody spoke.", TranslationPrompt.Scene.none()).system();

        assertEquals(one, other);
        assertTrue(one.contains("Name9"));
    }

    @Test
    void aBigGlossaryGoesWithTheEntriesThePartHas() {
        List<GlossaryEntry> entries = glossary(TranslationPrompt.WHOLE_GLOSSARY);
        RealLanguage japanese = Presets.make("ja-kana-kanji");

        String system = TranslationPrompt.build(japanese, entries, new Speakers(), "Saellen met Name7 at the ferry.", TranslationPrompt.Scene.none()).system();

        assertTrue(system.contains("- Saellen:"), system);
        assertTrue(system.contains("- Name7:"), system);
        assertFalse(system.contains("- Name8:"), system);
    }

    @Test
    void theQuickTranslationGetsOnlyTheEntriesOfTheTextAndItsContext() {
        RealLanguage japanese = Presets.make("ja-kana-kanji");

        String system = QuickTranslation.build(japanese, glossary(3), new Speakers(), "Hello, Saellen.", "", null).system();

        assertTrue(system.contains("- Saellen:"), system);
        assertFalse(system.contains("- Name1:"), system);
    }
}
