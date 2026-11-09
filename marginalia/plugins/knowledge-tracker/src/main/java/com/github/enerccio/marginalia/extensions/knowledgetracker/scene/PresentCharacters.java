package com.github.enerccio.marginalia.extensions.knowledgetracker.scene;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the free text of the present characters field of a part into names, leniently, so that only the tracked
 * characters that are really in the scene reach the model.
 * <p>
 * Text in brackets is dropped ({@code Mara (injured, asleep)}), the rest is split on the configured separators, a note
 * after a dash or a colon is cut ({@code Mara - wounded}) and what remains is a name. Names are compared normalized
 * (case, accents, fullwidth forms, spacing, a leading "the" are ignored) and as a whole: {@code Mara's brother} is not
 * {@code Mara}. No fuzzy matching: a miss costs one block, a false hit sends a character to the model that is not
 * there; aliases of the tracked character are the way to widen it.
 * <p>
 * Pure functions, no state.
 */
public final class PresentCharacters {

    /**
     * The separators when neither the book nor the user sets their own. A separator made of Latin letters only (for
     * example {@code and}) splits at that word between spaces, anything else is split at wherever it stands. A
     * newline is {@code "\n"}.
     */
    public static final List<String> DEFAULT_SEPARATORS = List.of(",", ";", "\n", "/", "|", "、", "，", "；");

    private static final String OPENERS = "([{（［【｛「『〈《";
    private static final String CLOSERS = ")]}）］】｝」』〉》";
    private static final Pattern NOTE = Pattern.compile("\\s[-–—]\\s|[:：]");
    private static final Pattern LATIN_WORD = Pattern.compile("[A-Za-z]+");
    private static final Pattern MARKS = Pattern.compile("[\\p{Mn}&&[^\\u3099\\u309A]]");
    private static final Pattern SPACES = Pattern.compile("\\s+");
    private static final Pattern EDGE_PUNCTUATION = Pattern.compile("^[\\p{P}\\s]+|[\\p{P}\\s]+$");

    private PresentCharacters() {
    }

    /**
     * The names in the field, in order, as they are written (brackets and notes removed, trimmed, no empty ones).
     *
     * @param text       the field, may be {@code null}
     * @param separators the separators, {@code null} or empty = {@link #DEFAULT_SEPARATORS}
     */
    public static List<String> parse(String text, Collection<String> separators) {
        List<String> names = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return names;
        }
        String cleaned = stripBrackets(text.replace("\r\n", "\n").replace('\r', '\n'));
        for (String piece : separatorPattern(separators).split(cleaned)) {
            String name = cutNote(piece).strip();
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return names;
    }

    /**
     * The normalized names of the field, see {@link #normalize(String)}.
     */
    public static Set<String> normalizedNames(String text, Collection<String> separators) {
        Set<String> result = new LinkedHashSet<>();
        for (String name : parse(text, separators)) {
            String normalized = normalize(name);
            if (!normalized.isEmpty()) {
                result.add(normalized);
            }
        }
        return result;
    }

    /**
     * Whether a tracked character is among the normalized names of a field.
     *
     * @param present the result of {@link #normalizedNames}
     * @param name    the name of the tracked character
     * @param aliases its aliases, may be {@code null}
     */
    public static boolean isPresent(Set<String> present, String name, Collection<String> aliases) {
        if (present == null || present.isEmpty()) {
            return false;
        }
        if (present.contains(normalize(name))) {
            return true;
        }
        if (aliases != null) {
            for (String alias : aliases) {
                if (present.contains(normalize(alias))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The form in which names are compared: fullwidth and compatibility forms folded, accents removed (not the Japanese
     * voicing marks), lower case, spaces collapsed, punctuation around it and a leading "the" removed. The same
     * function is applied to the field and to the tracked names, so "The Duke" is the same as "duke".
     */
    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        String s = Normalizer.normalize(name, Normalizer.Form.NFKC);
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = MARKS.matcher(s).replaceAll("");
        s = Normalizer.normalize(s, Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
        s = SPACES.matcher(s).replaceAll(" ");
        s = EDGE_PUNCTUATION.matcher(s).replaceAll("");
        if (s.startsWith("the ")) {
            s = s.substring(4).strip();
        }
        return s;
    }

    /**
     * Removes the text in brackets, nested ones included. A stray closing bracket is dropped. An opening bracket that is
     * never closed drops the rest of its line (the safe side: a name that is hidden costs one block, a name that is
     * taken from a note would send a character that is not there).
     */
    static String stripBrackets(String text) {
        StringBuilder out = new StringBuilder(text.length());
        int depth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (OPENERS.indexOf(c) >= 0) {
                depth++;
            } else if (CLOSERS.indexOf(c) >= 0) {
                if (depth > 0) {
                    depth--;
                }
            } else if (depth == 0) {
                out.append(c);
            } else if (c == '\n') {
                // an unclosed bracket ends with its line
                depth = 0;
                out.append(c);
            }
        }
        return out.toString();
    }

    private static String cutNote(String piece) {
        Matcher m = NOTE.matcher(piece);
        return m.find() ? piece.substring(0, m.start()) : piece;
    }

    private static Pattern separatorPattern(Collection<String> separators) {
        Collection<String> used = separators == null || separators.isEmpty() ? DEFAULT_SEPARATORS : separators;
        List<String> alternatives = new ArrayList<>();
        for (String separator : used) {
            if (separator == null) {
                continue;
            }
            if (separator.isBlank()) {
                // a blank separator is only meaningful as a newline
                if (separator.indexOf('\n') >= 0) {
                    alternatives.add("\\n");
                }
                continue;
            }
            String s = separator.strip();
            if (LATIN_WORD.matcher(s).matches()) {
                alternatives.add("(?<=\\s)" + Pattern.quote(s) + "(?=\\s)");
            } else {
                alternatives.add(Pattern.quote(s));
            }
        }
        if (alternatives.isEmpty()) {
            return separatorPattern(DEFAULT_SEPARATORS);
        }
        return Pattern.compile(String.join("|", alternatives), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }
}
