package com.github.enerccio.marginalia.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

/**
 * The one clock of the application for the times that are stored: {@code creation} and {@code modification} of the entities, the bulk
 * {@code modification} updates, the times of a generation ({@code request}, {@code ttft}, {@code reasoningEnd}), the usage and AI-limit periods,
 * the day of the writing statistics and the activity tracker, and the date of the prompt macros. In production it is the system clock and nothing
 * changes; a build tool or a test sets another clock ({@link #set}) to run the real services "on" a chosen day (the dataset build, see
 * {@code TODO.DATASET.MD}). Elapsed-time measurements (time-outs, debounce, throttling) keep reading the system time: they must not move with it.
 * <p>
 * The instance is itself a {@link Clock}, so a class that holds a {@code Clock} field (the statistics, the size service, the activity tracker, the
 * template context) uses {@link #INSTANCE} as its default and follows the replacement, while a test of that class can still give it its own.
 */
public final class AppClock extends Clock {

    public static final AppClock INSTANCE = new AppClock();

    private volatile Clock delegate = Clock.systemDefaultZone();

    private AppClock() {
    }

    /** The current time of the application as a {@link Date}: what {@code new Date()} was. */
    public static Date now() {
        return new Date(INSTANCE.millis());
    }

    /** Makes every reader use the clock, until {@link #reset()}. */
    public static void set(Clock clock) {
        INSTANCE.delegate = java.util.Objects.requireNonNull(clock);
    }

    /** The system clock again. */
    public static void reset() {
        INSTANCE.delegate = Clock.systemDefaultZone();
    }

    @Override
    public ZoneId getZone() {
        return delegate.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return delegate.withZone(zone);
    }

    @Override
    public Instant instant() {
        return delegate.instant();
    }

    @Override
    public long millis() {
        return delegate.millis();
    }
}
