package com.github.enerccio.marginalia.utils.docimport;

import com.github.enerccio.marginalia.utils.WordCounter;

/**
 * One paragraph or heading of an imported document, the text already in Markdown (emphasis as {@code *} and {@code **}, the
 * characters that would be read as Markdown escaped).
 *
 * @param level 0 for a paragraph, 1 to 6 for a heading of that level
 * @param text  never blank
 */
public record TextBlock(int level, String text) {

    public static final int MAX_LEVEL = 6;

    public boolean isHeading() {
        return level > 0;
    }

    /**
     * @return the block with a heading {@code by} levels deeper (the deepest is {@link #MAX_LEVEL}), a paragraph as it is
     */
    public TextBlock shifted(int by) {
        return level == 0 || by == 0 ? this : new TextBlock(Math.min(MAX_LEVEL, level + by), text);
    }

    /**
     * @return the number of words (see {@link WordCounter}: a run of letters, every Chinese or Japanese character)
     */
    public int words() {
        return TextBlock.words(text);
    }

    static int words(String text) {
        return WordCounter.count(text);
    }
}
