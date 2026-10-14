package com.github.enerccio.marginalia.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.domain.service.structured.SchemaCheck;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputException;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs;

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

    default CancellationToken infer(List<LLMChatMessage> payload, InferenceResultCallback callback) throws Exception {
        return infer(payload, null, callback);
    }

    default CancellationToken infer(List<LLMChatMessage> payload, Protocol protocol, InferenceResultCallback callback) throws Exception {
        return infer(payload, protocol, (JsonNode) null, callback);
    }

    /**
     * Asks for the whole answer at once instead of a stream. Still asynchronous: the call returns at once and the
     * callback gets the text when the model is done. Sampling parameters, limits and usage counting are those of
     * {@link #stream(List, Protocol, InferenceAsyncCallback)}. Reasoning is not part of the result.
     *
     * @param schema JSON schema the answer must match, null for any text. The provider is asked to follow it
     *               (response format), and the answer is checked against it either way: the callback learns from
     *               {@link SchemaCheck} whether it is valid, an invalid answer is still delivered
     * @throws UnsupportedOperationException when the service can not do it
     */
    default CancellationToken infer(List<LLMChatMessage> payload, Protocol protocol, JsonNode schema,
                                    InferenceResultCallback callback) throws Exception {
        return infer(payload, protocol, schema, null, callback);
    }

    /**
     * Like {@link #infer(List, Protocol, JsonNode, InferenceResultCallback)}, and the model may call the functions
     * on the way, exactly as in {@link #stream(List, Protocol, FunctionSet, InferenceAsyncCallback)}: the calls run,
     * the model is asked again with the results, up to {@link #MAX_FUNCTION_ROUNDS} rounds. The callback gets one
     * result, that of the last round; {@link InferenceOutcomeCallback#onFunctionCall} reports each call.
     *
     * @param functions the functions to offer, null or empty for none; build with {@link FunctionRegistry#bind}
     * @throws UnsupportedOperationException when the service can not call functions (or can not infer)
     */
    default CancellationToken infer(List<LLMChatMessage> payload, Protocol protocol, JsonNode schema,
                                    FunctionSet functions, InferenceResultCallback callback) throws Exception {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support infer");
    }

    /**
     * Asks for an answer in the shape of the class: its JSON schema is built from its fields ({@link
     * com.github.enerccio.marginalia.domain.traits.LLMSchema}, {@link com.github.enerccio.marginalia.domain.traits.LLMField}),
     * the answer is checked against it and read into an instance. An answer that doesn't match ends in
     * {@link TypedInferenceCallback#onError} with a {@link StructuredOutputException}.
     */
    default <T> CancellationToken infer(List<LLMChatMessage> payload, Protocol protocol, Class<T> type,
                                        TypedInferenceCallback<T> callback) throws Exception {
        return infer(payload, protocol, type, null, callback);
    }

    /** Like the above, the model may call the functions on the way (see the variant with a schema). */
    default <T> CancellationToken infer(List<LLMChatMessage> payload, Protocol protocol, Class<T> type,
                                        FunctionSet functions, TypedInferenceCallback<T> callback) throws Exception {
        return infer(payload, protocol, StructuredOutputs.schemaOf(type), functions, new InferenceResultCallback() {
            @Override
            public void onResult(String text, SchemaCheck check) throws Exception {
                if (!check.valid()) {
                    callback.onError(new StructuredOutputException(text, check.errors(), null));
                    return;
                }
                T result;
                try {
                    result = StructuredOutputs.parse(text, type);
                } catch (StructuredOutputException e) {
                    callback.onError(e);
                    return;
                }
                callback.onResult(result, text);
            }

            @Override
            public void onFunctionCall(LLMToolCall call, String result, boolean failed) throws Exception {
                callback.onFunctionCall(call, result, failed);
            }

            @Override
            public void onCancel() throws Exception {
                callback.onCancel();
            }

            @Override
            public void onError(Throwable exception) throws Exception {
                callback.onError(exception);
            }

            @Override
            public boolean isDead() {
                return callback.isDead();
            }
        });
    }

    default <T> CancellationToken infer(List<LLMChatMessage> payload, Class<T> type, TypedInferenceCallback<T> callback) throws Exception {
        return infer(payload, null, type, callback);
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

    /** What every asynchronous call ends in besides the result. */
    interface InferenceOutcomeCallback {

        void onCancel() throws Exception;
        void onError(Throwable exception) throws Exception;

        /** Whether nobody waits for the answer any more (the request is then dropped). */
        default boolean isDead() {
            return false;
        }

        /** Same as {@link InferenceAsyncCallback#onFunctionCall}. */
        default void onFunctionCall(LLMToolCall call, String result, boolean failed) throws Exception {
        }

    }

    /** The result of {@code infer}: the whole text. */
    interface InferenceResultCallback extends InferenceOutcomeCallback {

        /**
         * @param text  what the model wrote
         * @param check whether it matches the schema; {@link SchemaCheck#checked()} is false when none was passed
         */
        void onResult(String text, SchemaCheck check) throws Exception;

    }

    /** The result of {@code infer} with a class: the answer read into an instance. */
    interface TypedInferenceCallback<T> extends InferenceOutcomeCallback {

        /**
         * @param result the answer as an object
         * @param text   what the model wrote
         */
        void onResult(T result, String text) throws Exception;

    }

    interface InferenceAsyncController {

        void continueInference();
        void terminateInference();

    }

}
