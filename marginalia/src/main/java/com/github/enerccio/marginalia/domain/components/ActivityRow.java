package com.github.enerccio.marginalia.domain.components;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;

import java.time.LocalDate;
import java.util.Date;

/**
 * A stretch of time as the union of the times reads it: everything but the name of the book. The stretch is inside one day.
 */
public record ActivityRow(String bookUuid, ActivityKind kind, boolean agent, LocalDate day, Date startTime, Date endTime) {

    public ActivityRow {
        // the database gives java.sql.Timestamp, which is not equal to the Date of the same instant
        startTime = new Date(startTime.getTime());
        endTime = new Date(endTime.getTime());
    }

    public long millis() {
        return Math.max(0, endTime.getTime() - startTime.getTime());
    }

}
