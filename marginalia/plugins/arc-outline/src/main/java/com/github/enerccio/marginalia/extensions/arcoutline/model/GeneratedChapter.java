package com.github.enerccio.marginalia.extensions.arcoutline.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.enerccio.marginalia.domain.traits.LLMField;
import com.github.enerccio.marginalia.domain.traits.LLMSchema;

/**
 * A chapter the model answers with. The description is shown in the outline, so it is terse; the full version goes to
 * the note.
 */
@JsonPropertyOrder({"name", "description", "note"})
@LLMSchema(description = "A chapter of the arc")
public record GeneratedChapter(
        @LLMField(description = "A short title of the chapter", minLength = 1, maxLength = 200) String name,
        @LLMField(description = "A terse summary shown in the outline: one or two short sentences saying what happens in "
                + "the chapter. No details, no more than about 40 words", minLength = 1) String description,
        @LLMField(description = "The full version for the author's notes: the scene, who is in it, what happens step by "
                + "step, what changes and how the chapter ends. Everything that does not fit the terse description goes "
                + "here", minLength = 1) String note) {
}
