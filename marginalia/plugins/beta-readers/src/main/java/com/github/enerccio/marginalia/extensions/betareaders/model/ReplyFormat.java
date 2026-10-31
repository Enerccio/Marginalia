package com.github.enerccio.marginalia.extensions.betareaders.model;

/**
 * How the model is asked to write its replies.
 */
public enum ReplyFormat {
    /** JSON when the protocol of the chat supports structured outputs, free text otherwise. */
    AUTO,
    /** A JSON object in the schema of the reply. */
    JSON,
    /** Free text, a paragraph per message that starts with {@code **Name:**}. */
    TEXT
}
