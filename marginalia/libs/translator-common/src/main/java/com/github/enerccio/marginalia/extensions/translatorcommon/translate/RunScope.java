package com.github.enerccio.marginalia.extensions.translatorcommon.translate;

import com.github.enerccio.marginalia.domain.components.ModelText;

/**
 * What a translation of a part may touch. A run for {@link #ALL} translates every speech of the part again from its English. A run that is
 * {@code missingOnly} leaves every speech that is translated already (of any language) as it is and writes only the speeches the model finds that
 * are still in English; it can be limited to a window, a range the author selected, so a part the model left some speeches of is finished
 * without translating each one by hand.
 *
 * @param missingOnly whether the translated speeches are kept
 * @param storedStart first character of the window in the stored text of the part, -1 for no window
 * @param storedEnd   end of the window (exclusive)
 */
public record RunScope(boolean missingOnly, int storedStart, int storedEnd) {

    /** Every speech of the part, translated again. */
    public static final RunScope ALL = new RunScope(false, -1, -1);

    /** The speeches of the whole part that are not translated. */
    public static final RunScope MISSING = new RunScope(true, -1, -1);

    /**
     * The speeches that are not translated in the range of the stored text of the part.
     */
    public static RunScope missingIn(int storedStart, int storedEnd) {
        return new RunScope(true, storedStart, storedEnd);
    }

    public boolean hasWindow() {
        return storedStart >= 0 && storedEnd > storedStart;
    }

    /**
     * The window in the text the model reads (a window that touches a translated speech takes all of it), null when there is none.
     *
     * @param read the part as the model reads it
     */
    public int[] window(ModelText read) {
        return hasWindow() ? read.modelRange(storedStart, storedEnd) : null;
    }

    /**
     * Whether the model is shown the part as it is stored, with its translated speeches in it, and not as it reads it (the English of every
     * speech): a run that writes only the missing speeches leaves the translated ones as they are, so they would only cost tokens to translate again.
     *
     * @param read the part as the model reads it
     */
    public boolean showsStored(ModelText read) {
        return missingOnly && read.changed();
    }

    /**
     * The text that goes to the model in the request: {@link ModelText#stored()} when {@link #showsStored}, else {@link ModelText#text()}. The
     * quotes of the answer are looked up in it and their ranges are in it; {@link #inModel} turns them into the ranges of {@link ModelText#text()}.
     */
    public String shown(ModelText read) {
        return showsStored(read) ? read.stored() : read.text();
    }

    /**
     * A range of {@link #shown} as a range of the text the model reads ({@link ModelText#text()}), which the plan of the write and the window are
     * in. A range that touches a translated speech grows to all of it, so it overlaps the speech and is dropped like any answer for it.
     */
    public int[] inModel(ModelText read, int start, int end) {
        return showsStored(read) ? read.modelRange(start, end) : new int[]{start, end};
    }

    /**
     * Whether a speech of the text the model reads at {@code [start, end)} is one this run may write: it touches the window, or there is none.
     */
    public static boolean admits(int[] window, int start, int end) {
        return window == null || start < window[1] && end > window[0];
    }
}
