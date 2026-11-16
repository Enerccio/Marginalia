package com.github.enerccio.marginalia.extensions.statetracker.model;

import java.util.Locale;

/** The time of day of the story clock, in the order of a day. */
public enum TimeOfDay {
    DAWN, MORNING, NOON, AFTERNOON, EVENING, NIGHT;

    /** The word the model and the prompt use. */
    public String word() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** The time of day a word of the model names (lenient: dusk, sunset, midnight...), {@code null} for none or an unknown word. */
    public static TimeOfDay parse(String text) {
        if (text == null) {
            return null;
        }
        String word = text.strip().toLowerCase(Locale.ROOT).replace('-', ' ');
        return switch (word) {
            case "dawn", "sunrise", "daybreak", "early morning" -> DAWN;
            case "morning", "late morning" -> MORNING;
            case "noon", "midday" -> NOON;
            case "afternoon", "late afternoon" -> AFTERNOON;
            case "evening", "dusk", "sunset", "twilight", "nightfall" -> EVENING;
            case "night", "midnight", "late night" -> NIGHT;
            default -> null;
        };
    }
}
