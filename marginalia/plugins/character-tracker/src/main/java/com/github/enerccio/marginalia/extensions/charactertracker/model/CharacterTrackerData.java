package com.github.enerccio.marginalia.extensions.charactertracker.model;

import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;

import java.util.ArrayList;
import java.util.List;

/**
 * What is stored in the attributes of the book under {@link #KEY}.
 */
public class CharacterTrackerData {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.charactertracker";

    private List<StoryCharacter> characters = new ArrayList<>();
    private List<CharacterRelation> relations = new ArrayList<>();

    /**
     * Whether the characters and their relationships are inserted into the prompt of every generation.
     */
    private boolean promptEnabled = false;
    /**
     * Number of prompt messages that follow the characters: 0 puts them after the current instructions (the very end
     * of the prompt), 1 right before them and so on.
     */
    private int promptDepth = 1;
    private LLMRole promptRole = LLMRole.SYSTEM;

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

    public boolean isPromptEnabled() {
        return promptEnabled;
    }

    public void setPromptEnabled(boolean promptEnabled) {
        this.promptEnabled = promptEnabled;
    }

    public int getPromptDepth() {
        return promptDepth;
    }

    public void setPromptDepth(int promptDepth) {
        this.promptDepth = promptDepth;
    }

    public LLMRole getPromptRole() {
        return promptRole;
    }

    public void setPromptRole(LLMRole promptRole) {
        this.promptRole = promptRole;
    }
}
