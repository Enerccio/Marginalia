package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.github.enerccio.marginalia.domain.traits.Fulltextable;
import jakarta.persistence.*;

/**
 * A group of books, nested in other folders. The books in it (and in the folders below it) inherit the settings that are
 * set here: a blank (null) setting is inherited from the folder above, from the user settings at the top. The names of the
 * folders of one parent are unique (case insensitive), see {@code FolderService}.
 */
@Entity
@Table(name = "folders")
public class Folder extends ExtendableEntity {

    @Column(nullable = false, length = 255)
    @Fulltextable
    private String name;

    // null is the top level; the folders below a purged folder are purged with it (a folder is deleted with its content moved up)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @CleanupReference(Policy.OWNED_BY)
    private Folder parent;

    @ExtendedAttribute
    @Transient
    @Fulltextable
    private String description;

    // the settings of the books in the folder, the same as in Manuscript; none of them has a default here, null is inherited
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

    @ExtendedAttribute
    @Transient
    private String annotateSystemPrompt;

    @ExtendedAttribute
    @Transient
    private String annotatePrompt;

    @ExtendedAttribute
    @Transient
    private String impersonatePrompt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Folder getParent() {
        return parent;
    }

    public void setParent(Folder parent) {
        this.parent = parent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the language of the books of the folder, null when it is inherited
     */
    public Language getLanguage() {
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

    public String getSummaryPrompt() {
        return summaryPrompt;
    }

    public void setSummaryPrompt(String summaryPrompt) {
        this.summaryPrompt = summaryPrompt;
    }

    public String getMetaSummaryPrompt() {
        return metaSummaryPrompt;
    }

    public void setMetaSummaryPrompt(String metaSummaryPrompt) {
        this.metaSummaryPrompt = metaSummaryPrompt;
    }

    public String getRedraftSystemPrompt() {
        return redraftSystemPrompt;
    }

    public void setRedraftSystemPrompt(String redraftSystemPrompt) {
        this.redraftSystemPrompt = redraftSystemPrompt;
    }

    public String getRedraftPrompt() {
        return redraftPrompt;
    }

    public void setRedraftPrompt(String redraftPrompt) {
        this.redraftPrompt = redraftPrompt;
    }

    public String getAnnotateSystemPrompt() {
        return annotateSystemPrompt;
    }

    public void setAnnotateSystemPrompt(String annotateSystemPrompt) {
        this.annotateSystemPrompt = annotateSystemPrompt;
    }

    public String getAnnotatePrompt() {
        return annotatePrompt;
    }

    public void setAnnotatePrompt(String annotatePrompt) {
        this.annotatePrompt = annotatePrompt;
    }

    public String getImpersonatePrompt() {
        return impersonatePrompt;
    }

    public void setImpersonatePrompt(String impersonatePrompt) {
        this.impersonatePrompt = impersonatePrompt;
    }
}
