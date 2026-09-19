package com.github.enerccio.marginalia.domain.model.impl.settings;

import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
public class UserSetting extends Setting {

    @ExtendedAttribute
    @Transient
    private Long defaultModel;

    @ExtendedAttribute
    @Transient
    private Long defaultProtocol;

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

}
