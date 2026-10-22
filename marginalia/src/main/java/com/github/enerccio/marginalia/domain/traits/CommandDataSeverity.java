package com.github.enerccio.marginalia.domain.traits;

import com.github.enerccio.marginalia.loc.L;

/**
 * What a {@link Command} does to the data that exists, declared by {@link Command#dataSeverity()}. The user sees it next
 * to the command in the command bar, and an agent (LLM) that runs commands on its own can decide by it which ones it may
 * run freely and which ones to ask the user about first.
 */
public enum CommandDataSeverity {

    /** The command changes or removes data that exists (replaces text, deletes, overwrites). */
    DESTRUCTIVE(L.LABEL_COMMAND_DESTRUCTIVE, L.HELP_COMMAND_DESTRUCTIVE),

    /** The command only reads data or adds new data; whatever exists stays as it is. */
    NON_DESTRUCTIVE(L.LABEL_COMMAND_NON_DESTRUCTIVE, L.HELP_COMMAND_NON_DESTRUCTIVE);

    private final L label;
    private final L description;

    CommandDataSeverity(L label, L description) {
        this.label = label;
        this.description = description;
    }

    /** The short name shown to the user. */
    public L getLabel() {
        return label;
    }

    /** The sentence that explains it, for a tooltip or the help. */
    public L getDescription() {
        return description;
    }

    public boolean isDestructive() {
        return this == DESTRUCTIVE;
    }

}
