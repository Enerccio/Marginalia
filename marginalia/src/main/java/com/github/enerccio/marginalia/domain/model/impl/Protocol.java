package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.*;
import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.DiscriminatorOptions;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="protocols", indexes = {
        @Index(name = "protocol_is_deleted_idx", columnList = "is_deleted"),
        @Index(name = "protocol_user_id_ix", columnList = "userId")
})
public abstract class Protocol extends ExtendableEntity {

    @Lob
    private String name;

    @Enumerated(value = EnumType.ORDINAL)
    private ProtocolType protocolType;

    private int maxTokens;
    private int replyTokens;

    @Transient
    @ExtendedAttribute
    private Boolean temperatureEnabled;

    @Transient
    @ExtendedAttribute
    private Double temperature;

    @Transient
    @ExtendedAttribute
    private Boolean topPEnabled;

    @Transient
    @ExtendedAttribute
    private Double topP;

    @Transient
    @ExtendedAttribute
    private Boolean frequencyPenaltyEnabled;

    @Transient
    @ExtendedAttribute
    private Double frequencyPenalty;

    @Transient
    @ExtendedAttribute
    private Boolean presencePenaltyEnabled;

    @Transient
    @ExtendedAttribute
    private Double presencePenalty;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public int getReplyTokens() {
        return replyTokens;
    }

    public void setReplyTokens(int replyTokens) {
        this.replyTokens = replyTokens;
    }

    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }

    public Double getPresencePenalty() {
        return presencePenalty;
    }

    public void setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
    }

    public Boolean getTemperatureEnabled() {
        return temperatureEnabled == null ? Boolean.FALSE : temperatureEnabled;
    }

    public void setTemperatureEnabled(Boolean temperatureEnabled) {
        this.temperatureEnabled = temperatureEnabled;
    }

    public Boolean getTopPEnabled() {
        return topPEnabled == null ? Boolean.FALSE : topPEnabled;
    }

    public void setTopPEnabled(Boolean topPEnabled) {
        this.topPEnabled = topPEnabled;
    }

    public Boolean getFrequencyPenaltyEnabled() {
        return frequencyPenaltyEnabled == null ? Boolean.FALSE : frequencyPenaltyEnabled;
    }

    public void setFrequencyPenaltyEnabled(Boolean frequencyPenaltyEnabled) {
        this.frequencyPenaltyEnabled = frequencyPenaltyEnabled;
    }

    public Boolean getPresencePenaltyEnabled() {
        return presencePenaltyEnabled == null ? Boolean.FALSE : presencePenaltyEnabled;
    }

    public void setPresencePenaltyEnabled(Boolean presencePenaltyEnabled) {
        this.presencePenaltyEnabled = presencePenaltyEnabled;
    }
}
