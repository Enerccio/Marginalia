package com.github.enerccio.marginalia.extensions.statetracker.model;

/**
 * How the value of a category changes.
 */
public enum CategoryKind {
    /** One short value or nothing (the equipped weapon, the location): a new value replaces the old one. */
    SINGLE,
    /** A list of entries (inventory, injuries): entries are added and removed one by one. */
    LIST,
    /** One free text (clothes, appearance, mood): rewritten as a whole. */
    DESCRIPTION;

    /** Whether a character has at most one active value of the category. */
    public boolean isSingleValued() {
        return this != LIST;
    }
}
