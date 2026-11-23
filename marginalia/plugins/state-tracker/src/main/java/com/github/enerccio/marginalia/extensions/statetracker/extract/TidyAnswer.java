package com.github.enerccio.marginalia.extensions.statetracker.extract;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.enerccio.marginalia.domain.traits.LLMField;
import com.github.enerccio.marginalia.domain.traits.LLMSchema;

import java.util.List;

/**
 * What the model answers (in JSON) when it is asked to tidy the state of the tracked characters: which values to drop, which to
 * update and which list entries to merge. A value it does not name is kept. A list the model leaves out is {@code null};
 * {@link TidyChangeBuilder} treats it as empty.
 */
@JsonPropertyOrder({"characters"})
@LLMSchema(description = "What to drop, update or merge in the state of each tracked character")
public record TidyAnswer(
        @LLMField(description = "One entry for every tracked character of the request, with empty lists when everything is kept") List<CharacterTidy> characters) {

    @JsonPropertyOrder({"name", "drop", "update", "merge"})
    @LLMSchema(description = "What to tidy for one tracked character")
    public record CharacterTidy(
            @LLMField(description = "The name of the tracked character, as it is written in the request") String name,
            @LLMField(description = "Values that are no longer true or no longer relevant", required = false) List<Drop> drop,
            @LLMField(description = "Values the last parts show have moved on", required = false) List<Update> update,
            @LLMField(description = "Entries of one list that are the same thing, replaced by one entry", required = false) List<Merge> merge) {
    }

    @JsonPropertyOrder({"item", "reason"})
    @LLMSchema(description = "A value that is dropped")
    public record Drop(
            @LLMField(description = "The number of the value", minimum = 1) int item,
            @LLMField(description = "Why it is no longer true or relevant", minLength = 1) String reason) {
    }

    @JsonPropertyOrder({"item", "value", "reason"})
    @LLMSchema(description = "A value replaced by what is true now")
    public record Update(
            @LLMField(description = "The number of the value", minimum = 1) int item,
            @LLMField(description = "The new value, short, in the words of the story", minLength = 1) String value,
            @LLMField(description = "What in the last parts shows it", minLength = 1) String reason) {
    }

    @JsonPropertyOrder({"items", "value", "reason"})
    @LLMSchema(description = "Entries of one list category that are the same thing")
    public record Merge(
            @LLMField(description = "The numbers of two or more entries of the same list category", minItems = 2) List<Integer> items,
            @LLMField(description = "The one entry that replaces them", minLength = 1) String value,
            @LLMField(description = "Why they are the same thing", minLength = 1) String reason) {
    }
}
