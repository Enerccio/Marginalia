package com.github.enerccio.marginalia.extensions.conlangtranslator.diff;

/**
 * Why a whole {@link Change} is in conflict (a conflict of one field is on the {@link FieldChange}). A change in conflict is not picked by default.
 */
public enum Conflict {
    /** Changed here, deleted in the other language. */
    CHANGED_HERE_DELETED_THERE,
    /** Deleted here, changed in the other language: picking it brings the other one back. */
    DELETED_HERE_CHANGED_THERE,
    /** A word that is new in the other language is a word that is here already (the same form and a shared meaning) under another identity. */
    SAME_WORD_EXISTS,
    /** Moved here and moved in the other language. */
    BOTH_MOVED
}
