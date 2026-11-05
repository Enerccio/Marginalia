package com.github.enerccio.marginalia.extensions.reallangtranslator.model;

import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;

/**
 * What the plugin keeps in the attributes of a book: which language the book is translated into and how. Stored under {@link #KEY}; the language,
 * the automatic run, the model and the protocol are resolved through the inheritance chain (the book's own, a folder's, the user's), the rest is the
 * book's own.
 */
public class BookSettings {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.reallangtranslator.book";

    /** The language (id) of the library of the user; null for none: nothing is translated. */
    private String languageId;
    /** Translate a part by itself when it is final. */
    private boolean auto;
    /** The model and the protocol (uuids) of the request; null for the book's own, else the defaults of the user. */
    private String aiUuid;
    private String protocolUuid;
    /** The parts before the one translated that the request gets, for who speaks to whom. */
    private int contextParts = 2;
    /** Who speaks the language in this book, and what the translator knows of the characters. */
    private Speakers speakers = new Speakers();

    public String getLanguageId() {
        return languageId == null || languageId.isBlank() ? null : languageId;
    }

    public void setLanguageId(String languageId) {
        this.languageId = languageId;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public String getAiUuid() {
        return aiUuid == null || aiUuid.isBlank() ? null : aiUuid;
    }

    public void setAiUuid(String aiUuid) {
        this.aiUuid = aiUuid;
    }

    public String getProtocolUuid() {
        return protocolUuid == null || protocolUuid.isBlank() ? null : protocolUuid;
    }

    public void setProtocolUuid(String protocolUuid) {
        this.protocolUuid = protocolUuid;
    }

    public int getContextParts() {
        return Math.max(0, contextParts);
    }

    public void setContextParts(int contextParts) {
        this.contextParts = contextParts;
    }

    public Speakers getSpeakers() {
        if (speakers == null) {
            speakers = new Speakers();
        }
        return speakers;
    }

    public void setSpeakers(Speakers speakers) {
        this.speakers = speakers;
    }
}
