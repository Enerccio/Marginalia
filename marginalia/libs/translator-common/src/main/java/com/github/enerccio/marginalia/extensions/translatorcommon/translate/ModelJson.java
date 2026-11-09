package com.github.enerccio.marginalia.extensions.translatorcommon.translate;

import java.util.regex.Pattern;

/**
 * Finds the JSON object in what a model answered. A model may wrap the object in a code fence or write a word before it; whatever is outside the
 * outermost braces is ignored.
 */
public final class ModelJson {

    private static final Pattern FENCE = Pattern.compile("^```(?:json)?\\s*|\\s*```$");

    private ModelJson() {
    }

    /**
     * @return the text from the first {@code {} to the last {@code }}; null when there is no object in {@code raw}
     */
    public static String object(String raw) {
        if (raw == null) {
            return null;
        }
        String text = FENCE.matcher(raw.strip()).replaceAll("");
        int from = text.indexOf('{');
        int to = text.lastIndexOf('}');
        return from < 0 || to <= from ? null : text.substring(from, to + 1);
    }
}
