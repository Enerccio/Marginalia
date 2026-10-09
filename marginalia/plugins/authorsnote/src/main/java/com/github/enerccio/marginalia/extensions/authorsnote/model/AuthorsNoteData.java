package com.github.enerccio.marginalia.extensions.authorsnote.model;

import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;

/**
 * Author's note of a book, stored in the book's attributes under {@link #KEY}.
 */
public class AuthorsNoteData {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.authorsnote";

    /**
     * Whether {@link #note} is inserted into the prompt.
     */
    private boolean enabled = false;
    /**
     * Text inserted into the prompt.
     */
    private String note = "";
    /**
     * Notes for the author only, never sent to the model.
     */
    private String privateNote = "";
    /**
     * Number of prompt messages that follow the note: 0 puts it after the current instructions (the very end of the
     * prompt), 1 right before them and so on.
     */
    private int depth = 1;
    private LLMRole role = LLMRole.SYSTEM;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPrivateNote() {
        return privateNote;
    }

    public void setPrivateNote(String privateNote) {
        this.privateNote = privateNote;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public LLMRole getRole() {
        return role;
    }

    public void setRole(LLMRole role) {
        this.role = role;
    }
}
