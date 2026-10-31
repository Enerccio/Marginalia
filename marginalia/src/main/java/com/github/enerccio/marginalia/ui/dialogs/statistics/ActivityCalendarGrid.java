package com.github.enerccio.marginalia.ui.dialogs.statistics;

import com.github.enerccio.marginalia.domain.collections.ActivityMetric;
import com.github.enerccio.marginalia.domain.components.WritingActivity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * The layout of the activity calendar, without a component: the weeks as columns of seven days, the oldest first, the last one
 * ending with today, and the shade of every day. The first column starts on the first day of the week, so it is full.
 */
public final class ActivityCalendarGrid {

    private ActivityCalendarGrid() {
    }

    /**
     * @param level the shade, 0 (not worked on) to {@code WritingActivity.LEVELS - 1}
     */
    public record Cell(LocalDate day, int level, boolean today) {
    }

    /**
     * One week; {@code month} is the date whose month the column is labelled with (the first of the month falls in this week), null
     * when it is not labelled.
     */
    public record Week(List<Cell> days, LocalDate month) {
    }

    /**
     * @param weeks      how many weeks back the calendar reaches (the first column may start up to six days earlier)
     * @param firstDay   the day a week starts on
     */
    public static List<Week> weeks(WritingActivity activity, ActivityMetric metric, int weeks, DayOfWeek firstDay) {
        LocalDate today = activity.today();
        LocalDate start = start(activity, weeks, firstDay);
        long max = activity.max(metric, start, today);
        List<Week> columns = new ArrayList<>();
        for (LocalDate first = start; !first.isAfter(today); first = first.plusDays(7)) {
            List<Cell> days = new ArrayList<>();
            LocalDate month = null;
            for (LocalDate day = first; day.isBefore(first.plusDays(7)) && !day.isAfter(today); day = day.plusDays(1)) {
                days.add(new Cell(day, activity.level(day, metric, max), day.equals(today)));
                if (day.getDayOfMonth() == 1) {
                    month = day;
                }
            }
            columns.add(new Week(days, month));
        }
        return columns;
    }

    /**
     * The first day of the calendar.
     */
    public static LocalDate start(WritingActivity activity, int weeks, DayOfWeek firstDay) {
        return activity.today().minusDays(7L * (Math.max(1, weeks) - 1)).with(TemporalAdjusters.previousOrSame(firstDay));
    }

}
