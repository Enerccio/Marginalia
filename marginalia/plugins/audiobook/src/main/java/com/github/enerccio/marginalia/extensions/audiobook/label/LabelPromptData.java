package com.github.enerccio.marginalia.extensions.audiobook.label;

import com.github.enerccio.marginalia.domain.templates.TemplateData;

/** The variables of the labelling instruction (a template: a book, a folder or the user can change it). */
public class LabelPromptData extends TemplateData {

    /** The names of the characters known so far, comma separated; blank when none is known. */
    private String characters;
    /** The deliveries the model may choose, comma separated. */
    private String deliveries;
    /** The sound tags the model may insert, like {@code [laugh] [sigh]}. */
    private String tags;
    /** At most one sound tag per this many words. */
    private int wordsPerTag;

    public String getCharacters() {
        return characters;
    }

    public void setCharacters(String characters) {
        this.characters = characters;
    }

    public String getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(String deliveries) {
        this.deliveries = deliveries;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getWordsPerTag() {
        return wordsPerTag;
    }

    public void setWordsPerTag(int wordsPerTag) {
        this.wordsPerTag = wordsPerTag;
    }
}
