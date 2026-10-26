package com.github.enerccio.marginalia.extensions.mangagenerator.model;

/**
 * One slice of the story and what the model made of it: the script (pages and panels) and the image tags of it. The
 * parts are numbered from 1 like in the outline of the story. Everything the manga has as a default (prompts, rules) is
 * overridden here when set.
 */
public class MangaSection {

    /** The parts the model gets as the background, around the parts it adapts. */
    private int contextStart = 1;
    private int contextEnd = 1;
    /** The parts the model adapts. */
    private int startPart = 1;
    private int endPart = 1;

    private String promptOverride;
    private String tagsPromptOverride;
    private String additionalRules;
    /** Rules added to the rules of the manga, not replacing them. */
    private String additionalRulesAddendum;

    private String script = "";
    private String reasoning = "";
    private long reasoningMillis;
    private boolean reasoningFinished;

    private String tags = "";
    private String tagsReasoning = "";
    private long tagsReasoningMillis;
    private boolean tagsReasoningFinished;

    public int getContextStart() {
        return contextStart;
    }

    public void setContextStart(int contextStart) {
        this.contextStart = contextStart;
    }

    public int getContextEnd() {
        return contextEnd;
    }

    public void setContextEnd(int contextEnd) {
        this.contextEnd = contextEnd;
    }

    public int getStartPart() {
        return startPart;
    }

    public void setStartPart(int startPart) {
        this.startPart = startPart;
    }

    public int getEndPart() {
        return endPart;
    }

    public void setEndPart(int endPart) {
        this.endPart = endPart;
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

    public String getAdditionalRulesAddendum() {
        return additionalRulesAddendum;
    }

    public void setAdditionalRulesAddendum(String additionalRulesAddendum) {
        this.additionalRulesAddendum = additionalRulesAddendum;
    }

    public String getScript() {
        return script == null ? "" : script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getReasoning() {
        return reasoning == null ? "" : reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public long getReasoningMillis() {
        return reasoningMillis;
    }

    public void setReasoningMillis(long reasoningMillis) {
        this.reasoningMillis = reasoningMillis;
    }

    public boolean isReasoningFinished() {
        return reasoningFinished;
    }

    public void setReasoningFinished(boolean reasoningFinished) {
        this.reasoningFinished = reasoningFinished;
    }

    public String getTags() {
        return tags == null ? "" : tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTagsReasoning() {
        return tagsReasoning == null ? "" : tagsReasoning;
    }

    public void setTagsReasoning(String tagsReasoning) {
        this.tagsReasoning = tagsReasoning;
    }

    public long getTagsReasoningMillis() {
        return tagsReasoningMillis;
    }

    public void setTagsReasoningMillis(long tagsReasoningMillis) {
        this.tagsReasoningMillis = tagsReasoningMillis;
    }

    public boolean isTagsReasoningFinished() {
        return tagsReasoningFinished;
    }

    public void setTagsReasoningFinished(boolean tagsReasoningFinished) {
        this.tagsReasoningFinished = tagsReasoningFinished;
    }
}
