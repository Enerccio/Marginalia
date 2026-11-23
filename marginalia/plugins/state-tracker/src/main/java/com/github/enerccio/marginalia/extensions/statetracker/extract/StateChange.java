package com.github.enerccio.marginalia.extensions.statetracker.extract;

/**
 * One change the model proposed and the checks let through, with the choice of the user in the review (apply or not, the value
 * corrected). Applied by {@link StateApplier}.
 */
public class StateChange {

    /** What happens to the category. */
    public enum Action {
        /** A new value of a single or description category (the current one goes to the history). */
        SET,
        /** A single or description category has no value any more. */
        CLEAR,
        /** A new entry of a list. */
        ADD,
        /** An entry of a list goes to the history. */
        REMOVE,
        /** The tidy: one value (of any kind) goes to the history and a new one takes its place. */
        REPLACE
    }

    private final String characterId;
    private final String characterName;
    private final String categoryId;
    private final String categoryName;
    private final Action action;
    /** The value for SET, ADD and REPLACE; editable in the review. */
    private String value;
    /** The value that ends (REMOVE, CLEAR, and the current one a SET replaces), {@code null} for none. */
    private final String itemId;
    private final String itemText;
    private final String evidence;
    /** The number of the message (1-based in the request) the evidence is in. */
    private final int messageNumber;
    /** The part (uuid) of that message. */
    private final String part;
    private final boolean evidenceFound;
    private boolean apply = true;
    /** A change of the tidy: what it ends goes to the history as tidied, the evidence is the reason. */
    private boolean tidy;
    /** The position of the part on the active branch (the first part is 1), 0 when not known; what the review shows. */
    private int partNumber;

    public StateChange(String characterId, String characterName, String categoryId, String categoryName, Action action, String value,
                       String itemId, String itemText, String evidence, int messageNumber, String part, boolean evidenceFound) {
        this.characterId = characterId;
        this.characterName = characterName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.action = action;
        this.value = value;
        this.itemId = itemId;
        this.itemText = itemText;
        this.evidence = evidence;
        this.messageNumber = messageNumber;
        this.part = part;
        this.evidenceFound = evidenceFound;
    }

    public String getCharacterId() {
        return characterId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Action getAction() {
        return action;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemText() {
        return itemText;
    }

    public String getEvidence() {
        return evidence;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getPart() {
        return part;
    }

    public boolean isEvidenceFound() {
        return evidenceFound;
    }

    public int getPartNumber() {
        return partNumber > 0 ? partNumber : messageNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    /** Marks the change as one of the tidy. */
    public StateChange tidy() {
        this.tidy = true;
        return this;
    }

    public boolean isTidy() {
        return tidy;
    }

    public boolean isApply() {
        return apply;
    }

    public void setApply(boolean apply) {
        this.apply = apply;
    }
}
