package com.github.enerccio.marginalia.domain.model.impl.settings;

import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
@DiscriminatorValue("UserSetting")
public class UserSetting extends Setting {

    @ExtendedAttribute
    @Transient
    @CleanupReference(value = Policy.WEAK, target = AI.class)
    private Long defaultModel;

    @ExtendedAttribute
    @Transient
    @CleanupReference(value = Policy.WEAK, target = Protocol.class)
    private Long defaultProtocol;

    // templates

    @ExtendedAttribute
    @Transient
    private String masterTemplate;

    @ExtendedAttribute
    @Transient
    private String defaultPov;

    @ExtendedAttribute
    @Transient
    private String defaultTense;

    @ExtendedAttribute
    @Transient
    private String defaultStyle;

    @ExtendedAttribute
    @Transient
    private String defaultUserPrompt;

    @ExtendedAttribute
    @Transient
    private String defaultSummaryPrompt;

    @ExtendedAttribute
    @Transient
    private BackupStrategy backupStrategy;

    @ExtendedAttribute
    @Transient
    private String backupStrategyValue;

    public Long getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(Long defaultModel) {
        this.defaultModel = defaultModel;
    }

    public Long getDefaultProtocol() {
        return defaultProtocol;
    }

    public void setDefaultProtocol(Long defaultProtocol) {
        this.defaultProtocol = defaultProtocol;
    }

    public String getMasterTemplate() {
        return masterTemplate;
    }

    public void setMasterTemplate(String masterTemplate) {
        this.masterTemplate = masterTemplate;
    }

    public String getDefaultPov() {
        return defaultPov;
    }

    public void setDefaultPov(String defaultPov) {
        this.defaultPov = defaultPov;
    }

    public String getDefaultTense() {
        return defaultTense;
    }

    public void setDefaultTense(String defaultTense) {
        this.defaultTense = defaultTense;
    }

    public String getDefaultStyle() {
        return defaultStyle;
    }

    public void setDefaultStyle(String defaultStyle) {
        this.defaultStyle = defaultStyle;
    }

    public String getDefaultUserPrompt() {
        return defaultUserPrompt;
    }

    public void setDefaultUserPrompt(String defaultUserPrompt) {
        this.defaultUserPrompt = defaultUserPrompt;
    }

    public String getDefaultSummaryPrompt() {
        return defaultSummaryPrompt;
    }

    public void setDefaultSummaryPrompt(String defaultSummaryPrompt) {
        this.defaultSummaryPrompt = defaultSummaryPrompt;
    }

    public BackupStrategy getBackupStrategy() {
        return backupStrategy;
    }

    public void setBackupStrategy(BackupStrategy backupStrategy) {
        this.backupStrategy = backupStrategy;
    }

    public String getBackupStrategyValue() {
        return backupStrategyValue;
    }

    public void setBackupStrategyValue(String backupStrategyValue) {
        this.backupStrategyValue = backupStrategyValue;
    }
}
