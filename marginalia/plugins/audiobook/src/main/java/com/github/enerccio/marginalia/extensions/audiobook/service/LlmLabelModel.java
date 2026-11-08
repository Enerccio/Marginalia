package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceSession;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.audiobook.label.LabelAnswer;
import com.github.enerccio.marginalia.extensions.audiobook.label.LabelModel;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
        List<LLMChatMessage> payload = List.of(LLMChatMessage.of(LLMRole.SYSTEM, system), LLMChatMessage.of(LLMRole.USER, user));
        try (InferenceSession _ = InferenceSession.open("audiobook")) {
            service.infer(payload, protocol, LabelAnswer.class, new InferenceService.TypedInferenceCallback<LabelAnswer>() {
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
        try {
            return done.get(TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (ExecutionException e) {
            throw e.getCause() instanceof Exception cause ? cause : e;
        }
    }
}
