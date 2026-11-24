package com.github.enerccio.marginalia.extensions.statetracker.model;

import com.github.enerccio.marginalia.extensions.storytracker.characters.CharacterTrackerCharacters.Candidate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The characters of the Character Tracker imported as tracked characters with the categories the user picked.
 */
class CharacterTrackerImportTest {

    private static List<Candidate> candidates() {
        return List.of(new Candidate("Fiora", "", List.of()), new Candidate("mara", "", List.of()),
                new Candidate(" Thomas ", "the smith", List.of()));
    }

    @Test
    void importsTheUntrackedCharactersWithThePickedCategories() {
        StateBook state = new StateBook();
        Category weapon = new Category("weapon", "Weapon", CategoryKind.SINGLE, "");
        state.getCategories().add(weapon);
        TrackedCharacter fi = new TrackedCharacter("Fi");
        fi.setAliases(List.of("Fiora"));
        state.getCharacters().add(fi);
        Category mood = new Category("user-mood", "Mood", CategoryKind.DESCRIPTION, "");

        assertEquals(List.of("mara", " Thomas "), CharacterTrackerImport.untracked(state, candidates()).stream().map(Candidate::name).toList());

        assertEquals(2, CharacterTrackerImport.importInto(state, candidates(), List.of(mood, weapon)));
        Category moodCopy = state.copyOf("user-mood");
        assertNotNull(moodCopy, "a user category is copied into the book");
        TrackedCharacter thomas = state.findByName("thomas");
        assertEquals("Thomas", thomas.getName());
        assertEquals(List.of(moodCopy.getId(), "weapon"), thomas.getCategories());
        assertEquals(List.of(moodCopy.getId(), "weapon"), state.findByName("mara").getCategories());
        assertTrue(fi.getCategories().isEmpty(), "a character tracked already is left as it is");

        assertEquals(0, CharacterTrackerImport.importInto(state, candidates(), List.of(mood)), "a second import adds nothing");
        assertEquals(2, state.getCategories().size(), "the user category is not copied twice");
    }
}
