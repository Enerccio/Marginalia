package com.github.enerccio.marginalia.domain.model.impl.settings;

import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.google.gson.JsonObject;
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
    private String defaultMetaSummaryPrompt;

    /**
     * The system prompt of the agents of the user that have none of their own; blank is the one of the application
     * settings, and then {@code Defaults.DEFAULT_AGENT_PROMPT}.
     */
    @ExtendedAttribute
    @Transient
    private String defaultAgentPrompt;

    /**
     * The compaction prompt of the agents of the user that have none of their own; blank is the one of the application
     * settings, and then {@code Defaults.DEFAULT_AGENT_COMPACTION_PROMPT}.
     */
    @ExtendedAttribute
    @Transient
    private String defaultAgentCompactionPrompt;

    @ExtendedAttribute
    @Transient
    private BackupStrategy backupStrategy;

    @ExtendedAttribute
    @Transient
    private String backupStrategyValue;

    /**
     * Code of the language of the UI, {@code null} for the default one.
     */
    @ExtendedAttribute
    @Transient
    private String language;

    /**
     * Where the user left the workspace (selected tab, filters, the open book, ...), written by the workspace autosave
     * as the {@code Restorable} state of the workspace and read back after the login.
     */
    @ExtendedAttribute(inject = true, injectPrefix = "workspaceState")
    @Transient
    private JsonObject workspaceState = new JsonObject();

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

    public String getDefaultMetaSummaryPrompt() {
        return defaultMetaSummaryPrompt;
    }

    public void setDefaultMetaSummaryPrompt(String defaultMetaSummaryPrompt) {
        this.defaultMetaSummaryPrompt = defaultMetaSummaryPrompt;
    }

    public String getDefaultAgentCompactionPrompt() {
        return defaultAgentCompactionPrompt;
    }

    public void setDefaultAgentCompactionPrompt(String defaultAgentCompactionPrompt) {
        this.defaultAgentCompactionPrompt = defaultAgentCompactionPrompt;
    }

    public String getDefaultAgentPrompt() {
        return defaultAgentPrompt;
    }

    public void setDefaultAgentPrompt(String defaultAgentPrompt) {
        this.defaultAgentPrompt = defaultAgentPrompt;
    }

    public BackupStrategy getBackupStrategy() {
        return backupStrategy;
    }

    public void setBackupStrategy(BackupStrategy backupStrategy) {
        this.backupStrategy = backupStrategy;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getBackupStrategyValue() {
        return backupStrategyValue;
    }

    public void setBackupStrategyValue(String backupStrategyValue) {
        this.backupStrategyValue = backupStrategyValue;
    }

    public JsonObject getWorkspaceState() {
        return workspaceState;
    }

    public void setWorkspaceState(JsonObject workspaceState) {
        this.workspaceState = workspaceState == null ? new JsonObject() : workspaceState;
    }
}
