package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

/**
 * What of the lorebook goes into the request that asks what the characters learned.
 */
public enum LoreMode {
    /** Only the entries that the parts of the run activate, as a generation would. The default. */
    RESOLVE,
    /** Every entry of the book's lorebook and its sub lorebooks. */
    WHOLE,
    /** No lorebook. */
    NONE
}
