package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationController;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepBase;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepType;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.PrePromptData;
import com.github.enerccio.marginalia.domain.templates.UserPromptData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrepareConstantsStep extends GenerationStepBase {
    private static final Logger log = LoggerFactory.getLogger(PrepareConstantsStep.class);

    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_PREPARE_CONSTANT_DATA, () -> {
            PrePromptData data = new PrePromptData();

            Manuscript manuscript = controller.getManuscript();
            InferenceService inferenceService = inferenceServices.forAI(manuscript.getAi());

            log.trace("Processing model jailbreak");
            if (manuscript.getAi().getNeedsJailbreak()) {
                data.setJailbreak(manuscript.getAi().getJailbreak());
                if (StringUtils.isNotBlank(data.getJailbreak())) {
                    data.setJailbreakTokens(inferenceService.countTokens(data.getJailbreak()));
                }
            }

            log.trace("Processing manuscript prompts");
            data.setGeneralTemplate(manuscriptService.getMasterTemplate(controller.getManuscript()));
            data.setTense(manuscriptService.getTense(controller.getManuscript()));
            data.setPov(manuscriptService.getPov(controller.getManuscript()));
            data.setStyle(manuscriptService.getStyle(controller.getManuscript()));
            data.setUserPrompt(manuscriptService.getUserPrompt(controller.getManuscript()));
            controller.setPrePromptData(data);

            controller.emitEvent(Events.AFTER_STATIC_TEMPLATE_DATA, () -> {
                UserPromptData userPromptData = new UserPromptData();
                userPromptData.setInstructions(controller.getInput().instructions());
                userPromptData.setPovCharacter(controller.getInput().povCharacter());
                userPromptData.setPresentCharacters(controller.getInput().presentCharacters());
                userPromptData.setSceneSetting(controller.getInput().sceneSetting());

                String userPrompt = templateService.processTemplate(data.getUserPrompt(), "userPrompt", userPromptData);
                data.setUserPromptProcessed(userPrompt);
                if (StringUtils.isNotBlank(userPrompt)) {
                    data.setUserPromptProcessedTokens(inferenceService.countTokens(userPrompt));
                }

                controller.emitEvent(Events.AFTER_PREPARE_CONSTANT_DATA, controller::next);
            });
        });
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.PREPARE_CONSTANTS;
    }
}
