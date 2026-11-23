package com.github.enerccio.marginalia.domain.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * An entry of the phrase watch list: a phrase (or a regular expression) the author does not want in the prose. The lists of the user
 * settings, of the folders and of the book add up ({@code PhraseWatchService#resolve}); a nearer level that has the same phrase
 * replaces the entry of the one above it, and an entry that is {@link #isDisabled() off} takes the phrase out for the book. A hit in a
 * generated part becomes an annotation made from the {@link AnnotationTemplate} the entry names. Stored as JSON in the extended
 * attributes.
 */
public class WatchedPhrase {

    private String phrase;
    private boolean regex;
    /** The uuid of the {@link AnnotationTemplate} of the user settings that the annotation is made from; null is the built-in note. */
    private String template;
    private boolean disabled;

    public WatchedPhrase() {
    }

    public WatchedPhrase(String phrase, boolean regex, String template) {
        this.phrase = phrase;
        this.regex = regex;
        this.template = template;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public boolean isRegex() {
        return regex;
    }

    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * What makes two entries the same phrase across the levels: the expression as it is, or the phrase in lower case with its white
     * space collapsed.
     */
    public String key() {
        String text = StringUtils.defaultString(phrase).strip();
        return regex ? "re:" + text : "p:" + text.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    public WatchedPhrase copy() {
        WatchedPhrase copy = new WatchedPhrase(phrase, regex, template);
        copy.disabled = disabled;
        return copy;
    }
}
