package com.github.enerccio.marginalia.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Marks ranges of a markdown text with inline {@code <mark>} elements for the {@code Markdown} component. The ranges are
 * offsets in the text as it is, so several layers (the text the model wrote, the matches of a search, the passages of the
 * annotations) are computed on the same text and put into it in one go; marking one layer after the other would shift the
 * offsets of the next by the HTML of the first.
 * <p>
 * Ranges may overlap: the text is cut where a range starts or ends and every piece gets the elements of the ranges that cover
 * it, the first range given outermost. A range is wrapped line by line (an inline element would otherwise stay open over
 * paragraphs), white space at the edges of a line and the markdown that opens a line (indentation, headings, lists, quotes)
 * stay outside of the element.
 */
public final class TextMarkup {

    // markdown that has to stay at the start of its line to keep working: indentation, headings, lists, quotes
    private static final Pattern LINE_PREFIX = Pattern.compile("^\\s*(?:(?:#{1,6}|[-*+>]|\\d{1,9}[.)])\\s+)*");

    private TextMarkup() {
    }

    /**
     * @param start      first character of the range
     * @param end        end of the range (exclusive)
     * @param cssClass   the {@code class} of the element, may hold several classes
     * @param attributes more attributes of the element, written as they are in the tag (each with a space before it), or empty
     */
    public record Mark(int start, int end, String cssClass, String attributes) {

        public Mark(int start, int end, String cssClass) {
            this(start, end, cssClass, "");
        }

        private String open() {
            return "<mark class=\"" + cssClass + "\"" + attributes + ">";
        }
    }

    /**
     * @return the text with the ranges marked; the text itself when no range is in it
     */
    public static String apply(String text, List<Mark> marks) {
        if (text == null || text.isEmpty() || marks == null || marks.isEmpty()) {
            return text;
        }
        List<Mark> lines = new ArrayList<>();
        for (Mark mark : marks) {
            splitIntoLines(text, mark, lines);
        }
        if (lines.isEmpty()) {
            return text;
        }

        TreeSet<Integer> cuts = new TreeSet<>();
        cuts.add(0);
        cuts.add(text.length());
        for (Mark mark : lines) {
            cuts.add(mark.start());
            cuts.add(mark.end());
        }

        StringBuilder out = new StringBuilder(text.length() + 64 * lines.size());
        Integer from = null;
        for (int cut : cuts) {
            if (from != null) {
                StringBuilder open = new StringBuilder();
                int covering = 0;
                for (Mark mark : lines) {
                    if (mark.start() <= from && mark.end() >= cut) {
                        open.append(mark.open());
                        covering++;
                    }
                }
                out.append(open).append(text, from, cut).append("</mark>".repeat(covering));
            }
            from = cut;
        }
        return out.toString();
    }

    /**
     * The range cut into one range per line: white space at the edges of a line and the markdown that opens it are not in it, a
     * line with nothing else in it is left out.
     */
    private static void splitIntoLines(String text, Mark mark, List<Mark> out) {
        int start = Math.max(0, mark.start());
        int end = Math.min(text.length(), mark.end());
        int lineStart = start;
        while (lineStart < end) {
            int newline = text.indexOf('\n', lineStart);
            int lineEnd = newline < 0 || newline > end ? end : newline;
            int from = lineStart;
            if (lineStart == 0 || text.charAt(lineStart - 1) == '\n') {
                // the whole line is looked at: the markdown at its start is not part of the range, the range may start in it
                int lineBegin = lineStart;
                int lineFinish = newline < 0 ? text.length() : newline;
                Matcher prefix = LINE_PREFIX.matcher(text.substring(lineBegin, lineFinish));
                prefix.find();
                from = Math.max(lineStart, lineBegin + prefix.end());
            } else {
                while (from < lineEnd && Character.isWhitespace(text.charAt(from))) {
                    from++;
                }
            }
            int to = lineEnd;
            while (to > from && Character.isWhitespace(text.charAt(to - 1))) {
                to--;
            }
            if (from < to) {
                out.add(new Mark(from, to, mark.cssClass(), mark.attributes()));
            }
            lineStart = lineEnd + 1;
        }
    }

}
