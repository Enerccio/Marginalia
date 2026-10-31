package com.github.enerccio.marginalia.extensions.betareaders.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.enerccio.marginalia.domain.traits.LLMField;
import com.github.enerccio.marginalia.domain.traits.LLMSchema;

import java.util.List;

/**
 * What the model answers with: one turn of the conversation. {@code BetaSchemas} builds the schema of this class for a request:
 * the speakers are the names of the personas present (and the narrator), the number of messages is limited and the reasoning is
 * left out for a model that reasons on its own.
 */
@JsonPropertyOrder({"reasoning", "messages"})
@LLMSchema(description = "One turn of the conversation: what the personas say and do, in order")
public record Reply(
        @LLMField(description = "A short reasoning before the messages: who has something to say now, and why", required = false)
        String reasoning,
        @LLMField(description = "The messages of this turn, in order; the personas may answer one another", minItems = 1)
        List<Message> messages) {

    /** One message of the turn, said by one speaker. */
    @JsonPropertyOrder({"speaker", "parts"})
    @LLMSchema(description = "A message of one speaker")
    public record Message(
            @LLMField(description = "Who says it: a persona of the conversation, or the narrator for the description of the scene")
            String speaker,
            @LLMField(description = "What the speaker does and says, in order; actions and speech may alternate", minItems = 1)
            List<Part> parts) {
    }

    /** A piece of a message. */
    @JsonPropertyOrder({"type", "text"})
    @LLMSchema(description = "An action or a line of speech")
    public record Part(
            @LLMField(description = "action: a gesture, an expression or a tone, speech: what is said") Type type,
            @LLMField(description = "The text, without quotation marks or asterisks", minLength = 1) String text) {
    }

    /** What a part is. */
    public enum Type {
        @JsonProperty("action") ACTION,
        @JsonProperty("speech") SPEECH
    }
}
