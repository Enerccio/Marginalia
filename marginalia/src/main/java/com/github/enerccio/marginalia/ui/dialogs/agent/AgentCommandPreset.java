package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.loc.L;

/**
 * The quick choices of the commands an agent may call, offered above the table of the commands.
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
    NON_DESTRUCTIVE(L.ENUM_COMMAND_PRESET_NON_DESTRUCTIVE);

    private final L label;

    AgentCommandPreset(L label) {
        this.label = label;
    }

    public L getLabel() {
        return label;
    }
}
