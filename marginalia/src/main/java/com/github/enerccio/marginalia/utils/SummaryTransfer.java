package com.github.enerccio.marginalia.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The text file the summaries of a branch are exported to and imported from. It is meant to be edited in a text editor:
 * a line {@code <!-- summary part="uuid" order="3" -->} starts a summary, the text after it lasts to the next such line.
 * <pre>
 * &lt;!-- marginalia-summaries 1 --&gt;
 *
 * &lt;!-- summary part="3f2c..." order="3" --&gt;
 * The text of the summary, any number of lines.
 *
 * &lt;!-- summary part="9a11..." order="7" meta="true" --&gt;
 * ...
 * </pre>
 * {@code part} is the uuid of the part the summary is on and {@code order} its position in the branch (the root is 1); an
 * import finds the summary by the first, or by the second when asked to. Other attributes are ignored.
 */
public final class SummaryTransfer {

    /**
     * The first line of the file.
     */
    public static final String SIGNATURE = "<!-- marginalia-summaries 1 -->";

    private static final Pattern HEADER = Pattern.compile("^\\s*<!--\\s*summary\\s+(.*?)\\s*-->\\s*$");
    private static final Pattern ATTRIBUTE = Pattern.compile("(\\w+)=\"([^\"]*)\"");

    private SummaryTransfer() {
    }

    /**
     * One summary of the file.
     *
     * @param part  uuid of the part, null when the header has none
     * @param order position of the part in the branch, 0 when the header has none
     * @param text  the text without the blank lines around it
     */
    public record Entry(String part, int order, String text) {
    }

    /**
     * One summary to write.
     *
     * @param part  uuid of the part the summary is on
     * @param order position of the part in the branch
     * @param meta  whether the summary is a meta summary (the file only says so, an import does not read it)
     */
    public record Item(String part, int order, boolean meta, String text) {
    }

    public static String write(List<Item> items) {
        StringBuilder out = new StringBuilder(SIGNATURE).append('\n');
        for (Item item : items) {
            out.append('\n').append("<!-- summary part=\"").append(item.part()).append("\" order=\"").append(item.order()).append('"');
            if (item.meta()) {
                out.append(" meta=\"true\"");
            }
            out.append(" -->\n");
            out.append(normalize(item.text() == null ? "" : item.text())).append('\n');
        }
        return out.toString();
    }

    /**
     * @throws IllegalArgumentException when the text has no summary header, so it is not a file of this format
     */
    public static List<Entry> read(String text) {
        List<Entry> entries = new ArrayList<>();
        String part = null;
        int order = 0;
        StringBuilder current = null;
        for (String line : text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1)) {
            Matcher header = HEADER.matcher(line);
            if (header.matches()) {
                if (current != null) {
                    entries.add(new Entry(part, order, normalize(current.toString())));
                }
                part = null;
                order = 0;
                Matcher attribute = ATTRIBUTE.matcher(header.group(1));
                while (attribute.find()) {
                    if (attribute.group(1).equals("part") && !attribute.group(2).isBlank()) {
                        part = attribute.group(2).trim();
                    } else if (attribute.group(1).equals("order")) {
                        try {
                            order = Integer.parseInt(attribute.group(2).trim());
                        } catch (NumberFormatException ignored) {
                            order = 0;
                        }
                    }
                }
                current = new StringBuilder();
            } else if (current != null) {
                current.append(line).append('\n');
            }
        }
        if (current != null) {
            entries.add(new Entry(part, order, normalize(current.toString())));
        }
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("not a summaries file");
        }
        return entries;
    }

    /**
     * The text as the file keeps it: line ends are {@code \n}, the blank lines around it are removed (not the white space
     * inside the first and the last line). A summary is unchanged by an import when its normalized text is the same.
     */
    public static String normalize(String text) {
        text = text.replace("\r\n", "\n");
        int start = 0;
        int end = text.length();
        // lines of white space only count as blank, the line the text starts on keeps its indentation
        int lineStart = 0;
        for (int i = 0; i < end; i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                lineStart = i + 1;
            } else if (!Character.isWhitespace(c)) {
                break;
            }
        }
        start = lineStart;
        int lineEnd = end;
        for (int i = end - 1; i >= start; i--) {
            char c = text.charAt(i);
            if (c == '\n') {
                lineEnd = i;
            } else if (!Character.isWhitespace(c)) {
                break;
            }
        }
        end = Math.max(start, lineEnd);
        return text.substring(start, end);
    }
}
