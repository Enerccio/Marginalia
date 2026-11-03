package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

/**
 * One change that an automatic run applied without asking, kept so that the user can see it and take it back. The log is
 * read-only; the only thing that can be done with an entry is to revert it.
 */
public class ChangeLogEntry {

    public enum Type {
        /** The character learned something: an item was added. */
        ADDED,
        /** An item the story contradicts became old. */
        RETRACTED
    }

    private String id = java.util.UUID.randomUUID().toString();
    private long time;
    private Type type;
    private String characterId;
    private String characterName;
    private String itemId;
    /** The text of the item as it was when the change was made. */
    private String text;
    private ItemKind kind = ItemKind.KNOWS;
    /** The message (uuid) the change comes from. */
    private String messageUuid;
    private String evidence;
    private boolean reverted;

    public ChangeLogEntry() {
    }

    public ChangeLogEntry(long time, Type type, String characterId, String characterName, String itemId, String text,
                          ItemKind kind, String messageUuid, String evidence) {
        this.time = time;
        this.type = type;
        this.characterId = characterId;
        this.characterName = characterName;
        this.itemId = itemId;
        this.text = text;
        this.kind = kind;
        this.messageUuid = messageUuid;
        this.evidence = evidence;
    }

    public String getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public Type getType() {
        return type;
    }

    public String getCharacterId() {
        return characterId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public String getItemId() {
        return itemId;
    }

    public String getText() {
        return text;
    }

    public ItemKind getKind() {
        return kind == null ? ItemKind.KNOWS : kind;
    }

    public String getMessageUuid() {
        return messageUuid;
    }

    public String getEvidence() {
        return evidence;
    }

    public boolean isReverted() {
        return reverted;
    }

    public void setReverted(boolean reverted) {
        this.reverted = reverted;
    }
}
