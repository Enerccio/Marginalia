package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService.Verdict;
import com.github.enerccio.marginalia.domain.service.impl.generation.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LorebookEntryVerdict;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.PrePromptData;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ProcessLorebookStep extends GenerationStepBase {
    private static final Logger log = LoggerFactory.getLogger(ProcessLorebookStep.class);

    @Autowired
    private LorebookActivationService activationService;

    @SuppressWarnings("unchecked")
    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_PROCESS_LOREBOOK, () -> {
            log.debug("Processing lorebooks");
            Lorebook lorebook = lorebookService.find(controller.getManuscript().getLorebook());
            if (lorebook != null) {
                List<String> manuscriptTags = tagRelationService.getTagsForObject(controller.getManuscript()).stream().map(Tag::getValue).toList();
                InferenceService inferenceService = inferenceServices.forAIOrThrow(controller.getAi());

                List<Lorebook> foundLorebooks = activationService.collectLorebooks(lorebook);

                controller.getProperties().put(GenerationProperties.LOREBOOKS, foundLorebooks);
                log.debug("Lorebooks found: {}", foundLorebooks);
                controller.emitEvent(Events.PROCESS_LOREBOOKS, () -> {
                    List<Lorebook> lorebooks = (List<Lorebook>) controller.getProperties().get(GenerationProperties.LOREBOOKS);
                    log.debug("Lorebooks to be processed: {}", foundLorebooks);

                    List<LorebookEntry> entries = activationService.orderedEntries(lorebooks);
                    log.debug("Processing entries: {}", entries);

                    List<LorebookEntry> activatedEntries = new java.util.ArrayList<>();
                    List<LorebookEntryVerdict> verdicts = new java.util.ArrayList<>();
                    controller.getProperties().put(GenerationProperties.LOREBOOK_VERDICTS, verdicts);
                    forEachAsync(controller, entries, (entry, next) -> {
                        log.debug("Processing entry: {} ", entry.getName());
                        // evaluated before the event: listeners see the verdict and can replace it
                        controller.getProperties().put(GenerationProperties.LOREBOOK_ENTRY, entry);
                        controller.getProperties().put(GenerationProperties.LOREBOOK_VERDICT,
                                activationService.evaluate(entry, manuscriptTags,
                                        controller.getPrePromptData().getUserPromptProcessed()));
                        controller.emitEvent(Events.PROCESS_LOREBOOK_ENTRY, () -> {
                            Verdict verdict = (Verdict) controller.getProperties().get(GenerationProperties.LOREBOOK_VERDICT);
                            verdicts.add(new LorebookEntryVerdict(entry, verdict));
                            if (verdict.activated()) {
                                log.debug("Entry {} added to activated entries ({}).", entry.getName(), verdict.reason());
                                activatedEntries.add(entry);
                            } else {
                                log.debug("Entry {} not added: {} {}", entry.getName(), verdict.reason(), verdict.detail());
                            }
                            next.returnFromEvent();
                        });
                    }, () -> {
                        controller.getProperties().put(GenerationProperties.ACTIVATED_LOREBOOK_ENTRIES, activatedEntries);
                        controller.emitEvent(Events.PROCESS_ACTIVATED_ENTRIES, () -> {
                            List<LorebookEntry> finalActivatedEntries = (List<LorebookEntry>) controller.getProperties().get(GenerationProperties.ACTIVATED_LOREBOOK_ENTRIES);
                            // one template data (and so one context) for all entries - variables set in an entry
                            // are visible in the following ones
                            LorebookTemplateData templateData = createTemplateData(controller);
                            controller.getProperties().put(GenerationProperties.LOREBOOK_TEMPLATE_DATA, templateData);
                            StringBuilder builder = new StringBuilder();
                            StringBuilder builderUserPrompt = new StringBuilder();
                            for (LorebookEntry entry : finalActivatedEntries) {
                                String payload = activationService.renderPayload(entry, templateData);
                                if (StringUtils.isBlank(payload)) {
                                    continue;
                                }
                                if (entry.getInsertionMode() == InsertionMode.IN_LORE_BLOCK) {
                                    builder.append(payload);
                                    builder.append("\n\n");
                                } else {
                                    builderUserPrompt.append(payload);
                                    builderUserPrompt.append("\n\n");
                                }
                            }
                            String lorebookContent = builder.toString().trim();
                            String lorebookUserContent = builderUserPrompt.toString().trim();
                            log.debug("Final lorebook content: {}, {}", lorebookContent, lorebookUserContent);
                            controller.getPrePromptData().setBackgroundLore(lorebookContent);
                            controller.getPrePromptData().setBackgroundLoreTokens(inferenceService.countTokens(lorebookContent));
                            controller.getPrePromptData().setBackgroundUserLore(lorebookUserContent);
                            controller.getPrePromptData().setBackgroundUserLoreTokens(inferenceService.countTokens(lorebookUserContent));
                            if (StringUtils.isNotBlank(controller.getPrePromptData().getBackgroundUserLore())) {
                                controller.getPrePromptData().setUserPromptProcessed(controller.getPrePromptData().getBackgroundUserLore() + "\n\n" + controller.getPrePromptData().getUserPromptProcessed());
                                controller.getPrePromptData().setUserPromptProcessedTokens(controller.getPrePromptData().getBackgroundUserLoreTokens() + controller.getPrePromptData().getUserPromptProcessedTokens());
                            }
                            controller.emitEvent(Events.AFTER_PROCESS_LOREBOOK, controller::next);
                        });
                    });
                });
            } else {
                controller.emitEvent(Events.AFTER_PROCESS_LOREBOOK, controller::next);
            }
        });
    }

    private LorebookTemplateData createTemplateData(GenerationController controller) throws Exception {
        LorebookTemplateData templateData = new LorebookTemplateData();
        TurnInput input = controller.getInput();
        if (input != null) {
            templateData.setPovCharacter(input.povCharacter());
            templateData.setSceneSetting(input.sceneSetting());
            templateData.setPresentCharacters(input.presentCharacters());
            templateData.setInstructions(input.instructions());
        }
        PrePromptData data = controller.getPrePromptData();
        templateData.setNarrativePov(data.getPov());
        templateData.setNarrativeTense(data.getTense());
        templateData.setStyle(data.getStyle());
        templateData.setTemplateContext(getTemplateContext(controller));
        return templateData;
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.PROCESS_LOREBOOK;
    }
}

