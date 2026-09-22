package com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer;

import com.github.enerccio.marginalia.domain.model.impl.AI;

public interface TokenizerStrategy {

    long countTokens(AI ai, String text) throws Exception;

    String getName();
}