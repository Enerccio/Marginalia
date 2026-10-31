package com.github.enerccio.marginalia.domain.components;

import com.github.enerccio.marginalia.domain.collections.ActivityMetric;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * The days the author worked, for the activity calendar, the streaks and the goal: read from the counts and the editing time of
 * the author's side, nothing of its own is stored. A day is <b>worked on</b> when it has words written, an edit, a part created
 * or time spent editing; the agents' side, the words a model generated for a part that was not created and opening a book do not
 * make a day. The rule is {@link #worked}, one place to change.
 *
 * @param today      the last day of the calendar
 * @param days       the days that were worked on, by day (a day without an entry was not worked on)
 * @param goal       the goal of the author
 * @param timeSince  the first day the time is recorded from, null when every day has it
 */
public record WritingActivity(LocalDate today, SortedMap<LocalDate, Day> days, WritingGoal goal, LocalDate timeSince) {

    public WritingActivity {
        days = Collections.unmodifiableSortedMap(new TreeMap<>(days));
        goal = goal == null ? WritingGoal.NONE : goal;
    }

    /**
     * One day that was worked on: the words the author wrote, the time they spent editing, the last time something was done.
     *
     * @param lastActivity the end of the last stretch of editing or the last change of a count, null when not known
     * @param reconstructed the counts of the day were rebuilt from the history of the parts
     */
    public record Day(LocalDate day, long wordsWritten, long editMillis, boolean worked, Date lastActivity, boolean reconstructed) {

        public static Day empty(LocalDate day) {
            return new Day(day, 0, 0, false, null, false);
        }
    }

    /**
     * Whether the counts and the editing time of an author's day make it a day worked on.
     */
    public static boolean worked(StatisticCounts counts, long editMillis) {
        return counts.wordsWritten() > 0 || counts.edits() > 0 || counts.partsCreated() > 0 || editMillis > 0;
    }

    public Day day(LocalDate day) {
        Day found = days.get(day);
        return found == null ? Day.empty(day) : found;
    }

    public boolean timeKnown(LocalDate day) {
        return timeSince == null || !day.isBefore(timeSince);
    }

    // ---- streaks

    /**
     * The days in a row up to today that were worked on. Today does not break it while it is not over: when today has not been
     * worked on yet, the streak is the one that ended yesterday.
     */
    public int currentStreak() {
        return current(date -> day(date).worked());
    }

    public int longestStreak() {
        return longest(date -> day(date).worked());
    }

    /**
     * The same for the days that reached the goal; zero without a goal.
     */
    public int currentGoalStreak() {
        return goal.isSet() ? current(date -> goal.reachedBy(day(date))) : 0;
    }

    public int longestGoalStreak() {
        return goal.isSet() ? longest(date -> goal.reachedBy(day(date))) : 0;
    }

    private int current(Predicate<LocalDate> hit) {
        LocalDate date = hit.test(today) ? today : today.minusDays(1);
        int length = 0;
        while (hit.test(date)) {
            length++;
            date = date.minusDays(1);
        }
        return length;
    }

    private int longest(Predicate<LocalDate> hit) {
        int best = 0;
        int run = 0;
        LocalDate previous = null;
        for (LocalDate date : days.keySet()) {
            if (!hit.test(date)) {
                continue;
            }
            run = previous != null && previous.plusDays(1).equals(date) ? run + 1 : 1;
            best = Math.max(best, run);
            previous = date;
        }
        return best;
    }

    public long daysWorked(LocalDate from, LocalDate to) {
        return days.subMap(from, to.plusDays(1)).values().stream().filter(Day::worked).count();
    }

    // ---- the shade of a day

    /**
     * The shade of a day, 0 (not worked on) to {@code LEVELS - 1}, see {@link #level(long, long, long)}.
     *
     * @param max the most of the metric over the days that are shown, the scale when there is no goal
     */
    public int level(LocalDate date, ActivityMetric metric, long max) {
        Day day = day(date);
        if (!day.worked()) {
            return 0;
        }
        // a day that was worked on is marked even when it has none of this metric (or its time was not recorded)
        if (metric == ActivityMetric.WORDS) {
            return Math.max(1, level(day.wordsWritten(), goal.words(), max));
        }
        return timeKnown(date) ? Math.max(1, level(day.editMillis(), goal.minutesInMillis(), max)) : 1;
    }

    public static final int LEVELS = 5;

    /**
     * 0 for nothing, then 1 to 4 by how far the value is from the target: the goal when there is one, else the largest value
     * shown.
     */
    public static int level(long value, long goal, long max) {
        long target = goal > 0 ? goal : max;
        if (value <= 0) {
            return 0;
        }
        if (target <= 0) {
            return 1;
        }
        double ratio = (double) value / target;
        if (ratio >= 1) {
            return 4;
        }
        if (ratio >= 0.66) {
            return 3;
        }
        return ratio >= 0.33 ? 2 : 1;
    }

    /**
     * The most of the metric on a day in the range.
     */
    public long max(ActivityMetric metric, LocalDate from, LocalDate to) {
        long max = 0;
        for (Day day : days.subMap(from, to.plusDays(1)).values()) {
            max = Math.max(max, metric == ActivityMetric.WORDS ? day.wordsWritten() : day.editMillis());
        }
        return max;
    }

    /**
     * Builds the days from what was read: the author's counts by day, the editing time by day, the last time of each day.
     */
    public static WritingActivity of(LocalDate today, Map<LocalDate, StatisticCounts> counts, Map<LocalDate, Long> editMillis,
                                     Map<LocalDate, Date> lastActivity, Set<LocalDate> reconstructed, WritingGoal goal,
                                     LocalDate timeSince) {
        TreeMap<LocalDate, Day> days = new TreeMap<>();
        Set<LocalDate> all = new TreeSet<>(counts.keySet());
        all.addAll(editMillis.keySet());
        for (LocalDate date : all) {
            if (date.isAfter(today)) {
                continue;
            }
            StatisticCounts of = counts.getOrDefault(date, StatisticCounts.ZERO);
            long edit = editMillis.getOrDefault(date, 0L);
            if (worked(of, edit)) {
                days.put(date, new Day(date, of.wordsWritten(), edit, true, lastActivity.get(date), reconstructed.contains(date)));
            }
        }
        return new WritingActivity(today, days, goal, timeSince);
    }

}
