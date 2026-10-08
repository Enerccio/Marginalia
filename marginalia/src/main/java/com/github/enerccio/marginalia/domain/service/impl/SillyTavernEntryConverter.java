package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Maps a SillyTavern world info entry onto a Marginalia lorebook entry.
 * <p>
 * SillyTavern activates an entry when one of its primary keys appears in the scanned text, optionally combined with
 * secondary keys ({@code selectiveLogic}: AND ANY, NOT ALL, NOT ANY, AND ALL). Marginalia has a single filter per
 * entry, so the keys are compiled into one expression:
 * <ul>
 *     <li>a single plain key (case-insensitive, not whole-word) stays a readable {@link FilteringMode#TEXT} filter,</li>
 *     <li>anything else becomes a {@link FilteringMode#REGEX} of lookaheads, e.g. primary keys a, b and AND ALL
 *     secondary keys c, d give {@code (?s)^(?=.*(?:a|b))(?=.*c)(?=.*d)}.</li>
 * </ul>
 * Keys written as {@code /pattern/flags} are regular expressions in SillyTavern and are kept as such. Case-sensitive
 * and whole-word matching are inlined into the expression, because Marginalia matches case-insensitively.
 * <p>
 * Entries without primary keys and {@code constant} entries get no filter, so they are always active.
 */
final class SillyTavernEntryConverter {

    /**
     * SillyTavern {@code selectiveLogic} values.
     */
    static final int AND_ANY = 0;
    static final int NOT_ALL = 1;
    static final int NOT_ANY = 2;
    static final int AND_ALL = 3;

    /**
     * SillyTavern {@code position} values that are inserted near the end of the prompt: author's note top/bottom and
     * at depth. Everything else (before/after character definitions, example messages) is background lore.
     */
    private static final int POSITION_AN_TOP = 2;
    private static final int POSITION_AN_BOTTOM = 3;
    private static final int POSITION_AT_DEPTH = 4;

    private static final Pattern REGEX_KEY = Pattern.compile("^/(.+)/([a-z]*)$", Pattern.DOTALL);

    record Filter(String filtering, FilteringMode mode) {
    }

    private SillyTavernEntryConverter() {
    }

    static String name(JsonObject entry, List<String> primaryKeys) {
        String comment = string(entry, "comment");
        if (StringUtils.isNotBlank(comment)) {
            return comment.trim();
        }
        if (!primaryKeys.isEmpty()) {
            return primaryKeys.getFirst();
        }
        return "Entry";
    }

    static int order(JsonObject entry) {
        JsonElement order = entry.get("order");
        if (order == null || order.isJsonNull()) {
            return 100;
        }
        try {
            return order.getAsInt();
        } catch (Exception e) {
            return 100;
        }
    }

    static boolean enabled(JsonObject entry) {
        return !bool(entry, "disable", false);
    }

    static InsertionMode insertionMode(JsonObject entry) {
        Integer position = integer(entry, "position");
        if (position != null && (position == POSITION_AN_TOP || position == POSITION_AN_BOTTOM || position == POSITION_AT_DEPTH)) {
            return InsertionMode.BEFORE_USER_PROMPT;
        }
        return InsertionMode.IN_LORE_BLOCK;
    }

    /**
     * @return filter for the entry, {@code null} when the entry is always active
     */
    static Filter filter(JsonObject entry, List<String> primaryKeys, List<String> secondaryKeys) {
        if (bool(entry, "constant", false) || primaryKeys.isEmpty()) {
            return null;
        }
        boolean caseSensitive = bool(entry, "caseSensitive", false);
        boolean wholeWords = bool(entry, "matchWholeWords", false);
        boolean selective = bool(entry, "selective", true);
        List<String> secondary = selective ? secondaryKeys : List.of();

        if (primaryKeys.size() == 1 && secondary.isEmpty() && !caseSensitive && !wholeWords
                && !REGEX_KEY.matcher(primaryKeys.getFirst()).matches()) {
            return new Filter(primaryKeys.getFirst(), FilteringMode.TEXT);
        }

        StringBuilder regex = new StringBuilder("(?s)");
        if (wholeWords) {
            // \w also covers non-ASCII letters
            regex.append("(?U)");
        }
        if (caseSensitive) {
            regex.append("(?-i)");
        }
        regex.append('^');
        regex.append("(?=.*").append(anyOf(primaryKeys, wholeWords)).append(')');
        if (!secondary.isEmpty()) {
            int logic = integer(entry, "selectiveLogic") == null ? AND_ANY : integer(entry, "selectiveLogic");
            switch (logic) {
                case NOT_ALL -> regex.append("(?!").append(allOf(secondary, wholeWords)).append(')');
                case NOT_ANY -> regex.append("(?!.*").append(anyOf(secondary, wholeWords)).append(')');
                case AND_ALL -> regex.append(allOf(secondary, wholeWords));
                default -> regex.append("(?=.*").append(anyOf(secondary, wholeWords)).append(')');
            }
        }
        return new Filter(regex.toString(), FilteringMode.REGEX);
    }

    /**
     * Settings that have no Marginalia counterpart, so they are visible to the user instead of silently dropped.
     */
    static String unsupportedSettings(JsonObject entry) {
        List<String> notes = new ArrayList<>();
        Integer position = integer(entry, "position");
        if (position != null && position == POSITION_AT_DEPTH) {
            Integer depth = integer(entry, "depth");
            notes.add("inserted at depth " + (depth == null ? "?" : depth) + " (imported as before user prompt)");
        }
        Integer probability = integer(entry, "probability");
        if (bool(entry, "useProbability", true) && probability != null && probability < 100) {
            notes.add("probability " + probability + "%");
        }
        String group = string(entry, "group");
        if (StringUtils.isNotBlank(group)) {
            notes.add("inclusion group \"" + group + "\"");
        }
        for (String timed : List.of("sticky", "cooldown", "delay")) {
            Integer value = integer(entry, timed);
            if (value != null && value > 0) {
                notes.add(timed + " " + value);
            }
        }
        if (notes.isEmpty()) {
            return null;
        }
        return "Imported from SillyTavern, not supported: " + String.join(", ", notes) + ".";
    }

    private static String anyOf(List<String> keys, boolean wholeWords) {
        List<String> parts = new ArrayList<>();
        for (String key : keys) {
            parts.add(keyExpression(key, wholeWords));
        }
        return "(?:" + String.join("|", parts) + ")";
    }

    private static String allOf(List<String> keys, boolean wholeWords) {
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append("(?=.*").append(keyExpression(key, wholeWords)).append(')');
        }
        return sb.toString();
    }

    /**
     * One key as a regex fragment: {@code /pattern/flags} keys keep their pattern (case-sensitive unless flagged
     * {@code i}, as in JavaScript), plain keys are quoted.
     */
    static String keyExpression(String key, boolean wholeWords) {
        Matcher m = REGEX_KEY.matcher(key);
        if (m.matches()) {
            String pattern = m.group(1);
            String flags = m.group(2);
            try {
                Pattern.compile(pattern);
                StringBuilder inline = new StringBuilder();
                if (flags.contains("s")) inline.append('s');
                if (flags.contains("m")) inline.append('m');
                inline.append(flags.contains("i") ? "i" : "-i");
                return "(?" + inline + ":" + pattern + ")";
            } catch (PatternSyntaxException e) {
                // JavaScript-only syntax, match the text literally
            }
        }
        String quoted = Pattern.quote(key);
        return wholeWords ? "(?<!\\w)" + quoted + "(?!\\w)" : quoted;
    }

    static String string(JsonObject parent, String field) {
        JsonElement value = parent.get(field);
        return value == null || value.isJsonNull() || !value.isJsonPrimitive() ? null : value.getAsString();
    }

    private static boolean bool(JsonObject parent, String field, boolean defaultValue) {
        JsonElement value = parent.get(field);
        if (value == null || value.isJsonNull() || !value.isJsonPrimitive()) {
            return defaultValue;
        }
        return value.getAsBoolean();
    }

    private static Integer integer(JsonObject parent, String field) {
        JsonElement value = parent.get(field);
        if (value == null || value.isJsonNull() || !value.isJsonPrimitive()) {
            return null;
        }
        try {
            return value.getAsInt();
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
