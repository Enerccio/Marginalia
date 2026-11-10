package com.github.enerccio.marginalia.domain.service.impl.inference;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceListener;
import com.github.enerccio.marginalia.domain.service.InferenceListener.FunctionCall;
import com.github.enerccio.marginalia.domain.service.InferenceListener.Observation;
import com.github.enerccio.marginalia.domain.service.InferenceListener.Outcome;
import com.github.enerccio.marginalia.domain.service.InferenceListener.Response;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceSession;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wraps the inference service of an AI while {@link InferenceSession#addListener listeners} are installed and tells them
 * about each request and how it ended. Everything is passed on to the wrapped service, which knows nothing of it; the
 * callbacks of the caller are called after the listeners were told. Made by {@code InferenceServicesImpl.forAI} for the
 * moment of the call, so a listener installed or removed later is seen by the next request.
 */
public class ObservedInferenceService implements InferenceService {

    private static final Logger log = LoggerFactory.getLogger(ObservedInferenceService.class);

    private final InferenceService delegate;
    private final AI ai;
    private final List<InferenceListener> listeners;

    public ObservedInferenceService(InferenceService delegate, AI ai, List<InferenceListener> listeners) {
        this.delegate = delegate;
        this.ai = ai;
        this.listeners = listeners;
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
        Run run = begin(payload, protocol);
        try {
            return delegate.stream(payload, protocol, run.stream(callback));
        } catch (Exception | Error e) {
            run.failed(e);
            throw e;
        }
    }

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions,
                                    InferenceAsyncCallback callback) throws Exception {
        Run run = begin(payload, protocol);
        try {
            return delegate.stream(payload, protocol, functions, run.stream(callback));
        } catch (Exception | Error e) {
            run.failed(e);
            throw e;
        }
    }

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, JsonNode schema, FunctionSet functions,
                                    InferenceAsyncCallback callback) throws Exception {
        Run run = begin(payload, protocol);
        try {
            return delegate.stream(payload, protocol, schema, functions, run.stream(callback));
        } catch (Exception | Error e) {
            run.failed(e);
            throw e;
        }
    }

    @Override
    public CancellationToken infer(List<LLMChatMessage> payload, Protocol protocol, JsonNode schema, FunctionSet functions,
                                   InferenceResultCallback callback) throws Exception {
        Run run = begin(payload, protocol);
        try {
            return delegate.infer(payload, protocol, schema, functions, run.result(callback));
        } catch (Exception | Error e) {
            run.failed(e);
            throw e;
        }
    }

    @Override
    public CancellationToken round(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions,
                                   InferenceRoundCallback callback) throws Exception {
        Run run = begin(payload, protocol);
        try {
            return delegate.round(payload, protocol, functions, run.round(callback));
        } catch (Exception | Error e) {
            run.failed(e);
            throw e;
        }
    }

    @Override
    public CancellationToken round(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions, boolean noFunctionCalls,
                                   InferenceRoundCallback callback) throws Exception {
        Run run = begin(payload, protocol);
        try {
            return delegate.round(payload, protocol, functions, noFunctionCalls, run.round(callback));
        } catch (Exception | Error e) {
            run.failed(e);
            throw e;
        }
    }

    /**
     * Tells the listeners that a request starts, on the calling thread.
     */
    private Run begin(List<LLMChatMessage> payload, Protocol protocol) {
        InferenceSession.Scope scope = InferenceSession.current();
        InferenceListener.Request request = new InferenceListener.Request(Instant.now(),
                scope == null ? null : scope.kind(), scope == null ? null : scope.scope(),
                ai.getName(), modelOf(ai), protocol == null ? null : protocol.getName(),
                listeners.stream().anyMatch(InferenceListener::wantsMessages) ? snapshot(payload) : List.of());
        List<Observation> observations = new ArrayList<>();
        for (InferenceListener listener : listeners) {
            try {
                Observation observation = listener.onRequest(request);
                if (observation != null) {
                    observations.add(observation);
                }
            } catch (Exception e) {
                log.debug("The inference listener {} failed on a request: {}", listener.getClass().getName(), e.getMessage(), e);
            }
        }
        return new Run(observations);
    }

    /**
     * The model id of the AI: the setting of an OpenAI compatible one, the {@code model} of the settings of an extension AI.
     */
    static String modelOf(AI ai) {
        try {
            if (ai instanceof OpenAICompatible compatible) {
                return compatible.getModel();
            }
            if (ai instanceof ExtensionAI extensionAI && extensionAI.getExtensionId() != null) {
                JsonElement model = extensionAI.getSettings().get("model");
                return model == null || !model.isJsonPrimitive() ? null : model.getAsString();
            }
        } catch (Exception e) {
            log.debug("No model of {}: {}", ai.getName(), e.getMessage());
        }
        return null;
    }

    private static List<InferenceListener.Message> snapshot(List<LLMChatMessage> payload) {
        List<InferenceListener.Message> messages = new ArrayList<>();
        if (payload == null) {
            return messages;
        }
        for (LLMChatMessage message : List.copyOf(payload)) {
            List<FunctionCall> calls = new ArrayList<>();
            if (message.getToolCalls() != null) {
                for (LLMToolCall call : message.getToolCalls()) {
                    calls.add(new FunctionCall(call.getName(), call.getArguments(), null, false));
                }
            }
            messages.add(new InferenceListener.Message(message.getRole() == null ? null : message.getRole().name(),
                    message.getContent(), message.getAttachments() == null ? 0 : message.getAttachments().size(), calls));
        }
        return messages;
    }

    /**
     * One observed request: collects what comes back and reports it once.
     */
    private static final class Run {

        private final List<Observation> observations;
        private final long start = System.nanoTime();
        private final AtomicBoolean finished = new AtomicBoolean();
        private final StringBuilder text = new StringBuilder();
        private final StringBuilder reasoning = new StringBuilder();
        private final List<FunctionCall> calls = Collections.synchronizedList(new ArrayList<>());

        private Run(List<Observation> observations) {
            this.observations = observations;
        }

        private void finish(Outcome outcome, String finalText, String finalReasoning, String error) {
            if (!finished.compareAndSet(false, true)) {
                return;
            }
            String answer;
            String thought;
            synchronized (this) {
                answer = finalText != null ? finalText : text.toString();
                thought = finalReasoning != null ? finalReasoning : (reasoning.isEmpty() ? null : reasoning.toString());
            }
            Response response = new Response(outcome, answer, thought, List.copyOf(calls), error,
                    (System.nanoTime() - start) / 1_000_000);
            for (Observation observation : observations) {
                try {
                    observation.onFinish(response);
                } catch (Exception e) {
                    log.debug("An inference observation failed: {}", e.getMessage(), e);
                }
            }
        }

        private void failed(Throwable e) {
            finish(Outcome.FAILED, null, null, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }

        private void chunk(ChunkType type, String chunk) {
            if (chunk == null) {
                return;
            }
            synchronized (this) {
                (type == ChunkType.REASONING ? reasoning : text).append(chunk);
            }
            for (Observation observation : observations) {
                try {
                    observation.onChunk(type, chunk);
                } catch (Exception e) {
                    log.debug("An inference observation failed on a chunk: {}", e.getMessage(), e);
                }
            }
        }

        private void call(LLMToolCall call, String result, boolean failed) {
            calls.add(new FunctionCall(call.getName(), call.getArguments(), result, failed));
        }

        InferenceAsyncCallback stream(InferenceAsyncCallback callback) {
            if (observations.isEmpty()) {
                return callback;
            }
            return new InferenceAsyncCallback() {
                @Override
                public void onChunk(InferenceAsyncController controller, ChunkType chunkType, String chunk) throws Exception {
                    chunk(chunkType, chunk);
                    callback.onChunk(controller, chunkType, chunk);
                }

                @Override
                public void onCompletion() throws Exception {
                    finish(Outcome.COMPLETED, null, null, null);
                    callback.onCompletion();
                }

                @Override
                public void onCancel() throws Exception {
                    finish(Outcome.CANCELLED, null, null, null);
                    callback.onCancel();
                }

                @Override
                public void onError(Throwable exception) throws Exception {
                    failed(exception);
                    callback.onError(exception);
                }

                @Override
                public boolean isDead() {
                    return callback.isDead();
                }

                @Override
                public void onFunctionCall(LLMToolCall call, String result, boolean failed) throws Exception {
                    call(call, result, failed);
                    callback.onFunctionCall(call, result, failed);
                }
            };
        }

        InferenceResultCallback result(InferenceResultCallback callback) {
            if (observations.isEmpty()) {
                return callback;
            }
            return new InferenceResultCallback() {
                @Override
                public void onResult(String answer, com.github.enerccio.marginalia.domain.service.structured.SchemaCheck check) throws Exception {
                    finish(Outcome.COMPLETED, answer, null, null);
                    callback.onResult(answer, check);
                }

                @Override
                public void onCancel() throws Exception {
                    finish(Outcome.CANCELLED, null, null, null);
                    callback.onCancel();
                }

                @Override
                public void onError(Throwable exception) throws Exception {
                    failed(exception);
                    callback.onError(exception);
                }

                @Override
                public boolean isDead() {
                    return callback.isDead();
                }

                @Override
                public void onFunctionCall(LLMToolCall call, String result, boolean failed) throws Exception {
                    call(call, result, failed);
                    callback.onFunctionCall(call, result, failed);
                }
            };
        }

        InferenceRoundCallback round(InferenceRoundCallback callback) {
            if (observations.isEmpty()) {
                return callback;
            }
            return new InferenceRoundCallback() {
                @Override
                public void onRound(String answer, List<LLMToolCall> asked, RoundUsage usage) throws Exception {
                    asked(asked);
                    finish(Outcome.COMPLETED, answer, null, null);
                    callback.onRound(answer, asked, usage);
                }

                @Override
                public void onRound(String answer, String thought, List<LLMToolCall> asked, RoundUsage usage) throws Exception {
                    asked(asked);
                    finish(Outcome.COMPLETED, answer, thought, null);
                    callback.onRound(answer, thought, asked, usage);
                }

                @Override
                public void onChunk(ChunkType chunkType, String chunk) {
                    chunk(chunkType, chunk);
                    callback.onChunk(chunkType, chunk);
                }

                @Override
                public void onCancel() throws Exception {
                    finish(Outcome.CANCELLED, null, null, null);
                    callback.onCancel();
                }

                @Override
                public void onError(Throwable exception) throws Exception {
                    failed(exception);
                    callback.onError(exception);
                }

                @Override
                public boolean isDead() {
                    return callback.isDead();
                }

                @Override
                public void onFunctionCall(LLMToolCall call, String result, boolean failed) throws Exception {
                    call(call, result, failed);
                    callback.onFunctionCall(call, result, failed);
                }

                private void asked(List<LLMToolCall> asked) {
                    if (asked != null) {
                        for (LLMToolCall call : asked) {
                            calls.add(new FunctionCall(call.getName(), call.getArguments(), null, false));
                        }
                    }
                }
            };
        }
    }
}
