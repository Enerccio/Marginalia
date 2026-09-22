package com.github.enerccio.marginalia.domain.service.impl.generation;

public interface GenerationStep {

    GenerationStepType getType();
    void step(GenerationController controller) throws Exception;

}
