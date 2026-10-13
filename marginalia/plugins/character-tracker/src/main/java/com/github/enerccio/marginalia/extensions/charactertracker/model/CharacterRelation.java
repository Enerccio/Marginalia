package com.github.enerccio.marginalia.extensions.charactertracker.model;

import java.util.UUID;

/**
 * A relationship between two characters.
 */
public class CharacterRelation {

    private String uuid = UUID.randomUUID().toString();
    private RelationDirection direction = RelationDirection.SOURCE_TARGET;
    private String name = "";
    private String tagline = "";
    /**
     * Uuid of the {@link StoryCharacter} the relation starts at.
     */
    private String sourceUuid;
    /**
     * Uuid of the {@link StoryCharacter} the relation points to.
     */
    private String targetUuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public RelationDirection getDirection() {
        return direction;
    }

    public void setDirection(RelationDirection direction) {
        this.direction = direction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getSourceUuid() {
        return sourceUuid;
    }

    public void setSourceUuid(String sourceUuid) {
        this.sourceUuid = sourceUuid;
    }

    public String getTargetUuid() {
        return targetUuid;
    }

    public void setTargetUuid(String targetUuid) {
        this.targetUuid = targetUuid;
    }

    public boolean involves(String characterUuid) {
        return characterUuid != null && (characterUuid.equals(sourceUuid) || characterUuid.equals(targetUuid));
    }

    /**
     * The character at the other end of the relation, seen from {@code characterUuid}.
     */
    public String otherEnd(String characterUuid) {
        return characterUuid != null && characterUuid.equals(sourceUuid) ? targetUuid : sourceUuid;
    }
}
