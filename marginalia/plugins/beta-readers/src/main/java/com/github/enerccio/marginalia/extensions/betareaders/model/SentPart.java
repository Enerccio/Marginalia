package com.github.enerccio.marginalia.extensions.betareaders.model;

/**
 * A part of the story a message gave to the personas: its id and its number in the story then (for the line "parts 41-43 sent"). The
 * text is not kept: the personas read the part as it is in the story now.
 */
public class SentPart {

    private String id = "";
    private int number;

    public SentPart() {
    }

    public SentPart(String id, int number) {
        this.id = id;
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
}
