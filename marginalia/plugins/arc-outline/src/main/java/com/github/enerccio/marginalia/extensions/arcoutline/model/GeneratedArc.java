package com.github.enerccio.marginalia.extensions.arcoutline.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.enerccio.marginalia.domain.traits.LLMField;
import com.github.enerccio.marginalia.domain.traits.LLMSchema;

/**
 * What the model answers when it is asked for an arc; its JSON schema is sent to the provider and the answer is
 * checked against it. The description is shown in the outline, so it is terse; the full version goes to the note.
 */
@JsonPropertyOrder({"name", "description", "note"})
@LLMSchema(description = "A story arc")
public record GeneratedArc(
        @LLMField(description = "A short title of the arc", minLength = 1, maxLength = 200) String name,
        @LLMField(description = "A terse summary shown in the outline: one to three short sentences saying what the arc is about "
                + "and where it leads. No details, no lists, no more than about 60 words", minLength = 1) String description,
        @LLMField(description = "The full version for the author's notes: the premise, the main conflict, the turning points, "
                + "the characters and places involved and how the arc ends. Everything that does not fit the terse description "
                + "goes here", minLength = 1) String note) {
}
