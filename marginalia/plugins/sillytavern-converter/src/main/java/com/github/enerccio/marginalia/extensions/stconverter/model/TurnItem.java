package com.github.enerccio.marginalia.extensions.stconverter.model;

/**
 * The turn details the model prepared for a stretch of the chat: what the new turn form of the story takes (scene setting,
 * POV character, present characters, instructions) and the messages of the chat the turn is made from.
 */
public class TurnItem {

    /** The first message of the chat (from 1) the turn covers. */
    private int firstMessage;
    /** The last message of the chat the turn covers, inclusive. */
    private int lastMessage;
    private String sceneSetting;
    private String povCharacter;
    private String presentCharacters;
    private String instructions;
    /** Whether the author has used the turn (written it in the book). */
    private boolean reviewed;

    public TurnItem() {
    }

    public TurnItem(int firstMessage, int lastMessage, String sceneSetting, String povCharacter, String presentCharacters,
                    String instructions) {
        this.firstMessage = firstMessage;
        this.lastMessage = lastMessage;
        this.sceneSetting = sceneSetting;
        this.povCharacter = povCharacter;
        this.presentCharacters = presentCharacters;
        this.instructions = instructions;
    }

    /** The messages the turn covers, like {@code 12-15} ({@code 12} for one). */
    public String messages() {
        return firstMessage == lastMessage ? String.valueOf(firstMessage) : firstMessage + "-" + lastMessage;
    }

    public int getFirstMessage() {
        return firstMessage;
    }

    public void setFirstMessage(int firstMessage) {
        this.firstMessage = firstMessage;
    }

    public int getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(int lastMessage) {
        this.lastMessage = lastMessage;
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

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }
}
