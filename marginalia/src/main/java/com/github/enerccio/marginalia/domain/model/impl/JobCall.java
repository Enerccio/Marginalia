package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.service.job.JobCallType;
import com.github.enerccio.marginalia.domain.service.job.JobStatus;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import jakarta.persistence.*;

import java.util.Date;

/**
 * One call of a {@link Job} to an external backend: a request to a model, to a speech or an image server. Metadata only.
 */
@Entity
@Table(name = "job_calls")
public class JobCall extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    @CleanupReference(Policy.OWNED_BY)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private JobCallType callType;

    /**
     * What was called: the name of the AI, or the host of a speech or image server (never the path or the query).
     */
    @Column(length = 255)
    private String backend;

    @Column(length = 255)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private JobStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date startedAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endedAt;

    @Column(length = 1000)
    private String error;

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public JobCallType getCallType() {
        return callType;
    }

    public void setCallType(JobCallType callType) {
        this.callType = callType;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Date endedAt) {
        this.endedAt = endedAt;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
