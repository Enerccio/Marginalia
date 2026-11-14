package com.github.enerccio.marginalia.domain.service.job;

import java.time.Instant;
import java.util.List;

/**
 * A job that runs now, a snapshot for the overview of what the server is doing.
 *
 * @param calls        the calls it made so far (running ones have no end), oldest first
 * @param runningCalls how many of them are in flight now
 * @param callCount    all calls it made
 */
public record ActiveJob(String uuid, Long userId, String login, String kind, String label, Instant startedAt, List<ActiveCall> calls,
                        int runningCalls, int callCount) {

    /**
     * @param endedAt null while the call is in flight
     */
    public record ActiveCall(JobCallType type, String backend, String model, JobStatus status, Instant startedAt, Instant endedAt) {
    }
}
