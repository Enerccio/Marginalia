package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationController;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepBase;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepType;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.PrePromptData;
import com.github.enerccio.marginalia.domain.templates.MasterTemplateData;
import com.github.enerccio.marginalia.loc.L;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PrepareContentStep extends GenerationStepBase {
    public static final String MANUSCRIPT_CHRONICLE = "MANUSCRIPT_CHRONICLE";

    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_PREPARE_CONTENT, () -> {
            int limit = controller.getManuscript().getProtocol().getMaxTokens();
            if (limit < 0) {
                limit = controller.getManuscript().getAi().getMaxContext();
            }

            InferenceService inferenceService = inferenceServices.forAI(controller.getManuscript().getAi());
            PrePromptData data = controller.getPrePromptData();

            MasterTemplateData templateData = new MasterTemplateData();
            templateData.setBackgroundLore(data.getBackgroundLore());
            templateData.setNarrativePov(data.getPov());
            templateData.setStyle(data.getStyle());
            templateData.setNarrativeTense(data.getTense());

            String emptyTemplate = templateService.processTemplate(data.getGeneralTemplate(), "systemTemplate", templateData);
            long baseTokens = inferenceService.countTokens(emptyTemplate) + data.getJailbreakTokens() + data.getUserPromptProcessedTokens() + 100; /* Buffer for HEADERS */

            if (baseTokens >= limit) {
                controller.getUIListener().onSimpleError(loc.getValue(L.ERROR_CONTEXT_INSUFFICIENT));
            }

            ChatMessage activeMessage = chatMessageService.find(controller.getManuscript().getActiveLeaf());
            if (activeMessage != null) {
                List<ChatMessage> fromRoot = chatMessageService.getBranchFromLeaf(activeMessage);
                if (!fromRoot.isEmpty()) {
                    // remove last
                    fromRoot = fromRoot.subList(0, fromRoot.size() - 1);
                }
                Collections.reverse(fromRoot);
                Iterator<ChatMessage> iterator = fromRoot.iterator();

                long tokens = baseTokens;
                List<String> storyText = new ArrayList<>();

                while (true) {
                    if (tokens > limit - 256) {
                        break;
                    }
                    if (!iterator.hasNext())
                        break;
                    ChatMessage message = iterator.next();
                    if (StringUtils.isNotBlank(message.getResponse())) {
                        storyText.add(message.getResponse());
                        tokens += message.getTokenCount() + 100; /* Buffer for dummy messages */
                    }
                }

                Collections.reverse(storyText);
                controller.getProperties().put(MANUSCRIPT_CHRONICLE, storyText);

                controller.emitEvent(Events.AFTER_MANUSCRIPT_CONCATENATION, () -> {
                    String systemPrompt = templateService.processTemplate(data.getGeneralTemplate(), "systemTemplate", templateData);
                    data.setSystemPrompt(systemPrompt);
                    data.setSystemPromptTokens(inferenceService.countTokens(systemPrompt));
                    controller.emitEvent(Events.AFTER_PREPARE_CONTENT, controller::next);
                });
            } else {
                data.setSystemPrompt(emptyTemplate);
                data.setSystemPromptTokens(baseTokens);
                controller.emitEvent(Events.AFTER_PREPARE_CONTENT, controller::next);
            }
        });
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.PREPARE_CONTENT;
    }
}
