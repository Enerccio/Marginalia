package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceSession;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.audiobook.label.LabelAnswer;
import com.github.enerccio.marginalia.extensions.audiobook.label.LabelCancel;
import com.github.enerccio.marginalia.extensions.audiobook.label.LabelModel;

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
        try (InferenceSession _ = InferenceSession.open("audiobook")) {
            token = service.infer(payload, protocol, LabelAnswer.class, new InferenceService.TypedInferenceCallback<LabelAnswer>() {
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
}
