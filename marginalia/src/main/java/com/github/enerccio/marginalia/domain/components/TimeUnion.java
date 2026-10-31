package com.github.enerccio.marginalia.domain.components;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

/**
 * The one place that combines the stretches of time, everything that shows a time goes through it. The stretches are
 * sorted by their start and swept: the overlapping and the touching ones are one, and the length of what is left is the
 * time. Five agents that run for 24 hours on five books are 24 hours in total, which adding the lengths could not tell.
 * Exact to the millisecond; the rows are stretches and not requests, so it is done in memory.
 */
public final class TimeUnion {

    private TimeUnion() {
    }

    /**
     * The length of the union of the stretches in milliseconds.
     */
    public static long union(Collection<ActivityRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        List<ActivityRow> sorted = new ArrayList<>(rows);
        sorted.sort(Comparator.comparingLong((ActivityRow r) -> r.startTime().getTime()));
        long total = 0;
        long from = 0;
        long to = 0;
        boolean open = false;
        for (ActivityRow row : sorted) {
            long start = row.startTime().getTime();
            long end = Math.max(start, row.endTime().getTime());
            if (!open) {
                from = start;
                to = end;
                open = true;
            } else if (start <= to) {
                to = Math.max(to, end);
            } else {
                total += to - from;
                from = start;
                to = end;
            }
        }
        return open ? total + (to - from) : total;
    }

    /**
     * The time of each kind: the union of the stretches of that kind, whichever side or book they belong to.
     */
    public static ActivityTimes times(Collection<ActivityRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return ActivityTimes.ZERO;
        }
        EnumMap<ActivityKind, List<ActivityRow>> byKind = new EnumMap<>(ActivityKind.class);
        for (ActivityRow row : rows) {
            byKind.computeIfAbsent(row.kind(), k -> new ArrayList<>()).add(row);
        }
        return new ActivityTimes(
                union(byKind.get(ActivityKind.OPEN)),
                union(byKind.get(ActivityKind.EDIT)),
                union(byKind.get(ActivityKind.GENERATE)),
                union(byKind.get(ActivityKind.RUN)));
    }

    /**
     * The time of each day: the stretches are cut at midnight, so a day is the union of the stretches that carry it.
     */
    public static SortedMap<LocalDate, ActivityTimes> byDay(Collection<ActivityRow> rows) {
        return group(rows, ActivityRow::day, new TreeMap<>());
    }

    /**
     * The own time of each book, the union of its stretches. The times of the books overlap, so they are not parts of the
     * total.
     */
    public static Map<String, ActivityTimes> byBook(Collection<ActivityRow> rows) {
        return group(rows, ActivityRow::bookUuid, new LinkedHashMap<>());
    }

    private static <K, M extends Map<K, ActivityTimes>> M group(Collection<ActivityRow> rows, Function<ActivityRow, K> key, M into) {
        if (rows == null) {
            return into;
        }
        Map<K, List<ActivityRow>> grouped = new LinkedHashMap<>();
        for (ActivityRow row : rows) {
            grouped.computeIfAbsent(key.apply(row), k -> new ArrayList<>()).add(row);
        }
        grouped.forEach((k, list) -> into.put(k, times(list)));
        return into;
    }

}
