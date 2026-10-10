package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "manuscripts")
public class Manuscript extends ExtendableEntity {

    @Lob
    private String name;

    @ExtendedAttribute
    private String description;

    @ManyToOne
    private Lorebook lorebook;

    @ManyToOne
    private AI ai;

    @ManyToOne
    private Protocol protocol;

    @OneToOne
    private ChatMessage activeLeaf;

    private boolean published = false;

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

    @ExtendedAttribute
    @Transient
    private Boolean showBookStyles;

    @ExtendedAttribute
    @Transient
    private String summaryPrompt;

    @ExtendedAttribute
    @Transient
    private String metaSummaryPrompt;

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

    public Language getLanguage() {
        return language != null ? language : Language.ENGLISH;
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

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
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

    public String getMetaSummaryPrompt() {
        return metaSummaryPrompt;
    }

    public void setMetaSummaryPrompt(String metaSummaryPrompt) {
        this.metaSummaryPrompt = metaSummaryPrompt;
    }

    public String getSummaryPrompt() {
        return summaryPrompt;
    }

    public void setSummaryPrompt(String summaryPrompt) {
        this.summaryPrompt = summaryPrompt;
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
