package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

/**
 * Where an item comes from, which decides whether a reprocess may drop it.
 */
public enum ItemOrigin {
    /** Written by the user as the initial state of the character. Never dropped by a reprocess. */
    INITIAL,
    /** Added or edited by the user later. Never dropped by a reprocess. */
    MANUAL,
    /** Learned from the story by the processor: has a source message and goes with it. */
    LEARNED
}
