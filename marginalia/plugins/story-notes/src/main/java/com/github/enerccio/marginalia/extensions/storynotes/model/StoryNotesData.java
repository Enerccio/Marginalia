package com.github.enerccio.marginalia.extensions.storynotes.model;

import java.util.ArrayList;
import java.util.List;

/**
 * What is stored in the attributes of the book under {@link #KEY}.
 */
public class StoryNotesData {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.storynotes";

    private List<NotePage> pages = new ArrayList<>();

    public List<NotePage> getPages() {
        return pages;
    }

    public void setPages(List<NotePage> pages) {
        this.pages = pages;
    }
}
