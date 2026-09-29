package com.github.enerccio.marginalia.extensions.sidequery.model;

public class SideQueryOptions {

    private boolean includeLorebook = false;
    private boolean includeMessages = false;
    private int messagesFrom = 0;
    private int messagesTo = 5;

    public boolean isIncludeLorebook() {
        return includeLorebook;
    }

    public void setIncludeLorebook(boolean includeLorebook) {
        this.includeLorebook = includeLorebook;
    }

    public boolean isIncludeMessages() {
        return includeMessages;
    }

    public void setIncludeMessages(boolean includeMessages) {
        this.includeMessages = includeMessages;
    }

    public int getMessagesFrom() {
        return messagesFrom;
    }

    public void setMessagesFrom(int messagesFrom) {
        this.messagesFrom = messagesFrom;
    }

    public int getMessagesTo() {
        return messagesTo;
    }

    public void setMessagesTo(int messagesTo) {
        this.messagesTo = messagesTo;
    }
}