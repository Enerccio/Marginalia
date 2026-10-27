package com.github.enerccio.marginalia.domain.collections;

/**
 * What the filter of a lorebook entry is looked for in.
 */
public enum ActivationContext {

    /** The instructions of the turn (the user prompt). */
    INSTRUCTIONS,
    /** The instructions and the last few parts of the story ({@code LorebookEntry#activationParts} of them). */
    INCLUDE_PARTS

}
