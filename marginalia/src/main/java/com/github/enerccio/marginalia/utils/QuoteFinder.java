package com.github.enerccio.marginalia.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds a quoted passage in a text, for what has to point to a passage without a trustworthy offset: an annotation whose
 * part was replaced as a whole, and a model that was asked to quote what it means (it can't count characters).
 * <p>
 * Tries, in this order: the quote as it is; the quote ignoring case and the amount of white space; the passage of the text
 * whose words are the most like the words of the quote (a model quotes loosely, the author fixed a word). Where the passage is
 * found more than once, the text around it ({@code before}, {@code after}) and the {@code hint} (where it used to be) decide.
 */
public final class QuoteFinder {

    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}_]+(?:['’][\\p{L}\\p{N}_]+)*");
    // the words of the quote that are looked at, a longer quote is found by its start
    private static final int MAX_QUOTE_WORDS = 120;
    // the share of the words of the quote that the passage has in the same order, below this nothing is found
    private static final double MIN_SIMILARITY = 0.7;
    // the longest text the fuzzy search goes through (words times words of the quote)
    private static final long MAX_WORK = 30_000_000L;

    private QuoteFinder() {
    }

    /**
     * @param exact false when the passage is not the quote letter for letter
     */
    public record Match(int start, int end, boolean exact) {
    }

    /**
     * @param before the text that was right before the passage, null when not known
     * @param after  the text that was right after the passage, null when not known
     * @param hint   where the passage was (its start), negative when not known
     * @return null when the passage is not in the text
     */
    public static Match find(String text, String quote, String before, String after, int hint) {
        if (text == null || quote == null || quote.isBlank() || text.isBlank()) {
            return null;
        }

        List<Integer> hits = occurrences(text, quote);
        if (!hits.isEmpty()) {
            int best = best(text, hits, quote.length(), before, after, hint);
            return new Match(best, best + quote.length(), true);
        }

        Match loose = findLoose(text, quote, before, after, hint);
        if (loose != null) {
            return loose;
        }
        return findSimilar(text, quote, before, after, hint);
    }

    /**
     * Like {@link #find} but only a place where the quote is, letter for letter.
     *
     * @return null when the text does not have the quote
     */
    public static Match findExact(String text, String quote, String before, String after, int hint) {
        if (text == null || quote == null || quote.isBlank() || text.isBlank()) {
            return null;
        }
        List<Integer> hits = occurrences(text, quote);
        if (hits.isEmpty()) {
            return null;
        }
        int best = best(text, hits, quote.length(), before, after, hint);
        return new Match(best, best + quote.length(), true);
    }

    private static List<Integer> occurrences(String text, String quote) {
        List<Integer> hits = new ArrayList<>();
        for (int at = text.indexOf(quote); at >= 0; at = text.indexOf(quote, at + 1)) {
            hits.add(at);
        }
        return hits;
    }

    /**
     * The one of the places that has the most of the context around it, then the one closest to where the passage was.
     */
    private static int best(String text, List<Integer> starts, int length, String before, String after, int hint) {
        int best = starts.getFirst();
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int start : starts) {
            double score = contextScore(text, start, start + length, before, after);
            if (hint >= 0) {
                // less than one character of the context is worth, only a tie is decided by it
                score -= Math.min(Math.abs(start - hint), 100_000) / 1_000_000.0;
            }
            if (score > bestScore) {
                bestScore = score;
                best = start;
            }
        }
        return best;
    }

    /**
     * How many characters of the text right before and right after the passage are the same as the context.
     */
    private static double contextScore(String text, int start, int end, String before, String after) {
        int score = 0;
        if (before != null) {
            int i = start - 1;
            int j = before.length() - 1;
            while (i >= 0 && j >= 0 && text.charAt(i) == before.charAt(j)) {
                score++;
                i--;
                j--;
            }
        }
        if (after != null) {
            int i = end;
            int j = 0;
            while (i < text.length() && j < after.length() && text.charAt(i) == after.charAt(j)) {
                score++;
                i++;
                j++;
            }
        }
        return score;
    }

    // ---- ignoring the case and the white space

    private record Normalized(String text, int[] starts, int[] ends) {
    }

    /**
     * Lower case, runs of white space as one space; for every character of the result where it starts and ends in the text.
     */
    private static Normalized normalize(String text) {
        StringBuilder out = new StringBuilder(text.length());
        int[] starts = new int[text.length()];
        int[] ends = new int[text.length()];
        for (int i = 0; i < text.length(); ) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                int j = i;
                while (j < text.length() && Character.isWhitespace(text.charAt(j))) {
                    j++;
                }
                starts[out.length()] = i;
                ends[out.length()] = j;
                out.append(' ');
                i = j;
            } else {
                starts[out.length()] = i;
                ends[out.length()] = i + 1;
                out.append(Character.toLowerCase(c));
                i++;
            }
        }
        return new Normalized(out.toString(), starts, ends);
    }

    private static Match findLoose(String text, String quote, String before, String after, int hint) {
        String needle = normalize(quote.strip()).text();
        if (needle.isEmpty()) {
            return null;
        }
        Normalized haystack = normalize(text);
        List<Integer> hits = occurrences(haystack.text(), needle);
        if (hits.isEmpty()) {
            return null;
        }
        // the context is compared in the text itself, so every candidate is mapped back first
        int bestIndex = 0;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int k = 0; k < hits.size(); k++) {
            int start = haystack.starts()[hits.get(k)];
            int end = haystack.ends()[hits.get(k) + needle.length() - 1];
            double score = contextScore(text, start, end, before, after);
            if (hint >= 0) {
                score -= Math.min(Math.abs(start - hint), 100_000) / 1_000_000.0;
            }
            if (score > bestScore) {
                bestScore = score;
                bestIndex = k;
            }
        }
        int first = hits.get(bestIndex);
        return new Match(haystack.starts()[first], haystack.ends()[first + needle.length() - 1], false);
    }

    // ---- the most similar passage

    private record Words(List<String> words, List<int[]> spans) {
    }

    private static Words wordsOf(String text, boolean lowerCase) {
        List<String> words = new ArrayList<>();
        List<int[]> spans = new ArrayList<>();
        Matcher matcher = WORD.matcher(text);
        while (matcher.find()) {
            String word = matcher.group();
            words.add(lowerCase ? word.toLowerCase(Locale.ROOT) : word);
            spans.add(new int[]{matcher.start(), matcher.end()});
        }
        return new Words(words, spans);
    }

    private static Match findSimilar(String text, String quote, String before, String after, int hint) {
        Words quoted = wordsOf(quote, true);
        if (quoted.words().size() < 2) {
            return null;
        }
        List<String> q = quoted.words().size() > MAX_QUOTE_WORDS
                ? quoted.words().subList(0, MAX_QUOTE_WORDS) : quoted.words();
        Words haystack = wordsOf(text, true);
        List<String> t = haystack.words();
        int window = q.size();
        if (t.size() < Math.max(2, window / 2) || (long) t.size() * window > MAX_WORK) {
            return null;
        }

        // the words of the quote and how many times each is there, to find the windows with enough of them cheaply
        Map<String, Integer> wanted = new HashMap<>();
        for (String word : q) {
            wanted.merge(word, 1, Integer::sum);
        }
        int size = Math.min(window, t.size());
        Map<String, Integer> inWindow = new HashMap<>();
        int overlap = 0;
        double bestScore = 0;
        int bestFrom = -1;
        int bestTo = -1;
        for (int i = 0; i < t.size(); i++) {
            overlap += add(inWindow, wanted, t.get(i), 1);
            if (i >= size) {
                overlap += add(inWindow, wanted, t.get(i - size), -1);
            }
            if (i < size - 1 || overlap < MIN_SIMILARITY * window * 0.9) {
                continue;
            }
            int from = i - size + 1;
            double similarity = lcs(q, t, from, i + 1) / (double) window;
            if (similarity < MIN_SIMILARITY) {
                continue;
            }
            // the window can be shifted: its edges are the words the quote has
            int trimmedFrom = from;
            while (trimmedFrom <= i && !wanted.containsKey(t.get(trimmedFrom))) {
                trimmedFrom++;
            }
            int trimmedTo = i;
            while (trimmedTo >= trimmedFrom && !wanted.containsKey(t.get(trimmedTo))) {
                trimmedTo--;
            }
            if (trimmedTo < trimmedFrom) {
                continue;
            }
            int start = haystack.spans().get(trimmedFrom)[0];
            int end = haystack.spans().get(trimmedTo)[1];
            double score = similarity + Math.min(contextScore(text, start, end, before, after), 40) / 400.0;
            if (hint >= 0) {
                score -= Math.min(Math.abs(start - hint), 100_000) / 100_000_000.0;
            }
            if (score > bestScore) {
                bestScore = score;
                bestFrom = start;
                bestTo = end;
            }
        }
        return bestFrom < 0 ? null : new Match(bestFrom, bestTo, false);
    }

    /**
     * @return how much the number of the words of the window that the quote wants changed: 1 when the word is one the quote
     * still has room for, 0 when not
     */
    private static int add(Map<String, Integer> inWindow, Map<String, Integer> wanted, String word, int delta) {
        Integer limit = wanted.get(word);
        if (limit == null) {
            return 0;
        }
        int before = inWindow.getOrDefault(word, 0);
        int after = before + delta;
        inWindow.put(word, after);
        // counted while the window has no more of the word than the quote
        if (delta > 0) {
            return after <= limit ? 1 : 0;
        }
        return before <= limit ? -1 : 0;
    }

    /**
     * Length of the longest common subsequence of the words of the quote and the words [from, to) of the text.
     */
    private static int lcs(List<String> quote, List<String> text, int from, int to) {
        int n = quote.size();
        int m = to - from;
        int[] previous = new int[m + 1];
        int[] current = new int[m + 1];
        for (int i = 1; i <= n; i++) {
            String word = quote.get(i - 1);
            for (int j = 1; j <= m; j++) {
                current[j] = word.equals(text.get(from + j - 1))
                        ? previous[j - 1] + 1
                        : Math.max(previous[j], current[j - 1]);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[m];
    }

}
