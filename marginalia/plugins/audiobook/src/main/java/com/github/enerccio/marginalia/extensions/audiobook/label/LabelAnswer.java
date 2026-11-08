package com.github.enerccio.marginalia.extensions.audiobook.label;

import com.github.enerccio.marginalia.domain.traits.LLMField;
import com.github.enerccio.marginalia.domain.traits.LLMSchema;

import java.util.List;

/** The answer of the model: a label for each piece it was given, in order. */
@LLMSchema(description = "The labels of the pieces")
public record LabelAnswer(@LLMField(description = "One label for each piece, in the order of the pieces") List<PieceLabel> labels) {
}
