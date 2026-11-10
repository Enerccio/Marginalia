package com.github.enerccio.marginalia.utils.docimport;

import java.util.List;
import java.util.TreeSet;

/**
 * What a {@link DocumentFormat} reads from a file: the blocks of text in order, and the title when the document has one (the
 * {@code Title} style of a word processor), which is not among the blocks.
 *
 * @param title may be null
 */
public record TextDocument(String title, List<TextBlock> blocks) {

    /**
     * @return the levels of the headings the document has, the shallowest first
     */
    public List<Integer> headingLevels() {
        TreeSet<Integer> levels = new TreeSet<>();
        for (TextBlock block : blocks) {
            if (block.isHeading()) {
                levels.add(block.level());
            }
        }
        return List.copyOf(levels);
    }

    public int words() {
        int words = 0;
        for (TextBlock block : blocks) {
            words += block.words();
        }
        return words;
    }
}
