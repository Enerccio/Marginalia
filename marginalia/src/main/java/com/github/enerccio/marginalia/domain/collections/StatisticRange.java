package com.github.enerccio.marginalia.domain.collections;

import com.github.enerccio.marginalia.loc.L;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Locale;

/**
 * The ranges of days the statistics are asked for: the presets of the statistics windows and of the commands, and a custom
 * pair of dates. A range is two days (both ends included, in the time zone of the server) and is resolved against today.
 */
public enum StatisticRange {

    TODAY("today", L.ENUM_STATISTIC_RANGE_TODAY),
    YESTERDAY("yesterday", L.ENUM_STATISTIC_RANGE_YESTERDAY),
    /** Rolling: today is the last of the days. */
    LAST_7_DAYS("last-7-days", L.ENUM_STATISTIC_RANGE_LAST_7_DAYS),
    LAST_30_DAYS("last-30-days", L.ENUM_STATISTIC_RANGE_LAST_30_DAYS),
    THIS_MONTH("this-month", L.ENUM_STATISTIC_RANGE_THIS_MONTH),
    /** From {@link #BEGINNING}; the reader of the rows narrows it to the first day that has one. */
    ALL_TIME("all-time", L.ENUM_STATISTIC_RANGE_ALL_TIME),
    CUSTOM("custom", L.ENUM_STATISTIC_RANGE_CUSTOM);

    /**
     * The first day of {@link #ALL_TIME}: older than the statistics can be.
     */
    public static final LocalDate BEGINNING = LocalDate.of(2000, 1, 1);

    /**
     * Two days, both included.
     */
    public record Span(LocalDate from, LocalDate to) {

        public Span {
            if (from == null || to == null || to.isBefore(from)) {
                throw new IllegalArgumentException("A range needs two days, the second not before the first");
            }
        }

        /**
         * @return the number of days
         */
        public long days() {
            return to.toEpochDay() - from.toEpochDay() + 1;
        }

    }

    private final String id;
    private final L label;

    StatisticRange(String id, L label) {
        this.id = id;
        this.label = label;
    }

    /**
     * The name of the preset in the commands.
     */
    public String id() {
        return id;
    }

    public L getLabel() {
        return label;
    }

    /**
     * The days of a preset.
     *
     * @throws IllegalStateException for {@link #CUSTOM}, whose days are given
     */
    public Span span(LocalDate today) {
        return switch (this) {
            case TODAY -> new Span(today, today);
            case YESTERDAY -> new Span(today.minusDays(1), today.minusDays(1));
            case LAST_7_DAYS -> new Span(today.minusDays(6), today);
            case LAST_30_DAYS -> new Span(today.minusDays(29), today);
            case THIS_MONTH -> new Span(today.withDayOfMonth(1), today);
            case ALL_TIME -> new Span(BEGINNING, today);
            case CUSTOM -> throw new IllegalStateException("The days of a custom range are given");
        };
    }

    /**
     * Reads the range a command was given: the name of a preset ({@code last-7-days}, also with spaces or underscores), a day
     * ({@code 2026-03-14}) or two days ({@code 2026-03-01..2026-03-14}, the order does not matter).
     *
     * @throws IllegalArgumentException when it is none of them
     */
    public static Span parse(String text, LocalDate today) {
        if (text == null || text.isBlank()) {
            return LAST_7_DAYS.span(today);
        }
        String value = text.strip();
        String id = value.toLowerCase(Locale.ROOT).replace('_', '-').replace(' ', '-');
        for (StatisticRange range : values()) {
            if (range != CUSTOM && range.id.equals(id)) {
                return range.span(today);
            }
        }
        try {
            int dots = value.indexOf("..");
            if (dots < 0) {
                LocalDate day = LocalDate.parse(value);
                return new Span(day, day);
            }
            LocalDate first = LocalDate.parse(value.substring(0, dots).strip());
            LocalDate second = LocalDate.parse(value.substring(dots + 2).strip());
            return first.isAfter(second) ? new Span(second, first) : new Span(first, second);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Not a range of days: " + text, e);
        }
    }

}
