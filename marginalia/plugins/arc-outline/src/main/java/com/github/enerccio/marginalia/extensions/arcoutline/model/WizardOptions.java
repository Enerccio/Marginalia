package com.github.enerccio.marginalia.extensions.arcoutline.model;

/**
 * What the plot wizard sends to the model besides the conversation, and how it is to help. Stored in the user settings
 * ({@link ArcOutlineSettings#getWizard()}): the dialog opens with the choices of the last time.
 */
public class WizardOptions {

    /** How the model helps; the text of each is in {@code Defaults} and goes into {@code {{mode}}} of the prompt. */
    public enum Mode {
        /** No particular task: the author leads. */
        FREE,
        /** Alternatives for where the story goes next. */
        EXPLORE,
        /** Holes, loose threads, promises nobody keeps. */
        CHALLENGE,
        /** A rough idea grown into an arc with its chapters. */
        EXPAND
    }

    /** How much of the outline is sent. */
    public enum OutlineDetail {
        NONE, TERSE, FULL
    }

    private Mode mode = Mode.FREE;
    private boolean lorebook = true;
    private OutlineDetail outline = OutlineDetail.FULL;
    /** The summaries of the story: they stand in for the story up to the newest of them. */
    private boolean summaries = true;
    /** The parts of the story after the newest summary (all of them without summaries), the newest ones that fit. */
    private boolean story = false;
    /** The most tokens of the story parts, null for a third of what the model takes. */
    private Integer storyTokens;
    /** The template of the system prompt, blank for {@code Defaults.WIZARD_PROMPT}. */
    private String prompt;

    public Mode getMode() {
        return mode == null ? Mode.FREE : mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public boolean isLorebook() {
        return lorebook;
    }

    public void setLorebook(boolean lorebook) {
        this.lorebook = lorebook;
    }

    public OutlineDetail getOutline() {
        return outline == null ? OutlineDetail.FULL : outline;
    }

    public void setOutline(OutlineDetail outline) {
        this.outline = outline;
    }

    public boolean isSummaries() {
        return summaries;
    }

    public void setSummaries(boolean summaries) {
        this.summaries = summaries;
    }

    public boolean isStory() {
        return story;
    }

    public void setStory(boolean story) {
        this.story = story;
    }

    public Integer getStoryTokens() {
        return storyTokens;
    }

    public void setStoryTokens(Integer storyTokens) {
        this.storyTokens = storyTokens;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
