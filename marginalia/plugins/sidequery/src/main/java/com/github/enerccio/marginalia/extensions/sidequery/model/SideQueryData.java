package com.github.enerccio.marginalia.extensions.sidequery.model;

import java.util.ArrayList;
import java.util.List;

public class SideQueryData {

    private List<SideQuerySession> sessions = new ArrayList<>();
    private int activeTab = 0;

    public SideQueryData() {
        sessions.add(new SideQuerySession());
    }

    public List<SideQuerySession> getSessions() {
        return sessions;
    }

    public void setSessions(List<SideQuerySession> sessions) {
        this.sessions = sessions;
    }

    public int getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(int activeTab) {
        this.activeTab = activeTab;
    }
}