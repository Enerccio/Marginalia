package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

/**
 * A person (a name, tracked or not) that a tracked character has met, with the message it was seen in.
 */
public class MetPerson {

    private String name = "";
    /** Message (uuid) the meeting was seen in, {@code null} when the user wrote it. */
    private String sourceMessage;

    public MetPerson() {
    }

    public MetPerson(String name, String sourceMessage) {
        this.name = name;
        this.sourceMessage = sourceMessage;
    }

    public String getName() {
        return name;
    }

    public String getSourceMessage() {
        return sourceMessage;
    }
}
