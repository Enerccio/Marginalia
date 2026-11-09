package com.github.enerccio.marginalia.extensions.audiobook.model;

/** A character of the book and the actor that plays it. */
public class CastMember {

    private String name = "";
    /** Other names the book calls the character by, separated by commas. */
    private String aliases = "";
    private String actorId;

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAliases() {
        return aliases == null ? "" : aliases;
    }

    public void setAliases(String aliases) {
        this.aliases = aliases;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }
}
