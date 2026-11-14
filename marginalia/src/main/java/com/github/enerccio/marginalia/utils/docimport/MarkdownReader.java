package com.github.enerccio.marginalia.utils.docimport;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a Markdown file. The text is kept as it is written, only cut into blocks: a heading ({@code #} to {@code ######}, or
 * underlined with {@code ===} / {@code ---}), a fenced code block, or a paragraph (lines up to a blank line, a list or a
 * quote is one block too). A YAML header at the top ({@code ---} lines) is left out and gives the title; a single
 * {@code #} heading before the chapters ({@code ##}) is the title of the book, not a chapter.
 */
final class MarkdownReader {

    private static final Pattern ATX = Pattern.compile("^ {0,3}(#{1,6})(?:[ \\t]+(.*?))?(?:[ \\t]+#+)?[ \\t]*$");
    private static final Pattern FENCE = Pattern.compile("^ {0,3}(`{3,}|~{3,}).*$");
    private static final Pattern SETEXT = Pattern.compile("^ {0,3}(=+|-+)[ \\t]*$");
    private static final Pattern TITLE_KEY = Pattern.compile("^title:\\s*(.+?)\\s*$", Pattern.CASE_INSENSITIVE);

    private MarkdownReader() {
    }

    static TextDocument read(byte[] data) {
        return read(TextFiles.decode(data));
    }

    /**
     * @param text with the line breaks as {@code \n}
     */
    static TextDocument read(String text) {
        List<String> lines = new ArrayList<>(List.of(text.split("\n", -1)));
        String title = frontMatter(lines);
        List<TextBlock> blocks = new ArrayList<>();
        List<String> paragraph = new ArrayList<>();
        String fence = null;
        for (String line : lines) {
            if (fence != null) {
                paragraph.add(line);
                Matcher closing = FENCE.matcher(line);
                if (closing.matches() && closing.group(1).charAt(0) == fence.charAt(0) && closing.group(1).length() >= fence.length()
                        && line.strip().length() == closing.group(1).length()) {
                    fence = null;
                    flush(paragraph, blocks);
                }
                continue;
            }
            if (line.isBlank()) {
                flush(paragraph, blocks);
                continue;
            }
            Matcher open = FENCE.matcher(line);
            if (open.matches()) {
                flush(paragraph, blocks);
                fence = open.group(1);
                paragraph.add(line);
                continue;
            }
            Matcher atx = ATX.matcher(line);
            if (atx.matches()) {
                flush(paragraph, blocks);
                heading(atx.group(1).length(), atx.group(2), blocks);
                continue;
            }
            Matcher setext = SETEXT.matcher(line);
            if (setext.matches() && !paragraph.isEmpty()) {
                // the lines above are the heading, a thematic break is a --- under nothing
                heading(setext.group(1).charAt(0) == '=' ? 1 : 2, String.join(" ", paragraph.stream().map(String::strip).toList()), blocks);
                paragraph.clear();
                continue;
            }
            paragraph.add(line);
        }
        flush(paragraph, blocks);
        String lifted = liftLoneTitle(blocks);
        if (lifted != null) {
            title = lifted;
        }
        return new TextDocument(title, blocks);
    }

    /**
     * A lone level 1 heading in front of the deeper ones (the chapters) is the title of the book, not a chapter.
     *
     * @return its text, removed from the blocks; null when there is no such heading
     */
    static String liftLoneTitle(List<TextBlock> blocks) {
        long firstLevel = blocks.stream().filter(block -> block.level() == 1).count();
        boolean hasDeeper = blocks.stream().anyMatch(block -> block.level() > 1);
        if (firstLevel != 1 || !hasDeeper) {
            return null;
        }
        int index = 0;
        while (blocks.get(index).level() != 1) {
            index++;
        }
        List<TextBlock> before = blocks.subList(0, index);
        if (index == 0 || before.stream().noneMatch(TextBlock::isHeading) && before.stream().mapToInt(TextBlock::words).sum() < 50) {
            return blocks.remove(index).text();
        }
        return null;
    }

    private static void heading(int level, String text, List<TextBlock> blocks) {
        if (text != null && !text.isBlank()) {
            blocks.add(new TextBlock(level, text.strip()));
        }
    }

    private static void flush(List<String> paragraph, List<TextBlock> blocks) {
        if (paragraph.isEmpty()) {
            return;
        }
        String text = String.join("\n", paragraph).strip();
        paragraph.clear();
        if (!text.isEmpty()) {
            blocks.add(new TextBlock(0, text));
        }
    }

    // removes the YAML header from the lines, returns its title or null
    private static String frontMatter(List<String> lines) {
        if (lines.isEmpty() || !lines.getFirst().strip().equals("---")) {
            return null;
        }
        for (int end = 1; end < lines.size() && end < 100; end++) {
            String line = lines.get(end).strip();
            if (line.equals("---") || line.equals("...")) {
                String title = null;
                for (String header : lines.subList(1, end)) {
                    Matcher matcher = TITLE_KEY.matcher(header);
                    if (title == null && matcher.matches()) {
                        title = unquote(matcher.group(1));
                    }
                }
                lines.subList(0, end + 1).clear();
                return title == null || title.isBlank() ? null : title;
            }
            // a line that is not a key, a list item or an indented value is not a header: the file starts with a thematic break
            if (!line.isEmpty() && !lines.get(end).matches("^([\\w.-]+:.*|\\s+.*|-\\s.*|#.*)$")) {
                return null;
            }
        }
        return null;
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
