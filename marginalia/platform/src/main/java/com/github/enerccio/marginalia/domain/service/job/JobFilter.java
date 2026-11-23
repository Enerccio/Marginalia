package com.github.enerccio.marginalia.domain.service.job;

import java.util.Date;

/**
 * Which jobs of the log: the overview shows them and the cleanup deletes them. Every field is optional, null does not filter.
 *
 * @param userId the user the job ran for
 * @param kind   the kind of the job
 * @param status how it ended
 * @param from   started at or after this
 * @param before started before this (the cleanup of old jobs)
 */
public record JobFilter(Long userId, String kind, JobStatus status, Date from, Date before) {

    public static final JobFilter ALL = new JobFilter(null, null, null, null, null);

    public JobFilter withBefore(Date before) {
        return new JobFilter(userId, kind, status, from, before);
    }
}
