package com.github.enerccio.marginalia.extensions.charactertracker.model;

import java.util.ArrayList;
import java.util.List;

/**
 * What is stored in the attributes of the book under {@link #KEY}.
 */
public class CharacterTrackerData {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.charactertracker";

    private List<StoryCharacter> characters = new ArrayList<>();
    private List<CharacterRelation> relations = new ArrayList<>();

    public List<StoryCharacter> getCharacters() {
        return characters;
    }

    public void setCharacters(List<StoryCharacter> characters) {
        this.characters = characters;
    }

    public List<CharacterRelation> getRelations() {
        return relations;
    }

    public void setRelations(List<CharacterRelation> relations) {
        this.relations = relations;
    }
}
