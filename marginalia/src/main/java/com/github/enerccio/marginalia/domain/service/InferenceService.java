package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;

import java.util.List;

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
        void onCancel() throws Exception;
        void onError(Throwable exception) throws Exception;
        boolean isDead();

    }

    interface InferenceAsyncController {

        void continueInference();
        void terminateInference();

    }

}
