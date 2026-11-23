package com.github.enerccio.marginalia.extensions.statetracker.model;

import com.github.enerccio.marginalia.extensions.storytracker.characters.CharacterTrackerCharacters;
import com.github.enerccio.marginalia.extensions.storytracker.characters.CharacterTrackerCharacters.Candidate;
import com.github.enerccio.marginalia.extensions.storytracker.scene.PresentCharacters;

import java.util.ArrayList;
import java.util.List;

/**
 * The characters of the Character Tracker plugin brought into the State Tracker of the same book, all of them tracking the categories
 * the user picked.
 * <p>
 * The characters are read by {@link CharacterTrackerCharacters} of the library (plain JSON of the book, the other plugin is not
 * needed). Only the names are taken: the tagline and the note describe a character, they are not its state.
 */
public final class CharacterTrackerImport {

    private CharacterTrackerImport() {
    }

    /** The characters of {@code candidates} that {@code state} does not track yet (by name or alias). */
    public static List<Candidate> untracked(StateBook state, List<Candidate> candidates) {
        return candidates.stream().filter(c -> state.findByName(c.name()) == null).toList();
    }

    /**
     * Starts tracking the {@code chosen} characters with the {@code categories}; one the state tracks already (or one chosen twice) is
     * skipped.
     *
     * @param categories the categories in the order the user gave: the book's own, or user categories, which are copied into the book
     *                   (once, also when no character is added)
     * @return how many characters were added
     */
    public static int importInto(StateBook state, List<Candidate> chosen, List<Category> categories) {
        List<String> ids = new ArrayList<>();
        for (Category category : categories) {
            Category own = state.findCategory(category.getId()) != null ? category : state.adopt(category);
            if (!ids.contains(own.getId())) {
                ids.add(own.getId());
            }
        }
        int added = 0;
        for (Candidate candidate : chosen) {
            if (PresentCharacters.normalize(candidate.name()).isEmpty() || state.findByName(candidate.name()) != null) {
                continue;
            }
            TrackedCharacter character = new TrackedCharacter(candidate.name().strip());
            character.setCategories(ids);
            state.getCharacters().add(character);
            added++;
        }
        return added;
    }
}
