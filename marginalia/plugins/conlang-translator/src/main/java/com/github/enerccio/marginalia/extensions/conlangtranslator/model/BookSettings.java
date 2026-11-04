package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

/**
 * What the plugin keeps in the attributes of a book: which language the book is translated into and how. Stored under {@link #KEY}.
 */
public class BookSettings {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.conlangtranslator.book";

    /** The language (id) of the library of the user; null for none: nothing is translated. */
    private String languageId;
    /** Translate a part by itself when it is final. */
    private boolean auto;
    /** The model and the protocol (uuids) of the request; null for the book's own, else the defaults of the user. */
    private String aiUuid;
    private String protocolUuid;
    /** The parts before the one translated that the request gets, for who speaks to whom. */
    private int contextParts = 2;
    /** The size above which the lexicon is cut down in the request (characters); 0 never cuts. */
    private int maxLexiconChars = 60000;
    /** Who speaks the language in this book, and what the translator knows of the characters. Per book: the same language is spoken by other people in another story. */
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

    public int getMaxLexiconChars() {
        return Math.max(0, maxLexiconChars);
    }

    public void setMaxLexiconChars(int maxLexiconChars) {
        this.maxLexiconChars = maxLexiconChars;
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
