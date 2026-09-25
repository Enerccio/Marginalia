package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "summaries")
public class Summary extends ExtendableEntity {

    @ExtendedAttribute
    @Transient
    private String summary;

    @ExtendedAttribute
    @Transient
    private Long summaryTokens;

    @ExtendedAttribute
    @Transient
    private String reasoning;

    @ExtendedAttribute
    @Transient
    private Long reasoningTokens;

    @ExtendedAttribute
    @Transient
    private String summaryMessageHash;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Long getSummaryTokens() {
        return summaryTokens;
    }

    public void setSummaryTokens(Long summaryTokens) {
        this.summaryTokens = summaryTokens;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public Long getReasoningTokens() {
        return reasoningTokens;
    }

    public void setReasoningTokens(Long reasoningTokens) {
        this.reasoningTokens = reasoningTokens;
    }

    public String getSummaryMessageHash() {
        return summaryMessageHash;
    }

    public void setSummaryMessageHash(String summaryMessageHash) {
        this.summaryMessageHash = summaryMessageHash;
    }
}
