package com.github.enerccio.marginalia.extensions.storytracker.llm;

/**
 * What of the lorebook goes into a request that reads the story (what the characters learned, what changed).
 */
public enum LoreMode {
    /** Only the entries that the parts of the run activate, as a generation would. The default. */
    RESOLVE,
    /** Every entry of the book's lorebook and its sub lorebooks. */
    WHOLE,
    /** No lorebook. */
    NONE
}
