package com.github.enerccio.marginalia.extensions.sidequery.model;

public class SideQueryMessage {

    private boolean fromUser;
    private String contents = "";
    private String reasoning = "";
    private boolean included = true;
    private String genInfoText = "";

    public boolean isFromUser() {
        return fromUser;
    }

    public void setFromUser(boolean fromUser) {
        this.fromUser = fromUser;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public boolean isIncluded() {
        return included;
    }

    public void setIncluded(boolean included) {
        this.included = included;
    }

    public String getGenInfoText() {
        return genInfoText;
    }

    public void setGenInfoText(String genInfoText) {
        this.genInfoText = genInfoText;
    }
}