package com.github.enerccio.marginalia.extensions.storytracker.llm;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.storytracker.llm.TrackerModels.ModelSelection;
import com.github.enerccio.marginalia.extensions.storytracker.process.PartReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Asks the model one request of a tracker (a system and a user message) and waits for the whole answer. The request is streamed so
 * that the reasoning of the model, which a plain {@code infer} drops, can be kept for the review and shown while it is written.
 */
@Configurable
public class ModelCall {

    @Autowired
    private InferenceServices inferenceServices;

    /** What the model sent: the answer and, when the provider reports it, what it thought first. */
    public record Answered(String text, String reasoning) {
    }

    /**
     * How the model may read earlier parts: the reader, and the functions the plugin offers around it (its own descriptions).
     *
     * @param reader    what the model may read
     * @param iface     the interface with the {@code @LLMFunction} methods
     * @param functions makes the implementation of the interface from the reader (one that tells what is read)
     */
    public record Reading<T>(PartReader reader, Class<T> iface, Function<PartReader, ? extends T> functions) {
    }

    /**
     * Blocks until the answer is there; an interrupt of the calling thread cancels the request.
     *
     * @param session the kind of the inference session (the name of the plugin, for the log of the jobs)
     * @param live    told what the model writes while it writes it, may be {@code null}
     * @param reading how the model may read earlier parts, {@code null} for not at all
     */
    public Answered ask(ModelSelection model, Manuscript book, String session, String system, String user, Live live,
                        Reading<?> reading) throws Exception {
        List<LLMChatMessage> payload = List.of(LLMChatMessage.of(LLMRole.SYSTEM, system), LLMChatMessage.of(LLMRole.USER, user));
        CompletableFuture<Answered> answer = new CompletableFuture<>();
        StringBuilder text = new StringBuilder();
        StringBuilder reasoning = new StringBuilder();
        if (live != null) {
            live.started();
        }
        // what the model reads is shown like its reasoning and kept with it for the review
        PartReader noted = reading == null ? null : reading.reader().listening(number -> {
            String line = "\n[The model read part " + number + ".]\n";
            synchronized (reasoning) {
                reasoning.append(line);
            }
            tell(live, true, line);
        });
        InferenceService.InferenceAsyncCallback callback = new InferenceService.InferenceAsyncCallback() {
            @Override
            public void onChunk(InferenceService.InferenceAsyncController controller, InferenceService.ChunkType chunkType, String chunk)
                    throws Exception {
                boolean thinking = chunkType == InferenceService.ChunkType.REASONING;
                (thinking ? reasoning : text).append(chunk);
                tell(live, thinking, chunk);
                controller.continueInference();
            }

            @Override
            public void onCompletion() {
                answer.complete(new Answered(text.toString(), reasoning.toString().strip()));
            }

            @Override
            public void onCancel() {
                answer.cancel(false);
            }

            @Override
            public void onError(Throwable exception) {
                answer.completeExceptionally(exception);
            }

            @Override
            public boolean isDead() {
                return answer.isDone();
            }
        };
        CancellationToken token;
        try (InferenceSession _ = InferenceSession.open(session); UsageContext _ = UsageContext.open(book)) {
            InferenceService service = inferenceServices.forAIOrThrow(model.ai());
            if (noted != null) {
                try {
                    token = service.stream(payload, model.protocol(), bind(reading, noted), callback);
                } catch (UnsupportedOperationException e) {
                    // the provider can not call functions: the model reads the request as it is
                    token = service.stream(payload, model.protocol(), callback);
                }
            } else {
                token = service.stream(payload, model.protocol(), callback);
            }
        }
        try {
            return answer.get();
        } catch (InterruptedException e) {
            token.cancel();
            Thread.currentThread().interrupt();
            throw new CancellationException("The request was stopped");
        } catch (ExecutionException e) {
            throw e.getCause() instanceof Exception cause ? cause : e;
        }
    }

    private <T> FunctionSet bind(Reading<T> reading, PartReader noted) {
        return inferenceServices.getFunctionRegistry().bind(reading.iface(), reading.functions().apply(noted));
    }

    /** Showing what arrives must never fail the request. */
    private static void tell(Live live, boolean reasoning, String piece) {
        if (live != null) {
            try {
                live.chunk(reasoning, piece);
            } catch (RuntimeException e) {
                // ignored
            }
        }
    }
}
