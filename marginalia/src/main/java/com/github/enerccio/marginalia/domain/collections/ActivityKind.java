package com.github.enerccio.marginalia.domain.collections;

/**
 * What went on in a book during a stretch of time, see {@link com.github.enerccio.marginalia.domain.model.impl.ActivityInterval}.
 * The kinds overlap in time (editing is inside open), so their times are never added to each other.
 */
public enum ActivityKind {

    /**
     * The book was open on the screen of the author.
     */
    OPEN,
    /**
     * The author was typing in a part.
     */
    EDIT,
    /**
     * A model was generating for the book.
     */
    GENERATE,
    /**
     * An agent was working on the book.
     */
    RUN

}
