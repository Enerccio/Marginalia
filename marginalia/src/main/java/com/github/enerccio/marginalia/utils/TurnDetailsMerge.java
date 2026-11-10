package com.github.enerccio.marginalia.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Joins the turn details (scene setting, POV character, present characters, instructions) of two turns that are to become
 * one: what the merge dialog suggests before the author edits it.
 */
public final class TurnDetailsMerge {

    private TurnDetailsMerge() {
    }

    /**
     * The two texts under each other with an empty line between them; one of them when the other is blank or the same.
     */
    public static String text(String first, String second) {
        String a = StringUtils.trimToEmpty(first);
        String b = StringUtils.trimToEmpty(second);
        if (b.isEmpty() || a.equals(b)) {
            return a;
        }
        return a.isEmpty() ? b : a + "\n\n" + b;
    }

    /**
     * The names of both lists (separated by commas, semicolons or line breaks), each once, in the order they come in.
     * Names that differ only in case are the same name.
     */
    public static String names(String first, String second) {
        List<String> names = new ArrayList<>();
        for (String name : (StringUtils.defaultString(first) + "\n" + StringUtils.defaultString(second)).split("[,;\\n\\r]+")) {
            String stripped = name.strip();
            if (!stripped.isEmpty() && names.stream().noneMatch(stripped::equalsIgnoreCase)) {
                names.add(stripped);
            }
        }
        return String.join(", ", names);
    }

    /**
     * The value of the first turn, the one of the second when the first has none (a turn has one POV character).
     */
    public static String one(String first, String second) {
        return StringUtils.defaultString(StringUtils.firstNonBlank(StringUtils.strip(first), StringUtils.strip(second)));
    }

    /**
     * The two titles as one, {@code first / second}, cut to {@code maxLength} characters; one of them when the other is blank
     * or the same.
     */
    public static String title(String first, String second, int maxLength) {
        String a = StringUtils.trimToEmpty(first);
        String b = StringUtils.trimToEmpty(second);
        String joined = b.isEmpty() || a.equals(b) ? a : a.isEmpty() ? b : a + " / " + b;
        return StringUtils.abbreviate(joined, "…", Math.max(2, maxLength));
    }
}
