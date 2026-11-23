package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

import com.github.enerccio.marginalia.extensions.storytracker.characters.CharacterTrackerCharacters;
import com.github.enerccio.marginalia.extensions.storytracker.characters.CharacterTrackerCharacters.Candidate;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The characters of the Character Tracker imported as tracked characters.
 */
class CharacterTrackerImportTest {

    /** The attributes of a book as the Character Tracker stores them (its {@code CharacterTrackerData}). */
    private static JsonObject attributes() {
        String data = """
                {"characters": [
                   {"uuid": "t", "name": "Thomas", "tagline": "the smith", "note": "Fiora's father"},
                   {"uuid": "f", "name": " Fiora ", "tagline": "", "color": "#ffffff"},
                   {"uuid": "m", "name": "mara"},
                   {"uuid": "x", "name": "  "},
                   {"name": "No uuid"}
                 ],
                 "relations": [
                   {"uuid": "r1", "direction": "SOURCE_TARGET", "name": "father", "sourceUuid": "t", "targetUuid": "f"},
                   {"uuid": "r2", "direction": "UNIDIRECTIONAL", "name": "friends", "sourceUuid": "f", "targetUuid": "m"},
                   {"uuid": "r3", "direction": "SOURCE_TARGET", "name": "gone", "sourceUuid": "f", "targetUuid": "deleted"}
                 ],
                 "promptEnabled": false}
                """;
        JsonObject attributes = new JsonObject();
        attributes.add(CharacterTrackerCharacters.KEY, JsonParser.parseString(data));
        attributes.add("other-plugin", new JsonObject());
        return attributes;
    }

    @Test
    void importsOnlyTheCharactersThatAreNotTrackedYet() {
        KnowledgeState state = new KnowledgeState();
        TrackedCharacter fi = new TrackedCharacter("Fi");
        fi.setAliases(List.of("Fiora"));
        state.getCharacters().add(fi);

        List<Candidate> candidates = CharacterTrackerCharacters.read(attributes());
        List<Candidate> untracked = CharacterTrackerImport.untracked(state, candidates);
        assertEquals(List.of("mara", "Thomas"), untracked.stream().map(Candidate::name).toList());

        CharacterTrackerImport.ImportResult result = CharacterTrackerImport.importInto(state, candidates, false);
        assertEquals(new CharacterTrackerImport.ImportResult(2, 0), result);
        assertEquals(3, state.getCharacters().size());
        assertTrue(state.findByName("Thomas").getMet().isEmpty());
        assertTrue(state.findByName("Thomas").getItems().isEmpty(), "the tagline and the note are not knowledge");

        assertEquals(new CharacterTrackerImport.ImportResult(0, 0), CharacterTrackerImport.importInto(state, candidates, true),
                "a second import adds nothing");
    }

    @Test
    void relationshipsCanBeTakenAsMeetings() {
        KnowledgeState state = new KnowledgeState();
        List<Candidate> candidates = CharacterTrackerCharacters.read(attributes());
        CharacterTrackerImport.ImportResult result = CharacterTrackerImport.importInto(state, candidates, true);
        assertEquals(new CharacterTrackerImport.ImportResult(3, 4), result);
        TrackedCharacter fiora = state.findByName("fiora");
        assertEquals(List.of("Thomas", "mara"), fiora.getMet().stream().map(MetPerson::getName).toList());
        assertNull(fiora.getMet().getFirst().getSourceMessage(), "a meeting from a relationship is the user's own");
    }
}
