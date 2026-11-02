package com.github.enerccio.marginalia.extensions.betareaders.model;

import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersStore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A chat of a book, shown as a tab: its personas (copies), its settings that override the inherited ones, and its history.
 */
public class BetaChat {

    private String uuid = UUID.randomUUID().toString();
    private String title = "";
    private ChatKind kind = ChatKind.GROUP;
    private ReplyFormat format = ReplyFormat.AUTO;
    private long created = System.currentTimeMillis();
    /** Closed: the tab is not shown, the chat is kept and can be opened again. */
    private boolean closed;

    /**
     * What of the story the personas read: the last part they may read (the spoiler limit) and the first one they read, both places in the
     * active branch from 1 (0: no limit, the start of the story), and whether the first message sends the summaries of the story that
     * is before the parts it sends.
     */
    private int spoilerLimit;
    private int fromPart;
    private boolean summaries = true;

    private List<ChatPersona> personas = new ArrayList<>();

    /** What the personas call the user and what their messages show instead of "You"; empty for none. */
    private String userName = "";

    /** The scene of this chat; null: the inherited one. */
    private String scene;
    /** The id of the medium of this chat; null: the inherited one. */
    private String medium;
    /** The uuids of the AI and the protocol of this chat (the highest priority); null: the inherited ones. */
    private String ai;
    private String protocol;
    /** The most messages the model may write in one turn of this chat; 0: the inherited limit (the one of the user, else the built-in one). */
    private int maxMessages;

    /** A list that can be read while the reply of the model, which ends on another thread, is added to it. */
    private List<BetaMessage> messages = new CopyOnWriteArrayList<>();

    public BetaChat() {
    }

    public BetaChat(String title, ChatKind kind) {
        this.title = title;
        this.kind = kind;
    }

    /**
     * A chat that reads like this one with one more message at the end, to see what a request would hold before the message is sent. The
     * personas are the same objects and are not to be changed; the history is a list of its own.
     */
    public BetaChat preview(BetaMessage extra) {
        BetaChat view = new BetaChat(title, kind);
        view.format = format;
        view.userName = userName;
        view.personas = personas;
        view.messages = new CopyOnWriteArrayList<>(messages);
        view.messages.add(extra);
        return view;
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

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public int getSpoilerLimit() {
        return spoilerLimit;
    }

    public void setSpoilerLimit(int spoilerLimit) {
        this.spoilerLimit = Math.max(0, spoilerLimit);
    }

    public int getFromPart() {
        return fromPart;
    }

    public void setFromPart(int fromPart) {
        this.fromPart = Math.max(0, fromPart);
    }

    public boolean isSummaries() {
        return summaries;
    }

    public void setSummaries(boolean summaries) {
        this.summaries = summaries;
    }

    public List<ChatPersona> getPersonas() {
        return personas;
    }

    public void setPersonas(List<ChatPersona> personas) {
        this.personas = personas;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? "" : userName.strip();
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
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

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = Math.max(0, maxMessages);
    }

    public List<BetaMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<BetaMessage> messages) {
        this.messages = new CopyOnWriteArrayList<>(messages);
    }

    /**
     * Replaces what a stored object may lack (a list that was never written, an enum that is not known any more).
     */
    public BetaChat normalize() {
        kind = kind == null ? ChatKind.GROUP : kind;
        format = format == null ? ReplyFormat.AUTO : format;
        maxMessages = Math.max(0, maxMessages);
        userName = userName == null ? "" : userName.strip();
        personas = personas == null ? new ArrayList<>() : personas;
        messages = messages == null ? new CopyOnWriteArrayList<>() : new CopyOnWriteArrayList<>(messages);
        messages.forEach(BetaMessage::normalize);
        return this;
    }
}
