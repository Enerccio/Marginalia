package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.collections.ReasoningEffort;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import jakarta.persistence.*;

@Entity
@Table(name = "ais",
        indexes = {
        @Index(name = "ai_is_deleted_idx", columnList = "is_deleted"),
        @Index(name = "ai_type_idx", columnList = "ai_type"),
        @Index(name = "ai_user_id_ix", columnList = "userId")
})
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
}
