package com.github.enerccio.marginalia.domain.service.impl.generation.dto;

/**
 * How a generation ended, see {@code GenerationProperties.OUTCOME}.
 */
public enum GenerationOutcome {
    /** The model finished the text. */
    COMPLETED,
    /** The user stopped it. The part keeps the text that arrived, if any. */
    CANCELLED,
    /** An error ended it ({@code GenerationProperties.FAILURE} says which). The part keeps the text that arrived, if any. */
    FAILED
}
