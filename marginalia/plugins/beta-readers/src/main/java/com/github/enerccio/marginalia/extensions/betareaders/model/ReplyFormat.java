package com.github.enerccio.marginalia.extensions.betareaders.model;

/**
 * How the model is asked to write its replies.
 */
public enum ReplyFormat {
    /** JSON in the schema of the reply when the service can take a schema, free text when it can not. */
    AUTO,
    /** A JSON object in the schema of the reply (a response format of the request, the reply starts with a short reasoning). */
    JSON,
    /**
     * A JSON object that the prompt describes, without a response format: for a server that can't take a schema together with the
     * thinking of the model, or a model that does badly under one. The reply is read as the JSON it ends with.
     */
    JSON_NO_SCHEMA,
    /** Free text, a paragraph per message that starts with {@code **Name:**}. */
    TEXT
}
