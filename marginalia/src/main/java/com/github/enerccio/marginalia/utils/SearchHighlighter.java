package com.github.enerccio.marginalia.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Marks the places of a text that a search found: every match is wrapped in a {@code <mark>} element of the given CSS
 * class (the one the user is at gets a second class), the result is markdown with inline HTML for the {@code Markdown}
 * component. The HTML the text may already have from {@link GeneratedTextHighlighter} ({@code <mark>} tags) is left alone.
 */
public final class SearchHighlighter {

    private static final Pattern MARK_TAG = Pattern.compile("</?mark\\b[^>]*>");

    private SearchHighlighter() {
    }

    /**
     * Number of places the pattern matches in the text.
     */
    public static int count(String text, Pattern pattern) {
        return text == null || pattern == null ? 0 : mark(text, pattern, null, -1, null).count();
    }

    /**
     * @param current the number (from 0, in the order of the text) of the match that gets {@code currentCssClass} too,
     *                -1 for none
     * @return the text with the matches wrapped; the text itself when the pattern is null or nothing matches
     */
    public static String highlight(String text, Pattern pattern, String cssClass, int current, String currentCssClass) {
        if (text == null || text.isEmpty() || pattern == null) {
            return text;
        }
        Marked marked = mark(text, pattern, cssClass, current, currentCssClass);
        return marked.count() == 0 ? text : marked.text();
    }

    private record Marked(String text, int count) {
    }

    private static Marked mark(String text, Pattern pattern, String cssClass, int current, String currentCssClass) {
        boolean write = cssClass != null;
        StringBuilder out = new StringBuilder(write ? text.length() + 64 : 0);
        int count = 0;
        int position = 0;
        Matcher tags = MARK_TAG.matcher(text);
        // the text between two tags of the diff view is searched on its own, so a match never has a tag inside
        while (position <= text.length()) {
            boolean tag = tags.find(position);
            int end = tag ? tags.start() : text.length();
            Matcher matcher = pattern.matcher(text).region(position, end);
            int copied = position;
            while (matcher.find()) {
                if (matcher.end() == matcher.start()) {
                    continue;
                }
                if (write) {
                    out.append(text, copied, matcher.start()).append("<mark class=\"").append(cssClass);
                    if (count == current && currentCssClass != null) {
                        out.append(' ').append(currentCssClass);
                    }
                    out.append("\">").append(text, matcher.start(), matcher.end()).append("</mark>");
                }
                copied = matcher.end();
                count++;
            }
            if (write) {
                out.append(text, copied, end);
            }
            if (!tag) {
                break;
            }
            if (write) {
                out.append(text, tags.start(), tags.end());
            }
            position = tags.end();
        }
        return new Marked(write ? out.toString() : text, count);
    }
}
