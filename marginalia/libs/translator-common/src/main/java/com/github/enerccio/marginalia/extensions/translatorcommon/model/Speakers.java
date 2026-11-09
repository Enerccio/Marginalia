package com.github.enerccio.marginalia.extensions.translatorcommon.model;

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
     * Whether a part with these characters (its point of view and the ones present) may hold speech in the language: the translator asks the model
     * for a language only when it may. A language only the listed speak, with nobody listed, has no speakers: no. With no names the part says nothing,
     * so the answer is yes; a part whose characters are known and are all outside the speakers (every one excluded, or none of them listed when only
     * the listed speak it) holds none, as far as the metadata of the part goes. Narration can name a speaker the metadata does not list; the text of the part decides in the request, this only spares a request.
     *
     * @param names the names of the characters of the part, blank ones are ignored
     */
    public boolean mayBeInvolved(java.util.Collection<String> names) {
        if (getMode() == SpeakersMode.ONLY && getCharacters().stream().noneMatch(CharacterNote::isListed)) {
            // only the listed speak it and nobody is listed: nobody speaks it
            return false;
        }
        java.util.List<String> known = names == null ? java.util.List.of() : names.stream().filter(n -> n != null && !n.isBlank()).toList();
        // a character that has no note is not listed, so it speaks the language when everybody does and not when only the listed do
        return known.isEmpty() || known.stream().anyMatch(this::speaks);
    }

    /**
     * The names in the point of view and the present characters of a part (separated by commas, semicolons or lines), for {@link #mayBeInvolved}.
     */
    public static java.util.List<String> namesOf(String pointOfView, String present) {
        java.util.List<String> names = new ArrayList<>();
        for (String field : new String[]{pointOfView, present}) {
            if (field != null) {
                for (String name : field.split("[,;\\n]")) {
                    if (!name.isBlank()) {
                        names.add(name.strip());
                    }
                }
            }
        }
        return names;
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
