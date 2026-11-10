package com.github.enerccio.marginalia.utils.docimport;

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
     * @return the number of words (runs of non-blank characters)
     */
    public int words() {
        return TextBlock.words(text);
    }

    static int words(String text) {
        int words = 0;
        boolean inWord = false;
        for (int i = 0; i < text.length(); i++) {
            boolean blank = Character.isWhitespace(text.charAt(i));
            if (!blank && !inWord) {
                words++;
            }
            inWord = !blank;
        }
        return words;
    }
}
