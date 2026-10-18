package com.github.enerccio.marginalia.domain.security.model;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.*;

/**
 * Whether a user has a {@link PermissionType}: one per user and type, the user without one does not have the
 * permission. The {@link #getOwner() owner} is only the administrator who gave it, it does not matter for the check -
 * {@code PermissionService} finds the permission of the holder whoever created it.
 */
@Entity
@Table(name = "permissions")
public class Permission extends ExtendableEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private PermissionType permissionType;

    // nullable: the cleanup nulls the reference before it purges the user
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "holder_id")
    @CleanupReference(Policy.OWNED_BY)
    private User permissionHolder;

    @Column(nullable = false)
    private boolean granted;

    // settings of single permission types, extended attributes: no column is needed, the permissions are always read
    // by their holder

    /**
     * {@link PermissionType#HELPER}: for a shared help AI, whether the limits of the user and their usage apply to the
     * questions as to any other request to the AI. Without it, or for an AI that is not shared, the owner of the AI
     * pays: the questions are not limited and not counted for the user.
     */
    @Transient
    @ExtendedAttribute
    private Boolean helperApplyLimits = false;

    /**
     * {@link PermissionType#HELPER}: whether the help assistant may move through the interface for the user (open a
     * section, select a tab). Only moves that are harmless and can be undone.
     */
    @Transient
    @ExtendedAttribute
    private Boolean helperInteractions = false;

    /**
     * {@link PermissionType#AGENTS}: how many runs of the user can work at once (running, waiting for an approval,
     * pausing or stopping; a run that waits for the user to write to it is not counted). One when not set.
     */
    @Transient
    @ExtendedAttribute
    private Integer agentsMaxConcurrentRuns;

    /**
     * {@link PermissionType#AGENTS}: most tokens (sent and generated) one run of the user can spend; none or zero is
     * no limit.
     */
    @Transient
    @ExtendedAttribute
    private Long agentsRunTokenBudget;

    /**
     * {@link PermissionType#AGENTS}: how many agents the user can have, the clones of an agent included; five when not
     * set, zero lets the user keep their agents and run them but not create or clone another.
     */
    @Transient
    @ExtendedAttribute
    private Integer agentsMaxAgents;

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }

    public User getPermissionHolder() {
        return permissionHolder;
    }

    public void setPermissionHolder(User permissionHolder) {
        this.permissionHolder = permissionHolder;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    public boolean getHelperApplyLimits() {
        return helperApplyLimits != null && helperApplyLimits;
    }

    public void setHelperApplyLimits(boolean helperApplyLimits) {
        this.helperApplyLimits = helperApplyLimits;
    }

    public boolean getHelperInteractions() {
        return helperInteractions != null && helperInteractions;
    }

    public void setHelperInteractions(boolean helperInteractions) {
        this.helperInteractions = helperInteractions;
    }

    public Integer getAgentsMaxConcurrentRuns() {
        return agentsMaxConcurrentRuns;
    }

    public void setAgentsMaxConcurrentRuns(Integer agentsMaxConcurrentRuns) {
        this.agentsMaxConcurrentRuns = agentsMaxConcurrentRuns;
    }

    public Integer getAgentsMaxAgents() {
        return agentsMaxAgents;
    }

    public void setAgentsMaxAgents(Integer agentsMaxAgents) {
        this.agentsMaxAgents = agentsMaxAgents;
    }

    public Long getAgentsRunTokenBudget() {
        return agentsRunTokenBudget;
    }

    public void setAgentsRunTokenBudget(Long agentsRunTokenBudget) {
        this.agentsRunTokenBudget = agentsRunTokenBudget;
    }
}
