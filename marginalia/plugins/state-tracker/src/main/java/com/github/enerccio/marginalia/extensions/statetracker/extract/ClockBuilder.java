package com.github.enerccio.marginalia.extensions.statetracker.extract;

import com.github.enerccio.marginalia.extensions.statetracker.extract.StateAnswer.ClockReport;
import com.github.enerccio.marginalia.extensions.statetracker.model.ClockMark;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateBook;
import com.github.enerccio.marginalia.extensions.statetracker.model.StoryClock;
import com.github.enerccio.marginalia.extensions.statetracker.model.TimeOfDay;
import com.github.enerccio.marginalia.extensions.storytracker.process.Evidence;
import com.github.enerccio.marginalia.extensions.storytracker.process.Part;

import java.util.*;

/**
 * Turns the clock part of the answer into changes the user can review, and drops what is not usable: a message that does not exist
 * or is named twice, an unknown kind, a report without a quote, a negative or absurd number of days, a day below 1, a date that is
 * not a date. "none" is not a change. Nothing is applied here; the counting is {@link StoryClock}'s.
 */
public final class ClockBuilder {

    /** More days than this in one part is not a reading of the text (ten years). */
    static final int MAX_DAYS = 3660;

    /** The checked clock changes and, one line each, what was dropped and why. */
    public record Result(List<ClockChange> changes, List<String> dropped) {
    }

    private ClockBuilder() {
    }

    /**
     * Stores what a run read about the time on its parts: the marks the model read before on them go (a fixed point the user set
     * stays), the checked changes are stored.
     */
    public static void commit(StateBook state, List<Part> parts, List<ClockChange> changes) {
        for (Part part : parts) {
            ClockMark mark = state.getClock().getMarks().get(part.uuid());
            if (mark != null && !mark.isManual()) {
                state.getClock().getMarks().remove(part.uuid());
            }
        }
        for (ClockChange change : changes) {
            if (change.isApply()) {
                state.getClock().read(change.getMark());
            }
        }
    }

    public static Result build(StatePrompt.Request request, List<ClockReport> reports) {
        List<ClockChange> changes = new ArrayList<>();
        List<String> dropped = new ArrayList<>();
        if (!request.clock() || reports == null) {
            return new Result(changes, dropped);
        }
        List<Part> parts = request.numbering().parts();
        Set<Integer> seen = new HashSet<>();
        for (ClockReport report : reports) {
            if (report == null) {
                continue;
            }
            String kind = report.kind() == null ? "none" : report.kind().strip().toLowerCase(Locale.ROOT);
            if (kind.equals("none") || kind.isEmpty()) {
                continue;
            }
            int message = report.message();
            if (message < 1 || message > parts.size()) {
                dropped.add("Clock: a message that does not exist (" + message + ")");
                continue;
            }
            if (!seen.add(message)) {
                dropped.add("Clock: message " + message + " named twice");
                continue;
            }
            if (report.evidence() == null || report.evidence().isBlank()) {
                dropped.add("Clock: message " + message + " has no evidence");
                continue;
            }
            Part part = parts.get(message - 1);
            TimeOfDay timeOfDay = TimeOfDay.parse(report.timeOfDay());
            ClockMark mark = switch (kind) {
                case "advance" -> {
                    Integer days = report.days();
                    if (days != null && (days < 0 || days > MAX_DAYS)) {
                        dropped.add("Clock: message " + message + " advances " + days + " days");
                        yield null;
                    }
                    if (days == null && timeOfDay == null) {
                        yield null;
                    }
                    yield new ClockMark(part.uuid(), ClockMark.Kind.ADVANCE).days(days).timeOfDay(timeOfDay)
                            .approximate(Boolean.TRUE.equals(report.approximate()));
                }
                case "day" -> {
                    if (report.day() == null || report.day() < 1) {
                        dropped.add("Clock: message " + message + " states no day");
                        yield null;
                    }
                    yield new ClockMark(part.uuid(), ClockMark.Kind.DAY).day(report.day()).timeOfDay(timeOfDay);
                }
                case "date" -> {
                    if (StoryClock.parse(report.date()) == null) {
                        dropped.add("Clock: message " + message + " states no date (" + report.date() + ")");
                        yield null;
                    }
                    yield new ClockMark(part.uuid(), ClockMark.Kind.DATE).date(StoryClock.parse(report.date()).toString()).timeOfDay(timeOfDay);
                }
                case "flashback" -> new ClockMark(part.uuid(), ClockMark.Kind.FLASHBACK);
                default -> {
                    dropped.add("Clock: message " + message + " has the unknown kind " + report.kind());
                    yield null;
                }
            };
            if (mark != null) {
                mark.evidence(report.evidence().strip());
                changes.add(new ClockChange(mark, message, Evidence.found(part.text(), report.evidence())));
            }
        }
        return new Result(changes, dropped);
    }
}
