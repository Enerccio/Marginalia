package com.github.enerccio.marginalia.extensions.imagegenerator.model;

import static com.github.enerccio.marginalia.extensions.imagegenerator.ImageGeneratorLocalization.T;

/**
 * How the language model writes the image prompt for a backend: what the image model behind it understands best. The built-in
 * instructions are in the prompt language of the user ({@code T.defaults()}).
 */
public enum PromptForm {

    /** Short comma separated phrases: models with a CLIP text encoder (SDXL, Pony, Illustrious, SD 1.5). */
    TAGS,

    /** A paragraph of plain sentences: models with a language model as the text encoder (Flux, Krea, Qwen-Image, Z-Image). */
    SENTENCES,

    /** The instruction the user wrote for the backend. */
    CUSTOM;

    /** The built-in instruction in the prompt language, null for {@link #CUSTOM}. */
    public String instruction() {
        return switch (this) {
            case TAGS -> T.defaults().tagsInstruction();
            case SENTENCES -> T.defaults().sentencesInstruction();
            case CUSTOM -> null;
        };
    }
}
