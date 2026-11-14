package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

import java.util.UUID;

/**
 * One thing a tracked character knows or does not know. Active items are sent to the model, old ones are kept and shown
 * only; nothing the user or the processor wrote is deleted except by an explicit delete.
 */
public class KnowledgeItem {

    private String id = UUID.randomUUID().toString();
    private String text = "";
    private ItemKind kind = ItemKind.KNOWS;
    private ItemOrigin origin = ItemOrigin.MANUAL;
    /** {@code null} = active. */
    private OldReason oldReason;
    /** Message (uuid) the item was learned in, only for {@link ItemOrigin#LEARNED}. */
    private String sourceMessage;
    /** The short quote from the source message that shows it. */
    private String evidence;
    /**
     * Message (uuid) whose processing retracted the item; {@code null} for a retraction by hand (which a reprocess
     * leaves alone).
     */
    private String retractedAt;
    /** Locked: the tidy pass never sees it. The story can still contradict it (it is retracted then), nothing else changes it. */
    private boolean locked;

    public KnowledgeItem() {
    }

    public static KnowledgeItem initial(String text, ItemKind kind) {
        KnowledgeItem item = new KnowledgeItem();
        item.text = text;
        item.kind = kind;
        item.origin = ItemOrigin.INITIAL;
        return item;
    }

    public static KnowledgeItem manual(String text, ItemKind kind) {
        KnowledgeItem item = new KnowledgeItem();
        item.text = text;
        item.kind = kind;
        item.origin = ItemOrigin.MANUAL;
        return item;
    }

    public static KnowledgeItem learned(String text, ItemKind kind, String sourceMessage, String evidence) {
        KnowledgeItem item = new KnowledgeItem();
        item.text = text;
        item.kind = kind;
        item.origin = ItemOrigin.LEARNED;
        item.sourceMessage = sourceMessage;
        item.evidence = evidence;
        return item;
    }

    /**
     * An item read from an export file. It has no part of this book behind it: an initial item stays initial, everything else is the
     * user's (manual), so a reprocess never drops it.
     */
    public static KnowledgeItem imported(String text, ItemKind kind, boolean initial, boolean locked) {
        KnowledgeItem item = initial ? initial(text, kind) : manual(text, kind);
        item.locked = locked;
        return item;
    }

    public boolean isActive() {
        return oldReason == null;
    }

    /**
     * Makes the item old because the user retracted it.
     */
    public void retractByHand() {
        oldReason = OldReason.RETRACTED;
        retractedAt = null;
    }

    /**
     * Makes the item old because the processing of {@code messageUuid} contradicted it.
     */
    public void retract(String messageUuid) {
        oldReason = OldReason.RETRACTED;
        retractedAt = messageUuid;
    }

    /**
     * Makes the item old because the tidy pass dropped it or merged it into another one. A reprocess leaves it as it is.
     */
    public void tidy() {
        oldReason = OldReason.TIDIED;
        retractedAt = null;
    }

    /**
     * Makes the item old because the user declined it in the review.
     */
    public void reject() {
        oldReason = OldReason.REJECTED;
        retractedAt = null;
    }

    /**
     * Makes the item active again, from now on. What the user restores is the user's decision, so a rejected addition
     * becomes manual (a reprocess does not drop it).
     */
    public void restore() {
        if (oldReason == OldReason.REJECTED) {
            origin = ItemOrigin.MANUAL;
            sourceMessage = null;
            evidence = null;
        }
        oldReason = null;
        retractedAt = null;
    }

    /**
     * Edits the text by hand: the item is the user's from now on and a reprocess leaves it.
     */
    public void edit(String newText, ItemKind newKind) {
        text = newText;
        kind = newKind;
        if (origin == ItemOrigin.LEARNED) {
            origin = ItemOrigin.MANUAL;
            sourceMessage = null;
            evidence = null;
        }
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public ItemKind getKind() {
        return kind;
    }

    public ItemOrigin getOrigin() {
        return origin;
    }

    public OldReason getOldReason() {
        return oldReason;
    }

    public String getSourceMessage() {
        return sourceMessage;
    }

    public String getEvidence() {
        return evidence;
    }

    public String getRetractedAt() {
        return retractedAt;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * Whether the tidy pass may drop or merge it: an active item the model learned, that the user did not lock. What the user wrote
     * (the initial state, items added or edited by hand) is never touched.
     */
    public boolean isTidyCandidate() {
        return isActive() && origin == ItemOrigin.LEARNED && !locked;
    }
}
