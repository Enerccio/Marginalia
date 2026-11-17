package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

import com.github.enerccio.marginalia.extensions.storytracker.characters.CharacterTrackerCharacters;
import com.github.enerccio.marginalia.extensions.storytracker.characters.CharacterTrackerCharacters.Candidate;
import com.github.enerccio.marginalia.extensions.storytracker.scene.PresentCharacters;

import java.util.List;

/**
 * The characters of the Character Tracker plugin brought into the Knowledge Tracker of the same book.
 * <p>
 * The characters are read by {@link CharacterTrackerCharacters} of the library (plain JSON of the book, the other plugin is not
 * needed). Only the names are taken:
 * the tagline and the note describe a character, they are not something it knows. A relationship can be taken as a meeting:
 * the imported character has then met the people it is related to.
 */
public final class CharacterTrackerImport {

    /** What an import did: tracked characters added, and meetings recorded for them. */
    public record ImportResult(int added, int met) {
    }

    private CharacterTrackerImport() {
    }

    /**
     * The characters of {@code candidates} that {@code state} does not track yet (by name or alias).
     */
    public static List<Candidate> untracked(KnowledgeState state, List<Candidate> candidates) {
        return candidates.stream().filter(c -> state.findByName(c.name()) == null).toList();
    }

    /**
     * Starts tracking the {@code chosen} characters; one the state tracks already (or one chosen twice) is skipped.
     *
     * @param relationsAsMet whether an added character has met the people it is related to (as written by the user: a
     *                       reprocessing keeps them)
     */
    public static ImportResult importInto(KnowledgeState state, List<Candidate> chosen, boolean relationsAsMet) {
        int added = 0;
        int met = 0;
        for (Candidate candidate : chosen) {
            if (PresentCharacters.normalize(candidate.name()).isEmpty() || state.findByName(candidate.name()) != null) {
                continue;
            }
            TrackedCharacter character = new TrackedCharacter(candidate.name().strip());
            if (relationsAsMet) {
                candidate.related().forEach(person -> character.addMet(person, null));
                met += character.getMet().size();
            }
            state.getCharacters().add(character);
            added++;
        }
        return new ImportResult(added, met);
    }
}
