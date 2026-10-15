package com.github.enerccio.marginalia.extensions.arcoutline.model;

import com.github.enerccio.marginalia.domain.templates.TemplateData;

/**
 * The variables of the prompt for importing an arc from the written story.
 */
public class ImportGenerationTemplateData extends TemplateData {

    /** The entries of the book's lorebook (and its sub lorebooks). */
    private String backgroundLore;
    /** The arcs the book has, with their chapters. */
    private String existingArcs;
    /** The messages of the chosen range of the active branch. */
    private String story;
    /** How many chapters the author wants, in words. */
    private String numberOfChapters;

    public String getBackgroundLore() {
        return backgroundLore;
    }

    public void setBackgroundLore(String backgroundLore) {
        this.backgroundLore = backgroundLore;
    }

    public String getExistingArcs() {
        return existingArcs;
    }

    public void setExistingArcs(String existingArcs) {
        this.existingArcs = existingArcs;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getNumberOfChapters() {
        return numberOfChapters;
    }

    public void setNumberOfChapters(String numberOfChapters) {
        this.numberOfChapters = numberOfChapters;
    }
}
