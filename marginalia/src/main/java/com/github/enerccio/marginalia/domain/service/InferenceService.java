package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;

import java.util.List;

public interface InferenceService {

    List<String> getModels();

    /**
     * Checks that the provider works with the configured model: lists the models and makes a minimal completion.
     * Failures are thrown ({@link InferenceException} for what the provider answered).
     *
     * @return the models the provider lists, empty when it doesn't list them
     */
    default List<String> testConnection() {
        return getModels();
    }

    long countTokens(String text) throws Exception;
    long countTokensApprox(String text) throws Exception;

    default CancellationToken stream(List<LLMChatMessage> payload, InferenceAsyncCallback callback) throws Exception {
        return stream(payload, null, callback);
    }

    /**
     * Streams a response for the payload. Sampling parameters and the response limit come from the protocol, the
     * response limit falls back to the AI (see {@link TokenLimits}). The protocol may be null.
     */
    CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, InferenceAsyncCallback callback) throws Exception;


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
