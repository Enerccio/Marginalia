package com.github.enerccio.marginalia.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class AppClockTest {

    @AfterEach
    void reset() {
        AppClock.reset();
    }

    @Test
    void followsTheSystemClockByDefault() {
        long before = System.currentTimeMillis();
        long now = AppClock.now().getTime();
        long after = System.currentTimeMillis();
        assertTrue(before <= now && now <= after);
    }

    @Test
    void aSetClockMovesEveryReader() {
        Clock fixed = Clock.fixed(Instant.parse("2026-02-03T10:15:30Z"), ZoneOffset.UTC);
        AppClock.set(fixed);
        assertEquals(Instant.parse("2026-02-03T10:15:30Z").toEpochMilli(), AppClock.now().getTime());
        assertEquals(LocalDate.of(2026, 2, 3), LocalDate.now(AppClock.INSTANCE));
        assertEquals(ZoneOffset.UTC, AppClock.INSTANCE.getZone());
    }

    @Test
    void resetGoesBackToTheSystemClock() {
        AppClock.set(Clock.fixed(Instant.parse("2001-01-01T00:00:00Z"), ZoneOffset.UTC));
        AppClock.reset();
        assertTrue(AppClock.now().getTime() > Instant.parse("2025-01-01T00:00:00Z").toEpochMilli());
    }

    @Test
    void withZoneKeepsTheTime() {
        AppClock.set(Clock.fixed(Instant.parse("2026-02-03T23:30:00Z"), ZoneOffset.UTC));
        Clock tokyo = AppClock.INSTANCE.withZone(ZoneId.of("Asia/Tokyo"));
        assertEquals(LocalDate.of(2026, 2, 4), LocalDate.now(tokyo));
    }

    @Test
    void aMovingClockIsFollowedByTheStoredTimes() {
        Instant[] at = {Instant.parse("2026-03-01T08:00:00Z")};
        AppClock.set(new Clock() {
            public ZoneId getZone() { return ZoneOffset.UTC; }
            public Clock withZone(ZoneId zone) { return this; }
            public Instant instant() { return at[0]; }
        });
        long first = AppClock.now().getTime();
        at[0] = at[0].plusSeconds(90);
        assertEquals(90_000, AppClock.now().getTime() - first);
    }
}
