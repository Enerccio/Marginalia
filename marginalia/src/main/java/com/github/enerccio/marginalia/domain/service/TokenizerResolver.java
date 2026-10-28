package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.components.TokenizerKey;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer.TokenizerStrategy;

import java.util.List;

/**
 * Tells how the tokens of an AI are counted. The AI is given to the resolver of its own kind only, which decides what
 * identifies the way of counting ({@link #keyFor}) and builds the strategies to try ({@link #chain}); the
 * {@link TokenizerService} caches and probes by the key.
 */
public interface TokenizerResolver {

    /**
     * The identity of the way the AI counts: the AIs with an equal key share the strategy found to work.
     */
    TokenizerKey keyFor(AI ai);

    /**
     * The strategies to try for the key, the best first. Called when nothing is known for the key yet.
     */
    List<TokenizerStrategy> chain(TokenizerKey key, AI ai);
}
