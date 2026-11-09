package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.enerccio.marginalia.domain.traits.LLMField;
import com.github.enerccio.marginalia.domain.traits.LLMSchema;

import java.util.List;

/**
 * What the model answers (in JSON) when it is asked to tidy what the tracked characters know: which items to drop and which to merge.
 * A list the model leaves out is {@code null}; {@link TidyChangeSetBuilder} treats it as empty.
 */
@JsonPropertyOrder({"characters"})
@LLMSchema(description = "What to drop or merge in the knowledge of each tracked character")
public record TidyAnswer(
        @LLMField(description = "One entry for every tracked character of the request, with empty lists when nothing is to be done") List<CharacterTidy> characters) {

    @JsonPropertyOrder({"name", "drop", "merge"})
    @LLMSchema(description = "What to tidy for one tracked character")
    public record CharacterTidy(
            @LLMField(description = "The name of the tracked character, as it is written in the request") String name,
            @LLMField(description = "Items that are not worth keeping", required = false) List<Drop> drop,
            @LLMField(description = "Groups of items about the same thing, to be replaced by one item", required = false) List<Merge> merge) {
    }

    @JsonPropertyOrder({"item", "reason"})
    @LLMSchema(description = "An item that is not worth keeping")
    public record Drop(
            @LLMField(description = "The number of the item in the list of items that may be changed", minimum = 1) int item,
            @LLMField(description = "Why: trivia, shared by the whole scene, overtaken by later events, or the same as another item", minLength = 1) String reason) {
    }

    @JsonPropertyOrder({"items", "kind", "text", "reason"})
    @LLMSchema(description = "Items about the same thing, replaced by one")
    public record Merge(
            @LLMField(description = "The numbers of two or more items of the list of items that may be changed", minItems = 2) List<Integer> items,
            @LLMField(description = "knows, or does-not-know") String kind,
            @LLMField(description = "The merged item: one or two short sentences that keep every fact of the items", minLength = 1) String text,
            @LLMField(description = "Why they belong together", minLength = 1) String reason) {
    }
}
