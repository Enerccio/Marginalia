package com.github.enerccio.marginalia.domain.service.impl.generation;

import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.EventChain;

public interface GenerationEvent {

    void onEvent(GenerationControllerEvent event, EventChain chain) throws Exception;

}
