package com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer;

import com.github.enerccio.marginalia.domain.model.impl.AI;

/**
 * The tokenizer an AI is set to use is not there (the plugin is not loaded): every count fails, so the tokens are estimated.
 */
public class UnavailableTokenizerStrategy implements TokenizerStrategy {

    private final String name;

    public UnavailableTokenizerStrategy(String name) {
        this.name = name;
    }

    @Override
    public long countTokens(AI ai, String text) {
        throw new IllegalStateException("The tokenizer " + name + " is not available");
    }

    @Override
    public String getName() {
        return name;
    }
}
