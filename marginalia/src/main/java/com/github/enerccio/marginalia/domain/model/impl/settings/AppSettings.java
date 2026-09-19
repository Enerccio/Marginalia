package com.github.enerccio.marginalia.domain.model.impl.settings;

import com.github.enerccio.marginalia.domain.model.Setting;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AppSettings extends Setting {

    private Integer dbVersion = 1;
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
