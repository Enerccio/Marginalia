package com.github.enerccio.marginalia.extensions.arcoutline.service;

import com.github.enerccio.marginalia.extensions.arcoutline.model.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The chapter headings written into the text of the story from the parts the arcs and chapters are linked to (pure functions,
 * {@link ArcOutlineService#writeHeadings} stores them). A heading is a Markdown ATX heading ({@code # Chapter 1: Title}) at the start
 * of the part; the chapters are a level below the arcs when the arcs get headings too.
 */
public final class ChapterHeadings {

    /** A Markdown ATX heading line: up to three spaces, one to six {@code #} and a space or the end of the line. */
    private static final Pattern HEADING = Pattern.compile("^ {0,3}#{1,6}(?:[ \\t].*)?$");

    private ChapterHeadings() {
    }

    /**
     * A heading to write.
     *
     * @param part  the place of the part in the active branch, from 1
     * @param level 1 for {@code #}, 2 for {@code ##}
     * @param text  the text after the hashes
     * @param node  the arc or the chapter it is for
     */
    public record Heading(int part, int level, String text, OutlineNode node) {

        /** The Markdown line. */
        public String line() {
            return "#".repeat(level) + " " + text;
        }
    }

    /**
     * @param headings the headings of the linked arcs and chapters, in the order of the outline
     * @param unlinked how many chapters (and arcs, when they get headings) that are written or being written (not planned) have no
     *                 part to go to
     */
    public record Plan(List<Heading> headings, int unlinked) {
    }

    /**
     * The headings of the outline. Chapters are numbered by their place in the outline (in their arc with
     * {@link HeadingOptions#isRestartInArc()}), whether they are linked or not, so the numbers follow the plan of the book. An arc that is
     * not linked itself goes to the part of its first linked chapter.
     *
     * @param arcPrefix     the word before the number of an arc
     * @param chapterPrefix the word before the number of a chapter
     */
    public static Plan plan(ArcOutline outline, HeadingOptions options, String arcPrefix, String chapterPrefix) {
        List<Heading> headings = new ArrayList<>();
        int unlinked = 0;
        int arcNumber = 0;
        int chapterNumber = 0;
        int chapterLevel = options.isIncludeArcs() ? 2 : 1;
        for (Arc arc : outline.arcs()) {
            arcNumber++;
            if (options.isRestartInArc()) {
                chapterNumber = 0;
            }
            if (options.isIncludeArcs()) {
                Integer part = arc.getPart();
                if (part == null) {
                    part = arc.getChapters().stream().map(OutlineNode::getPart).filter(java.util.Objects::nonNull).findFirst().orElse(null);
                }
                if (part == null) {
                    unlinked += arc.getState() == OutlineState.PLANNED ? 0 : 1;
                } else {
                    headings.add(new Heading(part, 1, title(arcPrefix, arcNumber, options, arc.getTitle()), arc));
                }
            }
            for (Chapter chapter : arc.getChapters()) {
                chapterNumber++;
                if (chapter.getPart() == null) {
                    unlinked += chapter.getState() == OutlineState.PLANNED ? 0 : 1;
                } else {
                    headings.add(new Heading(chapter.getPart(), chapterLevel,
                            title(chapterPrefix, chapterNumber, options, chapter.getTitle()), chapter));
                }
            }
        }
        return new Plan(headings, unlinked);
    }

    /**
     * The text of a heading: {@code Chapter 4: Title}, {@code 4. Title} without a prefix, {@code Chapter 4} without the title,
     * {@code Chapter: Title} without numbers.
     */
    public static String title(String prefix, int number, HeadingOptions options, String nodeTitle) {
        String label = (StringUtils.defaultString(prefix).strip() + " " + numeral(number, options.getNumbering())).strip();
        String name = options.isWithTitles() ? StringUtils.defaultString(nodeTitle).strip() : "";
        if (label.isEmpty()) {
            return name.isEmpty() ? numeral(number, HeadingOptions.Numbering.ARABIC) : name;
        }
        if (name.isEmpty()) {
            return label;
        }
        boolean bareNumber = StringUtils.isBlank(prefix) && options.getNumbering() != HeadingOptions.Numbering.NONE;
        return label + (bareNumber ? ". " : ": ") + name;
    }

    /** The number written in the way asked for, empty for {@link HeadingOptions.Numbering#NONE}. */
    public static String numeral(int number, HeadingOptions.Numbering numbering) {
        return switch (numbering) {
            case ARABIC -> Integer.toString(number);
            case ROMAN -> roman(number);
            case NONE -> "";
        };
    }

    private static final int[] ROMAN_VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] ROMAN_DIGITS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    /** The Roman numeral of the number; one that has none (below 1, above 3999) stays Arabic. */
    public static String roman(int number) {
        if (number < 1 || number > 3999) {
            return Integer.toString(number);
        }
        StringBuilder text = new StringBuilder();
        int rest = number;
        for (int i = 0; i < ROMAN_VALUES.length; i++) {
            while (rest >= ROMAN_VALUES[i]) {
                text.append(ROMAN_DIGITS[i]);
                rest -= ROMAN_VALUES[i];
            }
        }
        return text.toString();
    }

    public static boolean isHeading(String line) {
        return HEADING.matcher(StringUtils.removeEnd(line, "\r")).matches();
    }

    /**
     * The text without the headings at its start (and the blank lines among and after them): where a chapter heading of an earlier
     * run is.
     */
    public static String stripLeadingHeadings(String text) {
        if (text == null) {
            return "";
        }
        int position = 0;
        while (position < text.length()) {
            int end = text.indexOf('\n', position);
            String line = end < 0 ? text.substring(position) : text.substring(position, end);
            if (!StringUtils.isBlank(line) && !isHeading(line)) {
                return text.substring(position);
            }
            if (end < 0) {
                return "";
            }
            position = end + 1;
        }
        return "";
    }

    /**
     * The text with the headings at its start replaced by {@code lines}, a blank line between them and after them.
     */
    public static String withHeadings(String text, List<String> lines) {
        String body = stripLeadingHeadings(text);
        String headings = String.join("\n\n", lines);
        return body.isEmpty() ? headings : headings + "\n\n" + body;
    }

    /**
     * The text without any heading line, wherever it is; the blank line that went with a removed heading goes too, so no gap is left.
     *
     * @return the text itself when it has no heading
     */
    public static String wipe(String text) {
        if (text == null || text.indexOf('#') < 0) {
            return text;
        }
        String[] lines = text.split("\n", -1);
        List<String> kept = new ArrayList<>();
        boolean removed = false;
        boolean skipBlank = false;
        for (String line : lines) {
            if (isHeading(line)) {
                removed = true;
                skipBlank = true;
                continue;
            }
            if (skipBlank && StringUtils.isBlank(line) && (kept.isEmpty() || StringUtils.isBlank(kept.getLast()))) {
                continue;
            }
            skipBlank = false;
            kept.add(line);
        }
        if (!removed) {
            return text;
        }
        // a heading that ended the text leaves the blank line before it
        while (!kept.isEmpty() && kept.getLast().isBlank() && !text.endsWith("\n")) {
            kept.removeLast();
        }
        return String.join("\n", kept);
    }
}
