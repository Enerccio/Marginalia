package com.github.enerccio.marginalia.extensions.betareaders.model;

/**
 * The two kinds of chat, chosen when the chat is started and never changed: they use different prompts.
 */
public enum ChatKind {
    /** A chat with one persona, uses the prompt for one persona; nobody joins or leaves. */
    SINGLE,
    /** A chat with a group of personas (which may start with one, so others can join later), uses the group prompt. */
    GROUP
}
