package com.github.enerccio.marginalia.extensions.statetracker.model;

import com.github.enerccio.marginalia.extensions.storytracker.process.ProcessedPart;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * The story clock of a book: its settings (on or off, the anchor event, the time at the start point) and the marks of the processed
 * parts. The time at a part is the start folded with the marks of the processed parts up to it, in their order: the model only
 * reports what a part says (three days later, the next morning, the fortieth day, a date), the counting is done here.
 */
public class StoryClock {

    private boolean enabled;
    /** The event day 1 is the day of ("the awakening"); may be empty. */
    private String anchor = "";
    /** The time before the first part that is processed. */
    private int startDay = 1;
    private TimeOfDay startTimeOfDay;
    /** ISO date, {@code null} = the story has no calendar date. */
    private String startDate;
    /** By part (uuid). */
    private Map<String, ClockMark> marks = new LinkedHashMap<>();

    /** The time before the first processed part. */
    public StoryTime start() {
        return new StoryTime(startDay, startTimeOfDay, parse(startDate), false);
    }

    /** The time after every processed part (the time of the story now). */
    public StoryTime now(StateBook state) {
        return upTo(state, null);
    }

    /**
     * The time right after the part (its own mark included); the time now when the part is not processed.
     *
     * @param part {@code null} = all the processed parts
     */
    public StoryTime upTo(StateBook state, String part) {
        StoryTime time = start();
        for (ProcessedPart processed : state.getProcessed()) {
            ClockMark mark = getMarks().get(processed.getUuid());
            if (mark != null) {
                time = apply(time, mark);
            }
            if (processed.getUuid().equals(part)) {
                break;
            }
        }
        return time;
    }

    /** The time after each processed part, by part (uuid), in their order. */
    public Map<String, StoryTime> timeline(StateBook state) {
        Map<String, StoryTime> times = new LinkedHashMap<>();
        StoryTime time = start();
        for (ProcessedPart processed : state.getProcessed()) {
            ClockMark mark = getMarks().get(processed.getUuid());
            if (mark != null) {
                time = apply(time, mark);
            }
            times.put(processed.getUuid(), time);
        }
        return times;
    }

    /** One step of the clock. */
    static StoryTime apply(StoryTime time, ClockMark mark) {
        return switch (mark.getKind()) {
            case FLASHBACK -> time;
            case ADVANCE -> {
                Integer days = mark.getDays();
                TimeOfDay to = mark.getTimeOfDay();
                if (days == null || days <= 0) {
                    // no days or "the same day": later in that day, or the next day when that time of day has passed already (an
                    // advance cannot go back; the model reports 0 days for "she woke at dawn" after an evening)
                    days = to == null || time.timeOfDay() == null || to.ordinal() >= time.timeOfDay().ordinal() ? 0 : 1;
                }
                yield new StoryTime(time.day() + days, to != null ? to : days > 0 ? null : time.timeOfDay(),
                        time.date() == null ? null : time.date().plusDays(days), time.approximate() || mark.isApproximate());
            }
            case DAY -> {
                int day = mark.getDay() == null ? time.day() : mark.getDay();
                TimeOfDay to = mark.getTimeOfDay() != null ? mark.getTimeOfDay() : day == time.day() ? time.timeOfDay() : null;
                yield new StoryTime(day, to, time.date() == null ? null : time.date().plusDays(day - time.day()), false);
            }
            case DATE -> {
                LocalDate date = parse(mark.getDate());
                if (date == null) {
                    yield time;
                }
                long between = time.date() == null ? 0 : ChronoUnit.DAYS.between(time.date(), date);
                TimeOfDay to = mark.getTimeOfDay() != null ? mark.getTimeOfDay() : between == 0 ? time.timeOfDay() : null;
                yield new StoryTime((int) (time.day() + between), to, date, time.date() == null && time.approximate());
            }
            case SET -> {
                int day = mark.getDay() == null ? time.day() : mark.getDay();
                LocalDate date = parse(mark.getDate());
                if (date == null && time.date() != null) {
                    date = time.date().plusDays(day - time.day());
                }
                yield new StoryTime(day, mark.getTimeOfDay(), date, false);
            }
        };
    }

    /** Takes back what the model read in the dropped parts; a fixed point the user set stays. */
    public int rewind(Predicate<String> dropped) {
        int before = getMarks().size();
        getMarks().values().removeIf(mark -> !mark.isManual() && dropped.test(mark.getPart()));
        return before - getMarks().size();
    }

    /** The mark the model read, unless the user set a fixed point on that part. */
    public void read(ClockMark mark) {
        ClockMark current = getMarks().get(mark.getPart());
        if (current == null || !current.isManual()) {
            getMarks().put(mark.getPart(), mark);
        }
    }

    /** A fixed point the user sets on a part; {@code null} removes it (the part then has no mark until it is read again). */
    public void fix(String part, ClockMark mark) {
        if (mark == null) {
            getMarks().remove(part);
        } else {
            getMarks().put(part, mark);
        }
    }

    public static LocalDate parse(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date.strip());
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAnchor() {
        return anchor == null ? "" : anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    public int getStartDay() {
        return startDay;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public TimeOfDay getStartTimeOfDay() {
        return startTimeOfDay;
    }

    public void setStartTimeOfDay(TimeOfDay startTimeOfDay) {
        this.startTimeOfDay = startTimeOfDay;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public Map<String, ClockMark> getMarks() {
        if (marks == null) {
            marks = new LinkedHashMap<>();
        }
        return marks;
    }

    /** The marks the runs set are those of {@code checkpoint} (a return to a checkpoint); the settings of the clock stay. */
    public void takeMarksOf(StoryClock checkpoint) {
        marks = new LinkedHashMap<>(checkpoint.getMarks());
    }
}
