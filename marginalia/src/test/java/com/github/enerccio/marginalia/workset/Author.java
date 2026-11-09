package com.github.enerccio.marginalia.workset;

import java.util.List;

/**
 * The model behind the bridge: gets the messages the application built for one request and answers with the story text.
 */
public interface Author {

    /** One message of the request the application sent. */
    record Message(String role, String content) {
    }

    /**
     * @param text       the answer
     * @param reasoning  the thinking, empty when the model gave none
     * @param inputTokens  tokens read (0 when unknown)
     * @param outputTokens tokens written (0 when unknown)
     * @param cachedTokens tokens read from the prompt cache (0 when unknown)
     * @param costUsd    cost the tool reported, 0 when unknown
     * @param millis     how long the call took
     * @param model      what answered ("fixed", "claude:haiku"...)
     */
    record Answer(String text, String reasoning, long inputTokens, long outputTokens, long cachedTokens, double costUsd, long millis, String model) {
    }

    /** Names what the answers are cached under, so an answer of one author is never read for another. */
    String id();

    Answer answer(List<Message> messages) throws Exception;

    /**
     * Like {@link #answer(List)} for a request that carries a JSON schema ({@code response_format} of the chat completion): a real server enforces it, so an author that
     * can (the {@code claude} tool: {@code --json-schema}) must too, else the answer is whatever shape the prompt suggests. The default ignores the schema.
     *
     * @param jsonSchema the schema as JSON text, never blank
     */
    default Answer answer(List<Message> messages, String jsonSchema) throws Exception {
        return answer(messages);
    }
}
