package com.github.enerccio.marginalia.domain.components;

/**
 * How much a book grew (or shrank) between two sizes: the end of a range minus the size before it, in words and tokens, of the
 * whole book and of the current branch. The numbers can be negative. {@code known} is false when there is nothing to show because the
 * book was first seen in the range and only its size then is known ({@code ManuscriptSize#isBaseline}).
 */
public record SizeGrowth(long bookWords, long bookTokens, long branchWords, long branchTokens, boolean known) {

    public static final SizeGrowth ZERO = new SizeGrowth(0, 0, 0, 0, true);
    public static final SizeGrowth UNKNOWN = new SizeGrowth(0, 0, 0, 0, false);

}
