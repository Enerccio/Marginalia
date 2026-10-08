package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.service.impl.generation.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.PrePromptData;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PreparePayloadStep extends GenerationStepBase {

    @SuppressWarnings("unchecked")
    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_PREPARE_PAYLOAD, () -> {
            List<LLMChatMessage> payload = new ArrayList<>();
            payload.add(createSystemPrompt(controller));
            List<String> story = (List<String>) controller.getProperties().get(GenerationProperties.MANUSCRIPT_CHRONICLE);
            if (story != null) {
                boolean first = true;
                for (String blob : story) {
                    if (first) {
                        payload.add(createUserPrompt("[ Generate story. ]"));
                        first = false;
                    } else {
                        payload.add(createUserPrompt("[ Generate more story. ]"));
                    }
                    payload.add(createAssistantPrompt(blob));
                }
            }
            payload.add(createUserPrompt(controller.getPrePromptData().getUserPromptProcessed()));
            controller.setPayload(payload);
            controller.emitEvent(Events.AFTER_PREPARE_PAYLOAD, controller::next);
        });
    }

    private LLMChatMessage createSystemPrompt(GenerationController controller) {
        LLMChatMessage message = new LLMChatMessage();
        message.setRole(LLMRole.SYSTEM);
        PrePromptData data = controller.getPrePromptData();
        // the jailbreak goes first, before everything else in the prompt (its tokens are counted in PrepareContentStep)
        if (StringUtils.isNotBlank(data.getJailbreak())) {
            message.setContent(data.getJailbreak() + "\n\n" + StringUtils.defaultString(data.getSystemPrompt()));
        } else {
            message.setContent(data.getSystemPrompt());
        }
        return message;
    }

    private LLMChatMessage createUserPrompt(String prompt) {
        LLMChatMessage message = new LLMChatMessage();
        message.setRole(LLMRole.USER);
        message.setContent(prompt);
        return message;
    }

    private LLMChatMessage createAssistantPrompt(String prompt) {
        LLMChatMessage message = new LLMChatMessage();
        message.setRole(LLMRole.ASSISTANT);
        message.setContent(prompt);
        return message;
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.PREPARE_PAYLOAD;
    }
}
