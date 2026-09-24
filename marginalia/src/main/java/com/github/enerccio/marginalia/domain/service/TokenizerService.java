package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;

public interface TokenizerService {
    long countTokens(AI ai, String text) throws Exception;
    long countTokensApprox(String text) throws Exception;
    void invalidateCache(Long aiId);
}