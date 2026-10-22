package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.collections.AgentRunPairKind;
import jakarta.persistence.*;

import java.util.Objects;

/**
 * Which item of the working copy of a run is which item of the original: the copy is made with new uuids, so the
 * merge back, the restore of a checkpoint and the handles of the agent match the items by these rows. One row per
 * item of the copy ({@link #getCopyUuid()}, unique in the run), pointing to the item it was copied from
 * ({@link #getOriginalUuid()}); an item the agent created has no original until it is applied.
 * <p>
 * A plain entity on purpose, not an {@code OwnedEntity}: a book of thousands of parts is thousands of rows that are
 * only looked up by the run, so they carry no uuid, no soft delete and no extended content. The rows belong to the
 * working copy and are deleted with it ({@code AgentRunPairService#deleteForRun}); the cleanup does not know them.
 * Reach them through a run loaded with {@code findForUser}, never by an id alone.
 */
@Entity
@Table(name = "agent_run_pairs")
public class AgentRunPair {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // the id of the run, not a reference: a row is never loaded with the run, and the run is not to be touched by it
    @Column(name = "run_id", nullable = false, updatable = false)
    private Long runId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16, updatable = false)
    private AgentRunPairKind kind;

    @Column(nullable = false, length = 36)
    private String copyUuid;

    @Column(length = 36)
    private String originalUuid;

    /**
     * The short name of the item the agent is given instead of the uuid; null until it is given one.
     */
    @Column(length = 16)
    private String handle;

    public AgentRunPair() {
    }

    public AgentRunPair(Long runId, AgentRunPairKind kind, String copyUuid, String originalUuid) {
        this.runId = runId;
        this.kind = kind;
        this.copyUuid = copyUuid;
        this.originalUuid = originalUuid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public AgentRunPairKind getKind() {
        return kind;
    }

    public void setKind(AgentRunPairKind kind) {
        this.kind = kind;
    }

    public String getCopyUuid() {
        return copyUuid;
    }

    public void setCopyUuid(String copyUuid) {
        this.copyUuid = copyUuid;
    }

    public String getOriginalUuid() {
        return originalUuid;
    }

    public void setOriginalUuid(String originalUuid) {
        this.originalUuid = originalUuid;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AgentRunPair that && id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
