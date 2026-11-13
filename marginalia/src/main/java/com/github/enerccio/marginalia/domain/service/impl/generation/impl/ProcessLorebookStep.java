package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.components.LoreConditions;
import com.github.enerccio.marginalia.domain.components.LoreUsage;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.LoreImages;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService.Verdict;
import com.github.enerccio.marginalia.domain.service.impl.generation.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LoreImage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LorebookEntryVerdict;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.PrePromptData;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

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

                    // the story is read only as far back as the deepest entry looks
                    LorebookActivationService.StoryText story = storyText(controller, entries);
                    Integer position = turnPosition(controller, entries);
                    // what the conditions of the entries are rendered against: the turn and a copy of the variables as they are
                    // now, so a condition changes nothing
                    LoreConditions conditions = activationService.conditionsNeeded(entries) ? new LoreConditions(createTemplateData(controller)) : null;
                    Map<Long, Integer> depths = activationService.scanDepths(lorebook, lorebooks);
                    LoreScan scan = new LoreScan(controller, entries, manuscriptTags, depths, activationService.deepestScan(depths), activationService, conditions);
                    controller.getProperties().put(GenerationProperties.LOREBOOK_VERDICTS, scan.verdicts);
                    forEachAsync(controller, entries, (entry, next) -> {
                        log.debug("Processing entry: {} ", entry.getName());
                        // evaluated before the event: listeners see the verdict and can replace it
                        scan.emit(entry, activationService.evaluate(entry, manuscriptTags,
                                controller.getPrePromptData().getUserPromptProcessed(), story, position, conditions), 0, verdict -> {
                            scan.record(entry, verdict, 0);
                            next.returnFromEvent();
                        });
                    }, () -> scanContent(scan, 1, scan.activated(), () -> {
                        List<LorebookEntry> activatedEntries = scan.activatedInOrder();
                        controller.getProperties().put(GenerationProperties.ACTIVATED_LOREBOOK_ENTRIES, activatedEntries);
                        controller.emitEvent(Events.PROCESS_ACTIVATED_ENTRIES, () -> {
                            List<LorebookEntry> finalActivatedEntries = (List<LorebookEntry>) controller.getProperties().get(GenerationProperties.ACTIVATED_LOREBOOK_ENTRIES);
                            // one template data (and so one context) for all entries - variables set in an entry
                            // are visible in the following ones
                            LorebookTemplateData templateData = createTemplateData(controller);
                            controller.getProperties().put(GenerationProperties.LOREBOOK_TEMPLATE_DATA, templateData);
                            StringBuilder builder = new StringBuilder();
                            StringBuilder builderUserPrompt = new StringBuilder();
                            // what each entry put into the prompt, kept with the prompt of the part (lore usage report)
                            LoreUsage usage = new LoreUsage();
                            // the images of the entries go along only to a model that is set to take them
                            boolean images = controller.getAi() != null && controller.getAi().isSendLoreImages();
                            List<LoreImage> loreImages = new java.util.ArrayList<>();
                            for (LorebookEntry entry : finalActivatedEntries) {
                                String payload = activationService.renderPayload(entry, templateData);
                                if (images) {
                                    for (LoreImages.Reference reference : LoreImages.references(entry.getPayload())) {
                                        loreImages.add(new LoreImage(entry.getName(), reference.caption(), reference.uuid(), entry.getInsertionMode()));
                                    }
                                }
                                if (StringUtils.isBlank(payload)) {
                                    continue;
                                }
                                usage.add(entry.getUuid(), inferenceService.countTokens(payload), scan.depthOf(entry));
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
                            controller.getProperties().put(GenerationProperties.LORE_IMAGES, loreImages);
                            controller.getPrePromptData().setLoreUsage(usage);
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
                    }));
                });
            } else {
                controller.emitEvent(Events.AFTER_PROCESS_LOREBOOK, controller::next);
            }
        });
    }

    /**
     * The number of the part the turn creates, null when no entry is limited to some parts (nothing to compare it with).
     * The part being replaced by a swipe or a regeneration is the one the turn creates.
     */
    private Integer turnPosition(GenerationController controller, List<LorebookEntry> entries) throws Exception {
        if (!activationService.positionNeeded(entries)) {
            return null;
        }
        GenerationRequestType requestType = controller.getRequest().getRequestType();
        return activationService.turnPosition(chatMessageService.find(controller.getManuscript().getActiveLeaf()),
                requestType == GenerationRequestType.SWIPE || requestType == GenerationRequestType.REGENERATE);
    }

    /**
     * What the entries look into besides the instructions: the newest parts of the story the turn continues, as far back as
     * the deepest entry looks, and the summaries when an entry asks for them. Like the story of the prompt, the part being
     * replaced by a swipe or a regeneration is not one of them.
     */
    private LorebookActivationService.StoryText storyText(GenerationController controller, List<LorebookEntry> entries) throws Exception {
        GenerationRequestType requestType = controller.getRequest().getRequestType();
        return activationService.storyText(entries, chatMessageService.find(controller.getManuscript().getActiveLeaf()),
                requestType == GenerationRequestType.SWIPE || requestType == GenerationRequestType.REGENERATE);
    }

    /**
     * One pass of the scan depth: the content of the entries the last pass used is searched for the filters of the entries
     * that are still unused. The entries it activates are the next pass; with the depth spent or nothing new, {@code done}.
     */
    private void scanContent(LoreScan scan, int depth, List<LorebookEntry> lastPass, GenerationController.FromEventCallback done) throws Exception {
        List<LorebookEntry> sources = depth > scan.deepest ? List.of() : activationService.scannable(lastPass);
        if (sources.isEmpty()) {
            done.returnFromEvent();
            return;
        }
        List<LorebookEntry> candidates = scan.entries.stream()
                .filter(entry -> scan.verdictOf(entry).reason().isScannable() && depth <= scan.scanLimit(entry)).toList();
        List<LorebookEntry> thisPass = new java.util.ArrayList<>();
        forEachAsync(scan.controller, candidates, (entry, next) -> {
            Verdict verdict = activationService.evaluateInContent(entry, scan.manuscriptTags, sources, scan.conditions);
            if (!verdict.activated()) {
                next.returnFromEvent();
                return;
            }
            log.debug("Entry {} found by the content of other entries at depth {}: {}", entry.getName(), depth, verdict.detail());
            scan.emit(entry, verdict, depth, decided -> {
                scan.record(entry, decided, depth);
                if (decided.activated()) {
                    thisPass.add(entry);
                }
                next.returnFromEvent();
            });
        }, () -> scanContent(scan, depth + 1, thisPass, done));
    }

    @FunctionalInterface
    private interface Decided {
        void accept(Verdict verdict) throws Exception;
    }

    /**
     * What the entries of one run of the step have got so far.
     */
    private static final class LoreScan {
        final GenerationController controller;
        final List<LorebookEntry> entries;
        final List<String> manuscriptTags;
        final Map<Long, Integer> scanDepths;
        final int deepest;
        final LorebookActivationService activationService;
        final LoreConditions conditions;
        final List<LorebookEntryVerdict> verdicts = new java.util.ArrayList<>();
        private final Map<LorebookEntry, Integer> index = new IdentityHashMap<>();
        private final Verdict[] current;

        LoreScan(GenerationController controller, List<LorebookEntry> entries, List<String> manuscriptTags, Map<Long, Integer> scanDepths,
                 int deepest, LorebookActivationService activationService, LoreConditions conditions) {
            this.controller = controller;
            this.activationService = activationService;
            this.conditions = conditions;
            this.entries = entries;
            this.manuscriptTags = manuscriptTags;
            this.scanDepths = scanDepths;
            this.deepest = deepest;
            this.current = new Verdict[entries.size()];
            for (int i = 0; i < entries.size(); i++) {
                index.put(entries.get(i), i);
            }
        }

        /**
         * Puts the verdict where the listeners of {@link Events#PROCESS_LOREBOOK_ENTRY} find it and reads back the one
         * they leave.
         */
        void emit(LorebookEntry entry, Verdict verdict, int depth, Decided decided) throws Exception {
            controller.getProperties().put(GenerationProperties.LOREBOOK_ENTRY, entry);
            controller.getProperties().put(GenerationProperties.LOREBOOK_VERDICT, verdict);
            controller.getProperties().put(GenerationProperties.LOREBOOK_SCAN_DEPTH, depth);
            controller.emitEvent(Events.PROCESS_LOREBOOK_ENTRY,
                    () -> decided.accept((Verdict) controller.getProperties().get(GenerationProperties.LOREBOOK_VERDICT)));
        }

        /**
         * The decision on the entry, replacing the one of an earlier pass in {@link #verdicts}.
         */
        void record(LorebookEntry entry, Verdict verdict, int depth) {
            int i = index.get(entry);
            current[i] = verdict;
            LorebookEntryVerdict record = new LorebookEntryVerdict(entry, verdict, verdict.activated() ? depth : 0);
            for (int v = 0; v < verdicts.size(); v++) {
                if (verdicts.get(v).entry() == entry) {
                    verdicts.set(v, record);
                    return;
                }
            }
            verdicts.add(record);
        }

        /**
         * How many passes may find the entry: the scan depth of its lorebook.
         */
        int scanLimit(LorebookEntry entry) {
            return activationService.scanDepthOf(scanDepths, entry);
        }

        /**
         * The pass of the scan depth that used the entry, 0 for the prompt (and for an entry that was not used).
         */
        int depthOf(LorebookEntry entry) {
            for (LorebookEntryVerdict record : verdicts) {
                if (record.entry() == entry) {
                    return record.depth();
                }
            }
            return 0;
        }

        Verdict verdictOf(LorebookEntry entry) {
            return current[index.get(entry)];
        }

        List<LorebookEntry> activated() {
            return entries.stream().filter(entry -> verdictOf(entry).activated()).toList();
        }

        List<LorebookEntry> activatedInOrder() {
            return new java.util.ArrayList<>(activated());
        }
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

