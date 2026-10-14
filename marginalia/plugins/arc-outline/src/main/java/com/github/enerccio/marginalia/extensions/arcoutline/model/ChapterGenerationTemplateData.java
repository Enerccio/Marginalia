package com.github.enerccio.marginalia.extensions.arcoutline.model;

import com.github.enerccio.marginalia.domain.templates.TemplateData;

/**
 * The variables of the prompt for generating the chapters of an arc.
 */
public class ChapterGenerationTemplateData extends TemplateData {

    /** The entries of the book's lorebook (and its sub lorebooks). */
    private String backgroundLore;
    /** The arcs (and chapters) up to the arc the chapters are written for, as the author chose to send them. */
    private String outline;
    /** How many chapters the author wants, in words. */
    private String numberOfChapters;
    /** What the author wants from the chapters in general. */
    private String generalSpecification;
    /** The numbered list of what the author wants from each chapter. */
    private String chapterSpecification;

    public String getBackgroundLore() {
        return backgroundLore;
    }

    public void setBackgroundLore(String backgroundLore) {
        this.backgroundLore = backgroundLore;
    }

    public String getOutline() {
        return outline;
    }

    public void setOutline(String outline) {
        this.outline = outline;
    }

    public String getNumberOfChapters() {
        return numberOfChapters;
    }

    public void setNumberOfChapters(String numberOfChapters) {
        this.numberOfChapters = numberOfChapters;
    }

    public String getGeneralSpecification() {
        return generalSpecification;
    }

    public void setGeneralSpecification(String generalSpecification) {
        this.generalSpecification = generalSpecification;
    }

    public String getChapterSpecification() {
        return chapterSpecification;
    }

    public void setChapterSpecification(String chapterSpecification) {
        this.chapterSpecification = chapterSpecification;
    }
}
