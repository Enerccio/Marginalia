package com.github.enerccio.marginalia.extensions.arcoutline.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.enerccio.marginalia.domain.traits.LLMField;
import com.github.enerccio.marginalia.domain.traits.LLMSchema;

import java.util.List;

/**
 * What the model answers when it is asked to turn a part of the written story into an arc: the arc and its chapters in
 * the order they are read. The number of chapters is limited further when the author asked for an exact number (see
 * {@code ArcOutlineService#importSchema}).
 */
@JsonPropertyOrder({"name", "description", "note", "chapters"})
@LLMSchema(description = "A story arc with its chapters, summarizing a part of the written story")
public record GeneratedStoryArc(
        @LLMField(description = "A short title of the arc", minLength = 1, maxLength = 200) String name,
        @LLMField(description = "A terse summary shown in the outline: one to three short sentences saying what the arc is about "
                + "and how it ends. No details, no lists, no more than about 60 words", minLength = 1) String description,
        @LLMField(description = "The full version for the author's notes: the premise, the main conflict, the turning points, "
                + "the characters and places involved and how the arc ends. Everything that does not fit the terse description "
                + "goes here", minLength = 1) String note,
        @LLMField(description = "The chapters of the arc in the order they are read, together covering the whole story given",
                minItems = 1) List<GeneratedChapter> chapters) {
}
