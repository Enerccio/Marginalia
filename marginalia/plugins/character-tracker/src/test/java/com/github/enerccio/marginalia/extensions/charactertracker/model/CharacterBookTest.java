package com.github.enerccio.marginalia.extensions.charactertracker.model;

import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The characters and relationships of a book as the view and the commands change them.
 */
class CharacterBookTest {

    static StoryCharacter character(String uuid, String name) {
        StoryCharacter character = new StoryCharacter();
        character.setUuid(uuid);
        character.setName(name);
        return character;
    }

    static CharacterRelation relation(String uuid, StoryCharacter source, StoryCharacter target, RelationDirection direction) {
        CharacterRelation relation = new CharacterRelation();
        relation.setUuid(uuid);
        relation.setSourceUuid(source.getUuid());
        relation.setTargetUuid(target.getUuid());
        relation.setDirection(direction);
        return relation;
    }

    private static List<String> names(List<StoryCharacter> characters) {
        return characters.stream().map(StoryCharacter::getName).toList();
    }

    @Test
    void charactersAreSortedByNameIgnoringCase() {
        CharacterBook book = new CharacterBook();
        book.put(character("3", "carol"));
        book.put(character("1", "Bob"));
        book.put(character("2", "alice"));
        book.put(character("0", null));
        assertEquals(java.util.Arrays.asList(null, "alice", "Bob", "carol"), names(book.sortedCharacters()));
    }

    @Test
    void putReplacesTheCharacterWithTheSameUuid() {
        CharacterBook book = new CharacterBook();
        book.put(character("1", "Bob"));
        book.put(character("1", "Robert"));
        assertEquals(List.of("Robert"), names(book.sortedCharacters()));
        assertEquals("Robert", book.get("1").getName());
    }

    @Test
    void deletingACharacterDeletesItsRelations() {
        CharacterBook book = new CharacterBook();
        StoryCharacter alice = character("a", "Alice");
        StoryCharacter bob = character("b", "Bob");
        StoryCharacter carol = character("c", "Carol");
        List.of(alice, bob, carol).forEach(book::put);
        book.put(relation("ab", alice, bob, RelationDirection.SOURCE_TARGET));
        book.put(relation("cb", carol, bob, RelationDirection.UNIDIRECTIONAL));
        book.put(relation("ac", alice, carol, RelationDirection.SOURCE_TARGET));

        assertEquals(List.of("ab", "cb"), book.relationsOf(bob).stream().map(CharacterRelation::getUuid).toList());
        book.delete(alice);
        assertNull(book.get("a"));
        assertEquals(List.of("cb"), book.allRelations().stream().map(CharacterRelation::getUuid).toList());

        book.delete(book.allRelations().getFirst());
        assertTrue(book.allRelations().isEmpty());
        assertEquals(2, book.sortedCharacters().size());
    }

    @Test
    void theOtherEndOfARelation() {
        CharacterRelation relation = relation("r", character("a", "A"), character("b", "B"), RelationDirection.SOURCE_TARGET);
        assertEquals("b", relation.otherEnd("a"));
        assertEquals("a", relation.otherEnd("b"));
        assertTrue(relation.involves("a"));
        assertFalse(relation.involves("c"));
        assertFalse(relation.involves(null));
    }

    @Test
    void storedDataIsReadBackAndBrokenPartsAreDropped() {
        StoryCharacter alice = character("a", "Alice");
        StoryCharacter bob = character("b", "Bob");
        CharacterRelation kept = relation("ab", alice, bob, RelationDirection.SOURCE_TARGET);
        CharacterRelation toGone = relation("ag", alice, character("gone", "Gone"), RelationDirection.SOURCE_TARGET);
        CharacterRelation noDirection = relation("nd", alice, bob, null);
        CharacterTrackerData data = new CharacterTrackerData();
        data.setCharacters(new java.util.ArrayList<>(java.util.Arrays.asList(alice, null, character(null, "No uuid"), bob)));
        data.setRelations(new java.util.ArrayList<>(java.util.Arrays.asList(kept, toGone, noDirection, null)));
        data.setPromptEnabled(true);
        data.setPromptDepth(-4);
        data.setPromptRole(null);

        CharacterBook book = new CharacterBook(data);
        assertEquals(List.of("Alice", "Bob"), names(book.sortedCharacters()));
        assertEquals(List.of("ab"), book.allRelations().stream().map(CharacterRelation::getUuid).toList());
        assertTrue(book.isPromptEnabled());
        assertEquals(0, book.getPromptDepth());
        assertEquals(LLMRole.SYSTEM, book.getPromptRole());

        CharacterTrackerData stored = book.toData();
        assertEquals(2, stored.getCharacters().size());
        assertEquals(1, stored.getRelations().size());
        assertTrue(stored.isPromptEnabled());
        assertEquals(0, stored.getPromptDepth());
    }

    @Test
    void noDataIsAnEmptyBookWithTheDefaults() {
        CharacterBook book = new CharacterBook(null);
        assertTrue(book.sortedCharacters().isEmpty());
        assertFalse(book.isPromptEnabled());
        assertEquals(1, book.getPromptDepth());
        assertEquals(LLMRole.SYSTEM, book.getPromptRole());
        book.setPromptDepth(-1);
        book.setPromptRole(null);
        assertEquals(0, book.getPromptDepth());
        assertEquals(LLMRole.SYSTEM, book.getPromptRole());
    }
}
