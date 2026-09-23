package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationController;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepBase;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepType;

public class CleanupStep extends GenerationStepBase {

    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_CLEANUP, () -> {
            // nothing, cleanup is for event hooks mostly for now
            controller.emitEvent(Events.AFTER_CLEANUP, controller::next);
        });
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.CLEANUP;
    }
}
