package com.github.enerccio.marginalia.extensions.translatorcommon.model;

/**
 * How much of a language a character follows when it is spoken around them. Set per character and per language of a book ({@link CharacterNote#getLevel()});
 * what the model does with it is left to the model. {@link #FLUENT} says nothing to the model (a character follows the language unless told otherwise).
 */
public enum UnderstandingLevel {
    /** Hears sounds, no meaning. */
    NONE,
    /** Knows a few words, cannot follow. */
    FEW_WORDS,
    /** Follows most of it, misses details. */
    FOLLOWS_MOST,
    /** Follows everything (the default, not written to the model). */
    FLUENT;

    /** The names the commands use. */
    public static final String[] KEYS = {"none", "few-words", "follows-most", "fluent"};

    /** The name the commands use: {@code none}, {@code few-words}, {@code follows-most}, {@code fluent}. */
    public String key() {
        return name().toLowerCase(java.util.Locale.ROOT).replace('_', '-');
    }

    /**
     * The level of a name of the commands (the case does not matter, a dash or an underscore), null for blank: not said.
     *
     * @throws IllegalArgumentException for a name that is none of {@link #KEYS}
     */
    public static UnderstandingLevel parse(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String wanted = key.strip().toLowerCase(java.util.Locale.ROOT).replace('_', '-');
        for (UnderstandingLevel level : values()) {
            if (level.key().equals(wanted)) {
                return level;
            }
        }
        throw new IllegalArgumentException("The level is one of " + String.join(", ", KEYS) + ", or empty for not said; not " + key);
    }
}
