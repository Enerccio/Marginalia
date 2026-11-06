package com.github.enerccio.marginalia.extensions.reallangtranslator.model;

import com.github.enerccio.marginalia.extensions.translatorcommon.model.BookLanguage;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    /** The other languages of the book, each with its own speakers; the book's own (it is not inherited). */
    private List<BookLanguage> otherLanguages = new ArrayList<>();
    /**
     * The glossary of this book (the nearest level): its names and terms, and the ones the model proposed, for each language of the book (the key is the
     * id of the language: a name is written another way in every language). The book's own, not inherited.
     */
    private Map<String, List<GlossaryEntry>> glossary = new LinkedHashMap<>();

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

    public List<BookLanguage> getOtherLanguages() {
        if (otherLanguages == null) {
            otherLanguages = new ArrayList<>();
        }
        return otherLanguages;
    }

    public void setOtherLanguages(List<BookLanguage> otherLanguages) {
        this.otherLanguages = otherLanguages;
    }

    /**
     * The speakers of a language of the book: the language the book names has {@link #getSpeakers()}, the others have their own.
     *
     * @param languageId the id of the language
     * @return null when the book does not have the language
     */
    public Speakers speakersOf(String languageId) {
        if (languageId == null) {
            return null;
        }
        if (languageId.equals(getLanguageId())) {
            return getSpeakers();
        }
        return getOtherLanguages().stream().filter(other -> languageId.equals(other.getLanguageId())).findFirst().map(BookLanguage::getSpeakers).orElse(null);
    }

    /**
     * The ids of the languages of the book, the one it names first; no duplicates.
     */
    public List<String> languageIds() {
        List<String> ids = new ArrayList<>();
        if (getLanguageId() != null) {
            ids.add(getLanguageId());
        }
        for (BookLanguage other : getOtherLanguages()) {
            if (other.getLanguageId() != null && !ids.contains(other.getLanguageId())) {
                ids.add(other.getLanguageId());
            }
        }
        return ids;
    }

    /**
     * The book's own entries of the glossary in a language, never null; the list belongs to the settings (changing it changes them).
     *
     * @param languageId the language (its id)
     */
    public List<GlossaryEntry> getGlossary(String languageId) {
        if (glossary == null) {
            glossary = new LinkedHashMap<>();
        }
        return glossary.computeIfAbsent(languageId == null ? "" : languageId, _ -> new ArrayList<>());
    }

    public void setGlossary(String languageId, List<GlossaryEntry> entries) {
        if (glossary == null) {
            glossary = new LinkedHashMap<>();
        }
        if (entries == null || entries.isEmpty()) {
            glossary.remove(languageId == null ? "" : languageId);
        } else {
            glossary.put(languageId == null ? "" : languageId, new ArrayList<>(entries));
        }
    }
}
