package com.github.enerccio.marginalia.extensions.arcoutline.model;

/**
 * How much of the outline the model sees when it writes the chapters of an arc. Only the arcs up to and including that
 * arc are ever sent.
 */
public enum ChapterContext {
    EVERYTHING("Whole story so far",
            "The arcs up to and including this one, each with all its chapters."),
    ARCS_ONLY("Arc summaries only",
            "The arcs up to and including this one, without any chapters."),
    ARCS_AND_FINISHED("Arc summaries and finished chapters",
            "The arcs up to and including this one, with only the chapters that are finished.");

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
