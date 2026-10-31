package com.github.enerccio.marginalia.extensions.betareaders.model;

/**
 * The kinds of lines of a chat's history.
 */
public enum MessageKind {
    /** What the user wrote (and the parts of the story it sent). */
    USER,
    /** A reply of the model: its reasoning and the messages of the personas and the narrator. */
    ASSISTANT,
    /** A line the user left when a persona joined the group; the model reads it as part of the user's side. */
    JOIN,
    /** A line the user left when a persona left the group. */
    LEAVE,
    /** A line the chat leaves when its medium is changed: the text is now read as another medium (its name is in the text). */
    MEDIUM
}
