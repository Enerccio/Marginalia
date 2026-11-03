package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ItemKind;

import java.util.UUID;

/**
 * One thing the model proposes: an item of a tracked character that the messages contradict (retract), or something the
 * character learned (add). The user may edit the text of an addition and uncheck a change before it is applied.
 */
public class Change {

    public enum Type {
        RETRACT, ADD
    }

    private final String id = UUID.randomUUID().toString();
    private final Type type;
    private final String characterId;
    private final String characterName;
    /** Retract: the item that is contradicted. */
    private final String itemId;
    private final String itemText;
    private ItemKind kind;
    /** Add: the text of the new item (editable). */
    private String text;
    private final String evidence;
    /** Whether the evidence quote is found in the message it names; a quote that is not may be made up. */
    private final boolean evidenceFound;
    private final String messageUuid;
    private final int messageNumber;
    /** Whether the change is applied; a rejected addition is kept as old knowledge, a rejected retraction changes nothing. */
    private boolean apply = true;

    Change(Type type, String characterId, String characterName, String itemId, String itemText, ItemKind kind, String text,
           String evidence, boolean evidenceFound, String messageUuid, int messageNumber) {
        this.type = type;
        this.characterId = characterId;
        this.characterName = characterName;
        this.itemId = itemId;
        this.itemText = itemText;
        this.kind = kind;
        this.text = text;
        this.evidence = evidence;
        this.evidenceFound = evidenceFound;
        this.messageUuid = messageUuid;
        this.messageNumber = messageNumber;
    }

    public String getId() {
        return id;
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

    public String getItemText() {
        return itemText;
    }

    public ItemKind getKind() {
        return kind;
    }

    public void setKind(ItemKind kind) {
        this.kind = kind;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getEvidence() {
        return evidence;
    }

    public boolean isEvidenceFound() {
        return evidenceFound;
    }

    public String getMessageUuid() {
        return messageUuid;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public boolean isApply() {
        return apply;
    }

    public void setApply(boolean apply) {
        this.apply = apply;
    }
}
