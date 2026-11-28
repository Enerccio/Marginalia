package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;

/**
 * Counts the tokens an image takes in the prompt of a model: the part of the token budget the text tokenizers can't see.
 * The application estimates every image by its size in pixels ({@code EstimatedImageTokenizer}); a provider whose API counts
 * images itself (Anthropic's and Google's {@code countTokens}) brings its own through
 * {@link TokenizerResolver#imageChain}. {@link TokenizerService#countImageTokens} tries those first and falls back to the
 * estimate when they fail.
 */
public interface ImageTokenizer {

    /**
     * Name for the logs and the tests.
     */
    String getName();

    /**
     * The tokens of the image in a request of the AI. An attachment that is not an image counts 0. Called while a prompt is
     * built, once per image (the counts are not cached by the caller), so an online count should cache by the bytes; an
     * exception makes the application estimate the image instead.
     */
    long countImageTokens(AI ai, LLMAttachment image) throws Exception;
}
