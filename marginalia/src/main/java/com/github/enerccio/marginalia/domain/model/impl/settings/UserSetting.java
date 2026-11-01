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

import java.util.ArrayList;
import java.util.List;

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

    /**
     * The first day (ISO date) the time of the writing statistics is recorded from; the days before it have counts rebuilt from the
     * history of the parts but no time. Set once by {@code StatisticsBackfillMigration}; null: the time of every day is known.
     */
    @ExtendedAttribute
    @Transient
    private String statisticsSince;

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
    private String defaultRedraftSystemPrompt;

    @ExtendedAttribute
    @Transient
    private String defaultRedraftPrompt;

    @ExtendedAttribute
    @Transient
    private String defaultAnnotateSystemPrompt;

    @ExtendedAttribute
    @Transient
    private String defaultAnnotatePrompt;

    @ExtendedAttribute
    @Transient
    private String defaultImpersonatePrompt;

    /**
     * The AI that annotates a part when the author does not choose another one for the run; annotating is analysis, which is
     * usually not done by the model that writes the prose. Null is the AI of the book.
     */
    @ExtendedAttribute
    @Transient
    @CleanupReference(value = Policy.WEAK, target = AI.class)
    private Long defaultAnnotateModel;

    @ExtendedAttribute
    @Transient
    @CleanupReference(value = Policy.WEAK, target = Protocol.class)
    private Long defaultAnnotateProtocol;

    /**
     * The story shows the notes of a part in a column left of its text instead of in a popover at the passage (a reading
     * preference of the desktop; the phone has no room for a margin).
     */
    @ExtendedAttribute
    @Transient
    private Boolean annotationsInMargin;

    /**
     * The arrangement of the tabs of the story editor ({@code TabDock.getLayout()}: columns, sheets, sizes, minimized tabs and
     * the side of their bar) that books open with when they have none of their own; empty when the user saved none.
     */
    @ExtendedAttribute(inject = true, injectPrefix = "defaultStoryLayout")
    @Transient
    private JsonObject defaultStoryLayout = new JsonObject();

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
     * The uuids of the books the user marked as favorites (the star of the list of books), which the list shows together
     * in the virtual folder *Favorites*. A book that is gone is dropped when the list is read, see
     * {@code ManuscriptService.getFavorites}.
     */
    @ExtendedAttribute(inject = true, injectPrefix = "favoriteBooks")
    @Transient
    private List<String> favoriteBooks = new ArrayList<>();

    /**
     * The keys of the user for the keyboard shortcuts of the story editor: action id to key combination, only the ones
     * that differ from the default of the action (an empty text is no key), see {@code KeyBindings}.
     */
    @ExtendedAttribute(inject = true, injectPrefix = "keyBindings")
    @Transient
    private JsonObject keyBindings = new JsonObject();

    /**
     * Whether the help assistant may look at the books of the user (folders, titles, descriptions, the settings they
     * resolve to, a search of their text), asked in the panel of the assistant. Off until the user turns it on: what it
     * reads is sent to the AI of the administrator that answers.
     */
    @ExtendedAttribute
    @Transient
    private Boolean helpAssistantBooks = false;

    /**
     * The most versions of a part the history keeps for the books of the user that set no limit themselves; {@code null} does not limit them here (see
     * {@code ManuscriptService#resolveHistoryLimit}: the strictest of the book, its folders, the user and the permission wins), zero
     * keeps no history.
     */
    @ExtendedAttribute
    @Transient
    private Integer historyMaxVersions;

    /**
     * The words the author wants to write on a day, over all the books, for the goal and the streak of the statistics; null or
     * zero is no goal.
     */
    @ExtendedAttribute
    @Transient
    private Integer dailyGoalWords;

    /**
     * The minutes the author wants to spend editing on a day, over all the books; null or zero is no goal.
     */
    @ExtendedAttribute
    @Transient
    private Integer dailyGoalMinutes;

    public Integer getDailyGoalWords() {
        return dailyGoalWords;
    }

    public void setDailyGoalWords(Integer dailyGoalWords) {
        this.dailyGoalWords = dailyGoalWords;
    }

    public Integer getDailyGoalMinutes() {
        return dailyGoalMinutes;
    }

    public void setDailyGoalMinutes(Integer dailyGoalMinutes) {
        this.dailyGoalMinutes = dailyGoalMinutes;
    }

    public boolean isHelpAssistantBooks() {
        return helpAssistantBooks != null && helpAssistantBooks;
    }

    public void setHelpAssistantBooks(Boolean helpAssistantBooks) {
        this.helpAssistantBooks = helpAssistantBooks;
    }

    public Integer getHistoryMaxVersions() {
        return historyMaxVersions;
    }

    public void setHistoryMaxVersions(Integer historyMaxVersions) {
        this.historyMaxVersions = historyMaxVersions;
    }

    public String getStatisticsSince() {
        return statisticsSince;
    }

    public void setStatisticsSince(String statisticsSince) {
        this.statisticsSince = statisticsSince;
    }

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

    public String getDefaultAnnotateSystemPrompt() {
        return defaultAnnotateSystemPrompt;
    }

    public void setDefaultAnnotateSystemPrompt(String defaultAnnotateSystemPrompt) {
        this.defaultAnnotateSystemPrompt = defaultAnnotateSystemPrompt;
    }

    public String getDefaultRedraftSystemPrompt() {
        return defaultRedraftSystemPrompt;
    }

    public void setDefaultRedraftSystemPrompt(String defaultRedraftSystemPrompt) {
        this.defaultRedraftSystemPrompt = defaultRedraftSystemPrompt;
    }

    public String getDefaultAnnotatePrompt() {
        return defaultAnnotatePrompt;
    }

    public void setDefaultAnnotatePrompt(String defaultAnnotatePrompt) {
        this.defaultAnnotatePrompt = defaultAnnotatePrompt;
    }

    public String getDefaultImpersonatePrompt() {
        return defaultImpersonatePrompt;
    }

    public void setDefaultImpersonatePrompt(String defaultImpersonatePrompt) {
        this.defaultImpersonatePrompt = defaultImpersonatePrompt;
    }

    public Long getDefaultAnnotateModel() {
        return defaultAnnotateModel;
    }

    public void setDefaultAnnotateModel(Long defaultAnnotateModel) {
        this.defaultAnnotateModel = defaultAnnotateModel;
    }

    public Long getDefaultAnnotateProtocol() {
        return defaultAnnotateProtocol;
    }

    public void setDefaultAnnotateProtocol(Long defaultAnnotateProtocol) {
        this.defaultAnnotateProtocol = defaultAnnotateProtocol;
    }

    public boolean isAnnotationsInMargin() {
        return annotationsInMargin != null && annotationsInMargin;
    }

    public void setAnnotationsInMargin(Boolean annotationsInMargin) {
        this.annotationsInMargin = annotationsInMargin;
    }

    public JsonObject getDefaultStoryLayout() {
        return defaultStoryLayout;
    }

    public void setDefaultStoryLayout(JsonObject defaultStoryLayout) {
        this.defaultStoryLayout = defaultStoryLayout == null ? new JsonObject() : defaultStoryLayout;
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

    public List<String> getFavoriteBooks() {
        return favoriteBooks;
    }

    public void setFavoriteBooks(List<String> favoriteBooks) {
        this.favoriteBooks = favoriteBooks == null ? new ArrayList<>() : new ArrayList<>(favoriteBooks);
    }

    public JsonObject getKeyBindings() {
        return keyBindings;
    }

    public void setKeyBindings(JsonObject keyBindings) {
        this.keyBindings = keyBindings == null ? new JsonObject() : keyBindings;
    }
}
