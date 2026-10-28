package com.github.enerccio.marginalia.extensions.stconverter.model;

/**
 * A message of the imported SillyTavern chat, as the author reads it: who said it and what. The number of a message is its
 * place in the list, from 1.
 */
public class ChatLine {

    /** The name of the speaker (the persona of the user or the character). */
    private String name;
    /** Whether the user wrote it (the other messages are the answers of the character). */
    private boolean user;
    private String text;
    /** Whether the author has gone through the message (rewritten it in the book). */
    private boolean reviewed;

    public ChatLine() {
    }

    public ChatLine(String name, boolean user, String text) {
        this.name = name;
        this.user = user;
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUser() {
        return user;
    }

    public void setUser(boolean user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }
}
