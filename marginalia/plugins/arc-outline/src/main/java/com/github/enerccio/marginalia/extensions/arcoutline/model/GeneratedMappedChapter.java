package com.github.enerccio.marginalia.extensions.arcoutline.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.enerccio.marginalia.domain.traits.LLMField;
import com.github.enerccio.marginalia.domain.traits.LLMSchema;

/**
 * A chapter of an arc written from the story, with the message of the story it begins in.
 */
@JsonPropertyOrder({"name", "description", "note", "part"})
@LLMSchema(description = "A chapter of the arc")
public record GeneratedMappedChapter(
        @LLMField(description = "A short title of the chapter", minLength = 1, maxLength = 200) String name,
        @LLMField(description = "A terse summary shown in the outline: one or two short sentences saying what happens in "
                + "the chapter. No details, no more than about 40 words", minLength = 1) String description,
        @LLMField(description = "The full version for the author's notes: the scene, who is in it, what happens step by "
                + "step, what changes and how the chapter ends. Everything that does not fit the terse description goes "
                + "here", minLength = 1) String note,
        @LLMField(description = "The number of the message of the story (the N of \"--- Message N ---\") in which the chapter "
                + "begins", minimum = 1) int part) {
}
