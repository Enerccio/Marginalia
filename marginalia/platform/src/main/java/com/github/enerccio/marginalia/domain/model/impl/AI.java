package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.collections.PromptPlacement;
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
     * Whether the model is multimodal (takes images): the application sends it the images that belong to a request. A
     * model without vision refuses such a request. Off when never set. The field keeps its old name because it is the key of the stored extended attribute.
     */
    @Transient
    @ExtendedAttribute
    private Boolean sendLoreImages;

    public boolean isMultimodal() {
        return Boolean.TRUE.equals(sendLoreImages);
    }

    public void setMultimodal(boolean multimodal) {
        this.sendLoreImages = multimodal ? Boolean.TRUE : null;
    }

    /**
     * The old setting "images in the system message" ({@code true} is {@link PromptPlacement#SYSTEM}), read when
     * {@link #imagePlacement} was never set; the field keeps its name, the key of the stored extended attribute.
     */
    @Transient
    @ExtendedAttribute
    private Boolean loreImagesInSystem;

    /**
     * Where the images go, used only with {@link #isMultimodal()}: {@link PromptPlacement#SYSTEM} the images of the system prompt into
     * the system message itself (OpenAI-compatible models whose chat template takes images there; the images of the other
     * messages after their text), {@link PromptPlacement#BEFORE} before the text of their message (as Gemma 4 wants it, the
     * images of the system prompt in a message of their own), {@link PromptPlacement#AFTER} after it (the default). Audio and
     * other files always follow the text.
     */
    @Transient
    @ExtendedAttribute
    private PromptPlacement imagePlacement;

    /**
     * Where the definitions of the functions go: {@link PromptPlacement#SYSTEM} only in the {@code tools} of the request (the
     * chat template puts them into the system turn; the default), {@link PromptPlacement#BEFORE} / {@link PromptPlacement#AFTER}
     * written out also at the start / end of the latest user message. The {@code tools} of the request are always sent: the
     * server needs them to recognise the calls.
     */
    @Transient
    @ExtendedAttribute
    private PromptPlacement functionsPlacement;

    /**
     * A rule added to every request that offers functions, such as "you act only through function calls, never write a call
     * as text"; null or blank: none.
     */
    @Transient
    @ExtendedAttribute
    private String functionRule;

    /** Where the {@link #functionRule} goes; {@link PromptPlacement#AFTER} when never set. */
    @Transient
    @ExtendedAttribute
    private PromptPlacement functionRulePlacement;

    /**
     * Whether consecutive messages of the same role (user, user, user) are sent as one message ({@code PromptPlacements#mergeConsecutive}).
     * A block inserted as a user message just before the latest one then becomes the start of it: llama.cpp keeps a checkpoint of the
     * prompt at the start of the last user message, so the story before the block stays cached on models with sliding window
     * attention. Off when never set.
     */
    @Transient
    @ExtendedAttribute
    private Boolean mergeConsecutiveRoles;

    public boolean isMergeConsecutiveRoles() {
        return Boolean.TRUE.equals(mergeConsecutiveRoles);
    }

    public void setMergeConsecutiveRoles(boolean mergeConsecutiveRoles) {
        this.mergeConsecutiveRoles = mergeConsecutiveRoles ? Boolean.TRUE : null;
    }

    /** Never null: the setting, or the old checkbox ({@link PromptPlacement#SYSTEM} / {@link PromptPlacement#AFTER}). */
    public PromptPlacement getImagePlacement() {
        if (imagePlacement != null) {
            return imagePlacement;
        }
        return Boolean.TRUE.equals(loreImagesInSystem) ? PromptPlacement.SYSTEM : PromptPlacement.AFTER;
    }

    public void setImagePlacement(PromptPlacement imagePlacement) {
        this.imagePlacement = imagePlacement == PromptPlacement.AFTER ? null : imagePlacement;
        this.loreImagesInSystem = null;
    }

    /** Never null, {@link PromptPlacement#SYSTEM} when it was not set. */
    public PromptPlacement getFunctionsPlacement() {
        return functionsPlacement == null ? PromptPlacement.SYSTEM : functionsPlacement;
    }

    public void setFunctionsPlacement(PromptPlacement functionsPlacement) {
        this.functionsPlacement = functionsPlacement == PromptPlacement.SYSTEM ? null : functionsPlacement;
    }

    public String getFunctionRule() {
        return functionRule;
    }

    public void setFunctionRule(String functionRule) {
        this.functionRule = functionRule == null || functionRule.isBlank() ? null : functionRule.strip();
    }

    /** Never null, {@link PromptPlacement#AFTER} when it was not set. */
    public PromptPlacement getFunctionRulePlacement() {
        return functionRulePlacement == null ? PromptPlacement.AFTER : functionRulePlacement;
    }

    public void setFunctionRulePlacement(PromptPlacement functionRulePlacement) {
        this.functionRulePlacement = functionRulePlacement == PromptPlacement.AFTER ? null : functionRulePlacement;
    }

    /**
     * The uuid of the {@link GpuLock} every request to this AI takes (see {@code GpuService}): the model runs on a GPU shared with other
     * AIs or extensions. Null: no lock.
     */
    @Transient
    @ExtendedAttribute
    private String gpuLock;

    public String getGpuLock() {
        return gpuLock;
    }

    public void setGpuLock(String gpuLock) {
        this.gpuLock = gpuLock == null || gpuLock.isBlank() ? null : gpuLock;
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
