package com.github.enerccio.marginalia.extensions.sidequery.model;

import java.util.TreeMap;

public class SideQuerySettings {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.sidequery";

    private TreeMap<String, SideQuerySetting> settings = new TreeMap<>();
    private String defaultSetting;

    public TreeMap<String, SideQuerySetting> getSettings() {
        return settings;
    }

    public void setSettings(TreeMap<String, SideQuerySetting> settings) {
        this.settings = settings;
    }

    public String getDefaultSetting() {
        return defaultSetting;
    }

    public void setDefaultSetting(String defaultSetting) {
        this.defaultSetting = defaultSetting;
    }
}