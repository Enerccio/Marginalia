package com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer;

import com.github.enerccio.marginalia.domain.components.TokenizerKey;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.TokenizerResolver;

import java.util.List;

/**
 * For the AIs that have no tokenizer of their own: the estimate made in this process.
 */
public class LocalTokenizerResolver implements TokenizerResolver {

    private static final TokenizerKey KEY = TokenizerKey.of("local");

    private final TokenizerStrategy tokkit;

    public LocalTokenizerResolver(TokenizerStrategy tokkit) {
        this.tokkit = tokkit;
    }

    @Override
    public TokenizerKey keyFor(AI ai) {
        return KEY;
    }

    @Override
    public List<TokenizerStrategy> chain(TokenizerKey key, AI ai) {
        return List.of(tokkit);
    }
}
