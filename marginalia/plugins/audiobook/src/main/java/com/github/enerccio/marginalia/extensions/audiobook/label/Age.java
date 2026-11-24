package com.github.enerccio.marginalia.extensions.audiobook.label;

/**
 * The age of a speaker the cast does not name, as the labelling reads it from the text: a child gets the children's base voice, a teenager and an
 * old speaker a shifted version of the base of their gender ({@code VoiceShift}). The names are the words of the schema and the prompt.
 */
public enum Age {
    CHILD, TEEN, ADULT, ELDERLY, UNKNOWN;

    /** The value for a missing one (a label made before the field existed). */
    public static Age orUnknown(Age age) {
        return age == null ? UNKNOWN : age;
    }
}
