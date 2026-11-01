package com.github.enerccio.marginalia.domain.components;

/**
 * How many parts there are and how many words they have.
 */
public record PartTotals(long parts, long words) {

    public static final PartTotals NONE = new PartTotals(0, 0);

    public PartTotals plus(PartTotals other) {
        return new PartTotals(parts + other.parts, words + other.words);
    }
}
