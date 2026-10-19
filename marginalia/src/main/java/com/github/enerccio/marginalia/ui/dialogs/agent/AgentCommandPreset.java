package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.loc.L;

import java.util.Set;

/**
 * The quick choices of the commands an agent may call, offered above the table of the commands. The roles
 * ({@link #AUDITOR}, {@link #LORE_KEEPER}, {@link #EDITOR}, {@link #DRAFTER}) are the commands that read data (every
 * command that declares {@code readOnly}, also those of the extensions) plus the named commands that write for that job;
 * the agent is still limited by the destructive mode of its dialog, none of the roles has a command that removes data.
 */
public enum AgentCommandPreset {

    /**
     * Every command there is.
     */
    ALL(L.ENUM_COMMAND_PRESET_ALL),

    /**
     * No command: the agent can only talk (and use its files).
     */
    NONE(L.ENUM_COMMAND_PRESET_NONE),

    /**
     * The commands that only read data or add new data, none that changes or removes what exists.
     */
    NON_DESTRUCTIVE(L.ENUM_COMMAND_PRESET_NON_DESTRUCTIVE),

    /**
     * Reads the book and the lorebooks and changes nothing: continuity checks, name and term audits, reports.
     */
    AUDITOR(L.ENUM_COMMAND_PRESET_AUDITOR),

    /**
     * Reads, and keeps the lorebooks: creates and updates entries and lorebooks, tags, links the sub-lorebooks.
     */
    LORE_KEEPER(L.ENUM_COMMAND_PRESET_LORE_KEEPER,
            "create-lore-entry", "update-lore-entry", "move-lore-entry", "set-lore-entry-tags", "create-lorebook",
            "set-lorebook", "add-subbook", "remove-subbook"),

    /**
     * Reads, and revises the written text: edits parts, fixes their fields, keeps the summaries and the book info.
     */
    EDITOR(L.ENUM_COMMAND_PRESET_EDITOR,
            "edit-part", "append-to-part", "set-part-fields", "set-summary", "revalidate-summaries", "regenerate-summary",
            "create-meta-summary", "set-book-info", "set-book-tags"),

    /**
     * Reads, and writes new text: adds parts and branches, lets the model write the next turn, keeps the summaries and
     * the lorebook entries of what it invents.
     */
    DRAFTER(L.ENUM_COMMAND_PRESET_DRAFTER,
            "create-part", "branch-part", "switch-branch", "append-to-part", "edit-part", "next", "swipe", "set-summary",
            "regenerate-summary", "create-meta-summary", "revalidate-summaries", "create-lore-entry", "update-lore-entry");

    private final L label;
    private final Set<String> writes;

    AgentCommandPreset(L label, String... writes) {
        this.label = label;
        this.writes = Set.of(writes);
    }

    public L getLabel() {
        return label;
    }

    /**
     * Whether the command belongs to the preset.
     */
    boolean includes(AgentCommandsEditor.Row row) {
        return switch (this) {
            case ALL -> true;
            case NONE -> false;
            case NON_DESTRUCTIVE -> !row.destructive();
            case AUDITOR -> row.readOnly();
            default -> row.readOnly() || writes.contains(row.name());
        };
    }
}
