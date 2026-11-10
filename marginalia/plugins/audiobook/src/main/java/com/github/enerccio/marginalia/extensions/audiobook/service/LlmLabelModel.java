package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceSession;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.structured.SchemaCheck;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputException;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs;
import com.github.enerccio.marginalia.extensions.audiobook.label.LabelAnswer;
import com.github.enerccio.marginalia.extensions.audiobook.label.LabelCancel;
import com.github.enerccio.marginalia.extensions.audiobook.label.LabelModel;
import com.github.enerccio.marginalia.extensions.audiobook.label.LabelWatch;

import java.util.List;
import java.util.concurrent.*;

/**
 * The language model of the user as a {@link LabelModel}: one structured request (the schema of {@link LabelAnswer}), waited for. The call is made
 * in an {@link InferenceSession} of its own kind, so the proxy that swaps the cache of the one slot of the model keeps the labelling apart from
 * the story of the user. The answer must match the schema; one that does not ends the labelling with the reason.
 */
public class LlmLabelModel implements LabelModel {

    /** A part of a thousand words is a few thousand tokens of answer; a model that thinks first takes minutes, and the first call may have to load it. */
    static final long TIMEOUT_MINUTES = 20;

    private final InferenceService service;
    private final Protocol protocol;

    /** @param protocol the sampling and limits, null for the defaults of the AI */
    public LlmLabelModel(InferenceService service, Protocol protocol) {
        this.service = service;
        this.protocol = protocol;
    }

    @Override
    public LabelAnswer ask(String system, String user) throws Exception {
        CompletableFuture<LabelAnswer> done = new CompletableFuture<>();
        if (LabelCancel.requested()) {
            throw new LabelCancel.Stopped();
        }
        CancellationToken token;
        List<LLMChatMessage> payload = List.of(LLMChatMessage.of(LLMRole.SYSTEM, system), LLMChatMessage.of(LLMRole.USER, user));
        // read here: the request is answered on other threads
        LabelWatch.Listener watch = LabelWatch.current();
        try (InferenceSession _ = InferenceSession.open("audiobook")) {
            token = watch == null ? infer(payload, done) : streamOrInfer(payload, done, watch);
        }
        long deadline = System.nanoTime() + TimeUnit.MINUTES.toNanos(TIMEOUT_MINUTES);
        try {
            while (true) {
                try {
                    return done.get(200, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    if (LabelCancel.requested()) {
                        // the request to the model is dropped at once, not when its answer is complete
                        if (token != null) {
                            token.cancel();
                        }
                        done.completeExceptionally(new LabelCancel.Stopped());
                        throw new LabelCancel.Stopped();
                    }
                    if (System.nanoTime() > deadline) {
                        if (token != null) {
                            token.cancel();
                        }
                        throw e;
                    }
                }
            }
        } catch (ExecutionException e) {
            throw e.getCause() instanceof Exception cause ? cause : e;
        }
    }

    /** The request without anyone watching: the whole answer at once. */
    private CancellationToken infer(List<LLMChatMessage> payload, CompletableFuture<LabelAnswer> done) throws Exception {
        return service.infer(payload, protocol, LabelAnswer.class, new InferenceService.TypedInferenceCallback<LabelAnswer>() {
            @Override
            public void onResult(LabelAnswer result, String text) {
                done.complete(result);
            }

            @Override
            public void onCancel() {
                done.completeExceptionally(new CancellationException("The labelling was cancelled"));
            }

            @Override
            public void onError(Throwable exception) {
                done.completeExceptionally(exception);
            }

            @Override
            public boolean isDead() {
                return done.isDone();
            }
        });
    }

    /**
     * The request as a stream with the same schema, so that the reasoning and the answer can be shown while the model writes them; the answer is
     * checked and read here, as {@code infer} does. A service that cannot stream with a schema is asked for the whole answer (nothing to watch then).
     */
    private CancellationToken streamOrInfer(List<LLMChatMessage> payload, CompletableFuture<LabelAnswer> done, LabelWatch.Listener watch) throws Exception {
        JsonNode schema = StructuredOutputs.schemaOf(LabelAnswer.class);
        StringBuilder answer = new StringBuilder();
        try {
            return service.stream(payload, protocol, schema, null, new InferenceService.InferenceAsyncCallback() {
                @Override
                public void onChunk(InferenceService.InferenceAsyncController controller, InferenceService.ChunkType chunkType, String text) {
                    boolean reasoning = chunkType == InferenceService.ChunkType.REASONING;
                    if (!reasoning) {
                        answer.append(text);
                    }
                    try {
                        watch.chunk(reasoning, text);
                    } catch (RuntimeException e) {
                        // the view must not stop the labelling
                    }
                    controller.continueInference();
                }

                @Override
                public void onCompletion() {
                    String text = answer.toString();
                    SchemaCheck check = StructuredOutputs.validate(schema, text);
                    try {
                        if (!check.valid()) {
                            done.completeExceptionally(new StructuredOutputException(text, check.errors(), null));
                        } else {
                            done.complete(StructuredOutputs.parse(text, LabelAnswer.class));
                        }
                    } catch (StructuredOutputException e) {
                        done.completeExceptionally(e);
                    }
                }

                @Override
                public void onCancel() {
                    done.completeExceptionally(new CancellationException("The labelling was cancelled"));
                }

                @Override
                public void onError(Throwable exception) {
                    done.completeExceptionally(exception);
                }

                @Override
                public boolean isDead() {
                    return done.isDone();
                }
            });
        } catch (UnsupportedOperationException e) {
            return infer(payload, done);
        }
    }
}
