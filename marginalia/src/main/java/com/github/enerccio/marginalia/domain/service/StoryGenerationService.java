package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationEvent;
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
    CancellationToken generateNextTurn(Manuscript manuscript, TurnInput input, GenerationRequest generationRequest, GenerationListener listener);
}