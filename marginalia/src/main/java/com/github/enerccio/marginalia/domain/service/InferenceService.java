package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;

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

    /**
     * Streams a response for the payload and lets the model call the functions. When it does, the service calls the
     * Java methods (asynchronously, see {@link FunctionContinuation}), appends the calls and their results to its copy of the payload (the list passed in is not
     * modified), asks the model again and goes on until it answers without a call. The callback sees one continuous
     * stream of chunks and one completion; {@link InferenceAsyncCallback#onFunctionCall} reports each call.
     * <p>
     * After {@link #MAX_FUNCTION_ROUNDS} rounds of calls the model is told not to call anything, so that the stream
     * always ends with an answer.
     *
     * @param functions the functions to offer, null or empty for none; build with
     *                  {@link FunctionRegistry#bind}
     * @throws UnsupportedOperationException when the service can not call functions
     */
    default CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions,
                                     InferenceAsyncCallback callback) throws Exception {
        if (functions != null && !functions.isEmpty()) {
            throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support function calls");
        }
        return stream(payload, protocol, callback);
    }

    /** How many times the model may call functions in one request before it has to answer. */
    int MAX_FUNCTION_ROUNDS = 10;


    enum ChunkType {
        REASONING, RESPONSE
    }

    interface InferenceAsyncCallback {

        void onChunk(InferenceAsyncController controller, ChunkType chunkType, String text) throws Exception;
        void onCompletion() throws Exception;
        void onCancel() throws Exception;
        void onError(Throwable exception) throws Exception;
        boolean isDead();

        /**
         * The model called a function and the Java method has run (or the call failed). Called on the thread the
         * function answered on, before the model is asked again; the chunks that follow are its answer to the result.
         *
         * @param result the text the model gets, for a failed call the error message
         * @param failed whether the call ended in an error instead of a result
         */
        default void onFunctionCall(LLMToolCall call, String result, boolean failed) throws Exception {
        }

    }

    interface InferenceAsyncController {

        void continueInference();
        void terminateInference();

    }

}
