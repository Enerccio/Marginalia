package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.components.TokenizerChoice;
import com.github.enerccio.marginalia.domain.components.TokenizerProbe;
import com.github.enerccio.marginalia.domain.model.impl.AI;

import java.util.List;

public interface TokenizerService {
    long countTokens(AI ai, String text) throws Exception;
    long countTokensApprox(String text) throws Exception;
    void invalidateCache(Long aiId);

    /**
     * Makes a tokenizer of an extension available to the OpenAI compatible AIs that are set to use it. Called by the extension
     * when it is loaded, undone with {@link #unregisterTokenizer} when it is unloaded.
     *
     * @throws IllegalStateException when another tokenizer has the same id
     */
    void registerTokenizer(ExtensionTokenizer tokenizer);

    void unregisterTokenizer(ExtensionTokenizer tokenizer);

    /**
     * What the user can set an OpenAI compatible AI to use: the built-in tokenizers, then those of the extensions that are loaded
     * (ordered by name). Not the automatic choice, which is no value (null).
     */
    List<TokenizerChoice> getChoices();

    /**
     * The id of the loaded tokenizer of an extension that accepts the endpoint and the model of the AI, null when none does.
     * To preselect it for a new AI, the AIs that exist are never changed by it.
     */
    String suggestChoice(AI ai);

    /**
     * Counts the text with each tokenizer the AI would try (the one it is set to, or all in the order of the automatic choice)
     * and tells what each answered. Nothing is cached. For the test of an AI in the dialog; calls the server, so not in a
     * transaction that writes.
     */
    List<TokenizerProbe> probe(AI ai, String text);
}
