package com.github.enerccio.marginalia.extensions.imagegenerator.service;

/**
 * One image to make.
 *
 * @param prompt       what the picture shows
 * @param negative     what it must not show, may be empty
 * @param seed         the seed; a new one for every reroll
 * @param lora         ComfyUI: the file of the LoRA for {@code {{lora}}}; null or blank for none (the LoRA nodes are bypassed)
 * @param loraStrength ComfyUI: the number for {@code {{lora_strength}}}
 */
public record ImageRequest(String prompt, String negative, long seed, int width, int height, String lora, double loraStrength) {

    public ImageRequest(String prompt, String negative, long seed, int width, int height) {
        this(prompt, negative, seed, width, height, null, 1.0);
    }

    /** Whether a LoRA was chosen. */
    public boolean hasLora() {
        return lora != null && !lora.isBlank();
    }
}
