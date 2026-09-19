package com.github.enerccio.marginalia.domain.model.impl.settings;

import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
@DiscriminatorValue("UserSetting")
public class UserSetting extends Setting {

    @ExtendedAttribute
    @Transient
    private Long defaultModel;

    @ExtendedAttribute
    @Transient
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
}
