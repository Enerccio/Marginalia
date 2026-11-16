package com.github.enerccio.marginalia.extensions.storytracker.log;

import java.util.UUID;

/**
 * One change that an automatic run applied without asking, kept so that the user can see it and take it back (see
 * {@link TrackerChangeLog}). A tracker extends it with what the change did; the fields are stored with the state of the book (Gson),
 * so their names do not change.
 */
public class LogEntry {

    private String id = UUID.randomUUID().toString();
    private long time;
    private String characterId;
    private String characterName;
    /** The item (value) the change made or ended. */
    private String itemId;
    /** The text of the item as it was when the change was made. */
    private String text;
    /** The message (uuid) the change comes from, {@code null} for the tidy. */
    private String messageUuid;
    /** The quote of the part, or the reason of the tidy. */
    private String evidence;
    private boolean reverted;

    public LogEntry() {
    }

    public LogEntry(long time, String characterId, String characterName, String itemId, String text, String messageUuid, String evidence) {
        this.time = time;
        this.characterId = characterId;
        this.characterName = characterName;
        this.itemId = itemId;
        this.text = text;
        this.messageUuid = messageUuid;
        this.evidence = evidence;
    }

    public String getId() {
        return id;
    }

    public long getTime() {
        return time;
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
