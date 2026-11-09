package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

import java.util.UUID;

/**
 * One titled section of the rules of a language (phonology, the grammar, how words are made, the compact instruction block...). The
 * rules are free text the plugin does not parse; the author decides who reads the section.
 */
public class RuleSection {

    private String id = UUID.randomUUID().toString();
    private String title = "";
    private String text = "";
    private Audience audience = Audience.MODEL;

    public RuleSection() {
    }

    public RuleSection(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public RuleSection(String title, String text, Audience audience) {
        this(title, text);
        this.audience = audience;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text == null ? "" : text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Audience getAudience() {
        return audience == null ? Audience.MODEL : audience;
    }

    public void setAudience(Audience audience) {
        this.audience = audience;
    }
}
