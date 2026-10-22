package com.github.enerccio.marginalia.domain.service.command;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;

import java.util.function.Function;

/**
 * A text of a registered command (its human name, a description...): either the {@link L} key of a localized text,
 * resolved in the language of the user whenever it is shown, or a plain text of an extension that brings no
 * translations. Never both; a text with neither is {@link #isEmpty() empty}.
 *
 * @param key  the key of the localized text, null when the text is a plain one or there is none
 * @param text the plain text, null when the text is a localized one or there is none
 */
public record CommandText(L key, String text) {

    /** No text. */
    public static final CommandText NONE = new CommandText(null, null);

    public CommandText {
        if (key != null && text != null) {
            throw new IllegalArgumentException("A command text is a localization key or a plain text, not both");
        }
    }

    public static CommandText of(L key) {
        return key == null ? NONE : new CommandText(key, null);
    }

    public static CommandText ofText(String text) {
        return text == null || text.isBlank() ? NONE : new CommandText(null, text);
    }

    /**
     * The text of the annotation attributes of a pair ({@code description} and {@code descriptionText}): the arrays
     * are the way to tell "not given" from "given", so each has none or one element and only one of them may have it (exactly one when the text is required).
     *
     * @param where the command (method) for the message
     * @param field the name of the attribute, for the message
     * @param keys  the localized attribute
     * @param texts the plain text attribute
     * @param required whether one of them has to be given
     * @throws IllegalArgumentException when the attributes are set wrongly
     */
    public static CommandText of(String where, String field, L[] keys, String[] texts, boolean required) {
        if (keys.length > 1 || texts.length > 1) {
            throw new IllegalArgumentException(where + ": " + field + " takes one value, not " + Math.max(keys.length, texts.length));
        }
        if (keys.length == 1 && texts.length == 1) {
            throw new IllegalArgumentException(where + ": " + field + " is given as a localization key and as a text, use one of them");
        }
        if (required && keys.length == 0 && texts.length == 0) {
            throw new IllegalArgumentException(where + ": " + field + " is not given, set it as a localization key or as a text ("
                    + field + " or " + field + "Text)");
        }
        return keys.length == 1 ? of(keys[0]) : texts.length == 1 ? ofText(texts[0]) : NONE;
    }

    /** Whether there is no text to show. */
    public boolean isEmpty() {
        return key == null && text == null;
    }

    /** The text in the language of the current session, empty string when there is none. */
    public String get(Localization loc) {
        return get(loc::getValue);
    }

    /** The text with the given lookup of the keys, empty string when there is none. */
    public String get(Function<L, String> lookup) {
        if (key != null) {
            String value = lookup.apply(key);
            return value == null ? "" : value;
        }
        return text == null ? "" : text;
    }

}
