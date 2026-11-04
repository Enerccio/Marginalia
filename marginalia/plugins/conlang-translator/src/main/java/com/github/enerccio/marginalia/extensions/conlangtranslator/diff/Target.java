package com.github.enerccio.marginalia.extensions.conlangtranslator.diff;

/**
 * What kind of thing of a language a {@link Change} is about. The order is the order the changes are applied in (a category before the words that use it).
 */
public enum Target {
    /** The language itself: its name, description, options and who speaks it. There is always exactly one. */
    LANGUAGE,
    RULE,
    CATEGORY,
    REGISTER,
    CHARACTER,
    WORD
}
