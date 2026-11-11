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
 * @param afterPart whether what the book does after a part ({@link AfterPart}: the automatic annotation, the translation...) follows a
 *                  completed generation; true but for an agent, which does those steps by its own commands.
 */
public record GenerationOptions(AI ai, Protocol protocol, RequestAttributes context, boolean afterPart) {

    private static final GenerationOptions DEFAULTS = new GenerationOptions(null, null, null);

    public GenerationOptions(AI ai, Protocol protocol, RequestAttributes context) {
        this(ai, protocol, context, true);
    }

    public static GenerationOptions defaults() {
        return DEFAULTS;
    }

    public GenerationOptions withModel(AI ai, Protocol protocol) {
        return new GenerationOptions(ai, protocol, context, afterPart);
    }

    public GenerationOptions withContext(RequestAttributes context) {
        return new GenerationOptions(ai, protocol, context, afterPart);
    }

    /** Nothing follows the generated part (an agent's generation). */
    public GenerationOptions withoutAfterPart() {
        return new GenerationOptions(ai, protocol, context, false);
    }
}
