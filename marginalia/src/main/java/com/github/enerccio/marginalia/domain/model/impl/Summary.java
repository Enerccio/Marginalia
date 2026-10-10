package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.collections.SummaryType;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.github.enerccio.marginalia.domain.traits.Fulltextable;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "summaries")
public class Summary extends ExtendableEntity {

    @ExtendedAttribute
    @Fulltextable
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

    @ExtendedAttribute
    @Transient
    private SummaryType summaryType;

    /**
     * Meta summary only: uuid of the summary where the summaries it merged end, the summary chain continues with it.
     */
    @ExtendedAttribute
    @Transient
    private String nextSummaryUuid;

    /**
     * Meta summary only: serialized extended content of the summary this meta summary replaced on its message,
     * deleting the meta summary can restore it. Nests when meta summaries are made of meta summaries.
     */
    @ExtendedAttribute
    @Transient
    private String replacedSummary;

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

    public SummaryType getSummaryType() {
        return summaryType == null ? SummaryType.SUMMARY : summaryType;
    }

    public void setSummaryType(SummaryType summaryType) {
        this.summaryType = summaryType;
    }

    public String getNextSummaryUuid() {
        return nextSummaryUuid;
    }

    public void setNextSummaryUuid(String nextSummaryUuid) {
        this.nextSummaryUuid = nextSummaryUuid;
    }

    public String getReplacedSummary() {
        return replacedSummary;
    }

    public void setReplacedSummary(String replacedSummary) {
        this.replacedSummary = replacedSummary;
    }
}
