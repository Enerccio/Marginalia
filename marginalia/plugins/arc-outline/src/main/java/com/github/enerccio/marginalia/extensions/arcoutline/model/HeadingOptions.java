package com.github.enerccio.marginalia.extensions.arcoutline.model;

import com.github.enerccio.marginalia.extensions.arcoutline.ArcOutlineLocalization;

/**
 * How the chapter headings are written into the text of the story (see {@code ChapterHeadings}), stored in the user settings
 * as the author left them. A {@code null} prefix means the default one of the language of the user.
 */
public class HeadingOptions {

    /**
     * How the arcs and chapters are numbered in their headings.
     */
    public enum Numbering {
        ARABIC(ArcOutlineLocalization.ARABIC_NUMBERS),
        ROMAN(ArcOutlineLocalization.ROMAN_NUMBERS),
        NONE(ArcOutlineLocalization.NO_NUMBERS);

        private final String label;

        Numbering(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /** The arcs get a heading too ({@code #}) and the chapters one level below it ({@code ##}). */
    private boolean includeArcs;
    /** The word before the number of an arc, like "Part". */
    private String arcPrefix;
    /** The word before the number of a chapter, like "Chapter". */
    private String chapterPrefix;
    private Numbering numbering = Numbering.ARABIC;
    /** The chapters are numbered from 1 in every arc instead of through the whole book. */
    private boolean restartInArc;
    /** The title of the arc or chapter follows the number. */
    private boolean withTitles = true;
    /** Every heading of the story is removed before the new ones are written. */
    private boolean wipeFirst;
    /** The summaries the change makes outdated are marked valid again: a heading does not change what happens. */
    private boolean revalidateSummaries = true;

    public boolean isIncludeArcs() {
        return includeArcs;
    }

    public void setIncludeArcs(boolean includeArcs) {
        this.includeArcs = includeArcs;
    }

    public String getArcPrefix() {
        return arcPrefix;
    }

    public void setArcPrefix(String arcPrefix) {
        this.arcPrefix = arcPrefix;
    }

    public String getChapterPrefix() {
        return chapterPrefix;
    }

    public void setChapterPrefix(String chapterPrefix) {
        this.chapterPrefix = chapterPrefix;
    }

    public Numbering getNumbering() {
        return numbering == null ? Numbering.ARABIC : numbering;
    }

    public void setNumbering(Numbering numbering) {
        this.numbering = numbering;
    }

    public boolean isRestartInArc() {
        return restartInArc;
    }

    public void setRestartInArc(boolean restartInArc) {
        this.restartInArc = restartInArc;
    }

    public boolean isWithTitles() {
        return withTitles;
    }

    public void setWithTitles(boolean withTitles) {
        this.withTitles = withTitles;
    }

    public boolean isWipeFirst() {
        return wipeFirst;
    }

    public void setWipeFirst(boolean wipeFirst) {
        this.wipeFirst = wipeFirst;
    }

    public boolean isRevalidateSummaries() {
        return revalidateSummaries;
    }

    public void setRevalidateSummaries(boolean revalidateSummaries) {
        this.revalidateSummaries = revalidateSummaries;
    }
}
