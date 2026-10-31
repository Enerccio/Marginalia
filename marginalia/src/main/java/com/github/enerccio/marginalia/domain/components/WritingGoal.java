package com.github.enerccio.marginalia.domain.components;

/**
 * The daily goal of the author: the words they write and/or the minutes they spend editing, over all the books. A goal that is
 * zero is not set.
 */
public record WritingGoal(int words, int minutes) {

    public static final WritingGoal NONE = new WritingGoal(0, 0);

    public WritingGoal {
        words = Math.max(0, words);
        minutes = Math.max(0, minutes);
    }

    public boolean isSet() {
        return words > 0 || minutes > 0;
    }

    public long minutesInMillis() {
        return minutes * 60_000L;
    }

    /**
     * Whether a day reaches every goal that is set; false when none is set. The time of a day that has none recorded does not
     * count against the goal of the words alone, but a goal of minutes cannot be reached on it.
     */
    public boolean reachedBy(WritingActivity.Day day) {
        if (!isSet()) {
            return false;
        }
        return (words == 0 || day.wordsWritten() >= words) && (minutes == 0 || day.editMillis() >= minutesInMillis());
    }

}
