package com.github.enerccio.marginalia.domain.model;

import java.util.UUID;

/**
 * A scene the author plans to write, an entry of the planner of a book ({@code Manuscript#getPlan}): what the turn details
 * of the new turn form take (scene setting, POV character, present characters, instructions) with a title for the list
 * and a flag the author sets when the scene is written. Stored as JSON in the extended attributes of the book.
 */
public class PlannedScene {

    private String uuid = UUID.randomUUID().toString();
    private String title;
    private String sceneSetting;
    private String povCharacter;
    private String presentCharacters;
    private String instructions;
    private boolean done;

    public PlannedScene() {
    }

    public PlannedScene(String title, String sceneSetting, String povCharacter, String presentCharacters, String instructions) {
        this.title = title;
        this.sceneSetting = sceneSetting;
        this.povCharacter = povCharacter;
        this.presentCharacters = presentCharacters;
        this.instructions = instructions;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSceneSetting() {
        return sceneSetting;
    }

    public void setSceneSetting(String sceneSetting) {
        this.sceneSetting = sceneSetting;
    }

    public String getPovCharacter() {
        return povCharacter;
    }

    public void setPovCharacter(String povCharacter) {
        this.povCharacter = povCharacter;
    }

    public String getPresentCharacters() {
        return presentCharacters;
    }

    public void setPresentCharacters(String presentCharacters) {
        this.presentCharacters = presentCharacters;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    /** A copy with the same uuid, so a list can be changed without touching the objects a reader holds. */
    public PlannedScene copy() {
        PlannedScene copy = new PlannedScene(title, sceneSetting, povCharacter, presentCharacters, instructions);
        copy.uuid = uuid;
        copy.done = done;
        return copy;
    }
}
