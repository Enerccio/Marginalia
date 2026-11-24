package com.github.enerccio.marginalia.extensions.audiobook.label;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.enerccio.marginalia.domain.traits.LLMField;
import com.github.enerccio.marginalia.domain.traits.LLMSchema;

/**
 * What the model says about one piece. Its JSON schema is sent to the provider and the answer is checked against it.
 *
 * @param taggedText for a short piece of speech the piece word for word with sound tags inserted, empty when there is none
 * @param gender     of a speaker the cast does not name (picks the base voice of unnamed speakers); null in labels made before it existed. Not required
 *                   by the schema: only a cast with base voices asks for it, an answer without it (any other book, a custom template) stays valid
 * @param age        of a speaker the cast does not name; null in labels made before it existed
 */
@JsonPropertyOrder({"id", "speaker", "delivery", "intensity", "tagged_text", "gender", "age"})
@LLMSchema(description = "The label of one piece of the text")
public record PieceLabel(
        @LLMField(description = "The number of the piece", minimum = 0) int id,
        @LLMField(description = "Who says it: for direct speech the character who speaks, resolved from the context; for narration Narrator, unless the text is "
                + "a character's own words (a letter, a note, a sign, thoughts), then that character",
                minLength = 1) String speaker,
        @LLMField(description = "How it should be read") Delivery delivery,
        @LLMField(description = "How strongly, from 0.0 (flat) to 1.0 (extreme)", minimum = 0, maximum = 1) double intensity,
        @com.fasterxml.jackson.annotation.JsonProperty("tagged_text")
        @LLMField(description = "For narration always an empty string. For direct speech the piece word for word and unchanged, except that a sound tag may be "
                + "inserted where the context clearly calls for that sound from the speaking character. Empty when no tag is needed") String taggedText,
        @LLMField(description = "The gender of the speaker when they are not one of the known characters, as the text shows or clearly implies; "
                + "UNKNOWN when it does not, for the narrator and for known characters", required = false) Gender gender,
        @LLMField(description = "The age of the speaker when they are not one of the known characters, as the text shows or clearly implies; "
                + "UNKNOWN when it does not, for the narrator and for known characters", required = false) Age age) {

    /** A label without the gender and the age (the narrator, a piece the model left out, labels made before they existed). */
    public PieceLabel(int id, String speaker, Delivery delivery, double intensity, String taggedText) {
        this(id, speaker, delivery, intensity, taggedText, null, null);
    }
}
