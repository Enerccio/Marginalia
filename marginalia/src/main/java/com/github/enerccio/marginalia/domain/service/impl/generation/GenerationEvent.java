package com.github.enerccio.marginalia.domain.service.impl.generation;

public interface GenerationEvent {

    void onEvent(GenerationControllerEvent event) throws Exception;

}
