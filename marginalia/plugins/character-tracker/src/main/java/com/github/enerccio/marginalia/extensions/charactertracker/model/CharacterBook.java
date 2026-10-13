package com.github.enerccio.marginalia.extensions.charactertracker.model;

import java.util.*;

/**
 * The characters and relationships of a book, kept in memory (by uuid) while the story view is open. Changes are made
 * here and the owner stores the book with {@link #toData()} after each one.
 */
public class CharacterBook {

    private static final Comparator<StoryCharacter> BY_NAME = Comparator
            .comparing((StoryCharacter c) -> c.getName() == null ? "" : c.getName(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(StoryCharacter::getUuid);

    private final Map<String, StoryCharacter> characters = new LinkedHashMap<>();
    private final Map<String, CharacterRelation> relations = new LinkedHashMap<>();

    public CharacterBook() {
    }

    public CharacterBook(CharacterTrackerData data) {
        if (data == null) {
            return;
        }
        if (data.getCharacters() != null) {
            data.getCharacters().stream().filter(c -> c != null && c.getUuid() != null)
                    .forEach(c -> characters.put(c.getUuid(), c));
        }
        if (data.getRelations() != null) {
            // a relation of a character that is not there anymore is dropped
            data.getRelations().stream()
                    .filter(r -> r != null && r.getUuid() != null && r.getDirection() != null
                            && characters.containsKey(r.getSourceUuid()) && characters.containsKey(r.getTargetUuid()))
                    .forEach(r -> relations.put(r.getUuid(), r));
        }
    }

    public CharacterTrackerData toData() {
        CharacterTrackerData data = new CharacterTrackerData();
        data.setCharacters(new ArrayList<>(characters.values()));
        data.setRelations(new ArrayList<>(relations.values()));
        return data;
    }

    public StoryCharacter get(String uuid) {
        return characters.get(uuid);
    }

    /**
     * All characters ordered by name (ignoring case).
     */
    public List<StoryCharacter> sortedCharacters() {
        return characters.values().stream().sorted(BY_NAME).toList();
    }

    /**
     * Adds a new character or replaces the one with the same uuid.
     */
    public void put(StoryCharacter character) {
        characters.put(character.getUuid(), character);
    }

    /**
     * Removes the character and all its relationships.
     */
    public void delete(StoryCharacter character) {
        characters.remove(character.getUuid());
        relations.values().removeIf(r -> r.involves(character.getUuid()));
    }

    public List<CharacterRelation> allRelations() {
        return new ArrayList<>(relations.values());
    }

    /**
     * Relationships the character is the target of, the source of, or (unidirectional) either end of.
     */
    public List<CharacterRelation> relationsOf(StoryCharacter character) {
        return relations.values().stream().filter(r -> r.involves(character.getUuid())).toList();
    }

    public void put(CharacterRelation relation) {
        relations.put(relation.getUuid(), relation);
    }

    public void delete(CharacterRelation relation) {
        relations.remove(relation.getUuid());
    }
}
