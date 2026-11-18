package com.github.enerccio.marginalia.ui.jobs;

import com.github.enerccio.marginalia.domain.service.job.JobCallType;
import com.github.enerccio.marginalia.domain.service.job.JobStatus;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.html.Span;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * How the jobs overview shows times, durations and statuses.
 */
public final class JobFormat {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private JobFormat() {
    }

    public static String time(Instant instant) {
        return instant == null ? "" : TIME.format(instant);
    }

    public static String time(Date date) {
        return date == null ? "" : TIME.format(date.toInstant());
    }

    /**
     * 850 ms, 12.3 s, 4m 05s, 2h 07m.
     */
    public static String duration(long millis) {
        long ms = Math.max(0, millis);
        if (ms < 1000) {
            return ms + " ms";
        }
        if (ms < 60_000) {
            return String.format(Locale.ROOT, "%.1f s", ms / 1000.0);
        }
        long seconds = ms / 1000;
        if (seconds < 3600) {
            return String.format(Locale.ROOT, "%dm %02ds", seconds / 60, seconds % 60);
        }
        return String.format(Locale.ROOT, "%dh %02dm", seconds / 3600, (seconds % 3600) / 60);
    }

    public static String duration(Instant from, Instant to) {
        return from == null ? "" : duration(((to == null ? Instant.now() : to).toEpochMilli()) - from.toEpochMilli());
    }

    public static String duration(Date from, Date to) {
        return from == null ? "" : duration(from.toInstant(), to == null ? null : to.toInstant());
    }

    public static String status(Localization loc, JobStatus status) {
        return switch (status) {
            case RUNNING -> loc.getValue(L.ENUM_JOB_STATUS_RUNNING);
            case COMPLETED -> loc.getValue(L.ENUM_JOB_STATUS_COMPLETED);
            case FAILED -> loc.getValue(L.ENUM_JOB_STATUS_FAILED);
            case CANCELLED -> loc.getValue(L.ENUM_JOB_STATUS_CANCELLED);
        };
    }

    public static String type(Localization loc, JobCallType type) {
        return switch (type) {
            case LLM -> loc.getValue(L.ENUM_JOB_CALL_TYPE_LLM);
            case AUDIO -> loc.getValue(L.ENUM_JOB_CALL_TYPE_AUDIO);
            case IMAGE -> loc.getValue(L.ENUM_JOB_CALL_TYPE_IMAGE);
            case OTHER -> loc.getValue(L.ENUM_JOB_CALL_TYPE_OTHER);
        };
    }

    /**
     * The status as a badge: green when completed, red when failed.
     */
    public static Span badge(Localization loc, JobStatus status) {
        Span badge = new Span(status(loc, status));
        badge.getElement().getThemeList().add(switch (status) {
            case RUNNING -> "badge";
            case COMPLETED -> "badge success";
            case FAILED -> "badge error";
            case CANCELLED -> "badge contrast";
        });
        return badge;
    }
}
