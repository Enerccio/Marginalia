package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationController;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepBase;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepType;

public class ProcessLorebookStep extends GenerationStepBase {

    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_PROCESS_LOREBOOK, () -> {


            controller.emitEvent(Events.AFTER_PROCESS_LOREBOOK, controller::next);
        });
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.PROCESS_LOREBOOK;
    }
}
