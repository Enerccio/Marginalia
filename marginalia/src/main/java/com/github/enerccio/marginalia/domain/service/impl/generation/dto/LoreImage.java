package com.github.enerccio.marginalia.domain.service.impl.generation.dto;

import com.github.enerccio.marginalia.domain.collections.InsertionMode;

/**
 * An image in the content of a lorebook entry the turn uses, sent to the model with the lore (see
 * {@link com.github.enerccio.marginalia.domain.service.impl.generation.GenerationProperties#LORE_IMAGES}).
 *
 * @param entryName the name of the entry
 * @param caption   the caption of the image in the entry (may be empty)
 * @param resource  the uuid of the resource
 * @param insertion where the entry's text goes: an image of the lore block goes into a message of its own after the system prompt,
 *                  the others with the user's prompt
 */
public record LoreImage(String entryName, String caption, String resource, InsertionMode insertion) {
}
