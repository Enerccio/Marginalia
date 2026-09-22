package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

public interface StoryGenerationService {

    /**
     * Spawns an asynchronous generation task.
     *
     * @param manuscript Active manuscript context.
     * @param input Turn parameters.
     * @param listener Callback listener for event updates.
     * @return CancellationToken handle to allow canceling from UI.
     */
    CancellationToken generateNextTurn(Manuscript manuscript, TurnInput input, GenerationListener listener);
}