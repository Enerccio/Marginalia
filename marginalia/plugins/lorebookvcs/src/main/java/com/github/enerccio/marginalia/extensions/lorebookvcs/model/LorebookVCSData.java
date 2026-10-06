package com.github.enerccio.marginalia.extensions.lorebookvcs.model;

import java.util.HashMap;
import java.util.Map;

public class LorebookVCSData {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.lorebookvcs";

    private String lorebookUuid;
    private Map<String, LoreEntryVCSData> entries = new HashMap<>();

    public String getLorebookUuid() { return lorebookUuid; }
    public void setLorebookUuid(String lorebookUuid) { this.lorebookUuid = lorebookUuid; }

    public Map<String, LoreEntryVCSData> getEntries() { return entries; }
    public void setEntries(Map<String, LoreEntryVCSData> entries) { this.entries = entries; }

    public LoreEntryVCSData getOrCreateEntry(String entryUuid) {
        return entries.computeIfAbsent(entryUuid, k -> {
            LoreEntryVCSData d = new LoreEntryVCSData();
            d.setEntryUuid(k);
            return d;
        });
    }
}