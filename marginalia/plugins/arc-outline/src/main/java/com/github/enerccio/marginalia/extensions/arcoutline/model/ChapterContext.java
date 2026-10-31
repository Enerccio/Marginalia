package com.github.enerccio.marginalia.extensions.arcoutline.model;

import com.github.enerccio.marginalia.extensions.arcoutline.ArcOutlineLocalization;

/**
 * How much of the outline the model sees when it writes the chapters of an arc. Only the arcs up to and including that
 * arc are ever sent.
 */
public enum ChapterContext {
    EVERYTHING(ArcOutlineLocalization.WHOLE_STORY_SO_FAR,
            ArcOutlineLocalization.THE_ARCS_UP_TO_AND_INCLUDING),
    ARCS_ONLY(ArcOutlineLocalization.ARC_SUMMARIES_ONLY,
            ArcOutlineLocalization.THE_ARCS_UP),
    ARCS_AND_FINISHED(ArcOutlineLocalization.ARC_SUMMARIES_AND_FINISHED,
            ArcOutlineLocalization.THE_ARCS_UP_TO_AND_INCLUDING_THIS_ONE);

    private final String label;
    private final String description;

    ChapterContext(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
