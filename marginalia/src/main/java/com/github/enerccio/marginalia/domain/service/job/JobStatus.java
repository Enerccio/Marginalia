package com.github.enerccio.marginalia.domain.service.job;

/**
 * How a job or one of its calls stands: {@link #RUNNING} until it ends, then how it ended.
 */
public enum JobStatus {
    RUNNING, COMPLETED, FAILED, CANCELLED
}
