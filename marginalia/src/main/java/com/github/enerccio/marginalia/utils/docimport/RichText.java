package com.github.enerccio.marginalia.utils.docimport;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The text of one paragraph as the runs of a word processor give it (a text with bold and italic on or off), turned into
 * Markdown: the runs with the same look are joined, the white space at the ends of a run is moved out of the emphasis (a
 * marker next to a space is not read as one) and the characters Markdown would take for its own are escaped.
 */
final class RichText {

    private record Run(String text, boolean bold, boolean italic) {
    }

    // a line that would start a list, a quote, a heading or a code fence
    private static final Pattern BLOCK_START = Pattern.compile("^(#|>|[-+](\\s|$)|\\d+[.)](\\s|$)|```|~~~)");

    private final List<Run> runs = new ArrayList<>();

    void add(String text, boolean bold, boolean italic) {
        if (text == null || text.isEmpty()) {
            return;
        }
        if (!runs.isEmpty()) {
            Run last = runs.getLast();
            if (last.bold == bold && last.italic == italic) {
                runs.set(runs.size() - 1, new Run(last.text + text, bold, italic));
                return;
            }
        }
        runs.add(new Run(text, bold, italic));
    }

    boolean isBlank() {
        return runs.stream().allMatch(run -> run.text.isBlank());
    }

    /**
     * @return the plain text, without any emphasis
     */
    String plain() {
        StringBuilder text = new StringBuilder();
        runs.forEach(run -> text.append(run.text));
        return normalize(text.toString(), " ");
    }

    /**
     * @return the Markdown of the paragraph, empty when it has no text
     */
    String markdown() {
        StringBuilder markdown = new StringBuilder();
        for (Run run : runs) {
            String text = run.text;
            int start = 0;
            int end = text.length();
            while (start < end && Character.isWhitespace(text.charAt(start))) {
                start++;
            }
            while (end > start && Character.isWhitespace(text.charAt(end - 1))) {
                end--;
            }
            if (start == end || !run.bold && !run.italic) {
                markdown.append(escape(text));
                continue;
            }
            String marker = run.bold && run.italic ? "***" : run.bold ? "**" : "*";
            markdown.append(escape(text.substring(0, start))).append(marker).append(escape(text.substring(start, end)))
                    .append(marker).append(escape(text.substring(end)));
        }
        String result = normalize(markdown.toString(), "  \n");
        if (BLOCK_START.matcher(result).find()) {
            result = "\\" + result;
        }
        return result;
    }

    // line breaks are kept as the given separator (a Markdown hard break), tabs and the other white space become a space, the ends are trimmed
    private static String normalize(String text, String lineBreak) {
        String[] lines = text.split("\n", -1);
        List<String> kept = new ArrayList<>();
        for (String line : lines) {
            String clean = line.replaceAll("[\\s\\u00A0]+", " ").strip();
            if (!clean.isEmpty()) {
                kept.add(clean);
            }
        }
        return String.join(lineBreak, kept);
    }

    private static String escape(String text) {
        StringBuilder result = new StringBuilder(text.length() + 8);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' || c == '*' || c == '_' || c == '`') {
                result.append('\\');
            }
            result.append(c);
        }
        return result.toString();
    }
}
