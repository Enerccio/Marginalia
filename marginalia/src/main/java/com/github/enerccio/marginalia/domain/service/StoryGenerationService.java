package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.impl.generation.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;

public interface StoryGenerationService {

    Registration addEventListener(Events event, GenerationEvent listener);

    /**
     * Watches an event of every generation without being able to change it: the observer is called synchronously on
     * the thread that emits the event, before the listeners, and never takes part in the event chain. For the hot
     * events (a chunk of the answer) this costs no hop to another thread. See {@link GenerationObserver}.
     */
    Registration addEventObserver(Events event, GenerationObserver observer);

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