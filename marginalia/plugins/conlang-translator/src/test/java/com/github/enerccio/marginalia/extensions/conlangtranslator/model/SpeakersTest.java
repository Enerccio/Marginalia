package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

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
}
