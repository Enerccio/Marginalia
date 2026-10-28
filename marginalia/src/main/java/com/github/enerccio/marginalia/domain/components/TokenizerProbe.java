package com.github.enerccio.marginalia.domain.components;

/**
 * What a tokenizer answered to a test: the number of tokens, or the reason it did not.
 */
public record TokenizerProbe(String name, Long tokens, String error) {

    public boolean worked() {
        return tokens != null;
    }
}
