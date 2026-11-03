package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.enerccio.marginalia.domain.traits.LLMField;
import com.github.enerccio.marginalia.domain.traits.LLMSchema;

import java.util.List;

/**
 * What the model answers (in JSON) when it is asked what the tracked characters learned in some messages. A list the
 * model leaves out is {@code null}; {@link ChangeSetBuilder} treats it as empty.
 */
@JsonPropertyOrder({"characters"})
@LLMSchema(description = "What each tracked character learned, and which of its items the messages contradict")
public record ExtractionAnswer(
        @LLMField(description = "One entry for every tracked character of the request, with empty lists when nothing changed") List<CharacterChanges> characters) {

    @JsonPropertyOrder({"name", "retract", "add"})
    @LLMSchema(description = "The changes of one tracked character")
    public record CharacterChanges(
            @LLMField(description = "The name of the tracked character, as it is written in the request") String name,
            @LLMField(description = "Items the messages clearly contradict", required = false) List<Retraction> retract,
            @LLMField(description = "What the character learned in these messages", required = false) List<Addition> add) {
    }

    @JsonPropertyOrder({"item", "evidence", "message"})
    @LLMSchema(description = "An item that the messages contradict")
    public record Retraction(
            @LLMField(description = "The number of the item in the list of the character", minimum = 1) int item,
            @LLMField(description = "A short quote copied from the messages that shows it", minLength = 1) String evidence,
            @LLMField(description = "The number of the message the quote is in", minimum = 1) int message) {
    }

    @JsonPropertyOrder({"kind", "text", "evidence", "message"})
    @LLMSchema(description = "Something the character learned")
    public record Addition(
            @LLMField(description = "knows, or does-not-know") String kind,
            @LLMField(description = "One short sentence", minLength = 1) String text,
            @LLMField(description = "A short quote copied from the messages that shows it", minLength = 1) String evidence,
            @LLMField(description = "The number of the message the quote is in", minimum = 1) int message) {
    }
}
