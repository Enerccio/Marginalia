package com.github.enerccio.marginalia.domain.service.job;

import java.time.Instant;
import java.util.List;

/**
 * A job that runs now, a snapshot for the overview of what the server is doing.
 *
 * @param calls        the calls it made so far (running ones have no end), oldest first
 * @param runningCalls how many of them are in flight now
 * @param callCount    all calls it made
 * @param book         the name of the book of the run the job works for ({@code Runs}), null when none or the job was opened outside
 *                     of a run
 * @param stoppable    whether that run has a Stop ({@link Jobs#stop})
 */
public record ActiveJob(String uuid, Long userId, String login, String kind, String label, Instant startedAt, List<ActiveCall> calls,
                        int runningCalls, int callCount, String book, boolean stoppable) {

    /** A job that knows no run: no book, no Stop. */
    public ActiveJob(String uuid, Long userId, String login, String kind, String label, Instant startedAt, List<ActiveCall> calls,
                     int runningCalls, int callCount) {
        this(uuid, userId, login, kind, label, startedAt, calls, runningCalls, callCount, null, false);
    }

    /**
     * @param endedAt null while the call is in flight
     */
    public record ActiveCall(JobCallType type, String backend, String model, JobStatus status, Instant startedAt, Instant endedAt) {
    }
}
