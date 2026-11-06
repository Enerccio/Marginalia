package com.github.enerccio.marginalia.extensions.reallangtranslator.model;

import java.util.UUID;

/**
 * A prompt of the extraction library: instructions for the model that reads the story and lists the names and special words of it (what to look for,
 * how each kind is written in the language). The output format is not part of it, the plugin adds it, so a prompt is only about the rules.
 */
public class ExtractionPrompt {

    private String id = UUID.randomUUID().toString();
    private String name = "";
    private String text = "";

    public ExtractionPrompt() {
    }

    public ExtractionPrompt(String id, String name, String text) {
        this.id = id;
        this.name = name;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name == null ? "" : name.strip();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text == null ? "" : text.strip();
    }

    public void setText(String text) {
        this.text = text;
    }

    public ExtractionPrompt copy() {
        return new ExtractionPrompt(id, name, text);
    }
}
