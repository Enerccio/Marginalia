package com.github.enerccio.marginalia.extensions.imagegenerator.service;

/**
 * What the <i>Generate image</i> window last used for a part, kept in the part (its dynamic attributes, under {@code ImageSettings.KEY}) so
 * the window opens with it again. Null fields were not set.
 */
public record PartImageValues(String details, String style, String negative, String prompt, Integer width, Integer height) {
}
