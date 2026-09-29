package com.github.enerccio.marginalia.extensions.sidequery.model;

import java.util.ArrayList;
import java.util.List;

public class SideQuerySession {

    private String name = "Tab 1";
    private boolean isManuallyRenamed = false;
    private SideQueryOptions options = new SideQueryOptions();
    private List<SideQueryMessage> messages = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isManuallyRenamed() {
        return isManuallyRenamed;
    }

    public void setManuallyRenamed(boolean manuallyRenamed) {
        this.isManuallyRenamed = manuallyRenamed;
    }

    public SideQueryOptions getOptions() {
        return options;
    }

    public void setOptions(SideQueryOptions options) {
        this.options = options;
    }

    public List<SideQueryMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<SideQueryMessage> messages) {
        this.messages = messages;
    }
}