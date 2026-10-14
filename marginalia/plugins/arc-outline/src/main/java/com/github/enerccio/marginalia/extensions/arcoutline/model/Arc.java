package com.github.enerccio.marginalia.extensions.arcoutline.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A part of the story made of chapters.
 */
public class Arc extends OutlineNode {

    private List<Chapter> chapters = new ArrayList<>();

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    /**
     * Finishing an arc finishes all its chapters; the other states leave the chapters as they are.
     */
    @Override
    public void setState(OutlineState state) {
        super.setState(state);
        if (state == OutlineState.FINISHED && chapters != null) {
            chapters.forEach(chapter -> chapter.setState(OutlineState.FINISHED));
        }
    }
}
