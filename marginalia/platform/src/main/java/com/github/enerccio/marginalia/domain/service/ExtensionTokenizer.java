package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.components.TokenizerKey;

/**
 * A tokenizer brought by an extension for the OpenAI compatible AIs (a custom endpoint, a model whose vocabulary the built-in
 * tokenizers do not know). The extension registers it with {@link TokenizerService#registerTokenizer} when it is loaded and
 * unregisters it when it is unloaded.
 * <p>
 * It is used only by the AIs whose user chose it as their tokenizer in the AI dialog, and it sees only the
 * {@link TokenizerKey#id() id of the key} (the endpoint and the model, never the API key or other settings of the AI) and the
 * text to count. Providers that are implemented by an extension count by themselves, see
 * {@code ExtensionAIFactory.createTokenizerResolver}.
 */
public interface ExtensionTokenizer {

    /**
     * Identifies the tokenizer, stored in the AIs that use it, so it must never change. The symbolic name of the bundle is a
     * good choice.
     */
    String getId();

    /**
     * Name for the user, in the selector of the AI dialog.
     */
    String getName();

    /**
     * Whether the tokenizer fits the endpoint and the model of the key. Used only to preselect it when the user creates an
     * AI, never to choose it for an AI that exists.
     */
    default boolean accepts(TokenizerKey key) {
        return false;
    }

    /**
     * The number of tokens of the text. Called often and in the middle of the work of the user, so it should be quick; an
     * exception makes the application count by the local estimate for that call.
     */
    long countTokens(TokenizerKey key, String text) throws Exception;
}
