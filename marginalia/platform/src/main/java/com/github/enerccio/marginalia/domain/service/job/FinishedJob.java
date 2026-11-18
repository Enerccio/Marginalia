package com.github.enerccio.marginalia.domain.service.job;

import java.time.Instant;
import java.util.List;

/**
 * A job that ended, with its calls, as it is handed to the log. Metadata only.
 *
 * @param userId     the user it ran for, null when the thread had none
 * @param login      their login at the time
 * @param calls      the calls that were kept (a job that made more than {@link Jobs#MAX_KEPT_CALLS} keeps the first ones)
 * @param callCount  all calls it made, also those that were not kept
 * @param failedCalls how many of them failed
 */
public record FinishedJob(String uuid, Long userId, String login, String kind, String label, JobStatus status, Instant startedAt,
                          Instant endedAt, List<FinishedCall> calls, int callCount, int failedCalls) {
}
