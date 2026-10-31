package com.github.enerccio.marginalia.extensions.betareaders.model;

/**
 * A part of the story a message sent to the personas: its id, a hash of its text at that moment (so a part that was edited
 * afterwards counts as new again) and its number in the story then (for the line "parts 41-43 sent").
 */
public class SentPart {

    private String id = "";
    private String hash = "";
    private int number;

    public SentPart() {
    }

    public SentPart(String id, String hash, int number) {
        this.id = id;
        this.hash = hash;
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
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
