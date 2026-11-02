package com.github.enerccio.marginalia.extensions.bookdescription.model;

import com.github.enerccio.marginalia.domain.templates.TemplateData;

/**
 * The variables of the prompt for the name and the description of the book.
 */
public class DescriptionTemplateData extends TemplateData {

    /** The story: the messages (and summaries) of the range, or the notes made of them when the range was too long. */
    private String story;
    /** Whether {@link #story} is the notes made of the story, not the story itself. */
    private boolean fromNotes;
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

    public boolean isFromNotes() {
        return fromNotes;
    }

    public void setFromNotes(boolean fromNotes) {
        this.fromNotes = fromNotes;
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
