package com.github.enerccio.marginalia.extensions.arcoutline.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The names the plot wizard shows for the numbers the model gives the characters and the relationships by.
 */
class CharacterNumbersTest {

    // what list-characters and list-character-relations of the Character Tracker return
    private static final String CHARACTERS = """
            {"characters":[{"number":1,"name":"Alice","tagline":"","relationships":1},
                           {"number":2,"name":"Bob","tagline":"","relationships":1},
                           {"number":3,"name":"7","tagline":"","relationships":0}]}""";
    private static final String RELATIONS = """
            {"relationships":[{"number":1,"source":"Alice","target":"Bob","bidirectional":false,"name":"sister"}]}""";

    private final CharacterNumbers numbers = CharacterNumbers.parse(CHARACTERS, RELATIONS);

    @Test
    void aCharacterGivenByItsNumberIsNamed() {
        assertEquals("Bob", numbers.subject("delete-character", CharacterNumbers.arguments("{\"character\":\"2\"}")));
        assertEquals("Bob", numbers.subject("delete-character", CharacterNumbers.arguments("{\"character\":2}")));
        assertEquals("2 (Bob)", numbers.describe("character", 2));
    }

    @Test
    void aNameStaysAsItIs() {
        assertEquals("alice", numbers.subject("update-character", Map.of("character", "alice")));
        assertEquals("alice", numbers.describe("character", "alice"));
        // a character whose name is a number is that character, as the command takes it
        assertEquals("7", numbers.describe("character", "7"));
        // what is not a character is not touched
        assertEquals("3", numbers.describe("color", "3"));
    }

    @Test
    void aRelationshipIsDescribedByItsCharacters() {
        assertEquals("Alice -> Bob (sister)", numbers.subject("delete-character-relation", Map.of("relationship", 1)));
        assertEquals("1 (Alice -> Bob (sister))", numbers.describe("relationship", 1));
        assertEquals("Alice -> Bob", numbers.subject("add-character-relation", Map.of("source", "1", "target", "Bob")));
    }

    @Test
    void theNewCharacterIsNamedByItsName() {
        assertEquals("Carol", numbers.subject("create-character", Map.of("name", " Carol ")));
        assertNull(numbers.subject("list-characters", Map.of()));
    }

    @Test
    void nothingKnownLeavesTheNumbers() {
        CharacterNumbers none = CharacterNumbers.parse("not json", null);
        assertSame(CharacterNumbers.NONE, none);
        assertEquals("2", none.describe("character", "2"));
        assertEquals("5", numbers.describe("character", "5"));
        assertEquals("2", none.subject("delete-character", Map.of("character", "2")));
    }
}
