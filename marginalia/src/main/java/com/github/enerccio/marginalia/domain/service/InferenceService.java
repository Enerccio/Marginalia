package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface InferenceService {

    List<String> getModels();
    long countTokens(String text) throws Exception;
    long countTokensApprox(String text) throws Exception;
    void stream(List<LLMChatMessage> payload, InferenceAsyncCallback callback) throws Exception;


    enum ChunkType {
        REASONING, RESPONSE
    }

    interface InferenceAsyncCallback {

        void onChunk(InferenceAsyncController controller, ChunkType chunkType, String text) throws Exception;
        void onCompletion() throws Exception;
        void onError(Exception exception) throws Exception;

    }

    interface InferenceAsyncController {

        void continueInference();
        void terminateInference();

    }

}
