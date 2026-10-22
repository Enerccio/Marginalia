package com.github.enerccio.marginalia.domain.service.tool;

import com.github.enerccio.marginalia.domain.service.function.FunctionSet;

/**
 * What a {@link ToolSelection} gives a request: the functions of the chosen providers put together, and the hints of the
 * providers that contribute.
 *
 * @param functions the functions for the model, null when the selection gives none
 * @param hint      the hints to add to the prompt, one after another; empty when there are none
 */
public record ToolSet(FunctionSet functions, String hint) {

    public static final ToolSet NONE = new ToolSet(null, "");

    public boolean isEmpty() {
        return functions == null;
    }

}
