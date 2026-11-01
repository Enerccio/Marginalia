package com.github.enerccio.marginalia.extensions.imagegenerator.service;

/**
 * One image to make.
 *
 * @param prompt   what the picture shows
 * @param negative what it must not show, may be empty
 * @param seed     the seed; a new one for every reroll
 */
public record ImageRequest(String prompt, String negative, long seed, int width, int height) {
}
