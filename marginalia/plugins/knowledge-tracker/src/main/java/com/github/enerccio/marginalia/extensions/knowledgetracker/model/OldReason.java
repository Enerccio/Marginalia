package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

/**
 * Why an item is old (kept and shown, never sent to the model).
 */
public enum OldReason {
    /** The story (or the user) contradicted it. */
    RETRACTED,
    /** An addition that the user declined in the review. */
    REJECTED,
    /** The tidy pass dropped it (trivia, superseded, a duplicate) or merged it into another item. */
    TIDIED
}
