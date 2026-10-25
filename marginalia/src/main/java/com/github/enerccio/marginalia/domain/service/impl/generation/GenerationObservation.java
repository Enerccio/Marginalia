package com.github.enerccio.marginalia.domain.service.impl.generation;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;

import java.util.Map;

/**
 * What a {@link GenerationObserver} sees of an event: the state of the generation, nothing it could change through
 * this view. The objects it returns are the ones of the generation: look, do not modify them.
 */
public interface GenerationObservation {

    /**
     * The event that is being emitted.
     */
    Events getEvent();

    /**
     * Id of the generation, the value of {@link GenerationProperties#RUN_ID}.
     */
    String getRunId();

    Manuscript getManuscript();

    AI getAi();

    Protocol getProtocol();

    /**
     * Message being generated, available from {@link Events#AFTER_GENERATE_NEW_MESSAGE}; null before.
     */
    ChatMessage getMessage();

    GenerationRequest getRequest();

    TurnInput getInput();

    /**
     * The properties of the generation (see {@link GenerationProperties}), a view that cannot be changed.
     */
    Map<String, Object> getProperties();

    @SuppressWarnings("unchecked")
    default <T> T getProperty(String key) {
        return (T) getProperties().get(key);
    }
}
