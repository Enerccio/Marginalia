package com.github.enerccio.marginalia.domain.model.impl.settings;

import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("AppSettings")
public class AppSettings extends Setting {

    @Transient
    @ExtendedAttribute
    private Integer dbVersion = 1;

    @Transient
    @ExtendedAttribute
    private Integer appVersion = 1;

    public Integer getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(Integer dbVersion) {
        this.dbVersion = dbVersion;
    }

    public Integer getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(Integer appVersion) {
        this.appVersion = appVersion;
    }
}
