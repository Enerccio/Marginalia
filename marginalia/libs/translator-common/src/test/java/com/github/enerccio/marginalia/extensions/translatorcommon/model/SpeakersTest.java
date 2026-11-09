package com.github.enerccio.marginalia.extensions.translatorcommon.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Who speaks the language by default: everybody except the listed, or only the listed; names match by name or alias.
 */
class SpeakersTest {

    private static CharacterNote listed(String name, String... aliases) {
        CharacterNote note = new CharacterNote(name);
        note.getAliases().addAll(java.util.List.of(aliases));
        note.setListed(true);
        return note;
    }

    @Test
    void everybodySpeaksItExceptTheListedOnes() {
        Speakers speakers = new Speakers();
        speakers.getCharacters().add(listed("Mara", "the Captain"));
        CharacterNote voiceOnly = new CharacterNote("Brog");
        voiceOnly.setVoice("rough, crude soldier");
        speakers.getCharacters().add(voiceOnly);

        assertFalse(speakers.speaks("Mara"));
        assertFalse(speakers.speaks("  the captain "));
        assertTrue(speakers.speaks("Brog"));
        assertTrue(speakers.speaks("Somebody Else"));
    }

    @Test
    void onlyTheListedSpeakItInTheOtherMode() {
        Speakers speakers = new Speakers();
        speakers.setMode(SpeakersMode.ONLY);
        speakers.getCharacters().add(listed("Mara"));
        speakers.getCharacters().add(new CharacterNote("Brog"));

        assertTrue(speakers.speaks("MARA"));
        assertFalse(speakers.speaks("Brog"));
        assertFalse(speakers.speaks("Somebody Else"));
    }

    @Test
    void aNoteKeepsTheGenderAndTheVoiceForTheRequest() {
        Speakers speakers = new Speakers();
        CharacterNote mara = new CharacterNote("Mara");
        mara.setGender("a woman");
        mara.setVoice("formal court speech");
        speakers.getCharacters().add(mara);

        assertEquals("a woman", speakers.find("mara").getGender());
        assertEquals("formal court speech", speakers.find("Mara").getVoice());
        assertNull(speakers.find("nobody"));
        assertNull(speakers.find(null));
        assertNull(speakers.find(" "));
    }

    @Test
    void theNamesOfAPartAreItsPointOfViewAndTheCharactersPresent() {
        assertEquals(java.util.List.of("Mara", "Brog", "the cook", "Ana"), Speakers.namesOf("Mara", "Brog, the cook;\nAna"));
        assertTrue(Speakers.namesOf(null, " ").isEmpty());
    }

    @Test
    void aPartWithNoNamedCharactersMayHoldAnyLanguageThatHasSpeakers() {
        Speakers speakers = new Speakers();
        speakers.setMode(SpeakersMode.ONLY);
        speakers.getCharacters().add(listed("Mara"));

        assertTrue(speakers.mayBeInvolved(java.util.List.of()));
        assertTrue(speakers.mayBeInvolved(null));
        assertTrue(new Speakers().mayBeInvolved(null));
    }

    @Test
    void aLanguageAddedToABookHasNoSpeakersUntilTheyAreListed() {
        BookLanguage added = new BookLanguage("dwarf");

        assertEquals(SpeakersMode.ONLY, added.getSpeakers().getMode());
        assertFalse(added.getSpeakers().mayBeInvolved(java.util.List.of("Brog")));
        assertFalse(added.getSpeakers().mayBeInvolved(null));
    }

    @Test
    void aLanguageOfTheListedOnlyIsAskedForWhenOneOfThemIsInThePart() {
        Speakers speakers = new Speakers();
        speakers.setMode(SpeakersMode.ONLY);
        speakers.getCharacters().add(listed("Mara", "the Captain"));

        assertTrue(speakers.mayBeInvolved(java.util.List.of("Brog", "the captain")));
        assertFalse(speakers.mayBeInvolved(java.util.List.of("Brog", "The cook")));
    }

    @Test
    void aLanguageOfEverybodyExceptIsSkippedOnlyWhenEveryoneInThePartIsExcluded() {
        Speakers speakers = new Speakers();
        speakers.getCharacters().add(listed("Mara"));
        speakers.getCharacters().add(listed("Brog"));

        assertFalse(speakers.mayBeInvolved(java.util.List.of("Mara", "Brog")));
        assertTrue(speakers.mayBeInvolved(java.util.List.of("Mara", "the cook")));
    }

}
