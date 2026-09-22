package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.AIType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface InferenceService {

    List<String> getModels();
    long countTokens(String text) throws Exception;

}
