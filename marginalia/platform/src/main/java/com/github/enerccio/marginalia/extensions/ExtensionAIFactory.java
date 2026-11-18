package com.github.enerccio.marginalia.extensions;

import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.TokenizerResolver;

/**
 * An inference provider implemented by an extension. The extension registers the factory with
 * {@code InferenceServices.registerExtensionFactory} when it is loaded and unregisters it when it is unloaded. The AIs
 * of the provider are {@link ExtensionAI}s with the {@link #getExtensionId() id} of the factory: the AI dialog offers the
 * registered factories when the user creates an AI of the type <i>Extension</i>, shows the {@link #createForm form} of
 * the factory for its settings, and {@code InferenceServices.forAI} asks the factory for the {@link InferenceService}
 * of the AI (the service is cached in the AI like the one of an OpenAI compatible AI).
 * <p>
 * An AI of an extension that is not loaded has no inference service: {@code forAI} returns {@code null} and the AI
 * dialog tells the user which extension is missing.
 */
public interface ExtensionAIFactory {

    /**
     * Identifies the provider, stored in every AI of it, so it must never change. Also the key of the settings of the
     * AI ({@link ExtensionAI#getSettings()}). The symbolic name of the bundle is a good choice.
     */
    String getExtensionId();

    /**
     * Name of the provider for the user, in the selector of the AI dialog and in the list of the AIs.
     */
    String getName();

    /**
     * Creates the inference service of the AI. Called again when the AI is loaded anew or saved, not for every request.
     * The AI may be a copy with the unsaved values of the AI dialog (without id) used to test the connection.
     */
    InferenceService createInferenceService(ExtensionAI ai) throws Exception;

    /**
     * Creates the form of the provider's settings for the tab of the AI dialog.
     *
     * @param ai the AI being edited, a new one (without id, with only the extension id set) when the user creates it;
     *           the form reads its {@link ExtensionAI#getSettings() settings}
     */
    ExtensionAIForm createForm(ExtensionAI ai);

    /**
     * How the tokens of the AIs of the provider are counted by {@code TokenizerService}, for a provider that has tokenizers
     * (even several, or a different one for each URL it calls). The resolver gives each AI a
     * {@link com.github.enerccio.marginalia.domain.components.TokenizerKey} (AIs with an equal key share the tokenizer found to
     * work) and the strategies to try for it. It is asked for the AIs of this provider only, so nothing of other AIs reaches it.
     * The service asks once for each registered factory and keeps the resolver until the factory is unregistered.
     * <p>
     * The provider uses it by calling {@code TokenizerService.countTokens(ai, text)} from
     * {@link InferenceService#countTokens}; the service counts approximately there when called in a read-write transaction.
     *
     * @return the resolver, null (the default) when the provider counts the tokens by itself
     */
    default TokenizerResolver createTokenizerResolver() {
        return null;
    }

}
