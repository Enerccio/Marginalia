package com.github.enerccio.marginalia.extensions.stconverter.model;

import com.github.enerccio.marginalia.domain.traits.LLMField;
import com.github.enerccio.marginalia.domain.traits.LLMSchema;

import java.util.List;

/**
 * What the model answers when it is asked for the turn details of a stretch of the chat, in the order of the chat.
 */
@LLMSchema(description = "The turns of the story the chat is rewritten into, in the order they are read")
public record GeneratedTurns(
        @LLMField(description = "The turns, first to last, together covering every message of the chat given exactly once",
                minItems = 1) List<GeneratedTurn> turns) {
}
