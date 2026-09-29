package com.github.enerccio.marginalia.extensions.reviewer.model;

import java.util.TreeMap;

public class ReviewerSettings {
    
    public static final String KEY = "com.github.enerccio.marginalia.extensions.reviewer";

    private TreeMap<String, ReviewerSetting> settings = new TreeMap<>();
    private String defaultSetting;

    public TreeMap<String, ReviewerSetting> getSettings() {
        return settings;
    }

    public void setSettings(TreeMap<String, ReviewerSetting> settings) {
        this.settings = settings;
    }

    public String getDefaultSetting() {
        return defaultSetting;
    }

    public void setDefaultSetting(String defaultSetting) {
        this.defaultSetting = defaultSetting;
    }
}