package com.github.enerccio.marginalia.domain.collections;

/**
 * Where an {@link com.github.enerccio.marginalia.domain.model.impl.Annotation} is in its life.
 */
public enum AnnotationStatus {

    /**
     * Something to look at; shown in the text.
     */
    OPEN,

    /**
     * Dealt with; leaves the text, stays in the list.
     */
    RESOLVED,

    /**
     * Not worth dealing with; leaves the text, stays in the list.
     */
    DISMISSED,

    /**
     * The passage it was about is gone (deleted, or rewritten beyond finding); kept so that the author can attach it to
     * another passage or dismiss it.
     */
    ORPHANED;

    /**
     * @return true when the annotation is marked in the text of the part (if it has a range)
     */
    public boolean isMarked() {
        return this == OPEN;
    }

}
