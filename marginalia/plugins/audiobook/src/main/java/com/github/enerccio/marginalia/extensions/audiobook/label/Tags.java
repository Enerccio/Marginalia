package com.github.enerccio.marginalia.extensions.audiobook.label;

import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The sound tags: written in the text of a piece of speech where the voice should make that sound. Which ones the labelling may use depends on
 * the model that speaks: Chatterbox Turbo reads its own nine, Higgs Audio v3 has more, turned into its own tokens by {@link HiggsText}.
 */
public final class Tags {

    /** The tags of Chatterbox Turbo, exactly as written in the text. */
    public static final List<String> TURBO = List.of("laugh", "chuckle", "sigh", "gasp", "cough", "clear throat", "sniff", "groan", "shush");

    /** The tags {@link HiggsText} can voice with Higgs Audio v3. */
    public static final List<String> HIGGS = List.of("laugh", "chuckle", "giggle", "sigh", "gasp", "cough", "clear throat", "sniff", "sneeze", "groan", "cry",
            "sob", "scream", "pant", "moan", "hum", "shush");

    /** Every tag of every model: what the comparison of a tagged text with its piece ignores. */
    public static final List<String> ALL = List.copyOf(new LinkedHashSet<>(concat(TURBO, HIGGS)));

    private static final Pattern ANY_TAG = Pattern.compile("\\[([^\\[\\]]*)]");
    private static final Pattern KNOWN_TAG = pattern(ALL);

    private Tags() {
    }

    /** The tags the model can voice; empty for the models that read tags as words. */
    public static List<String> forModel(String model) {
        if (BackendConfig.MODEL_TURBO.equals(model)) {
            return TURBO;
        }
        if (BackendConfig.MODEL_HIGGS.equals(model)) {
            return HIGGS;
        }
        return List.of();
    }

    /** The text without its tags (any model's) and the white space they leave. */
    public static String strip(String text) {
        return KNOWN_TAG.matcher(text).replaceAll(" ").replaceAll("\\s{2,}", " ").strip();
    }

    /** How many tags of the list the text has. */
    public static int count(String text, List<String> allowed) {
        return allowed.isEmpty() ? 0 : (int) pattern(allowed).matcher(text).results().count();
    }

    /** Whether every bracketed word of the text is one of the tags (a text of the book may have brackets of its own: then none is allowed). */
    public static boolean onlyAllowed(String text, List<String> allowed) {
        return ANY_TAG.matcher(text).results().allMatch(m -> allowed.contains(m.group(1)));
    }

    private static Pattern pattern(List<String> tags) {
        return Pattern.compile("\\[(" + String.join("|", tags.stream().map(Pattern::quote).toList()) + ")]");
    }

    private static List<String> concat(List<String> a, List<String> b) {
        List<String> all = new java.util.ArrayList<>(a);
        all.addAll(b);
        return all;
    }
}
