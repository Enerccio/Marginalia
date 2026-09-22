package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
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
            Manuscript manuscript = controller.getManuscript();
            AI aiModel = aiService.find(manuscript.getAi());
            if (aiModel == null) {
                controller.getUIListener().onError(new IllegalStateException(loc.getValue(L.ERROR_AI_NOT_SET)));
                controller.jumpTo(GenerationStepType.CLEANUP);
                return;
            }
            manuscript.setAi(aiModel);

            Protocol protocol = protocolService.find(manuscript.getProtocol());
            if (protocol == null) {
                controller.getUIListener().onError(new IllegalStateException(loc.getValue(L.ERROR_PROTOCOL_NOT_SET)));
                controller.jumpTo(GenerationStepType.CLEANUP);
                return;
            }
            manuscript.setProtocol(protocol);

            controller.emitEvent(Events.AFTER_PREPARE_GENERATION, controller::next);
        });
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.PREPARE_GENERATION;
    }
}
