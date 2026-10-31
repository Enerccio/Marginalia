package com.github.enerccio.marginalia.extensions.arcoutline.model;

import com.github.enerccio.marginalia.extensions.arcoutline.ArcOutlineLocalization;

/**
 * Where an arc or a chapter is in the story.
 */
public enum OutlineState {
    PLANNED(ArcOutlineLocalization.PLANNED),
    CURRENT(ArcOutlineLocalization.CURRENT),
    FINISHED(ArcOutlineLocalization.FINISHED);

    private final String label;

    OutlineState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
