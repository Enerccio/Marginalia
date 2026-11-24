package com.github.enerccio.marginalia.extensions.audiobook.label;

/**
 * How a piece should be read, as the labelling says it. What the engine can do with it is decided later (a mood needs its own clip, see TODO.md).
 * <p>
 * The names are what the schema offers the model, what the prompt lists and what is read back, all the same words: the schema generator of the
 * application takes the Java names of an enum and ignores a {@code @JsonValue} or {@code @JsonProperty} on them, so a lower case wire name would
 * have the provider answer in one form and the parser expect another.
 */
public enum Delivery {
    NEUTRAL, CALM, TENSE, SAD, ANGRY, JOYFUL, AMUSED, WHISPER, SHOUT,
    /** The moods that Higgs Audio v3 can voice (an emotion of its own each, see {@code HiggsText}); for Chatterbox they are a mood clip of the cast or nothing. */
    FEARFUL, SURPRISED, TENDER, SARCASTIC, PLEADING, DESPERATE, DISGUSTED, AROUSED;

    /** The name for people (the studio): lower case. */
    public String label() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
