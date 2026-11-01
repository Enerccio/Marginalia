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
    MEDIUM,
    /** A line the chat leaves when its scene is changed (the new scene is in the text). */
    SCENE,
    /**
     * A scene the user sends after the first one: a change of scene or a description of what the user does, said by the author and not as
     * a line of dialogue. The personas react to it, so it is asked for a reply like a message of the user.
     */
    SCENE_MESSAGE
}
