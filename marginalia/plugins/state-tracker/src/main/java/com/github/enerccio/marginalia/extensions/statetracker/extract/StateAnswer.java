package com.github.enerccio.marginalia.extensions.statetracker.extract;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.enerccio.marginalia.domain.traits.LLMField;
import com.github.enerccio.marginalia.domain.traits.LLMSchema;

import java.util.List;

/**
 * What the model answers (in JSON) when it is asked what changed in the state of the tracked characters. A list the model leaves
 * out is {@code null}; {@link StateChangeBuilder} treats it as empty.
 */
@JsonPropertyOrder({"clock", "characters"})
@LLMSchema(description = "What changed in the state of each tracked character, and how the time of the story moved")
public record StateAnswer(
        @LLMField(description = "How the time of the story moves in each message, when the request has a clock", required = false) List<ClockReport> clock,
        @LLMField(description = "One entry for every tracked character of the request, with an empty list when nothing changed") List<CharacterChanges> characters) {

    @JsonPropertyOrder({"message", "kind", "days", "day", "date", "timeOfDay", "approximate", "evidence"})
    @LLMSchema(description = "How the time of the story moves in one message")
    public record ClockReport(
            @LLMField(description = "The number of the message", minimum = 1) int message,
            @LLMField(description = "none, advance, day, date or flashback") String kind,
            @LLMField(description = "advance: how many days pass, 0 for the same day", required = false) Integer days,
            @LLMField(description = "day: the story day the message states", required = false) Integer day,
            @LLMField(description = "date: the date the message states, YYYY-MM-DD", required = false) String date,
            @LLMField(description = "dawn, morning, noon, afternoon, evening or night, when the message says so", required = false) String timeOfDay,
            @LLMField(description = "advance: true for a vague span", required = false) Boolean approximate,
            @LLMField(description = "A short quote copied word for word from the message, except for none", required = false) String evidence) {
    }

    @JsonPropertyOrder({"name", "changes"})
    @LLMSchema(description = "The changes of one tracked character")
    public record CharacterChanges(
            @LLMField(description = "The name of the tracked character, as it is written in the request") String name,
            @LLMField(description = "What changed in its categories", required = false) List<Change> changes) {
    }

    @JsonPropertyOrder({"category", "action", "value", "item", "evidence", "message"})
    @LLMSchema(description = "One change of a category")
    public record Change(
            @LLMField(description = "The name of the category, as it is written in the request") String category,
            @LLMField(description = "set, clear, add or remove") String action,
            @LLMField(description = "The new value (set) or the new entry (add)", required = false) String value,
            @LLMField(description = "The number of the value that is removed (remove)", required = false) Integer item,
            @LLMField(description = "A short quote copied word for word from the messages that shows it", minLength = 1) String evidence,
            @LLMField(description = "The number of the message the quote is in", minimum = 1) int message) {
    }
}
