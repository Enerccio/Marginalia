package com.github.enerccio.marginalia.domain.service.impl.generation;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import org.springframework.web.context.request.RequestAttributes;

/**
 * What a caller can decide about one generation besides the turn itself. Every part is optional, {@code null} means
 * the default, so {@link #defaults()} is what the user interface uses.
 *
 * @param ai AI used for this generation, the manuscript's AI when {@code null}. Never stored in the manuscript.
 * @param protocol protocol used for this generation, the manuscript's protocol when {@code null}.
 * @param context request context the worker threads of the generation run in (it resolves the session scoped
 *                beans, ie the user), a copy of the context of the calling thread when {@code null}.
 */
public record GenerationOptions(AI ai, Protocol protocol, RequestAttributes context) {

    private static final GenerationOptions DEFAULTS = new GenerationOptions(null, null, null);

    public static GenerationOptions defaults() {
        return DEFAULTS;
    }

    public GenerationOptions withModel(AI ai, Protocol protocol) {
        return new GenerationOptions(ai, protocol, context);
    }

    public GenerationOptions withContext(RequestAttributes context) {
        return new GenerationOptions(ai, protocol, context);
    }
}
