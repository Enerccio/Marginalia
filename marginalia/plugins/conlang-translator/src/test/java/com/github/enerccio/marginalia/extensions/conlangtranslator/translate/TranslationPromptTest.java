package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.CharacterNote;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Speakers;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.SpeakersMode;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationPrompt.Request;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationPrompt.Scene;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The request: a system message that is the same for every part of a book (so it can be cached) and a user message with the part.
 */
class TranslationPromptTest {

    private static final String PART = "Mara drew her sword. \"I see the enemy,\" she said. Brog only nodded.";

    @Test
    void theSystemMessageHasTheInstructionsTheRulesTheRegistersAndTheLexicon() {
        Language language = TestLanguages.vrakht();

        Request request = TranslationPrompt.build(language, new Speakers(), PART, Scene.none(), 0);

        String system = request.system();
        assertTrue(system.contains("translate the direct speech of a story into a constructed language"));
        assertTrue(system.contains("\"language\": \"Vrakht\" when the speech is spoken in it"));
        assertTrue(system.contains("a character speaks Vrakht unless the CHARACTERS list says they do not know it."));
        assertTrue(system.contains("=== THE LANGUAGE: VRAKHT ==="));
        assertTrue(system.contains("The speech of the empire."));
        assertTrue(system.contains("--- Grammar ---\nA speech starts with a mood particle"));
        assertFalse(system.contains("Made by soldiers."), "a section for the designer is not sent");
        assertFalse(system.contains("private notes"), "the author's own notes are not sent");
        assertTrue(system.contains("=== LEXICON ===\n[Particles]\nDa - statement mood"));
        assertFalse(system.contains("%"), "every placeholder is filled");
    }

    @Test
    void theRegistersAreInTheSystemMessage() {
        String system = TranslationPrompt.build(TestLanguages.aethel(), new Speakers(), PART, Scene.none(), 0).system();

        assertTrue(system.contains("=== REGISTERS ===\nformal - healers and nobles\nsoft - lovers\nvulgar - soldiers\n"), system);
        assertTrue(system.contains("breast: mela (formal), melis (soft), lelya (vulgar)"));
    }

    @Test
    void theSystemMessageDoesNotDependOnThePartWhileTheLexiconIsSentWhole() {
        Language language = TestLanguages.vrakht();

        String one = TranslationPrompt.build(language, new Speakers(), PART, Scene.none(), 0).system();
        String two = TranslationPrompt.build(language, new Speakers(), "A different part with no speech.", new Scene("Brog", "Brog, Mara", List.of("Before.")), 0).system();

        assertEquals(one, two, "what is the same for every part is the same text, so the server's cache reads it once");
    }

    @Test
    void aWordAddedToTheLexiconChangesTheSystemMessageOnlyFromItsPlaceOn() {
        Language language = TestLanguages.vrakht();
        String before = TranslationPrompt.build(language, new Speakers(), PART, Scene.none(), 0).system();

        TestLanguages.word(language, language.getCategories().get(2), "eiz-blod", "veteran");
        String after = TranslationPrompt.build(language, new Speakers(), PART, Scene.none(), 0).system();

        assertTrue(after.startsWith(before.stripTrailing()), "everything that was there is untouched: the new word is at the end of its category");
        assertTrue(after.contains("eiz-blod - veteran"));
    }

    @Test
    void theUserMessageHasTheCharactersTheSceneAndThePart() {
        Language language = TestLanguages.vrakht();
        CharacterNote mara = new CharacterNote("Mara");
        mara.setGender("a woman");
        mara.setVoice("formal court speech");
        CharacterNote brog = new CharacterNote("Brog");
        brog.getAliases().add("the guard");
        brog.setListed(true);
        CharacterNote absent = new CharacterNote("Nobody In This Part");
        absent.setVoice("never mentioned");
        Speakers speakers = new Speakers();
        speakers.getCharacters().addAll(List.of(mara, brog, absent));

        String user = TranslationPrompt.build(language, speakers, PART, new Scene("Mara", "Mara, Brog", List.of("Earlier, Brog arrived.")), 0).user();

        assertTrue(user.contains("CHARACTERS:\n- Mara: speaks Vrakht; a woman; voice: formal court speech\n"), user);
        assertTrue(user.contains("- Brog (also the guard): does NOT know Vrakht\n"), user);
        assertFalse(user.contains("Nobody In This Part"), "a character that is not in the part is not listed");
        assertTrue(user.contains("THE STORY BEFORE (for who speaks to whom; do not translate it):\nEarlier, Brog arrived."));
        assertTrue(user.contains("Point of view: Mara\nCharacters present: Mara, Brog\n"));
        assertTrue(user.endsWith("PART:\n" + PART + "\n"));
    }

    @Test
    void inTheOtherModeTheListedCharactersAreTheOnesWhoSpeak() {
        Language language = TestLanguages.vrakht();
        Speakers speakers = new Speakers();
        speakers.setMode(SpeakersMode.ONLY);
        CharacterNote mara = new CharacterNote("Mara");
        mara.setListed(true);
        CharacterNote brog = new CharacterNote("Brog");
        speakers.getCharacters().addAll(List.of(mara, brog));

        Request request = TranslationPrompt.build(language, speakers, PART, Scene.none(), 0);

        assertTrue(request.system().contains("only the characters the CHARACTERS list marks as speaking Vrakht speak it."));
        assertTrue(request.user().contains("- Mara: speaks Vrakht\n"));
        assertTrue(request.user().contains("- Brog: does NOT know Vrakht\n"));
    }

    @Test
    void aPartWithNoNotesHasNoCharactersListAndNoEmptySections() {
        String user = TranslationPrompt.build(TestLanguages.vrakht(), new Speakers(), PART, Scene.none(), 0).user();

        assertEquals("\nPART:\n" + PART + "\n", user);
    }

    @Test
    void aHyphenThatIsStructuralIsSaidInTheRequest() {
        Language language = TestLanguages.vrakht();
        assertFalse(TranslationPrompt.build(language, new Speakers(), PART, Scene.none(), 0).system().contains("A hyphen is a part of a word"));

        language.setHyphenStructural(true);

        assertTrue(TranslationPrompt.build(language, new Speakers(), PART, Scene.none(), 0).system().contains("A hyphen is a part of a word of this language"));
    }
}
