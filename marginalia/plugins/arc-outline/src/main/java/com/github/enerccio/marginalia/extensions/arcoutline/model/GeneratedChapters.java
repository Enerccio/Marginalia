package com.github.enerccio.marginalia.extensions.arcoutline.model;

import com.github.enerccio.marginalia.domain.traits.LLMField;
import com.github.enerccio.marginalia.domain.traits.LLMSchema;

import java.util.List;

/**
 * What the model answers when it is asked for the chapters of an arc, in the order they are read. The number of items
 * is limited further when the author asked for an exact number of chapters (see {@code ArcOutlineService#chaptersSchema}).
 */
@LLMSchema(description = "The chapters of an arc, in reading order")
public record GeneratedChapters(
        @LLMField(description = "The chapters, first to last", minItems = 1) List<GeneratedChapter> chapters) {
}
