package com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer;

import com.github.enerccio.marginalia.domain.components.TokenizerChoice;
import com.github.enerccio.marginalia.domain.components.TokenizerKey;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.ExtensionTokenizer;

/**
 * The tokenizer of a plugin as a strategy: it gets the key (the endpoint and the model) and the text, never the AI.
 */
public class ExtensionTokenizerStrategy implements TokenizerStrategy {

    private final ExtensionTokenizer tokenizer;
    private final TokenizerKey key;

    public ExtensionTokenizerStrategy(ExtensionTokenizer tokenizer, TokenizerKey key) {
        this.tokenizer = tokenizer;
        this.key = key;
    }

    public ExtensionTokenizer tokenizer() {
        return tokenizer;
    }

    @Override
    public long countTokens(AI ai, String text) throws Exception {
        return tokenizer.countTokens(key, text);
    }

    @Override
    public String getName() {
        return TokenizerChoice.EXTENSION_PREFIX + tokenizer.getId();
    }
}
