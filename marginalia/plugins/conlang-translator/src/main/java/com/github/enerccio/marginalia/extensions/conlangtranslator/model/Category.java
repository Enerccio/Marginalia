package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

import java.util.UUID;

/**
 * A semantic group of the lexicon (People & Social, Military & War, Numbers...), in the author's order. The lexicon is organized, shown
 * and sent to the model under these headings.
 */
public class Category {

    private String id = UUID.randomUUID().toString();
    private String name = "";
    private String note = "";
    /** Whether the whole category goes to the model even when the lexicon is too big to be sent whole (particles, numbers, kinship). */
    private boolean alwaysSend;

    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note == null ? "" : note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isAlwaysSend() {
        return alwaysSend;
    }

    public void setAlwaysSend(boolean alwaysSend) {
        this.alwaysSend = alwaysSend;
    }
}
