package com.github.enerccio.marginalia.extensions.stconverter.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.enerccio.marginalia.domain.traits.LLMField;
import com.github.enerccio.marginalia.domain.traits.LLMSchema;

/**
 * One turn of the answer of the model; its JSON schema is sent to the provider and the answer is checked against it.
 */
@JsonPropertyOrder({"firstMessage", "lastMessage", "sceneSetting", "povCharacter", "presentCharacters", "instructions"})
@LLMSchema(description = "The details of one turn of the story the chat is rewritten into")
public record GeneratedTurn(
        @LLMField(description = "The number of the first message of the chat this turn is made from", minimum = 1) int firstMessage,
        @LLMField(description = "The number of the last message of the chat this turn is made from, the same as the first one "
                + "when the turn is made from one message", minimum = 1) int lastMessage,
        @LLMField(description = "Where and when the turn happens, who and what is there: the place, the time, the mood, the "
                + "things that matter. A few sentences", minLength = 1) String sceneSetting,
        @LLMField(description = "The name of the one character whose point of view the turn is written from", minLength = 1) String povCharacter,
        @LLMField(description = "The names of the characters who are present in the scene, separated by commas",
                minLength = 1) String presentCharacters,
        @LLMField(description = "What the writer is told to write for the turn: the events that happen, in order, what the "
                + "characters do, say and feel, how the turn ends. Everything the messages tell, in the words of an instruction",
                minLength = 1) String instructions) {
}
