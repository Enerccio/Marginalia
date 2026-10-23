package com.github.enerccio.marginalia.domain.model.impl.settings;

import com.github.enerccio.marginalia.domain.collections.ThemeMode;
import com.github.enerccio.marginalia.domain.collections.WebhookFormat;
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

    @ExtendedAttribute
    @Transient
    private String defaultRedraftPrompt;

    /**
     * The system prompt of the agents of the user that have none of their own; blank is the one of the application
     * settings, and then {@code Defaults.DEFAULT_AGENT_PROMPT}.
     */
    @ExtendedAttribute
    @Transient
    private String defaultAgentPrompt;

    /**
     * The URL the application sends a short message to when an agent of the user needs them (asks, finished, failed) and the
     * user has the application closed, see {@code AgentWebhookService}. Blank is no message. The URL may hold a secret, it is not logged.
     */
    @ExtendedAttribute
    @Transient
    private String agentWebhookUrl;

    /**
     * The body the service behind {@link #agentWebhookUrl} expects; null is {@code PLAIN}.
     */
    @ExtendedAttribute
    @Transient
    private WebhookFormat agentWebhookFormat;

    /**
     * Whether the message is sent also while the user has the application open somewhere; the default is only when they have not.
     */
    @ExtendedAttribute
    @Transient
    private Boolean agentWebhookAlways;

    /**
     * The instructions ("agents.md") of the agents of the user that have none of their own; blank is no instructions.
     */
    @ExtendedAttribute
    @Transient
    private String defaultAgentInstructions;

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
     * The color scheme of the UI, {@code null} for the default one ({@link ThemeMode#DARK}).
     */
    @ExtendedAttribute
    @Transient
    private ThemeMode themeMode;

    /**
     * Code of the language of the UI, {@code null} for the default one.
     */
    @ExtendedAttribute
    @Transient
    private String language;

    /**
     * How many parts of a book the story and the reader load at a time (the story the last ones, the reader the first
     * ones, the rest as the user scrolls); {@code null} or less than one loads them all.
     */
    @ExtendedAttribute
    @Transient
    private Integer storyPartsLimit;

    /**
     * Where the user left the workspace (selected tab, filters, the open book, ...), written by the workspace autosave
     * as the {@code Restorable} state of the workspace and read back after the login.
     */
    @ExtendedAttribute(inject = true, injectPrefix = "workspaceState")
    @Transient
    private JsonObject workspaceState = new JsonObject();

    /**
     * The keys of the user for the keyboard shortcuts of the story editor: action id to key combination, only the ones
     * that differ from the default of the action (an empty text is no key), see {@code KeyBindings}.
     */
    @ExtendedAttribute(inject = true, injectPrefix = "keyBindings")
    @Transient
    private JsonObject keyBindings = new JsonObject();

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

    public String getDefaultRedraftPrompt() {
        return defaultRedraftPrompt;
    }

    public void setDefaultRedraftPrompt(String defaultRedraftPrompt) {
        this.defaultRedraftPrompt = defaultRedraftPrompt;
    }

    public String getDefaultAgentCompactionPrompt() {
        return defaultAgentCompactionPrompt;
    }

    public void setDefaultAgentCompactionPrompt(String defaultAgentCompactionPrompt) {
        this.defaultAgentCompactionPrompt = defaultAgentCompactionPrompt;
    }

    public String getAgentWebhookUrl() {
        return agentWebhookUrl;
    }

    public void setAgentWebhookUrl(String agentWebhookUrl) {
        this.agentWebhookUrl = agentWebhookUrl;
    }

    public WebhookFormat getAgentWebhookFormat() {
        return agentWebhookFormat;
    }

    public void setAgentWebhookFormat(WebhookFormat agentWebhookFormat) {
        this.agentWebhookFormat = agentWebhookFormat;
    }

    public Boolean getAgentWebhookAlways() {
        return agentWebhookAlways;
    }

    public void setAgentWebhookAlways(Boolean agentWebhookAlways) {
        this.agentWebhookAlways = agentWebhookAlways;
    }

    public String getDefaultAgentInstructions() {
        return defaultAgentInstructions;
    }

    public void setDefaultAgentInstructions(String defaultAgentInstructions) {
        this.defaultAgentInstructions = defaultAgentInstructions;
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

    public ThemeMode getThemeMode() {
        return themeMode == null ? ThemeMode.DARK : themeMode;
    }

    public void setThemeMode(ThemeMode themeMode) {
        this.themeMode = themeMode;
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

    public Integer getStoryPartsLimit() {
        return storyPartsLimit;
    }

    public void setStoryPartsLimit(Integer storyPartsLimit) {
        this.storyPartsLimit = storyPartsLimit;
    }

    public JsonObject getWorkspaceState() {
        return workspaceState;
    }

    public void setWorkspaceState(JsonObject workspaceState) {
        this.workspaceState = workspaceState == null ? new JsonObject() : workspaceState;
    }

    public JsonObject getKeyBindings() {
        return keyBindings;
    }

    public void setKeyBindings(JsonObject keyBindings) {
        this.keyBindings = keyBindings == null ? new JsonObject() : keyBindings;
    }
}
