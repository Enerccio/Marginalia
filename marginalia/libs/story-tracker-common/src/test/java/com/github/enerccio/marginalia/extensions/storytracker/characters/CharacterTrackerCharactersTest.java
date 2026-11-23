package com.github.enerccio.marginalia.extensions.storytracker.characters;

import com.github.enerccio.marginalia.extensions.storytracker.characters.CharacterTrackerCharacters.Candidate;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The characters of the Character Tracker read from the attributes of the book.
 */
class CharacterTrackerCharactersTest {

    /** The attributes of a book as the Character Tracker stores them (its {@code CharacterTrackerData}). */
    public static JsonObject attributes() {
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
    void readsTheNamedCharactersByNameWithTheirRelationships() {
        List<Candidate> candidates = CharacterTrackerCharacters.read(attributes());
        assertEquals(List.of("Fiora", "mara", "Thomas"), candidates.stream().map(Candidate::name).toList());
        assertEquals(List.of("Thomas", "mara"), candidates.get(0).related(), "a relationship with a missing character is left out");
        assertEquals("the smith", candidates.get(2).tagline());
        assertEquals("", candidates.get(1).tagline());
    }

    @Test
    void aBookWithoutTheCharacterTrackerHasNoCandidates() {
        assertTrue(CharacterTrackerCharacters.read(null).isEmpty());
        assertTrue(CharacterTrackerCharacters.read(new JsonObject()).isEmpty());
        JsonObject broken = new JsonObject();
        broken.addProperty(CharacterTrackerCharacters.KEY, "not an object");
        assertTrue(CharacterTrackerCharacters.read(broken).isEmpty());
    }
}
