package com.github.enerccio.marginalia.extensions.statetracker.ui;

import com.github.enerccio.marginalia.extensions.statetracker.model.ClockMark;
import com.github.enerccio.marginalia.extensions.statetracker.model.StoryTime;
import com.github.enerccio.marginalia.extensions.statetracker.model.TimeOfDay;
import com.vaadin.flow.component.UI;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.*;

/** The story time as the user reads it, in the language of the session (the prompt has its own English words, {@link StoryTime}). */
public final class ClockText {

    private ClockText() {
    }

    /** "Day 14 after the awakening, evening, 20 April 1453", "about: Day 34 after the awakening". */
    public static String of(StoryTime time, String anchor) {
        List<String> parts = new ArrayList<>();
        parts.add(anchor == null || anchor.isBlank() ? T.localized(DAY_N).formatted(time.day())
                : T.localized(DAY_N_AFTER_S).formatted(time.day(), anchor.strip()));
        if (time.timeOfDay() != null) {
            parts.add(of(time.timeOfDay()));
        }
        if (time.date() != null) {
            parts.add(date(time.date()));
        }
        String text = String.join(", ", parts);
        return time.approximate() ? T.localized(ABOUT_S).formatted(text) : text;
    }

    public static String of(TimeOfDay timeOfDay) {
        return T.localized(switch (timeOfDay) {
            case DAWN -> DAWN;
            case MORNING -> MORNING;
            case NOON -> NOON;
            case AFTERNOON -> AFTERNOON;
            case EVENING -> EVENING;
            case NIGHT -> NIGHT;
        });
    }

    /** What a mark the model read says, for the review: "+3 day(s), evening", "Day 40", "20 April 1453", "the clock does not move". */
    public static String of(ClockMark mark) {
        String what = switch (mark.getKind()) {
            case ADVANCE -> mark.getDays() == null || mark.getDays() == 0 ? T.localized(THE_SAME_DAY)
                    : T.localized(PLUS_N_DAYS).formatted(mark.getDays());
            case DAY, SET -> T.localized(DAY_N).formatted(mark.getDay() == null ? 0 : mark.getDay());
            case DATE -> mark.getDate() == null ? "" : date(LocalDate.parse(mark.getDate()));
            case FLASHBACK -> T.localized(THE_CLOCK_DOES_NOT_MOVE);
        };
        if (mark.getTimeOfDay() != null) {
            what += ", " + of(mark.getTimeOfDay());
        }
        if (mark.isApproximate()) {
            what += " (" + T.localized(APPROXIMATE) + ")";
        }
        return what;
    }

    private static String date(LocalDate date) {
        UI ui = UI.getCurrent();
        Locale locale = ui == null ? Locale.ENGLISH : ui.getLocale();
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale).format(date);
    }
}
