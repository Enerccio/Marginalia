package com.github.enerccio.marginalia.extensions.sidequery.model;

public class SideQuerySetting {

    private String initialQuery = "[System Instruction: You are a precise context-parsing AI assistant.]";
    private String instructionsBeforeUser = "";
    private Long selectedAiId = null;
    private Long selectedProtocolId = null;
    private boolean enableAiTabNames = false;

    public String getInitialQuery() {
        return initialQuery;
    }

    public void setInitialQuery(String initialQuery) {
        this.initialQuery = initialQuery;
    }

    public String getInstructionsBeforeUser() {
        return instructionsBeforeUser;
    }

    public void setInstructionsBeforeUser(String instructionsBeforeUser) {
        this.instructionsBeforeUser = instructionsBeforeUser;
    }

    public Long getSelectedAiId() {
        return selectedAiId;
    }

    public void setSelectedAiId(Long selectedAiId) {
        this.selectedAiId = selectedAiId;
    }

    public Long getSelectedProtocolId() {
        return selectedProtocolId;
    }

    public void setSelectedProtocolId(Long selectedProtocolId) {
        this.selectedProtocolId = selectedProtocolId;
    }

    public boolean isEnableAiTabNames() {
        return enableAiTabNames;
    }

    public void setEnableAiTabNames(boolean enableAiTabNames) {
        this.enableAiTabNames = enableAiTabNames;
    }
}