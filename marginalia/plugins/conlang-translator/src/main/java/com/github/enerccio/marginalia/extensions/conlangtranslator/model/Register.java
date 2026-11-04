package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

import java.util.UUID;

/**
 * A register of a language (formal, neutral, soft, colloquial, vulgar, archaic, poetic...): the model chooses the form of a word by the
 * register the speaker and the situation ask for.
 */
public class Register {

    private String id = UUID.randomUUID().toString();
    private String name = "";
    private String note = "";

    public Register() {
    }

    public Register(String name, String note) {
        this.name = name;
        this.note = note;
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
}
