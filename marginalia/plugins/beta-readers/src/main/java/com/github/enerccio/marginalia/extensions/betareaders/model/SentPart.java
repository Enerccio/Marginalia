package com.github.enerccio.marginalia.extensions.betareaders.model;

/**
 * A part of the story a message sent to the personas: its id and a hash of its text at that moment, so a part that was edited
 * afterwards counts as new again.
 */
public class SentPart {

    private String id = "";
    private String hash = "";

    public SentPart() {
    }

    public SentPart(String id, String hash) {
        this.id = id;
        this.hash = hash;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
