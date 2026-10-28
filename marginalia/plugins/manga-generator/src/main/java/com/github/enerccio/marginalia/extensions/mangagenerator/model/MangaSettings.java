package com.github.enerccio.marginalia.extensions.mangagenerator.model;

/**
 * The defaults of the user, kept in the attributes of the user settings. They are copied into a manga when it is
 * created, a manga keeps its own afterwards.
 */
public class MangaSettings {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.mangagenerator";

    public static final int DEFAULT_TOKEN_BUDGET = 4096;
    /** Story parts are long (a scene or more), a few of them already make a long section. */
    public static final int DEFAULT_SPLIT_COUNT = 3;
    public static final int DEFAULT_CONTEXT = 1;

    /** Put at the beginning of every request, before the lore. */
    private String initialQuery = "";
    private String defaultPromptOverride;
    private String defaultTagsPromptOverride;
    private String defaultAdditionalRules;
    private int defaultTokenBudget = DEFAULT_TOKEN_BUDGET;
    private int defaultSplitCount = DEFAULT_SPLIT_COUNT;
    private int defaultContextLeft = DEFAULT_CONTEXT;
    private int defaultContextRight = DEFAULT_CONTEXT;
    /** The model to use instead of the one of the book, null for the book's. */
    private Long selectedAiId;
    private Long selectedProtocolId;

    public String getInitialQuery() {
        return initialQuery == null ? "" : initialQuery;
    }

    public void setInitialQuery(String initialQuery) {
        this.initialQuery = initialQuery;
    }

    public String getDefaultPromptOverride() {
        return defaultPromptOverride;
    }

    public void setDefaultPromptOverride(String defaultPromptOverride) {
        this.defaultPromptOverride = defaultPromptOverride;
    }

    public String getDefaultTagsPromptOverride() {
        return defaultTagsPromptOverride;
    }

    public void setDefaultTagsPromptOverride(String defaultTagsPromptOverride) {
        this.defaultTagsPromptOverride = defaultTagsPromptOverride;
    }

    public String getDefaultAdditionalRules() {
        return defaultAdditionalRules;
    }

    public void setDefaultAdditionalRules(String defaultAdditionalRules) {
        this.defaultAdditionalRules = defaultAdditionalRules;
    }

    public int getDefaultTokenBudget() {
        return defaultTokenBudget;
    }

    public void setDefaultTokenBudget(int defaultTokenBudget) {
        this.defaultTokenBudget = defaultTokenBudget;
    }

    public int getDefaultSplitCount() {
        return defaultSplitCount;
    }

    public void setDefaultSplitCount(int defaultSplitCount) {
        this.defaultSplitCount = defaultSplitCount;
    }

    public int getDefaultContextLeft() {
        return defaultContextLeft;
    }

    public void setDefaultContextLeft(int defaultContextLeft) {
        this.defaultContextLeft = defaultContextLeft;
    }

    public int getDefaultContextRight() {
        return defaultContextRight;
    }

    public void setDefaultContextRight(int defaultContextRight) {
        this.defaultContextRight = defaultContextRight;
    }

    public Long getSelectedAiId() {
        return selectedAiId;
    }

    public void setSelectedAiId(Long selectedAiId) {
        this.selectedAiId = selectedAiId;
    }

    public Long getSelectedProtocolId() {
        return selectedProtocolId;
    }

    public void setSelectedProtocolId(Long selectedProtocolId) {
        this.selectedProtocolId = selectedProtocolId;
    }
}
