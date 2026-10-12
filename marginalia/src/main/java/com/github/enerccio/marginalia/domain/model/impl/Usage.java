package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import jakarta.persistence.*;

import java.util.Date;

/**
 * What a user has used of an AI. There is one per AI and user (the owner of this entity): an AI that is used by its
 * owner only has one, a shared AI has one for everybody who uses it. Counting only for now, nothing is limited by it.
 * <p>
 * Counters are real columns because they are added up in SQL ({@code UsageRepository#add}), not read and saved, so
 * parallel generations don't lose counts.
 */
@Entity
@Table(name = "usages")
public class Usage extends OwnedEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "ai_id", nullable = false)
    @CleanupReference(Policy.OWNED_BY)
    private AI ai;

    /**
     * Tokens sent to the model (the prompt).
     */
    @Column(nullable = false)
    private long prefillTokens;

    /**
     * Tokens the model generated (the response, reasoning included).
     */
    @Column(nullable = false)
    private long generatedTokens;

    /**
     * Number of requests the counters were added up from.
     */
    @Column(nullable = false)
    private long requests;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUsed;

    public AI getAi() {
        return ai;
    }

    public void setAi(AI ai) {
        this.ai = ai;
    }

    public long getPrefillTokens() {
        return prefillTokens;
    }

    public void setPrefillTokens(long prefillTokens) {
        this.prefillTokens = prefillTokens;
    }

    public long getGeneratedTokens() {
        return generatedTokens;
    }

    public void setGeneratedTokens(long generatedTokens) {
        this.generatedTokens = generatedTokens;
    }

    public long getRequests() {
        return requests;
    }

    public void setRequests(long requests) {
        this.requests = requests;
    }

    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }
}
