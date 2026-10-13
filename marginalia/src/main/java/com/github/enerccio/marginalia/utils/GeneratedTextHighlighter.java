package com.github.enerccio.marginalia.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Marks the text of a part that is still the text the LLM wrote: diffs the current response against the original one
 * word by word and wraps what both have in common in {@code <mark>} elements of the given CSS class, so the author's
 * additions and rewrites stay unmarked. The result is markdown with inline HTML for the {@code Markdown} component.
 */
public final class GeneratedTextHighlighter {

    private static final Pattern TOKEN = Pattern.compile("\\s+|[\\p{L}\\p{N}_]+|[^\\p{L}\\p{N}_\\s]");
    // markdown that has to stay at the start of its line to keep working: indentation, headings, lists, quotes
    private static final Pattern LINE_PREFIX = Pattern.compile("^\\s*(?:(?:#{1,6}|[-*+>]|\\d{1,9}[.)])\\s+)*");
    // size of the LCS table (cells) above which only the common start and end are matched
    private static final long MAX_TABLE = 4_000_000L;

    private GeneratedTextHighlighter() {
    }

    /**
     * @return response with the parts shared with the original wrapped in {@code <mark class="cssClass">}; the
     * response itself when there is no original (the part was not generated) or nothing is shared
     */
    public static String highlight(String response, String original, String cssClass) {
        if (response == null || response.isBlank() || original == null || original.isBlank()) {
            return response;
        }

        List<String> tokens = tokenize(response);
        boolean[] generated = matchGenerated(tokens, tokenize(original));
        if (!anyTrue(generated)) {
            return response;
        }

        String open = "<mark class=\"" + cssClass + "\">";
        StringBuilder out = new StringBuilder(response.length() + 64);
        StringBuilder run = new StringBuilder();
        int runStart = 0;
        int position = 0;
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (generated[i]) {
                if (run.isEmpty()) {
                    runStart = position;
                }
                run.append(token);
            } else {
                if (!run.isEmpty()) {
                    appendRun(out, run.toString(), runStart == 0 || response.charAt(runStart - 1) == '\n', open);
                    run.setLength(0);
                }
                out.append(token);
            }
            position += token.length();
        }
        if (!run.isEmpty()) {
            appendRun(out, run.toString(), runStart == 0 || response.charAt(runStart - 1) == '\n', open);
        }
        return out.toString();
    }

    /**
     * Wraps every line of the run on its own: an inline element would otherwise stay open over paragraphs, and the
     * markdown that opens a line stays outside of the element.
     */
    private static void appendRun(StringBuilder out, String run, boolean startsLine, String open) {
        String[] lines = run.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                out.append('\n');
            }
            String line = lines[i];
            int from = 0;
            if (i > 0 || startsLine) {
                Matcher prefix = LINE_PREFIX.matcher(line);
                prefix.find();
                from = prefix.end();
            } else {
                while (from < line.length() && Character.isWhitespace(line.charAt(from))) {
                    from++;
                }
            }
            int to = line.length();
            while (to > from && Character.isWhitespace(line.charAt(to - 1))) {
                to--;
            }
            if (from >= to) {
                out.append(line);
                continue;
            }
            out.append(line, 0, from).append(open).append(line, from, to).append("</mark>").append(line, to, line.length());
        }
    }

    private static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(text);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    private static boolean anyTrue(boolean[] flags) {
        for (boolean flag : flags) {
            if (flag) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSpace(String token) {
        return Character.isWhitespace(token.charAt(0));
    }

    /**
     * Longest common subsequence of the words (white space is not compared) of both token lists; a white space token
     * between two shared words is shared too.
     */
    private static boolean[] matchGenerated(List<String> current, List<String> original) {
        List<Integer> currentWords = wordIndexes(current);
        List<Integer> originalWords = wordIndexes(original);
        int n = originalWords.size();
        int m = currentWords.size();
        boolean[] sharedWord = new boolean[m];

        int prefix = 0;
        while (prefix < n && prefix < m
                && original.get(originalWords.get(prefix)).equals(current.get(currentWords.get(prefix)))) {
            sharedWord[prefix] = true;
            prefix++;
        }
        int suffix = 0;
        while (suffix < n - prefix && suffix < m - prefix
                && original.get(originalWords.get(n - 1 - suffix)).equals(current.get(currentWords.get(m - 1 - suffix)))) {
            sharedWord[m - 1 - suffix] = true;
            suffix++;
        }

        int rows = n - prefix - suffix;
        int columns = m - prefix - suffix;
        if (rows > 0 && columns > 0 && (long) (rows + 1) * (columns + 1) <= MAX_TABLE) {
            int[][] lcs = new int[rows + 1][columns + 1];
            for (int i = rows - 1; i >= 0; i--) {
                String a = original.get(originalWords.get(prefix + i));
                for (int j = columns - 1; j >= 0; j--) {
                    lcs[i][j] = a.equals(current.get(currentWords.get(prefix + j)))
                            ? lcs[i + 1][j + 1] + 1
                            : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
                }
            }
            int i = 0;
            int j = 0;
            while (i < rows && j < columns) {
                if (original.get(originalWords.get(prefix + i)).equals(current.get(currentWords.get(prefix + j)))) {
                    sharedWord[prefix + j] = true;
                    i++;
                    j++;
                } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
                    i++;
                } else {
                    j++;
                }
            }
        }

        boolean[] shared = new boolean[current.size()];
        for (int w = 0; w < m; w++) {
            shared[currentWords.get(w)] = sharedWord[w];
        }
        for (int w = 0; w + 1 < m; w++) {
            int from = currentWords.get(w);
            int to = currentWords.get(w + 1);
            if (sharedWord[w] && sharedWord[w + 1]) {
                for (int t = from + 1; t < to; t++) {
                    shared[t] = true;
                }
            }
        }
        return shared;
    }

    private static List<Integer> wordIndexes(List<String> tokens) {
        List<Integer> words = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            if (!isSpace(tokens.get(i))) {
                words.add(i);
            }
        }
        return words;
    }
}
