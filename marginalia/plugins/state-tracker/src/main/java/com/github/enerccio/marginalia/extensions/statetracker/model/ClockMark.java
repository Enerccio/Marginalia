package com.github.enerccio.marginalia.extensions.statetracker.model;

/**
 * What one processed part says about the time of the story (read by the model), or a fixed point the user set on it. The clock is
 * not stored as a time per part: it is folded from these marks in the order of the parts ({@link StoryClock}), so a reprocess or a
 * branch change only drops the marks of the parts that went.
 */
public class ClockMark {

    public enum Kind {
        /** Time passes: {@link #days} (0 = the same day; {@code null} = only the time of day is known). */
        ADVANCE,
        /** The part states the story day itself. */
        DAY,
        /** The part states a calendar date. */
        DATE,
        /** The part is set in the past: the clock does not move. */
        FLASHBACK,
        /** A fixed point the user set: the day, and the time of day and the date when given. */
        SET
    }

    private String part;
    private Kind kind;
    private Integer days;
    private Integer day;
    /** ISO date (YYYY-MM-DD). */
    private String date;
    private TimeOfDay timeOfDay;
    private boolean approximate;
    private String evidence;

    public ClockMark() {
    }

    public ClockMark(String part, Kind kind) {
        this.part = part;
        this.kind = kind;
    }

    /** Whether the user set it: a reading of the part does not replace it. */
    public boolean isManual() {
        return kind == Kind.SET;
    }

    public String getPart() {
        return part;
    }

    public Kind getKind() {
        return kind;
    }

    public Integer getDays() {
        return days;
    }

    public ClockMark days(Integer days) {
        this.days = days;
        return this;
    }

    public Integer getDay() {
        return day;
    }

    public ClockMark day(Integer day) {
        this.day = day;
        return this;
    }

    public String getDate() {
        return date;
    }

    public ClockMark date(String date) {
        this.date = date;
        return this;
    }

    public TimeOfDay getTimeOfDay() {
        return timeOfDay;
    }

    public ClockMark timeOfDay(TimeOfDay timeOfDay) {
        this.timeOfDay = timeOfDay;
        return this;
    }

    public boolean isApproximate() {
        return approximate;
    }

    public ClockMark approximate(boolean approximate) {
        this.approximate = approximate;
        return this;
    }

    public String getEvidence() {
        return evidence;
    }

    public ClockMark evidence(String evidence) {
        this.evidence = evidence;
        return this;
    }
}
