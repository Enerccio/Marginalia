package com.github.enerccio.marginalia.domain.service.tool;

import com.github.enerccio.marginalia.domain.service.function.FunctionSet;

import java.util.Collection;
import java.util.List;

/**
 * A suite of tools an extension (or the application) offers to whoever lets the model call functions: the Lore
 * Generator, the Arc Outline... The user chooses which suites the model gets, and of a suite that has {@link #items()}
 * which of its items, in the {@code ToolSelectionDialog}. A provider is registered with
 * {@link com.github.enerccio.marginalia.domain.service.ToolService#registerProvider} under a human name.
 * <p>
 * The same suite can be offered as commands too ({@link #commands()}): one registration gives the command bar and the
 * agents the commands, and whoever needs functions (a generator that has no agent) the functions.
 */
public interface ToolProvider {

    /**
     * What the user can check, in the order they are shown. Empty (the default) for a suite that is only on or off.
     * Asked on every use, the list may change while the application runs.
     */
    default List<ToolItem> items() {
        return List.of();
    }

    /**
     * The functions for the model, limited to the checked items.
     *
     * @param items   the ids of the checked items, all of them known to {@link #items()}; empty when the provider has none
     * @param request who asks
     * @return the functions; null or empty when there is nothing to give
     */
    FunctionSet functions(Collection<String> items, ToolRequest request) throws Exception;

    /**
     * Text added to the prompt of a request that gets the functions of this provider, telling the model when and how to
     * use them. The user can replace it where they choose the tools. Null or blank for none.
     */
    default String defaultHint() {
        return null;
    }

    /**
     * What the suite is for, shown above its items. Null for nothing.
     */
    default String description() {
        return null;
    }

    /**
     * A suite of commands ({@code @Command} methods) that does the same for the command bar and for agents. The service
     * registers it with the command service when the provider is registered and removes it with the provider. Null for
     * none.
     */
    default Object commands() {
        return null;
    }

}
