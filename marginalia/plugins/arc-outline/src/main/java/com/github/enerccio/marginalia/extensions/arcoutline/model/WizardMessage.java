package com.github.enerccio.marginalia.extensions.arcoutline.model;

/**
 * A message of the conversation of the plot wizard.
 */
public class WizardMessage {

    private boolean fromUser;
    private String text;
    /** What the model thought before it answered, null for none. */
    private String reasoning;
    /** The message is a summary of the chat before it: the model gets it and what comes after, not the messages before. */
    private boolean summary;

    public WizardMessage() {
    }

    public WizardMessage(boolean fromUser, String text, String reasoning) {
        this.fromUser = fromUser;
        this.text = text;
        this.reasoning = reasoning;
    }

    public boolean isSummary() {
        return summary;
    }

    public void setSummary(boolean summary) {
        this.summary = summary;
    }

    public WizardMessage copy() {
        WizardMessage copy = new WizardMessage(fromUser, text, reasoning);
        copy.summary = summary;
        return copy;
    }

    public boolean isFromUser() {
        return fromUser;
    }

    public void setFromUser(boolean fromUser) {
        this.fromUser = fromUser;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }
}
