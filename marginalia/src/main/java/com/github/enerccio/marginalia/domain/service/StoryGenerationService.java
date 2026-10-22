package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationEvent;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;

public interface StoryGenerationService {

    Registration addEventListener(Events event, GenerationEvent listener);

    /**
     * Spawns an asynchronous generation task.
     *
     * @param manuscript Active manuscript context.
     * @param input Turn parameters.
     * @param listener Callback listener for event updates.
     * @return CancellationToken handle to allow canceling from UI.
     */
    default CancellationToken generateNextTurn(Manuscript manuscript, TurnInput input, GenerationRequest generationRequest, GenerationListener listener) {
        return generateNextTurn(manuscript, input, generationRequest, listener, GenerationOptions.defaults());
    }

    /**
     * Spawns an asynchronous generation task that can use another AI / protocol than the manuscript and a request
     * context that does not come from the calling thread (see {@link GenerationOptions}). The manuscript itself is
     * not changed by the options.
     */
    CancellationToken generateNextTurn(Manuscript manuscript, TurnInput input, GenerationRequest generationRequest, GenerationListener listener, GenerationOptions options);
}