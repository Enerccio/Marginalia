package com.github.enerccio.marginalia.extensions.bookdescription.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.enerccio.marginalia.domain.traits.LLMField;
import com.github.enerccio.marginalia.domain.traits.LLMSchema;

/**
 * What the model answers (in JSON) when it is asked for the name and the description of a book.
 */
@JsonPropertyOrder({"name", "description"})
@LLMSchema(description = "The name and the description of a book")
public record GeneratedDescription(
        @LLMField(description = "The name (title) of the book", minLength = 1, maxLength = 200) String name,
        @LLMField(description = "The description of the book, as it would be shown to a reader browsing a library", minLength = 1) String description) {
}
