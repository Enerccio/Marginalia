package com.github.enerccio.marginalia.domain.service.search;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Query of a full-text search over the {@code _fulltext} column: the words (or phrases in double quotes) that must all
 * be in the text, in any order. {@code *} in a word matches any characters. The words are searched with
 * {@code LIKE ... ESCAPE '\'} (see {@link #likePatterns()}), the matched text is used for {@link #snippet}.
 * <p>
 * SQLite's {@code LIKE} ignores case only for the letters A-Z, so each word is also tried in lower case, upper case
 * and capitalized, which covers accented letters in the usual spellings of a word.
 */
public final class FulltextQuery {

    private static final Pattern TERM = Pattern.compile("\"([^\"]+)\"|(\\S+)");

    private final List<String> terms;

    private FulltextQuery(List<String> terms) {
        this.terms = terms;
    }

    public static FulltextQuery parse(String query) {
        List<String> terms = new ArrayList<>();
        if (query != null) {
            Matcher matcher = TERM.matcher(query);
            while (matcher.find()) {
                String term = matcher.group(1) != null ? matcher.group(1).strip() : matcher.group(2);
                if (!term.isEmpty()) {
                    terms.add(term);
                }
            }
        }
        return new FulltextQuery(terms);
    }

    /**
     * No words: nothing is searched.
     */
    public boolean isEmpty() {
        return terms.isEmpty();
    }

    /**
     * For each word the {@code LIKE} patterns (escape character {@code \}), one of which must match; the text has to
     * match one pattern of every word.
     */
    public List<List<String>> likePatterns() {
        List<List<String>> patterns = new ArrayList<>();
        for (String term : terms) {
            Set<String> variants = new LinkedHashSet<>();
            variants.add(term);
            variants.add(term.toLowerCase());
            variants.add(term.toUpperCase());
            variants.add(capitalize(term));
            patterns.add(variants.stream().map(v -> "%" + LikePatterns.fromWildcards(v) + "%").toList());
        }
        return patterns;
    }

    private static String capitalize(String term) {
        int first = term.offsetByCodePoints(0, 1);
        return term.substring(0, first).toUpperCase() + term.substring(first).toLowerCase();
    }

    /**
     * The first match of the first word with {@code radius} characters around it, on one line.
     */
    public String snippet(String text, int radius) {
        if (text == null || terms.isEmpty()) {
            return "";
        }
        StringBuilder regex = new StringBuilder();
        for (String part : terms.getFirst().split("\\*", -1)) {
            if (!regex.isEmpty() || part.isEmpty()) {
                regex.append(".*?");
            }
            if (!part.isEmpty()) {
                regex.append(Pattern.quote(part));
            }
        }
        Matcher matcher = Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL)
                .matcher(text);
        if (!matcher.find()) {
            return "";
        }
        int start = Math.max(0, matcher.start() - radius);
        int end = Math.min(text.length(), matcher.end() + radius);
        // do not cut a surrogate pair
        if (start > 0 && Character.isLowSurrogate(text.charAt(start))) {
            start--;
        }
        if (end < text.length() && Character.isLowSurrogate(text.charAt(end))) {
            end++;
        }
        String snippet = text.substring(start, end).replaceAll("\\s+", " ").strip();
        return (start > 0 ? "…" : "") + snippet + (end < text.length() ? "…" : "");
    }
}
