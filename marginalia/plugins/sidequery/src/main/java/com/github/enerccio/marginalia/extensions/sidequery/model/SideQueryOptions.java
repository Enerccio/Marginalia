package com.github.enerccio.marginalia.extensions.sidequery.model;

public class SideQueryOptions {

    private boolean includeLorebook = false;
    private boolean includeMessages = false;
    /**
     * Range of story parts, numbered from 1 like in the outline. Null in data stored by older versions, which used
     * the 0-based {@link #messagesFrom} / {@link #messagesTo} instead.
     */
    private Integer partsFrom;
    private Integer partsTo;
    // legacy 0-based range, only read
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

    public int getPartsFrom() {
        return partsFrom != null ? partsFrom : messagesFrom + 1;
    }

    public void setPartsFrom(int partsFrom) {
        this.partsFrom = partsFrom;
    }

    public int getPartsTo() {
        return partsTo != null ? partsTo : messagesTo + 1;
    }

    public void setPartsTo(int partsTo) {
        this.partsTo = partsTo;
    }
}
