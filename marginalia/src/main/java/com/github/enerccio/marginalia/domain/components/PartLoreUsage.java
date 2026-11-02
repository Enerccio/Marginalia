package com.github.enerccio.marginalia.domain.components;

import java.util.List;

/**
 * The lorebook entries the prompt of one part used.
 *
 * @param recorded true when the generation of the part recorded them; false for a part typed by hand, made before the
 *                 records existed, or made for a book without a lorebook (then there are no entries)
 * @param entries  the entries, in the order the generation recorded them
 */
public record PartLoreUsage(boolean recorded, List<Used> entries) {

    /**
     * An entry the prompt used.
     *
     * @param entry    the uuid of the entry
     * @param name     its name, null when it cannot be found any more (deleted)
     * @param lorebook the name of its lorebook, null when unknown
     * @param tokens   the tokens of its text in the prompt
     * @param depth    0 when the prompt activated it, n when the content of the entries of the pass n - 1 did
     */
    public record Used(String entry, String name, String lorebook, long tokens, int depth) {
    }

    /**
     * @return the tokens of all the entries
     */
    public long totalTokens() {
        long sum = 0;
        for (Used used : entries) {
            sum += used.tokens();
        }
        return sum;
    }
}
