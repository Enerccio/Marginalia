package com.github.enerccio.marginalia.domain.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

/**
 * The pure parts of merging a story part into the part before it: how the texts of the prompt are combined, how the
 * data kept on the two parts ({@code attributes}) differ and how they are put together. No database, see
 * {@link com.github.enerccio.marginalia.domain.service.ChatMessageService#mergeIntoPrevious}.
 */
public final class PartMerge {

    private PartMerge() {
    }

    /**
     * What stands between the text of the previous part and the text of the part merged into it.
     */
    public enum Join {
        /**
         * An empty line.
         */
        PARAGRAPH("\n\n"),
        /**
         * A line break.
         */
        LINE("\n"),
        /**
         * One space: the text goes on in the same paragraph.
         */
        SPACE(" "),
        /**
         * Nothing: the previous part was cut inside a word.
         */
        NONE("");

        private final String separator;

        Join(String separator) {
            this.separator = separator;
        }

        public String separator() {
            return separator;
        }

        /**
         * The join a text asks for when the author does not say: a paragraph after a text that ended a sentence or a line,
         * a space after one that was cut in the middle of a sentence.
         */
        public static Join suggest(String previous, String next) {
            if (previous == null || previous.isBlank() || next == null || next.isBlank()) {
                return NONE;
            }
            if (Character.isWhitespace(previous.charAt(previous.length() - 1)) || Character.isWhitespace(next.charAt(0))) {
                return NONE;
            }
            char last = previous.charAt(previous.length() - 1);
            return ".!?…\"'”’»)*_~`]".indexOf(last) >= 0 ? PARAGRAPH : SPACE;
        }
    }

    /**
     * Puts two texts together with the join; a blank text is left out.
     */
    public static String join(String previous, String next, Join join) {
        if (previous == null || previous.isBlank()) {
            return next == null ? "" : next;
        }
        if (next == null || next.isBlank()) {
            return previous;
        }
        return previous + join.separator() + next;
    }

    /**
     * The text of a prompt field of the merged turn: both texts without the start they share. {@code a} is the field of the
     * earlier turn, {@code b} the field of the later one. The shared start is cut at a word, so "Write the scene. Keep it
     * short" and "Write the scene. Add rain" give "Write the scene. Keep it short" and then "Add rain" on a new line. When
     * one text is the start of the other the longer one is the result, when they are the same the result is that text.
     * Null when both are blank.
     */
    public static String combineText(String a, String b) {
        boolean blankA = a == null || a.isBlank();
        boolean blankB = b == null || b.isBlank();
        if (blankA && blankB) {
            return null;
        }
        if (blankA) {
            return b;
        }
        if (blankB) {
            return a;
        }
        if (startsWithWords(b, a)) {
            return b;
        }
        if (startsWithWords(a, b)) {
            return a;
        }
        int common = 0;
        int limit = Math.min(a.length(), b.length());
        while (common < limit && a.charAt(common) == b.charAt(common)) {
            common++;
        }
        // do not cut inside a word: go back to the end of the last whole word the texts share
        int cut = common;
        while (cut > 0 && !Character.isWhitespace(a.charAt(cut - 1))) {
            cut--;
        }
        String rest = b.substring(cut).strip();
        return rest.isEmpty() ? a : a.stripTrailing() + "\n" + rest;
    }

    /**
     * Whether {@code text} starts with {@code start} and the start ends where a word ends ("Write" is not the start of
     * "Writing").
     */
    private static boolean startsWithWords(String text, String start) {
        if (!text.startsWith(start)) {
            return false;
        }
        return text.length() == start.length() || !Character.isLetterOrDigit(text.charAt(start.length()))
                || !Character.isLetterOrDigit(start.charAt(start.length() - 1));
    }

    /**
     * The same data of the two parts under one name.
     *
     * @param path     the name of the value in the attributes: the keys from the top, a value inside a JSON object is
     *                 looked at on its own ({@code templateVariables, mood})
     * @param previous the value on the previous part, {@code null} when it has none
     * @param later    the value on the part that is merged, {@code null} when it has none
     */
    public record AttributeChange(List<String> path, JsonElement previous, JsonElement later) {

        public Kind kind() {
            if (previous == null) {
                return Kind.ONLY_LATER;
            }
            if (later == null) {
                return Kind.ONLY_PREVIOUS;
            }
            return previous.equals(later) ? Kind.SAME : Kind.DIFFERENT;
        }

        public String name() {
            return String.join(" › ", path);
        }

        /**
         * The value that is taken when the author does not choose: the one of the later part, it is where the story is now
         * (variables are carried from part to part), the value of the previous part only when the later one has none.
         */
        public boolean takesPreviousByDefault() {
            return later == null;
        }
    }

    public enum Kind {
        /**
         * Both parts have the same value, nothing to decide.
         */
        SAME,
        /**
         * The values differ.
         */
        DIFFERENT,
        /**
         * Only the previous part has the value.
         */
        ONLY_PREVIOUS,
        /**
         * Only the part that is merged has the value.
         */
        ONLY_LATER
    }

    /**
     * Compares the attributes of two parts value by value: JSON objects are opened (so two variables are two lines, not one
     * {@code templateVariables} that differs), anything else is a value. Sorted by the name, the same ones are included.
     */
    public static List<AttributeChange> diffAttributes(JsonObject previous, JsonObject later) {
        Map<List<String>, JsonElement> before = new TreeMap<>(PATHS);
        Map<List<String>, JsonElement> after = new TreeMap<>(PATHS);
        flatten(List.of(), previous, before);
        flatten(List.of(), later, after);
        Set<List<String>> paths = new TreeSet<>(PATHS);
        paths.addAll(before.keySet());
        paths.addAll(after.keySet());
        List<AttributeChange> changes = new ArrayList<>();
        for (List<String> path : paths) {
            changes.add(new AttributeChange(path, before.get(path), after.get(path)));
        }
        return changes;
    }

    /**
     * The attributes of the merged part: the values of the later part, except the ones in {@code fromPrevious}, which come from
     * the previous part, and the ones only the previous part has (unless it is in {@code dropped}). Empty objects that remain
     * are not kept.
     *
     * @param fromPrevious the paths ({@link AttributeChange#path()}) whose value of the previous part is taken
     * @param dropped      the paths that are left out of the result altogether
     */
    public static JsonObject mergeAttributes(JsonObject previous, JsonObject later, Set<List<String>> fromPrevious, Set<List<String>> dropped) {
        JsonObject merged = new JsonObject();
        for (AttributeChange change : diffAttributes(previous, later)) {
            if (dropped.contains(change.path())) {
                continue;
            }
            boolean takePrevious = change.later() == null || fromPrevious.contains(change.path()) && change.previous() != null;
            JsonElement value = takePrevious ? change.previous() : change.later();
            if (value != null) {
                put(merged, change.path(), value.deepCopy());
            }
        }
        return merged;
    }

    private static final Comparator<List<String>> PATHS = (x, y) -> {
        for (int i = 0; i < Math.min(x.size(), y.size()); i++) {
            int c = x.get(i).compareTo(y.get(i));
            if (c != 0) {
                return c;
            }
        }
        return Integer.compare(x.size(), y.size());
    };

    private static void flatten(List<String> prefix, JsonObject object, Map<List<String>, JsonElement> into) {
        if (object == null) {
            return;
        }
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            List<String> path = new ArrayList<>(prefix);
            path.add(entry.getKey());
            JsonElement value = entry.getValue();
            if (value != null && value.isJsonObject() && !value.getAsJsonObject().isEmpty()) {
                flatten(path, value.getAsJsonObject(), into);
            } else {
                into.put(path, value == null ? com.google.gson.JsonNull.INSTANCE : value);
            }
        }
    }

    private static void put(JsonObject root, List<String> path, JsonElement value) {
        JsonObject at = root;
        for (int i = 0; i < path.size() - 1; i++) {
            JsonElement next = at.get(path.get(i));
            if (next == null || !next.isJsonObject()) {
                next = new JsonObject();
                at.add(path.get(i), next);
            }
            at = next.getAsJsonObject();
        }
        at.add(path.getLast(), value);
    }

}
