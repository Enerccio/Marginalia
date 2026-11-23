package com.github.enerccio.marginalia.extensions.statetracker.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * The time of the story at some point: the day counted from the anchor event (day 1 is its day), the time of day when known, the
 * calendar date when the story has one, and whether the day is an estimate (a vague span since the last stated day or date).
 */
public record StoryTime(int day, TimeOfDay timeOfDay, LocalDate date, boolean approximate) {

    private static final DateTimeFormatter ENGLISH_DATE = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

    /** "Day 14 after the awakening, evening", "About day 34 after the awakening": the model-facing words, English. */
    public String words(String anchor) {
        StringBuilder text = new StringBuilder(approximate ? "About day " : "Day ").append(day);
        if (anchor != null && !anchor.isBlank()) {
            text.append(" after ").append(anchor.strip());
        }
        if (timeOfDay != null) {
            text.append(", ").append(timeOfDay.word());
        }
        return text.toString();
    }

    /** The {@code [Clock]} section of the extraction request (the wording that was tested, see TODO.md). */
    public String forRequest(String anchor) {
        return words(anchor) + "." + (date == null ? " No calendar date." : " Calendar date: " + date + ".");
    }

    /** The line of the block: "Story time: day 14 after the awakening, evening (20 April 1453)." */
    public String forBlock(String anchor) {
        String words = words(anchor);
        return "Story time: " + Character.toLowerCase(words.charAt(0)) + words.substring(1)
                + (date == null ? "" : " (" + ENGLISH_DATE.format(date) + ")") + ".";
    }
}
