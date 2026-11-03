package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

/**
 * A part of the story that was processed: its uuid, a hash of its text at that time and when. The list of these is
 * information for the user (a part whose text changed since is shown as such); the cursor and the sources of the items
 * are what decide anything.
 */
public class ProcessedPart {

    private String uuid;
    private String hash;
    private long time;

    public ProcessedPart() {
    }

    public ProcessedPart(String uuid, String hash, long time) {
        this.uuid = uuid;
        this.hash = hash;
        this.time = time;
    }

    public String getUuid() {
        return uuid;
    }

    public String getHash() {
        return hash;
    }

    public long getTime() {
        return time;
    }
}
