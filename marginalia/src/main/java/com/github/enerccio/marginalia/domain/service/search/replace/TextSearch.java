package com.github.enerccio.marginalia.domain.service.search.replace;

import com.github.enerccio.marginalia.utils.TimeSafeText;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * The text a search and replace looks for: a literal text, which is case sensitive or not as asked, or a regular
 * expression, which is always case sensitive unless it switches that off itself with {@code (?i)}. The replacement of a
 * regular expression is the one of
 * {@link Matcher#appendReplacement}: {@code $1} is a group, {@code \$} a dollar sign.
 * <p>
 * Matches of zero length (a regular expression like {@code a*}) are ignored. A regular expression gets
 * {@link TimeSafeText#timeoutMillis()} for each text and then fails with {@link com.github.enerccio.marginalia.utils.RegexTimeoutException}.
 */
public final class TextSearch {

    /**
     * How much text is kept around a match for {@link TextHit}.
     */
    public static final int CONTEXT_RADIUS = 40;

    private final String search;
    private final String replacement;
    // null for a literal text that is case sensitive, which is searched without a pattern
    private final Pattern pattern;
    private final boolean regex;
    private final boolean caseSensitive;

    private TextSearch(String search, String replacement, Pattern pattern, boolean regex, boolean caseSensitive) {
        this.search = search;
        this.replacement = replacement;
        this.pattern = pattern;
        this.regex = regex;
        this.caseSensitive = caseSensitive;
    }

    /**
     * Case sensitive.
     */
    public static TextSearch of(String search, String replacement, boolean regex) {
        return of(search, replacement, regex, true);
    }

    /**
     * @throws IllegalArgumentException with the message for the user, when the search is empty or is not a valid
     *                                  regular expression, or the replacement is not valid for it
     * @param caseSensitive           whether a literal text has to match in the case of the letters, a regular expression
     *                                is not affected
     */
    public static TextSearch of(String search, String replacement, boolean regex, boolean caseSensitive) {
        if (search == null || search.isEmpty()) {
            throw new IllegalArgumentException("empty search");
        }
        String replace = replacement == null ? "" : replacement;
        if (!regex) {
            if (caseSensitive) {
                return new TextSearch(search, replace, null, false, true);
            }
            return new TextSearch(search, Matcher.quoteReplacement(replace),
                    Pattern.compile(search, Pattern.LITERAL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), false, false);
        }
        Pattern pattern;
        try {
            pattern = Pattern.compile(search, Pattern.MULTILINE);
        } catch (PatternSyntaxException e) {
            throw new InvalidSearchException(InvalidSearchException.Kind.SEARCH, e.getDescription(), e);
        }
        TextSearch result = new TextSearch(search, replace, pattern, true, true);
        try {
            result.checkReplacement(pattern);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            throw new InvalidSearchException(InvalidSearchException.Kind.REPLACEMENT, e.getMessage(), e);
        }
        return result;
    }

    private void checkReplacement(Pattern pattern) {
        // the references to groups the pattern has not, found out here instead of when the first match is replaced
        int groups = pattern.matcher("").groupCount();
        for (int i = 0; i < replacement.length(); i++) {
            char c = replacement.charAt(i);
            if (c == '\\') {
                i++;
                if (i >= replacement.length()) {
                    throw new IllegalArgumentException("character to be escaped is missing");
                }
            } else if (c == '$') {
                i++;
                if (i >= replacement.length()) {
                    throw new IllegalArgumentException("illegal group reference: group index is missing");
                }
                char next = replacement.charAt(i);
                if (next == '{') {
                    int end = replacement.indexOf('}', i);
                    if (end < 0) {
                        throw new IllegalArgumentException("named capturing group is missing trailing '}'");
                    }
                    String name = replacement.substring(i + 1, end);
                    if (!pattern.namedGroups().containsKey(name)) {
                        throw new IllegalArgumentException("No group with name {" + name + "}");
                    }
                    i = end;
                } else if (next >= '0' && next <= '9') {
                    int number = next - '0';
                    if (number > groups) {
                        throw new IndexOutOfBoundsException("No group " + number);
                    }
                    // like the Matcher, more digits belong to the number while the group exists
                    while (i + 1 < replacement.length() && Character.isDigit(replacement.charAt(i + 1))) {
                        int more = number * 10 + (replacement.charAt(i + 1) - '0');
                        if (more > groups) {
                            break;
                        }
                        number = more;
                        i++;
                    }
                } else {
                    throw new IllegalArgumentException("Illegal group reference");
                }
            }
        }
    }

    /**
     * A text with its matches replaced.
     *
     * @param hits    the matches in the order they are in the text
     * @param newText the text with every match replaced
     */
    public record Result(List<TextHit> hits, String newText) {

        public boolean found() {
            return !hits.isEmpty();
        }
    }

    /**
     * @return whether the text has a match, without building the hits
     */
    public boolean matches(String text) {
        return matches(text, TimeSafeText.view());
    }

    /**
     * @param view the text as the regular expression reads it, a text of the same characters; a caller that can't let
     *             a pattern run for long gives one that throws when its time is over
     */
    public boolean matches(String text, Function<String, CharSequence> view) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        if (pattern == null) {
            return text.contains(search);
        }
        Matcher matcher = pattern.matcher(view.apply(text));
        while (matcher.find()) {
            if (matcher.end() > matcher.start()) {
                return true;
            }
        }
        return false;
    }

    /**
     * The literal text, for a prefilter of the database ({@code LIKE}): null when there is none, for a regular expression
     * (it cannot be narrowed that way) and a text that ignores the case and has other characters than ASCII.
     */
    public String literal() {
        if (regex) {
            return null;
        }
        // the database compares letters without regard to case only for A-Z, a text with other characters is not
        // narrowed down when the case does not matter
        return caseSensitive || search.chars().allMatch(c -> c < 128) ? search : null;
    }

    public Result apply(String text) {
        return apply(null, text);
    }

    /**
     * Finds the matches in the text and replaces them.
     *
     * @param field name of the field the text is from, put into the hits
     */
    public Result apply(String field, String text) {
        return apply(field, text, TimeSafeText.view());
    }

    /**
     * @param view the text as the regular expression reads it, see {@link #matches(String, Function)}
     */
    public Result apply(String field, String text, Function<String, CharSequence> view) {
        List<TextHit> hits = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return new Result(hits, text);
        }
        StringBuilder out = new StringBuilder(text.length() + 16);
        int copied = 0;
        if (pattern == null) {
            for (int at = text.indexOf(search); at >= 0; at = text.indexOf(search, at + search.length())) {
                hits.add(hit(field, text, at, at + search.length(), replacement));
                out.append(text, copied, at).append(replacement);
                copied = at + search.length();
            }
        } else {
            Matcher matcher = pattern.matcher(view.apply(text));
            while (matcher.find()) {
                if (matcher.end() == matcher.start()) {
                    continue;
                }
                int mark = out.length() + (matcher.start() - copied);
                matcher.appendReplacement(out, replacement);
                // appendReplacement copied the text between the matches before the replacement
                hits.add(hit(field, text, matcher.start(), matcher.end(), out.substring(mark)));
                copied = matcher.end();
            }
        }
        if (hits.isEmpty()) {
            return new Result(hits, text);
        }
        out.append(text, copied, text.length());
        return new Result(hits, out.toString());
    }

    private static TextHit hit(String field, String text, int start, int end, String replacement) {
        return new TextHit(field, contextBefore(text, start), text.substring(start, end), contextAfter(text, end), replacement);
    }

    private static String contextBefore(String text, int start) {
        int from = Math.max(0, start - CONTEXT_RADIUS);
        if (from > 0 && Character.isLowSurrogate(text.charAt(from))) {
            from--;
        }
        String context = text.substring(from, start).replaceAll("\\s+", " ");
        if (from > 0) {
            // do not start in the middle of a word
            int space = context.indexOf(' ');
            context = "…" + (space >= 0 ? context.substring(space + 1) : context);
        }
        return context;
    }

    private static String contextAfter(String text, int end) {
        int to = Math.min(text.length(), end + CONTEXT_RADIUS);
        if (to < text.length() && Character.isLowSurrogate(text.charAt(to))) {
            to++;
        }
        String context = text.substring(end, to).replaceAll("\\s+", " ");
        if (to < text.length()) {
            // do not end in the middle of a word
            int space = context.lastIndexOf(' ');
            context = (space >= 0 ? context.substring(0, space) : context) + "…";
        }
        return context;
    }

    /**
     * The search or the replacement of a regular expression is not valid.
     */
    public static final class InvalidSearchException extends IllegalArgumentException {

        public enum Kind {SEARCH, REPLACEMENT}

        private final Kind kind;

        InvalidSearchException(Kind kind, String message, Throwable cause) {
            super(message, cause);
            this.kind = kind;
        }

        public Kind getKind() {
            return kind;
        }
    }

}
