package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Who speaks a language: by default everybody except the listed characters, or only the listed ones. This is the default only; the text of
 * the part overrides it ("she said in Common", "the guard could not understand a word"), which the model reads.
 */
public class Speakers {

    private SpeakersMode mode = SpeakersMode.EVERYONE_EXCEPT;
    private List<CharacterNote> characters = new ArrayList<>();

    /**
     * Whether the character speaks the language by default. A character that is not in the list of notes is not listed.
     */
    public boolean speaks(String name) {
        CharacterNote note = find(name);
        boolean listed = note != null && note.isListed();
        return getMode() == SpeakersMode.EVERYONE_EXCEPT ? !listed : listed;
    }

    /**
     * The note of the character called {@code name} (its name or an alias), or null.
     */
    public CharacterNote find(String name) {
        return getCharacters().stream().filter(note -> note.isCalled(name)).findFirst().orElse(null);
    }

    public SpeakersMode getMode() {
        return mode == null ? SpeakersMode.EVERYONE_EXCEPT : mode;
    }

    public void setMode(SpeakersMode mode) {
        this.mode = mode;
    }

    public List<CharacterNote> getCharacters() {
        if (characters == null) {
            characters = new ArrayList<>();
        }
        return characters;
    }

    public void setCharacters(List<CharacterNote> characters) {
        this.characters = characters;
    }
}
