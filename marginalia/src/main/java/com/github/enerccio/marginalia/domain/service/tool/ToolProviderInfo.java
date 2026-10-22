package com.github.enerccio.marginalia.domain.service.tool;

import java.util.List;

/**
 * A registered {@link ToolProvider} as the user interface shows it. A copy: it does not change afterwards.
 *
 * @param ref         identifies the provider in a {@link ToolSelection}: bundle and key
 * @param name        the human name
 * @param description what the suite is for, null for nothing
 * @param items       what can be checked; empty for a suite that is only on or off
 * @param defaultHint the hint added to the prompt when the user has not written one, null for none
 * @param commands    whether the suite is also offered as commands
 */
public record ToolProviderInfo(String ref, String name, String description, List<ToolItem> items, String defaultHint,
                               boolean commands) {

    /** Whether the user chooses among items; if not, the suite is on or off. */
    public boolean selectable() {
        return !items.isEmpty();
    }

}
