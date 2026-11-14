package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.model.PlannedScene;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.github.enerccio.marginalia.domain.traits.Fulltextable;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "manuscripts")
public class Manuscript extends ExtendableEntity {

    @Lob
    @Fulltextable
    private String name;

    @ExtendedAttribute
    @Transient
    @Fulltextable
    private String description;

    @ManyToOne
    private Lorebook lorebook;

    // null is the top level; a folder is never purged with its books, they are moved out of it first
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    @CleanupReference(Policy.WEAK)
    private Folder folder;

    @ManyToOne
    private AI ai;

    @ManyToOne
    private Protocol protocol;

    @OneToOne
    private ChatMessage activeLeaf;

    private boolean published = false;

    // whether the readers of the published book see the images of its parts (the owner always does); not set: yes
    @ExtendedAttribute
    @Transient
    private Boolean showImagesToReaders;

    // the working copy of an agent, never listed with the books of the user
    private boolean agentic = false;

    // touched only by the owner opening the book from UI, keeps modification for content changes
    @SuppressWarnings("deprecation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastOpened;

    @ExtendedAttribute
    @Transient
    private Language language;

    @ExtendedAttribute
    @Transient
    private String template;

    @ExtendedAttribute
    @Transient
    private String pov;

    @ExtendedAttribute
    @Transient
    private String tense;

    @ExtendedAttribute
    @Transient
    private String style;

    @ExtendedAttribute
    @Transient
    private String userPrompt;

    // the scenes the author plans to write, in the order they want them (see PlannerService); a list is stored as one JSON value
    @ExtendedAttribute(inject = true, injectPrefix = "plan")
    @Transient
    private List<PlannedScene> plan;

    /**
     * Legacy: the book style (see {@code PartStyleRegistry.BOOK}) was turned on for the story. Read only when {@link #partStyle} is
     * not set, never written any more.
     */
    @ExtendedAttribute
    @Transient
    private Boolean showBookStyles;

    /**
     * The id of the text style (a built in one or one of the user, see {@code PartStyleService}) the author chose for this book from the
     * menu of the story; null follows the default style of the user.
     */
    @ExtendedAttribute
    @Transient
    private String partStyle;

    @ExtendedAttribute
    @Transient
    private String summaryPrompt;

    @ExtendedAttribute
    @Transient
    private String metaSummaryPrompt;

    @ExtendedAttribute
    @Transient
    private String redraftSystemPrompt;

    @ExtendedAttribute
    @Transient
    private String redraftPrompt;

    /**
     * Added to the instructions of a redraft asked to be shorter. Part of the inherited text settings, see {@code PromptField.REDRAFT_SHORTER}.
     */
    @ExtendedAttribute
    @Transient
    private String redraftShorter;

    /**
     * Added to the instructions of a redraft asked to be longer. Part of the inherited text settings, see {@code PromptField.REDRAFT_LONGER}.
     */
    @ExtendedAttribute
    @Transient
    private String redraftLonger;

    @ExtendedAttribute
    @Transient
    private String annotateSystemPrompt;

    @ExtendedAttribute
    @Transient
    private String annotatePrompt;

    /**
     * What the model looks for when it annotates a part ("flag continuity errors"); blank is the default task of the annotate
     * prompt. Part of the inherited text settings, see {@code PromptField.ANNOTATE_INSTRUCTIONS}.
     */
    @ExtendedAttribute
    @Transient
    private String annotateInstructions;

    @ExtendedAttribute
    @Transient
    private String impersonatePrompt;

    /**
     * The most versions of a part the history keeps for the book; {@code null} does not limit them here (see
     * {@code ManuscriptService#resolveHistoryLimit}: the strictest of the book, its folders, the user and the permission wins), zero
     * keeps no history.
     */
    @ExtendedAttribute
    @Transient
    private Integer historyMaxVersions;

    // the automatic annotation of a generated part (Tools menu of the story): on, what to look for, the model to use (uuids of
    // the AI and the protocol, none is the model of the book), whether the notes are added and the redraft started and applied
    // without asking
    @ExtendedAttribute
    @Transient
    private Boolean autoAnnotate;

    @ExtendedAttribute
    @Transient
    private String autoAnnotateInstructions;

    @ExtendedAttribute
    @Transient
    private String autoAnnotateModel;

    @ExtendedAttribute
    @Transient
    private String autoAnnotateProtocol;

    @ExtendedAttribute
    @Transient
    private Boolean autoAnnotateAccept;

    @ExtendedAttribute
    @Transient
    private Boolean autoAnnotateRedraft;

    @ExtendedAttribute
    @Transient
    private Boolean autoAnnotateAcceptRedraft;

    @ExtendedAttribute
    @Transient
    private BackupStrategy backupStrategy;

    @ExtendedAttribute
    @Transient
    private String backupStrategyValue;

    @ExtendedAttribute
    @Transient
    private String backupStrategyCurrentValue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AI getAi() {
        return ai;
    }

    public void setAi(AI ai) {
        this.ai = ai;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    /**
     * The language of the book as it is shown when nothing else decides: English when the book has none. The language that
     * a book has with the folders above it is {@code ManuscriptService.getLanguage}.
     */
    public Language getLanguage() {
        return language != null ? language : Language.ENGLISH;
    }

    /**
     * @return the language the book sets itself, null when it does not (the folders above it decide)
     */
    public Language getOwnLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getPov() {
        return pov;
    }

    public void setPov(String pov) {
        this.pov = pov;
    }

    public String getTense() {
        return tense;
    }

    public void setTense(String tense) {
        this.tense = tense;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public void setUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
    }

    /**
     * The planned scenes, never null. Change them through {@code PlannerService}: a list changed here is only stored when the book is saved.
     */
    public List<PlannedScene> getPlan() {
        return plan == null ? new ArrayList<>() : plan;
    }

    public void setPlan(List<PlannedScene> plan) {
        this.plan = plan == null || plan.isEmpty() ? null : plan;
    }

    public ChatMessage getActiveLeaf() {
        return activeLeaf;
    }

    public void setActiveLeaf(ChatMessage activeLeaf) {
        this.activeLeaf = activeLeaf;
    }

    public Lorebook getLorebook() {
        return lorebook;
    }

    public void setLorebook(Lorebook lorebook) {
        this.lorebook = lorebook;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public boolean isAgentic() {
        return agentic;
    }

    public void setAgentic(boolean agentic) {
        this.agentic = agentic;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isShowImagesToReaders() {
        return showImagesToReaders == null || showImagesToReaders;
    }

    public void setShowImagesToReaders(Boolean showImagesToReaders) {
        this.showImagesToReaders = showImagesToReaders;
    }

    public Date getLastOpened() {
        return lastOpened;
    }

    public void setLastOpened(Date lastOpened) {
        this.lastOpened = lastOpened;
    }

    public Boolean getShowBookStyles() {
        return showBookStyles != null && showBookStyles;
    }

    public void setShowBookStyles(Boolean showBookStyles) {
        this.showBookStyles = showBookStyles;
    }

    public String getPartStyle() {
        return partStyle;
    }

    public void setPartStyle(String partStyle) {
        this.partStyle = partStyle;
    }

    public String getMetaSummaryPrompt() {
        return metaSummaryPrompt;
    }

    public void setMetaSummaryPrompt(String metaSummaryPrompt) {
        this.metaSummaryPrompt = metaSummaryPrompt;
    }

    public String getAnnotateSystemPrompt() {
        return annotateSystemPrompt;
    }

    public void setAnnotateSystemPrompt(String annotateSystemPrompt) {
        this.annotateSystemPrompt = annotateSystemPrompt;
    }

    public String getRedraftSystemPrompt() {
        return redraftSystemPrompt;
    }

    public void setRedraftSystemPrompt(String redraftSystemPrompt) {
        this.redraftSystemPrompt = redraftSystemPrompt;
    }

    public String getAnnotatePrompt() {
        return annotatePrompt;
    }

    public void setAnnotatePrompt(String annotatePrompt) {
        this.annotatePrompt = annotatePrompt;
    }

    public String getAnnotateInstructions() {
        return annotateInstructions;
    }

    public void setAnnotateInstructions(String annotateInstructions) {
        this.annotateInstructions = annotateInstructions;
    }

    public String getImpersonatePrompt() {
        return impersonatePrompt;
    }

    public void setImpersonatePrompt(String impersonatePrompt) {
        this.impersonatePrompt = impersonatePrompt;
    }

    public Integer getHistoryMaxVersions() {
        return historyMaxVersions;
    }

    public void setHistoryMaxVersions(Integer historyMaxVersions) {
        this.historyMaxVersions = historyMaxVersions;
    }

    public String getRedraftPrompt() {
        return redraftPrompt;
    }

    public void setRedraftPrompt(String redraftPrompt) {
        this.redraftPrompt = redraftPrompt;
    }

    public String getRedraftShorter() {
        return redraftShorter;
    }

    public void setRedraftShorter(String redraftShorter) {
        this.redraftShorter = redraftShorter;
    }

    public String getRedraftLonger() {
        return redraftLonger;
    }

    public void setRedraftLonger(String redraftLonger) {
        this.redraftLonger = redraftLonger;
    }

    public String getSummaryPrompt() {
        return summaryPrompt;
    }

    public void setSummaryPrompt(String summaryPrompt) {
        this.summaryPrompt = summaryPrompt;
    }

    public Boolean getAutoAnnotate() {
        return autoAnnotate != null && autoAnnotate;
    }

    public void setAutoAnnotate(Boolean autoAnnotate) {
        this.autoAnnotate = autoAnnotate;
    }

    public String getAutoAnnotateInstructions() {
        return autoAnnotateInstructions;
    }

    public void setAutoAnnotateInstructions(String autoAnnotateInstructions) {
        this.autoAnnotateInstructions = autoAnnotateInstructions;
    }

    public String getAutoAnnotateModel() {
        return autoAnnotateModel;
    }

    public void setAutoAnnotateModel(String autoAnnotateModel) {
        this.autoAnnotateModel = autoAnnotateModel;
    }

    public String getAutoAnnotateProtocol() {
        return autoAnnotateProtocol;
    }

    public void setAutoAnnotateProtocol(String autoAnnotateProtocol) {
        this.autoAnnotateProtocol = autoAnnotateProtocol;
    }

    public Boolean getAutoAnnotateAccept() {
        return autoAnnotateAccept != null && autoAnnotateAccept;
    }

    public void setAutoAnnotateAccept(Boolean autoAnnotateAccept) {
        this.autoAnnotateAccept = autoAnnotateAccept;
    }

    public Boolean getAutoAnnotateRedraft() {
        return autoAnnotateRedraft != null && autoAnnotateRedraft;
    }

    public void setAutoAnnotateRedraft(Boolean autoAnnotateRedraft) {
        this.autoAnnotateRedraft = autoAnnotateRedraft;
    }

    public Boolean getAutoAnnotateAcceptRedraft() {
        return autoAnnotateAcceptRedraft != null && autoAnnotateAcceptRedraft;
    }

    public void setAutoAnnotateAcceptRedraft(Boolean autoAnnotateAcceptRedraft) {
        this.autoAnnotateAcceptRedraft = autoAnnotateAcceptRedraft;
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

    public String getBackupStrategyCurrentValue() {
        return backupStrategyCurrentValue;
    }

    public void setBackupStrategyCurrentValue(String backupStrategyCurrentValue) {
        this.backupStrategyCurrentValue = backupStrategyCurrentValue;
    }
}
