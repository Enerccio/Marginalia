package com.github.enerccio.marginalia.extensions.arcoutline.model;

import com.github.enerccio.marginalia.domain.templates.TemplateData;

/**
 * The variables of the system prompt of the plot wizard. A part the author did not choose to send is empty and its
 * {@code has...} flag is false, so the template can leave out its heading.
 */
public class PlotWizardTemplateData extends TemplateData {

    /** The entries of the book's lorebook (and its sub lorebooks). */
    private String backgroundLore = "";
    /** The arcs the book has, with their chapters (and notes when the author wants them). */
    private String outline = "";
    /** The summaries of the story, oldest first. */
    private String summaries = "";
    /** The newest parts of the story after the summaries. */
    private String story = "";
    /** How the model is to help (the mode). */
    private String mode = "";

    public boolean isHasLore() {
        return !backgroundLore.isBlank();
    }

    public boolean isHasOutline() {
        return !outline.isBlank();
    }

    public boolean isHasSummaries() {
        return !summaries.isBlank();
    }

    public boolean isHasStory() {
        return !story.isBlank();
    }

    public boolean isHasMode() {
        return !mode.isBlank();
    }

    public String getBackgroundLore() {
        return backgroundLore;
    }

    public void setBackgroundLore(String backgroundLore) {
        this.backgroundLore = backgroundLore == null ? "" : backgroundLore;
    }

    public String getOutline() {
        return outline;
    }

    public void setOutline(String outline) {
        this.outline = outline == null ? "" : outline;
    }

    public String getSummaries() {
        return summaries;
    }

    public void setSummaries(String summaries) {
        this.summaries = summaries == null ? "" : summaries;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story == null ? "" : story;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode == null ? "" : mode;
    }
}
