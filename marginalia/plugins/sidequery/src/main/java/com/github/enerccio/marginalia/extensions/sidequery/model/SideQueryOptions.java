package com.github.enerccio.marginalia.extensions.sidequery.model;

import java.util.ArrayList;
import java.util.List;

public class SideQueryOptions {

    private boolean includeLorebook = false;
    private boolean includeMessages = false;
    /**
     * Range of story parts, numbered from 1 like in the outline. Null in data stored by older versions, which used
     * the 0-based {@link #messagesFrom} / {@link #messagesTo} instead.
     */
    private Integer partsFrom;
    private Integer partsTo;
    /**
     * Sources of data about the book the user checked, other extensions offer them (see {@code IPCService}), as
     * {@link #sourceId}. Kept even when the extension is gone, they are used again when it comes back.
     */
    private List<String> manuscriptSources = new ArrayList<>();
    /** Sources of data about story parts, added to the text of each included part. */
    private List<String> messageSources = new ArrayList<>();
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

    /**
     * Identifies a data source of an extension in the options.
     */
    public static String sourceId(String bundleIdentifier, String providerKey) {
        return bundleIdentifier + "::" + providerKey;
    }

    public List<String> getManuscriptSources() {
        if (manuscriptSources == null) {
            manuscriptSources = new ArrayList<>();
        }
        return manuscriptSources;
    }

    public List<String> getMessageSources() {
        if (messageSources == null) {
            messageSources = new ArrayList<>();
        }
        return messageSources;
    }
}
