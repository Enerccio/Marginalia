package com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer;

import com.github.enerccio.marginalia.domain.model.impl.AI;

public class HeuristicTokenizerStrategy implements TokenizerStrategy {

    @Override
    public long countTokens(AI ai, String text) {
        return (long) Math.ceil(text.length() / 3.35);
    }

    @Override
    public String getName() {
        return "LOCAL_HEURISTIC";
    }
}