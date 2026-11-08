package com.github.enerccio.marginalia.extensions.audiobook.label;

import java.util.List;
import java.util.regex.Pattern;

/** The sound tags of Chatterbox Turbo: written in the text of a piece of speech where the voice should make that sound. */
public final class Tags {

    /** The tags the labelling may put in, exactly as written in the text. */
    public static final List<String> ALLOWED = List.of("laugh", "chuckle", "sigh", "gasp", "cough", "clear throat", "sniff", "groan", "shush");

    private static final Pattern ANY_TAG = Pattern.compile("\\[([^\\[\\]]*)]");
    private static final Pattern ALLOWED_TAG = Pattern.compile("\\[(" + String.join("|", ALLOWED) + ")]");

    private Tags() {
    }

    /** The text without its allowed tags (and the white space they leave). */
    public static String strip(String text) {
        return ALLOWED_TAG.matcher(text).replaceAll(" ").replaceAll("\\s{2,}", " ").strip();
    }

    /** How many allowed tags the text has. */
    public static int count(String text) {
        return (int) ALLOWED_TAG.matcher(text).results().count();
    }

    /** Whether every bracketed word of the text is an allowed tag (a text of the book may have brackets of its own: then none is allowed). */
    public static boolean onlyAllowed(String text) {
        return ANY_TAG.matcher(text).results().allMatch(m -> ALLOWED.contains(m.group(1)));
    }
}
