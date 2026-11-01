package com.github.enerccio.marginalia.extensions.betareaders.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A line of the history of a chat. What the model is sent is built from these lines every time; nothing in them depends on who
 * is present now (the roster is added to the last user message when the request is built).
 */
public class BetaMessage {

    private String id = UUID.randomUUID().toString();
    private MessageKind kind = MessageKind.USER;
    private long created = System.currentTimeMillis();

    /** USER, SCENE_MESSAGE: what the user wrote. JOIN and LEAVE: the user's note about it (may be empty). */
    private String text = "";

    /**
     * USER: the parts of the story the message gave to the personas (when it included the latest parts). What a chat has been given is
     * the union of these; the text of the parts is not kept, the personas read the story as it is now.
     */
    private List<SentPart> sentParts = new ArrayList<>();

    /** JOIN, LEAVE: the persona (uuid and the name it had). */
    private String personaUuid;
    private String personaName;

    /** ASSISTANT: the short reasoning the model wrote before the messages; empty when it wrote none. */
    private String reasoning = "";

    /** ASSISTANT: the messages the personas and the narrator say, in order. */
    private List<BetaSpoken> spoken = new ArrayList<>();

    /** ASSISTANT: a reply that could not be read as messages, kept as it was written (then {@link #spoken} is empty). */
    private String rawText = "";

    public BetaMessage() {
    }

    public static BetaMessage user(String text) {
        BetaMessage message = new BetaMessage();
        message.kind = MessageKind.USER;
        message.text = text;
        return message;
    }

    /** A scene the user describes (a change of scene, what the user does); the model answers it like a message of the user. */
    public static BetaMessage scene(String text) {
        BetaMessage message = new BetaMessage();
        message.kind = MessageKind.SCENE_MESSAGE;
        message.text = text == null ? "" : text.strip();
        return message;
    }

    public static BetaMessage assistant(String reasoning, List<BetaSpoken> spoken) {
        BetaMessage message = new BetaMessage();
        message.kind = MessageKind.ASSISTANT;
        message.reasoning = reasoning == null ? "" : reasoning;
        message.spoken = new ArrayList<>(spoken);
        return message;
    }

    public static BetaMessage joined(Persona persona, String note) {
        return presence(MessageKind.JOIN, persona, note);
    }

    public static BetaMessage left(Persona persona, String note) {
        return presence(MessageKind.LEAVE, persona, note);
    }

    private static BetaMessage presence(MessageKind kind, Persona persona, String note) {
        BetaMessage message = new BetaMessage();
        message.kind = kind;
        message.text = note == null ? "" : note;
        message.personaUuid = persona.getUuid();
        message.personaName = persona.getName();
        return message;
    }

    /** True for the lines that are said on the side of the user: what they wrote, and the joining and leaving of personas. */
    public boolean isUserSide() {
        return kind != MessageKind.ASSISTANT;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MessageKind getKind() {
        return kind;
    }

    public void setKind(MessageKind kind) {
        this.kind = kind;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<SentPart> getSentParts() {
        return sentParts;
    }

    public void setSentParts(List<SentPart> sentParts) {
        this.sentParts = sentParts;
    }

    public String getPersonaUuid() {
        return personaUuid;
    }

    public void setPersonaUuid(String personaUuid) {
        this.personaUuid = personaUuid;
    }

    public String getPersonaName() {
        return personaName;
    }

    public void setPersonaName(String personaName) {
        this.personaName = personaName;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning == null ? "" : reasoning;
    }

    public List<BetaSpoken> getSpoken() {
        return spoken;
    }

    public void setSpoken(List<BetaSpoken> spoken) {
        this.spoken = spoken;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText == null ? "" : rawText;
    }

    /**
     * Replaces what a stored object may lack (a list that was never written).
     */
    public BetaMessage normalize() {
        kind = kind == null ? MessageKind.USER : kind;
        text = text == null ? "" : text;
        sentParts = sentParts == null ? new ArrayList<>() : sentParts;
        reasoning = reasoning == null ? "" : reasoning;
        rawText = rawText == null ? "" : rawText;
        spoken = spoken == null ? new ArrayList<>() : spoken;
        spoken.forEach(s -> s.setParts(s.getParts() == null ? new ArrayList<>() : s.getParts()));
        return this;
    }
}
