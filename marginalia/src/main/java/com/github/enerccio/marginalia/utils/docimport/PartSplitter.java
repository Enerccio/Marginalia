package com.github.enerccio.marginalia.utils.docimport;

import java.util.ArrayList;
import java.util.List;

/**
 * Cuts the blocks of a {@link TextDocument} into the parts of a book. A heading of the chapter level or above starts a
 * chapter, the chapter starts with its heading ({@code # Title}, the rule the exporters and the Chapter Marker use) and a
 * part never holds the end of one chapter and the beginning of the next. A chapter is cut between paragraphs into parts of
 * about {@code wordsPerPart} words, at the paragraph boundary nearest to the target. A chapter is not cut for nothing: what
 * would be left over at the end of a chapter, when it is less than half of the target, stays with the part before it (so a
 * part has up to one and a half times the target), and a chapter shorter than that is one part.
 */
public final class PartSplitter {

    public static final int MIN_WORDS_PER_PART = 100;
    public static final int DEFAULT_WORDS_PER_PART = 1000;
    public static final int MAX_WORDS_PER_PART = 50_000;

    /**
     * @param parts    the Markdown of the parts in order
     * @param chapters how many chapters the document has (a document without headings is one)
     * @param words    how many words there are
     */
    public record Result(List<String> parts, int chapters, int words) {
    }

    private PartSplitter() {
    }

    /**
     * @param chapterLevel the headings of this level and the ones above it start a chapter, the deeper ones stay in the text
     *                     (as {@code ##} for the level below the chapter level, and so on)
     * @throws IllegalArgumentException when the target is out of {@link #MIN_WORDS_PER_PART} to {@link #MAX_WORDS_PER_PART}
     */
    public static Result split(TextDocument document, int chapterLevel, int wordsPerPart) {
        if (wordsPerPart < MIN_WORDS_PER_PART || wordsPerPart > MAX_WORDS_PER_PART) {
            throw new IllegalArgumentException("Words per part out of range: " + wordsPerPart);
        }
        List<Chapter> chapters = chapters(document, chapterLevel);
        List<String> parts = new ArrayList<>();
        int words = 0;
        for (Chapter chapter : chapters) {
            words += chapter.words();
            cut(chapter, wordsPerPart, parts);
        }
        return new Result(parts, chapters.size(), words);
    }

    private static final class Chapter {
        final List<String> texts = new ArrayList<>();
        final List<Integer> words = new ArrayList<>();

        void add(String text, int count) {
            texts.add(text);
            words.add(count);
        }

        int words() {
            return words.stream().mapToInt(Integer::intValue).sum();
        }
    }

    private static List<Chapter> chapters(TextDocument document, int chapterLevel) {
        List<Chapter> chapters = new ArrayList<>();
        Chapter current = null;
        for (TextBlock block : document.blocks()) {
            boolean starts = block.isHeading() && block.level() <= chapterLevel;
            if (current == null || starts) {
                current = new Chapter();
                chapters.add(current);
            }
            if (block.isHeading()) {
                // the heading of the chapter is #, a deeper one is one level below the chapter's for each level it is deeper
                int depth = starts ? 1 : block.level() - chapterLevel + 1;
                current.add("#".repeat(Math.min(depth, TextBlock.MAX_LEVEL)) + " " + block.text(), block.words());
            } else {
                current.add(block.text(), block.words());
            }
        }
        return chapters;
    }

    private static void cut(Chapter chapter, int target, List<String> parts) {
        int count = chapter.texts.size();
        int remaining = chapter.words();
        int from = 0;
        while (from < count) {
            int to;
            // the rest of the chapter is one part when cutting it would leave less than half of the target
            if (remaining * 2 <= target * 3) {
                to = count;
            } else {
                int taken = 0;
                to = from;
                while (to < count) {
                    int next = chapter.words.get(to);
                    boolean overshoots = taken + next > target;
                    // a paragraph that carries the part over the target stays out when the part is nearer the target without it
                    if (taken > 0 && overshoots && taken + next - target > target - taken) {
                        break;
                    }
                    taken += next;
                    to++;
                    if (taken >= target) {
                        break;
                    }
                }
                if ((remaining - taken) * 2 < target) {
                    to = count;
                    taken = remaining;
                }
                remaining -= taken;
            }
            parts.add(String.join("\n\n", chapter.texts.subList(from, to)));
            if (to == count) {
                break;
            }
            from = to;
        }
    }
}
