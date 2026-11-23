package com.github.enerccio.marginalia.extensions.audiobook.label;

/**
 * The gender of a speaker the cast does not name, as the labelling reads it from the text: it picks the base voice of the speaker (the cast's
 * <i>Other men</i> or <i>Other women</i>). The names are the words of the schema and the prompt (see {@link Delivery} for why they are the Java names).
 */
public enum Gender {
    MALE, FEMALE, UNKNOWN;

    /** The value for a missing one (a label made before the field existed). */
    public static Gender orUnknown(Gender gender) {
        return gender == null ? UNKNOWN : gender;
    }
}
