package com.github.enerccio.marginalia.domain.service.job;

import java.time.Instant;

/**
 * A call of a job that ended (or was ended with its job).
 *
 * @param backend the name of the AI, or the host of a speech or an image server
 * @param model   the model id, null when there is none
 * @param error   the message of the failure, null otherwise
 */
public record FinishedCall(JobCallType type, String backend, String model, JobStatus status, Instant startedAt, Instant endedAt,
                           String error) {
}
