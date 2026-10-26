package com.github.enerccio.marginalia.extensions.mangagenerator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A manga of the book: its sections, in the order of the story, and the settings they share. A null prompt or rules
 * text means the one of the user's defaults (and, when there is none, the built-in one).
 */
public class Manga {

    private String title = "Untitled";
    private List<MangaSection> sections = new ArrayList<>();

    private String promptOverride;
    private String tagsPromptOverride;
    private String additionalRules;

    /** The size of the script the model is asked for, in tokens. */
    private int tokenBudget = MangaSettings.DEFAULT_TOKEN_BUDGET;
    /** How many story parts a new section adapts. */
    private int splitCount = MangaSettings.DEFAULT_SPLIT_COUNT;
    /** How many parts before and after the adapted ones a new section gets as the context. */
    private int contextLeft = MangaSettings.DEFAULT_CONTEXT;
    private int contextRight = MangaSettings.DEFAULT_CONTEXT;

    public Manga() {
    }

    /**
     * A manga that starts with the settings the user chose as the defaults.
     */
    public Manga(String title, MangaSettings defaults) {
        this.title = title;
        this.promptOverride = defaults.getDefaultPromptOverride();
        this.tagsPromptOverride = defaults.getDefaultTagsPromptOverride();
        this.additionalRules = defaults.getDefaultAdditionalRules();
        this.tokenBudget = defaults.getDefaultTokenBudget();
        this.splitCount = defaults.getDefaultSplitCount();
        this.contextLeft = defaults.getDefaultContextLeft();
        this.contextRight = defaults.getDefaultContextRight();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<MangaSection> getSections() {
        if (sections == null) {
            sections = new ArrayList<>();
        }
        return sections;
    }

    public String getPromptOverride() {
        return promptOverride;
    }

    public void setPromptOverride(String promptOverride) {
        this.promptOverride = promptOverride;
    }

    public String getTagsPromptOverride() {
        return tagsPromptOverride;
    }

    public void setTagsPromptOverride(String tagsPromptOverride) {
        this.tagsPromptOverride = tagsPromptOverride;
    }

    public String getAdditionalRules() {
        return additionalRules;
    }

    public void setAdditionalRules(String additionalRules) {
        this.additionalRules = additionalRules;
    }

    public int getTokenBudget() {
        return tokenBudget;
    }

    public void setTokenBudget(int tokenBudget) {
        this.tokenBudget = tokenBudget;
    }

    public int getSplitCount() {
        return splitCount;
    }

    public void setSplitCount(int splitCount) {
        this.splitCount = splitCount;
    }

    public int getContextLeft() {
        return contextLeft;
    }

    public void setContextLeft(int contextLeft) {
        this.contextLeft = contextLeft;
    }

    public int getContextRight() {
        return contextRight;
    }

    public void setContextRight(int contextRight) {
        this.contextRight = contextRight;
    }
}
