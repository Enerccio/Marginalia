package com.github.enerccio.marginalia.domain.service.impl.inference;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;

import java.util.List;

/**
 * Sends every request of an AI with its parts where the AI wants them ({@link PromptPlacements#forRequest}): the images before the
 * text, the function definitions and the rule about calls. Made by {@code InferenceServicesImpl.forAI} for an AI that needs it, for
 * the services of the core and of extensions alike, outside the observation so the listeners see the payload as it is sent.
 */
public class PlacedInferenceService implements InferenceService {

    private final InferenceService delegate;
    private final AI ai;

    public PlacedInferenceService(InferenceService delegate, AI ai) {
        this.delegate = delegate;
        this.ai = ai;
    }

    private List<LLMChatMessage> placed(List<LLMChatMessage> payload, FunctionSet functions, boolean noFunctionCalls) {
        return PromptPlacements.forRequest(ai, payload, functions, noFunctionCalls);
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        return type.isInstance(this) ? type.cast(this) : delegate.unwrap(type);
    }

    @Override
    public List<String> getModels() {
        return delegate.getModels();
    }

    @Override
    public List<String> testConnection() {
        return delegate.testConnection();
    }

    @Override
    public long countTokens(String text) throws Exception {
        return delegate.countTokens(text);
    }

    @Override
    public long countTokensApprox(String text) throws Exception {
        return delegate.countTokensApprox(text);
    }

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, InferenceAsyncCallback callback) throws Exception {
        return delegate.stream(placed(payload, null, false), protocol, callback);
    }

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions,
                                    InferenceAsyncCallback callback) throws Exception {
        return delegate.stream(placed(payload, functions, false), protocol, functions, callback);
    }

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, JsonNode schema, FunctionSet functions,
                                    InferenceAsyncCallback callback) throws Exception {
        return delegate.stream(placed(payload, functions, false), protocol, schema, functions, callback);
    }

    @Override
    public CancellationToken infer(List<LLMChatMessage> payload, Protocol protocol, JsonNode schema, FunctionSet functions,
                                   InferenceResultCallback callback) throws Exception {
        return delegate.infer(placed(payload, functions, false), protocol, schema, functions, callback);
    }

    @Override
    public CancellationToken round(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions,
                                   InferenceRoundCallback callback) throws Exception {
        return delegate.round(placed(payload, functions, false), protocol, functions, callback);
    }

    @Override
    public CancellationToken round(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions, boolean noFunctionCalls,
                                   InferenceRoundCallback callback) throws Exception {
        return delegate.round(placed(payload, functions, noFunctionCalls), protocol, functions, noFunctionCalls, callback);
    }
}
