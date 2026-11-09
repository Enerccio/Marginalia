package com.github.enerccio.marginalia.extensions.reallangtranslator.model;

import java.util.*;
import java.util.regex.Pattern;

/**
 * The glossary of names and special words, which the three levels (the language, the folders of the book, the book) hold and this class merges: the
 * <b>union</b> of the levels, and for the same source word the <b>nearest</b> level wins. An entry that is switched off at a nearer level takes the
 * word out of the glossary for that book. Pure code, no service.
 */
public final class Glossary {

    /** The levels of the glossary, the farthest first. */
    public enum Level {
        LANGUAGE, FOLDER, BOOK
    }

    /**
     * The entries of one level.
     *
     * @param level   which kind of level
     * @param id      what tells the level apart from the others of its kind: the uuid of the folder, null for the language and the book
     * @param label   what the user calls it (the language's name, the folder's name, the book's name)
     * @param entries the entries as the level stores them
     */
    public record Layer(Level level, String id, String label, List<GlossaryEntry> entries) {
    }

    /**
     * An entry as the merge found it.
     *
     * @param entry  the entry of the nearest level that has the word
     * @param level  the level it comes from
     * @param id     the id of that level (see {@link Layer#id()})
     * @param label  the name of that level
     * @param active whether it is used: it is enabled and usable
     */
    public record Effective(GlossaryEntry entry, Level level, String id, String label, boolean active) {
    }

    private Glossary() {
    }

    /** The key of a source word: stripped, lower case. */
    public static String key(String source) {
        return source == null ? "" : source.strip().toLowerCase(Locale.ROOT);
    }

    /**
     * Merges the levels.
     *
     * @param layers the levels, the farthest first (the language, the outer folder, the inner folder, the book)
     * @return one entry for every source word in the order the words first appear, the nearest level's version of it
     */
    public static List<Effective> merge(List<Layer> layers) {
        Map<String, Effective> merged = new LinkedHashMap<>();
        for (Layer layer : layers) {
            for (GlossaryEntry entry : layer.entries()) {
                String key = entry.key();
                if (key.isEmpty()) {
                    continue;
                }
                Effective effective = new Effective(entry, layer.level(), layer.id(), layer.label(), entry.isEnabled() && entry.isUsable());
                merged.remove(key);
                merged.put(key, effective);
            }
        }
        return new ArrayList<>(merged.values());
    }

    /**
     * What the model is given: the active entries of the merge.
     */
    public static List<GlossaryEntry> active(List<Effective> merged) {
        return merged.stream().filter(Effective::active).map(Effective::entry).toList();
    }

    /**
     * The entries that matter for a text: those whose source word is in it (a whole word, the case does not matter). Used when the glossary is too big to
     * be sent whole.
     */
    public static List<GlossaryEntry> relevant(List<GlossaryEntry> entries, String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        return entries.stream().filter(entry -> contains(text, entry.getSource())).toList();
    }

    /**
     * Whether the text has the word, as a whole word (so {@code Ann} is not found in {@code Anna}), the case does not matter. A word that has no letter or
     * digit at its edge (a symbol) is found as it is.
     */
    public static boolean contains(String text, String word) {
        if (text == null || word == null || word.isBlank()) {
            return false;
        }
        String stripped = word.strip();
        String pattern = Pattern.quote(stripped);
        String before = Character.isLetterOrDigit(stripped.codePointAt(0)) ? "(?<![\\p{L}\\p{N}])" : "";
        String after = Character.isLetterOrDigit(stripped.codePointBefore(stripped.length())) ? "(?![\\p{L}\\p{N}])" : "";
        return Pattern.compile(before + pattern + after, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(text).find();
    }

    /**
     * A copy of the list with the entry added or, when the level already has the source word, replaced (the id and the place are kept).
     */
    public static List<GlossaryEntry> put(List<GlossaryEntry> entries, GlossaryEntry entry) {
        List<GlossaryEntry> result = new ArrayList<>(entries);
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).getId().equals(entry.getId()) || result.get(i).key().equals(entry.key())) {
                entry.setId(result.get(i).getId());
                result.set(i, entry);
                return result;
            }
        }
        result.add(entry);
        return result;
    }
}
