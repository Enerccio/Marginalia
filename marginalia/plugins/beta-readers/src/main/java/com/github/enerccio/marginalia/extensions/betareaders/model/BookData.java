package com.github.enerccio.marginalia.extensions.betareaders.model;

import java.util.ArrayList;
import java.util.List;

/**
 * What the plugin keeps with a book, in its attributes under {@link #KEY}: the personas of the book (they win over the ones of the
 * library with the same name) and its chats.
 */
public class BookData {

    public static final String KEY = BetaReadersSettings.KEY + ".book";

    private List<Persona> personas = new ArrayList<>();
    private List<BetaChat> chats = new ArrayList<>();
    /** The uuid of the chat whose tab was open last. */
    private String activeChat;

    public List<Persona> getPersonas() {
        return personas;
    }

    public void setPersonas(List<Persona> personas) {
        this.personas = personas;
    }

    public List<BetaChat> getChats() {
        return chats;
    }

    public void setChats(List<BetaChat> chats) {
        this.chats = chats;
    }

    public String getActiveChat() {
        return activeChat;
    }

    public void setActiveChat(String activeChat) {
        this.activeChat = activeChat;
    }

    /**
     * Replaces what a stored object may lack (a list that was never written).
     */
    public BookData normalize() {
        personas = personas == null ? new ArrayList<>() : personas;
        chats = chats == null ? new ArrayList<>() : chats;
        chats.forEach(BetaChat::normalize);
        return this;
    }
}
