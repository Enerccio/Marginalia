package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

import com.github.enerccio.marginalia.extensions.storytracker.log.LogEntry;

/**
 * One change that an automatic run applied without asking, kept so that the user can see it and take it back. The log is
 * read-only; the only thing that can be done with an entry is to revert it (see {@code ChangeLog}). The shared fields are those of
 * {@link LogEntry}; the names of the stored fields did not change when they moved there.
 */
public class ChangeLogEntry extends LogEntry {

    public enum Type {
        /** The character learned something: an item was added. */
        ADDED,
        /** An item the story contradicts became old. */
        RETRACTED
    }

    private Type type;
    private ItemKind kind = ItemKind.KNOWS;

    public ChangeLogEntry() {
    }

    public ChangeLogEntry(long time, Type type, String characterId, String characterName, String itemId, String text,
                          ItemKind kind, String messageUuid, String evidence) {
        super(time, characterId, characterName, itemId, text, messageUuid, evidence);
        this.type = type;
        this.kind = kind;
    }

    public Type getType() {
        return type;
    }

    public ItemKind getKind() {
        return kind == null ? ItemKind.KNOWS : kind;
    }
}
