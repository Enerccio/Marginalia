package com.github.enerccio.marginalia.extensions.bookdescription.model;

/**
 * How the model is asked to write the name and the description of the book.
 */
public enum OutputFormat {
    /** JSON in a schema when the service can take one, Markdown when it can not. */
    AUTO,
    /** A JSON object in the schema (a response format of the request). */
    JSON,
    /**
     * A JSON object that the prompt describes, without a response format: for a server that can't take a schema together with the
     * thinking of the model, or a model that does badly under one.
     */
    JSON_NO_SCHEMA,
    /** Markdown: the name in the first heading, the description below it. */
    MARKDOWN;

    /** Whether the answer is asked for as JSON. */
    public boolean json() {
        return this != MARKDOWN;
    }

    /** Whether the request carries the schema. */
    public boolean schema() {
        return this == AUTO || this == JSON;
    }
}
