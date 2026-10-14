package com.github.enerccio.marginalia.extensions.arcoutline.model;

/**
 * Where an arc or a chapter is in the story.
 */
public enum OutlineState {
    PLANNED("Planned"),
    CURRENT("Current"),
    FINISHED("Finished");

    private final String label;

    OutlineState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
