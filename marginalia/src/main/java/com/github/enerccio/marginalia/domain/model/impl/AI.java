package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.collections.ReasoningEffort;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.*;

@Entity
@Table(name = "ais")
@Inheritance(strategy = InheritanceType.JOINED)
public class AI extends ExtendableEntity {

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ai_type")
    private AIType aiType;

    @Transient
    private InferenceService inferenceService;

    @Lob
    private String name;

    @Lob
    private String jailbreak;

    @Column
    private Boolean needsJailbreak;

    // reasoning

    @Column
    private Boolean enabledReasoning;

    @Column
    @Enumerated(EnumType.STRING)
    private ReasoningEffort reasoningEffort;

    @Column
    private Integer maxCompletionTokens;

    @Column
    private Integer maxContext;

    /**
     * Shared with all users: they can pick it for their books, only the owner (an administrator) can see and change
     * it. Real column because the lookups of the user's AIs filter on it.
     */
    @Column(nullable = false)
    private boolean shared = false;

    /**
     * Limits of the users of a shared AI: a JSON object with the settings of each user under their uuid, see
     * {@code AILimits}. Not a column, nothing searches by it; what a user used is in their {@link Usage} (counters
     * that change with every request must not be saved with the AI).
     */
    @Transient
    @ExtendedAttribute
    private String limits;

    /**
     * The connections of this AI: how many requests may be on the way to it at once, every request of every user and every kind
     * (the generation, a side query, an agent, a plugin); the rest waits in the order it came (see {@code AIGate}). Null (or
     * less than one) is one connection, see {@link #getConnections()}: a local server with one slot (a single GPU) needs no
     * setting, a cloud model that answers several at once sets more.
     */
    @Transient
    @ExtendedAttribute
    private Integer maxConcurrentRequests;

    /**
     * Whether the images in the content of the lorebook entries go to this model with the lore (the model must take images:
     * a model without vision refuses the request). Off when never set.
     */
    @Transient
    @ExtendedAttribute
    private Boolean sendLoreImages;

    public boolean isSendLoreImages() {
        return Boolean.TRUE.equals(sendLoreImages);
    }

    public void setSendLoreImages(boolean sendLoreImages) {
        this.sendLoreImages = sendLoreImages ? Boolean.TRUE : null;
    }

    /**
     * The images of the entries of the lore block go into the system message itself (OpenAI-compatible models whose chat template
     * takes images there), not into a user message of their own after it. Off when never set; used only with
     * {@link #isSendLoreImages()}.
     */
    @Transient
    @ExtendedAttribute
    private Boolean loreImagesInSystem;

    public boolean isLoreImagesInSystem() {
        return Boolean.TRUE.equals(loreImagesInSystem);
    }

    public void setLoreImagesInSystem(boolean loreImagesInSystem) {
        this.loreImagesInSystem = loreImagesInSystem ? Boolean.TRUE : null;
    }

    /** The number as it is stored, null when it was never set; {@link #getConnections()} is what applies. */
    public Integer getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }

    /** The connections that apply: the setting, one when there is none. */
    public int getConnections() {
        return maxConcurrentRequests == null || maxConcurrentRequests < 1 ? 1 : maxConcurrentRequests;
    }

    public void setMaxConcurrentRequests(Integer maxConcurrentRequests) {
        this.maxConcurrentRequests = maxConcurrentRequests;
    }

    public AIType getAiType() {
        return aiType;
    }

    public void setAiType(AIType aiType) {
        this.aiType = aiType;
        this.inferenceService = null;
    }

    public InferenceService getInferenceService() {
        return inferenceService;
    }

    public void setInferenceService(InferenceService inferenceService) {
        this.inferenceService = inferenceService;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJailbreak() {
        return jailbreak;
    }

    public void setJailbreak(String jailbreak) {
        this.jailbreak = jailbreak;
    }

    public Boolean getNeedsJailbreak() {
        return needsJailbreak;
    }

    public void setNeedsJailbreak(Boolean needsJailbreak) {
        this.needsJailbreak = needsJailbreak;
    }

    public Boolean getEnabledReasoning() {
        return enabledReasoning;
    }

    public void setEnabledReasoning(Boolean enabledReasoning) {
        this.enabledReasoning = enabledReasoning;
    }

    public ReasoningEffort getReasoningEffort() {
        return reasoningEffort;
    }

    public void setReasoningEffort(ReasoningEffort reasoningEffort) {
        this.reasoningEffort = reasoningEffort;
    }

    public Integer getMaxCompletionTokens() {
        return maxCompletionTokens;
    }

    public void setMaxCompletionTokens(Integer maxCompletionTokens) {
        this.maxCompletionTokens = maxCompletionTokens;
    }

    public Integer getMaxContext() {
        return maxContext;
    }

    public void setMaxContext(Integer maxContext) {
        this.maxContext = maxContext;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public String getLimits() {
        return limits;
    }

    public void setLimits(String limits) {
        this.limits = limits;
    }
}
