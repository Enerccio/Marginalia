package com.github.enerccio.marginalia.extensions.bookdescription.model;

import com.github.enerccio.marginalia.domain.templates.TemplateData;

/**
 * The variables of the prompt for the notes of one part of a story that is too long for the model.
 */
public class NotesTemplateData extends TemplateData {

    /** The messages (or summaries, or earlier notes) of this part. */
    private String story;
    /** The number of this part, from 1. */
    private int part;
    /** How many parts the story is divided into in this round. */
    private int parts;
    /** The name the book has now. */
    private String bookName;
    /** The description the book has now, blank for none. */
    private String bookDescription;

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public int getPart() {
        return part;
    }

    public void setPart(int part) {
        this.part = part;
    }

    public int getParts() {
        return parts;
    }

    public void setParts(int parts) {
        this.parts = parts;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookDescription() {
        return bookDescription;
    }

    public void setBookDescription(String bookDescription) {
        this.bookDescription = bookDescription;
    }
}
