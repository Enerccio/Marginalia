package com.github.enerccio.marginalia.domain.service.tool;

/**
 * One thing of a {@link ToolProvider} the user can allow the model to use: a category of names, a list... The provider
 * limits its functions to the ones that are checked.
 *
 * @param id    stable between versions of the provider, the user's selection keeps it
 * @param label what the user sees
 */
public record ToolItem(String id, String label) {

    public ToolItem(String id) {
        this(id, id);
    }

}
