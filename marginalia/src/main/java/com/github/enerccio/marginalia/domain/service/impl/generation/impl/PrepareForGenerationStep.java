package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationController;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepBase;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepType;
import com.github.enerccio.marginalia.loc.L;

public class PrepareForGenerationStep extends GenerationStepBase {

    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_PREPARE_GENERATION, () -> {
            // rehydrate and check AI/Protocol
            AI aiModel = aiService.find(controller.getAi());
            if (aiModel == null) {
                controller.getUIListener().onSimpleError(loc.getValue(L.ERROR_AI_NOT_SET));
                controller.jumpTo(GenerationStepType.CLEANUP);
                return;
            }
            controller.setAi(aiModel);

            Protocol protocol = protocolService.find(controller.getProtocol());
            if (protocol == null) {
                controller.getUIListener().onSimpleError(loc.getValue(L.ERROR_PROTOCOL_NOT_SET));
                controller.jumpTo(GenerationStepType.CLEANUP);
                return;
            }
            controller.setProtocol(protocol);

            InferenceService inferenceService = inferenceServices.forAIOrThrow(aiModel);
            if (inferenceService == null) {
                controller.getUIListener().onError(new IllegalStateException("Missing inference service!"));
                controller.jumpTo(GenerationStepType.CLEANUP);
                return;
            }

            controller.emitEvent(Events.AFTER_PREPARE_GENERATION, controller::next);
        });
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.PREPARE_GENERATION;
    }
}
