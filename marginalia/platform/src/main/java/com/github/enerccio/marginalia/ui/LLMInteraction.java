package com.github.enerccio.marginalia.ui;

import java.util.Map;

/**
 * Something the help assistant's model may do to an element of the UI, see {@link LLMInteract#getInteraction()}: a
 * move through the interface (open a section, select a tab) that the user could make with one click and undo with
 * another. Never something that changes or deletes data, saves, sends, generates or logs out.
 *
 * @param description what it does, in English, as the model reads it ("Open the section Lorebooks.")
 * @param action      does it, on the thread of the page under its lock
 */
public record LLMInteraction(String description, Runnable action) {

    /** The interaction with the key, ignoring blanks around it and case; null when there is none. */
    static LLMInteraction lookup(Map<String, LLMInteraction> offered, String key) {
        if (key == null) {
            return null;
        }
        String wanted = key.strip();
        LLMInteraction exact = offered.get(wanted);
        if (exact != null) {
            return exact;
        }
        return offered.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(wanted))
                .map(Map.Entry::getValue).findFirst().orElse(null);
    }
}
