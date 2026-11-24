package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.service.job.JobStatus;
import jakarta.persistence.*;

import java.util.Date;

/**
 * A finished unit of work that talked to an external AI (a generation, a summary, an audiobook chapter...), logged for the
 * administrator's overview, metadata only: no prompt, no answer, no text. The owner is the user it ran for. A job that is
 * running now is not here but in the {@code JobRegistry}; it is written when it ends, together with its {@link JobCall}s.
 */
@Entity
@Table(name = "jobs")
public class Job extends OwnedEntity {

    /**
     * The login of the owner when the job ran; stays readable if the user is renamed or purged.
     */
    @Column(length = 255)
    private String ownerLogin;

    @Column(nullable = false, length = 64)
    private String kind;

    @Column(length = 255)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private JobStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date startedAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endedAt;

    @Column(nullable = false)
    private int callCount;

    @Column(nullable = false)
    private int failedCalls;

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public void setOwnerLogin(String ownerLogin) {
        this.ownerLogin = ownerLogin;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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

    public int getCallCount() {
        return callCount;
    }

    public void setCallCount(int callCount) {
        this.callCount = callCount;
    }

    public int getFailedCalls() {
        return failedCalls;
    }

    public void setFailedCalls(int failedCalls) {
        this.failedCalls = failedCalls;
    }
}
