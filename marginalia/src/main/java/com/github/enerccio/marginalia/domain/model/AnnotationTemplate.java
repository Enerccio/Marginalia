package com.github.enerccio.marginalia.domain.model;

import java.util.UUID;

/**
 * What the annotation of a hit of the phrase watch list says ({@link WatchedPhrase#getTemplate()}), defined in the user settings: the
 * note, where {@code {{match}}} is the words found and {@code {{phrase}}} the entry of the list, the type of the note and whether it is
 * given to the model with the part. Stored as JSON in the extended attributes of the user settings.
 */
public class AnnotationTemplate {

    public static final String MATCH = "{{match}}";
    public static final String PHRASE = "{{phrase}}";

    private String uuid = UUID.randomUUID().toString();
    private String name;
    private String text;
    private String type;
    private boolean sendToPrompt;

    public AnnotationTemplate() {
    }

    public AnnotationTemplate(String name, String text, String type, boolean sendToPrompt) {
        this.name = name;
        this.text = text;
        this.type = type;
        this.sendToPrompt = sendToPrompt;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSendToPrompt() {
        return sendToPrompt;
    }

    public void setSendToPrompt(boolean sendToPrompt) {
        this.sendToPrompt = sendToPrompt;
    }

    /**
     * The note for a hit: the text with the words found and the entry of the list put in.
     */
    public String render(String match, String phrase) {
        return render(text, match, phrase);
    }

    public static String render(String text, String match, String phrase) {
        return (text == null ? "" : text).replace(MATCH, match == null ? "" : match).replace(PHRASE, phrase == null ? "" : phrase);
    }

    public AnnotationTemplate copy() {
        AnnotationTemplate copy = new AnnotationTemplate(name, text, type, sendToPrompt);
        copy.uuid = uuid;
        return copy;
    }
}
