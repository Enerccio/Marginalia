package com.github.enerccio.marginalia.utils.docimport;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a plain text file. Paragraphs are separated by blank lines (the lines of a paragraph are joined, the text of a
 * mail or an old e-book is wrapped at a column); a file without blank lines has a paragraph on each line. There is no markup
 * to tell a heading, so a short line of its own that is a chapter title in words ({@code Chapter 12}, {@code Part IV},
 * {@code Prologue}, with or without a name after it) is taken for a chapter heading, and nothing else is.
 */
final class PlainTextReader {

    private static final int MAX_HEADING_LENGTH = 80;

    private static final String NUMBER = "[0-9]+|[ivxlcdm]+|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen"
            + "|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|first|second|third|last|final";
    private static final Pattern NUMBERED = Pattern.compile("^(?:chapter|part|book)\\s+(?:" + NUMBER + ")\\b(.*)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAMED = Pattern.compile("^(?:prologue|epilogue|interlude|preface|foreword|afterword)\\b(.*)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SEPARATORS = Pattern.compile("^[\\s.:\\u2014\\u2013-]*");

    private PlainTextReader() {
    }

    static TextDocument read(byte[] data) {
        String[] lines = TextFiles.decode(data).split("\n", -1);
        boolean paragraphsByBlankLines = false;
        boolean seenText = false;
        boolean seenBlankAfterText = false;
        for (String line : lines) {
            if (line.isBlank()) {
                seenBlankAfterText |= seenText;
            } else if (seenBlankAfterText) {
                paragraphsByBlankLines = true;
                break;
            } else {
                seenText = true;
            }
        }
        List<TextBlock> blocks = new ArrayList<>();
        List<String> paragraph = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank()) {
                flush(paragraph, blocks);
                continue;
            }
            paragraph.add(line.strip());
            if (!paragraphsByBlankLines) {
                flush(paragraph, blocks);
            }
        }
        flush(paragraph, blocks);
        return new TextDocument(null, blocks);
    }

    private static void flush(List<String> paragraph, List<TextBlock> blocks) {
        if (paragraph.isEmpty()) {
            return;
        }
        String joined = String.join(" ", paragraph);
        paragraph.clear();
        RichText text = new RichText();
        text.add(joined, false, false);
        if (isChapterTitle(joined)) {
            blocks.add(new TextBlock(1, text.plain()));
        } else {
            blocks.add(new TextBlock(0, text.markdown()));
        }
    }

    // the title is a line of its own: a paragraph of several lines was joined, so it is longer than a title
    static boolean isChapterTitle(String line) {
        String text = line.strip();
        if (text.length() > MAX_HEADING_LENGTH) {
            return false;
        }
        Matcher matcher = NUMBERED.matcher(text);
        if (!matcher.matches()) {
            matcher = NAMED.matcher(text);
            if (!matcher.matches()) {
                return false;
            }
        }
        // what follows the number is the name of the chapter, a sentence is not a title ("Part of the plan was lost.")
        String rest = SEPARATORS.matcher(matcher.group(1)).replaceFirst("").strip();
        return rest.isEmpty() || !rest.matches(".*[.!?,;]$");
    }
}
