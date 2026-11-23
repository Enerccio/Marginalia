package com.github.enerccio.marginalia.extensions.statetracker.extract;

import com.github.enerccio.marginalia.extensions.statetracker.model.ClockMark;

/**
 * What the model read about the time of the story in one part of a run, with the choice of the user in the review (apply or not).
 * Applied by storing its {@link ClockMark} on the part (a fixed point the user set there stays).
 */
public class ClockChange {

    private final ClockMark mark;
    private final int messageNumber;
    private final boolean evidenceFound;
    private boolean apply = true;
    private int partNumber;

    public ClockChange(ClockMark mark, int messageNumber, boolean evidenceFound) {
        this.mark = mark;
        this.messageNumber = messageNumber;
        this.evidenceFound = evidenceFound;
    }

    public ClockMark getMark() {
        return mark;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public boolean isEvidenceFound() {
        return evidenceFound;
    }

    public boolean isApply() {
        return apply;
    }

    public void setApply(boolean apply) {
        this.apply = apply;
    }

    /** The position of the part on the active branch (the first part is 1), else the message number. */
    public int getPartNumber() {
        return partNumber > 0 ? partNumber : messageNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }
}
