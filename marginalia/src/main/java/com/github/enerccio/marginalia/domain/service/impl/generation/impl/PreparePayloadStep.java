package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationController;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepBase;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepType;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;

import java.util.ArrayList;
import java.util.List;

public class PreparePayloadStep extends GenerationStepBase {

    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_PREPARE_PAYLOAD, () -> {
            List<LLMChatMessage> payload = new ArrayList<>();
            payload.add(createSystemPrompt(controller));
            payload.add(createUserPrompt(controller));
            controller.setPayload(payload);
            controller.emitEvent(Events.AFTER_PREPARE_PAYLOAD, controller::next);
        });
    }

    private LLMChatMessage createSystemPrompt(GenerationController controller) {
        LLMChatMessage message = new LLMChatMessage();
        message.setRole(LLMRole.SYSTEM);
        message.setContent(controller.getPrePromptData().getSystemPrompt());
        return message;
    }

    private LLMChatMessage createUserPrompt(GenerationController controller) {
        LLMChatMessage message = new LLMChatMessage();
        message.setRole(LLMRole.USER);
        message.setContent(controller.getPrePromptData().getUserPromptProcessed());
        return message;
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.PREPARE_PAYLOAD;
    }
}
