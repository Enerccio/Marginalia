package com.github.enerccio.marginalia.extensions.stconverter.model;

import com.github.enerccio.marginalia.domain.templates.TemplateData;

/**
 * The variables of the prompt for preparing the turn details of a chat.
 */
public class ConverterTemplateData extends TemplateData {

    /** The entries of the book's lorebook (and its sub lorebooks). */
    private String backgroundLore;
    /** The messages of the chosen range of the chat, each under a heading with its number. */
    private String chat;
    /** The number of the first message of the range. */
    private int firstMessage;
    /** The number of the last message of the range. */
    private int lastMessage;

    public String getBackgroundLore() {
        return backgroundLore;
    }

    public void setBackgroundLore(String backgroundLore) {
        this.backgroundLore = backgroundLore;
    }

    public String getChat() {
        return chat;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    public int getFirstMessage() {
        return firstMessage;
    }

    public void setFirstMessage(int firstMessage) {
        this.firstMessage = firstMessage;
    }

    public int getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(int lastMessage) {
        this.lastMessage = lastMessage;
    }
}
