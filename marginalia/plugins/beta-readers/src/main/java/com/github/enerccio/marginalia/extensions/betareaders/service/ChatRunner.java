package com.github.enerccio.marginalia.extensions.betareaders.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceService.ChunkType;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncController;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaChat;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaMessage;
import com.github.enerccio.marginalia.extensions.betareaders.model.Persona;
import com.github.enerccio.marginalia.extensions.betareaders.model.ReplyFormat;
import com.github.enerccio.marginalia.extensions.betareaders.service.PromptBuilder.Options;
import com.github.enerccio.marginalia.extensions.betareaders.service.PromptBuilder.Prompt;
import com.github.enerccio.marginalia.extensions.betareaders.service.ReplyReader.Reading;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.ToIntFunction;

/**
 * Asks the model for the next turn of a chat and reads the reply as it streams. It builds the request ({@link PromptBuilder}, and the
 * schema when the reply is JSON), streams it through the inference service, tells the listener what has been read after every chunk
 * ({@link ReplyReader}: the messages, the one being written too) and, when the stream ends, what the reply is. It stores nothing; the
 * caller adds the reply to the chat.
 * <p>
 * The listener is called on the threads of the inference service. The reply is JSON unless the chat asks for text; a chat that leaves
 * it to the plugin ({@link ReplyFormat#AUTO}) is asked in text when the service can not take a response schema.
 */
public final class ChatRunner {

    private ChatRunner() {
    }

    /**
     * How a chat is asked.
     *
     * @param json   the reply is a JSON object (else free text)
     * @param schema the request carries the schema as its response format
     */
    public record Mode(boolean json, boolean schema) {
    }

    /**
     * What the format of the chat comes to.
     */
    public static Mode mode(ReplyFormat format) {
        return switch (format) {
            case TEXT -> new Mode(false, false);
            case JSON_NO_SCHEMA -> new Mode(true, false);
            case JSON, AUTO -> new Mode(true, true);
        };
    }

    /**
     * What the request is made of.
     *
     * @param chat            the chat; the reply is for the end of its history
     * @param system          the system prompt of the chat, rendered
     * @param maxMessages     the most messages the model may write in a turn
     * @param maxPromptTokens the most tokens the prompt may have; 0 for no limit
     * @param tokens          counts the tokens of a text
     * @param story           the story the personas read, see {@link PartsTracker#story}; empty for none
     */
    public record Request(BetaChat chat, String system, int maxMessages, int maxPromptTokens,
                          ToIntFunction<String> tokens, String story) {

        public Request(BetaChat chat, String system, int maxMessages, int maxPromptTokens, ToIntFunction<String> tokens) {
            this(chat, system, maxMessages, maxPromptTokens, tokens, "");
        }
    }

    /**
     * How the run ended.
     *
     * @param reply     the reply to add to the history; null when there is nothing to add. A reply that was stopped is the part
     *                  that was read; one that ended in an error is not kept
     * @param cancelled the user stopped it
     * @param error     what went wrong; null when nothing did ({@link EmptyReplyException} when the model wrote nothing)
     * @param prompt    what was sent (how many messages of the history were left out of it, see {@link Prompt#dropped()})
     * @param json      whether the reply was asked for in JSON
     */
    public record Outcome(BetaMessage reply, boolean cancelled, Throwable error, Prompt prompt, boolean json) {
    }

    public interface Listener {

        /** What has been read of the reply so far; only the finished messages. */
        void onProgress(Reading reading);

        void onFinished(Outcome outcome);
    }

    /**
     * Starts the request.
     *
     * @return what stops it
     * @throws UnsupportedOperationException when the chat asks for JSON and the service can not take a schema
     */
    public static CancellationToken run(InferenceService inference, Protocol protocol, Request request, Listener listener) throws Exception {
        Mode mode = mode(request.chat().getFormat());
        try {
            return start(inference, protocol, request, mode, listener);
        } catch (UnsupportedOperationException e) {
            // a service that can not take a schema
            if (!mode.schema() || request.chat().getFormat() == ReplyFormat.JSON) {
                throw e;
            }
            return start(inference, protocol, request, new Mode(false, false), listener);
        }
    }

    private static CancellationToken start(InferenceService inference, Protocol protocol, Request request, Mode mode,
                                           Listener listener) throws Exception {
        BetaChat chat = request.chat();
        boolean json = mode.json();
        List<String> speakers = chat.present().stream().map(Persona::getName).toList();
        Options options = new Options(json, request.maxMessages(), request.maxPromptTokens(), request.tokens(), request.story());
        Prompt prompt = PromptBuilder.build(chat, request.system(), options);
        List<LLMChatMessage> payload = prompt.messages();
        JsonNode schema = mode.schema() ? BetaSchemas.replySchema(speakers, request.maxMessages()) : null;

        StringBuilder reply = new StringBuilder();
        StringBuilder reasoning = new StringBuilder();
        AtomicBoolean finished = new AtomicBoolean();
        CancellationToken[] token = new CancellationToken[1];

        InferenceService.InferenceAsyncCallback callback = new InferenceService.InferenceAsyncCallback() {
            @Override
            public void onChunk(InferenceAsyncController controller, ChunkType type, String text) {
                try {
                    // the thinking of the model is progress too: a model that thinks for a minute before it answers would show nothing
                    // until its first word of the reply, and the reasoning only when it is done
                    if (type == ChunkType.REASONING) {
                        reasoning.append(text);
                    } else {
                        reply.append(text);
                    }
                    Reading reading = ReplyReader.read(reply.toString(), false, speakers, request.maxMessages());
                    listener.onProgress(withNative(reading, reasoning));
                } finally {
                    controller.continueInference();
                }
            }

            @Override
            public void onCompletion() {
                finish(false, null);
            }

            @Override
            public void onCancel() {
                finish(true, null);
            }

            @Override
            public void onError(Throwable exception) {
                finish(false, exception);
            }

            @Override
            public boolean isDead() {
                return token[0] != null && token[0].isCancelled();
            }

            private void finish(boolean cancelled, Throwable error) {
                if (!finished.compareAndSet(false, true)) {
                    return;
                }
                Reading reading = withNative(ReplyReader.read(reply.toString(), true, speakers, request.maxMessages()), reasoning);
                BetaMessage message = reading.isEmpty() ? null : reading.toMessage();
                Throwable problem = error;
                if (message == null && error == null && !cancelled) {
                    problem = new EmptyReplyException();
                }
                listener.onFinished(new Outcome(error == null ? message : null, cancelled, problem, prompt, json));
            }
        };

        token[0] = inference.stream(payload, protocol, schema, null, callback);
        return token[0];
    }

    /**
     * The reasoning the model did on its own is the reasoning shown. A reply has none of its own (the plugin does not ask for one: a
     * short field written under a schema is a plan, not thinking, and gets in the way of a model that thinks), so a model that does
     * not think shows no reasoning.
     */
    private static Reading withNative(Reading reading, StringBuilder nativeReasoning) {
        if (nativeReasoning.isEmpty()) {
            return reading;
        }
        return new Reading(nativeReasoning.toString().strip(), reading.messages(), reading.rawText(), reading.dropped());
    }
}
