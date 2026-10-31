package com.github.enerccio.marginalia.extensions.betareaders.model;

import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersStore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A chat of a book, shown as a tab: its personas (copies), its settings that override the inherited ones, and its history.
 */
public class BetaChat {

    private String uuid = UUID.randomUUID().toString();
    private String title = "";
    private ChatKind kind = ChatKind.GROUP;
    private ReplyFormat format = ReplyFormat.AUTO;
    private long created = System.currentTimeMillis();

    private List<ChatPersona> personas = new ArrayList<>();

    /** The id of the medium of this chat; null: the inherited one. */
    private String medium;
    /** The uuids of the AI and the protocol of this chat (the highest priority); null: the inherited ones. */
    private String ai;
    private String protocol;

    private List<BetaMessage> messages = new ArrayList<>();

    public BetaChat() {
    }

    public BetaChat(String title, ChatKind kind) {
        this.title = title;
        this.kind = kind;
    }

    /** The personas in the conversation now, in the order they were added. */
    public List<Persona> present() {
        return personas.stream().filter(ChatPersona::isPresent).map(ChatPersona::getPersona).toList();
    }

    /** The persona the chat has under this name (present or not), case and spacing ignored; null when there is none. */
    public Persona findPersona(String name) {
        String key = BetaReadersStore.nameKey(name);
        return personas.stream().map(ChatPersona::getPersona)
                .filter(persona -> BetaReadersStore.nameKey(persona.getName()).equals(key)).findFirst().orElse(null);
    }

    /**
     * Puts a copy of the persona into the conversation (or back into it, when it left before).
     */
    public void addPersona(Persona persona) {
        for (ChatPersona existing : personas) {
            if (existing.getPersona().getUuid().equals(persona.getUuid())) {
                existing.setPresent(true);
                return;
            }
        }
        personas.add(new ChatPersona(persona.copy(), true));
    }

    /**
     * Takes the persona out of the conversation; its copy stays for the messages it has said.
     */
    public void removePersona(String uuid) {
        personas.stream().filter(existing -> existing.getPersona().getUuid().equals(uuid))
                .forEach(existing -> existing.setPresent(false));
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ChatKind getKind() {
        return kind;
    }

    public void setKind(ChatKind kind) {
        this.kind = kind;
    }

    public ReplyFormat getFormat() {
        return format;
    }

    public void setFormat(ReplyFormat format) {
        this.format = format;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public List<ChatPersona> getPersonas() {
        return personas;
    }

    public void setPersonas(List<ChatPersona> personas) {
        this.personas = personas;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getAi() {
        return ai;
    }

    public void setAi(String ai) {
        this.ai = ai;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public List<BetaMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<BetaMessage> messages) {
        this.messages = messages;
    }

    /**
     * Replaces what a stored object may lack (a list that was never written, an enum that is not known any more).
     */
    public BetaChat normalize() {
        kind = kind == null ? ChatKind.GROUP : kind;
        format = format == null ? ReplyFormat.AUTO : format;
        personas = personas == null ? new ArrayList<>() : personas;
        messages = messages == null ? new ArrayList<>() : messages;
        messages.forEach(BetaMessage::normalize);
        return this;
    }
}
