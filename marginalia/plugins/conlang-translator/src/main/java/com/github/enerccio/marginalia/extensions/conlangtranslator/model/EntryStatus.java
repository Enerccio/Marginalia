package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

/**
 * Where a {@link LexiconEntry} is in the author's review.
 */
public enum EntryStatus {
    /** Added by the model (or by an import) and not looked at yet. */
    NEW,
    /** The author looked at it. */
    ACCEPTED,
    /** The model may use it but never redefine it (the padlock of the Knowledge Tracker's items). */
    LOCKED,
    /** Kept, not offered to the model any more (a word that was replaced). */
    RETIRED;

    /**
     * @return whether the word is offered to the model
     */
    public boolean isOffered() {
        return this != RETIRED;
    }
}
