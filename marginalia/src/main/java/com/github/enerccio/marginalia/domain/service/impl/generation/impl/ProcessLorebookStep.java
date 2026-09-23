package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.model.impl.TagRelation;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationController;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepBase;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepType;
import org.apache.commons.collections4.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ProcessLorebookStep extends GenerationStepBase {
    private static final Logger log = LoggerFactory.getLogger(ProcessLorebookStep.class);
    public static final String LOREBOOKS = "LOREBOOKS";
    public static final String LOREBOOK_ENTRY = "LOREBOOK_ENTRY";
    public static final String ACTIVATED_LOREBOOK_ENTRIES = "ACTIVATED_LOREBOOK_ENTRIES";

    @SuppressWarnings("unchecked")
    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_PROCESS_LOREBOOK, () -> {
            log.debug("Processing lorebooks");
            Lorebook lorebook = lorebookService.find(controller.getManuscript().getLorebook());
            if (lorebook != null) {
                List<String> manuscriptTags = tagRelationService.getTagsForObject(controller.getManuscript()).stream().map(Tag::getValue).toList();
                InferenceService inferenceService = inferenceServices.forAI(controller.getManuscript().getAi());

                Set<Long> processedLorebooks = new HashSet<>();
                Stack<Lorebook> lorebookStack = new Stack<>();
                Queue<Lorebook> toProcess = new LinkedList<>();
                toProcess.add(lorebook);
                while (!toProcess.isEmpty()) {
                    Lorebook l = toProcess.remove();
                    if (!processedLorebooks.contains(l.getId())) {
                        processedLorebooks.add(l.getId());
                        lorebookStack.add(l);
                        toProcess.addAll(lorebookService.getSubbooks(l));
                    }
                }

                List<Lorebook> foundLorebooks = new ArrayList<>(lorebookStack);
                for (Lorebook book : foundLorebooks) {
                    book = lorebookService.fillEntries(book);
                    lorebookService.evict(book);
                    for (LorebookEntry entry : book.getCachedEntries()) {
                        lorebookEntryService.evict(entry);
                    }
                }

                controller.getProperties().put(LOREBOOKS, foundLorebooks);
                log.debug("Lorebooks found: {}", foundLorebooks);
                controller.emitEvent(Events.PROCESS_LOREBOOKS, () -> {
                    List<Lorebook> lorebooks = (List<Lorebook>) controller.getProperties().get(LOREBOOKS);
                    log.debug("Lorebooks to be processed: {}", foundLorebooks);

                    Map<Integer, List<LorebookEntry>> lorebookEntries = new TreeMap<>();

                    for (Lorebook l : lorebooks) {
                        for (LorebookEntry lorebookEntry : l.getCachedEntries()) {
                            lorebookEntries.computeIfAbsent(lorebookEntry.getOrder(), _ -> new ArrayList<>()).add(lorebookEntry);
                        }
                    }

                    for (List<LorebookEntry> list : lorebookEntries.values()) {
                        list.sort(Comparator.comparing(LorebookEntry::getCreation, Comparator.nullsLast(Comparator.naturalOrder())));
                    }

                    List<LorebookEntry> entries = new ArrayList<>();
                    for (List<LorebookEntry> list : lorebookEntries.values()) {
                        entries.addAll(list);
                    }
                    log.debug("Processing entries: {}", entries);

                    List<LorebookEntry> activatedEntries = new ArrayList<>();
                    forEachAsync(controller, entries, (entry, next) -> {
                        controller.getProperties().put(LOREBOOK_ENTRY, entry);
                        controller.emitEvent(Events.PROCESS_LOREBOOK_ENTRY, () -> {
                            log.debug("Processing entry: {} ", entry.getName());

                            if (!entry.isEnabled()) {
                                log.debug("Entry {} not added because it's not enabled", entry.getName());
                            } else if (hasNegativeTag(entry.getCachedNegativeTags(), manuscriptTags)) {
                                log.debug("Entry {} not added because negative tags {} matches book tags {}.", entry.getName(), entry.getCachedNegativeTags(), manuscriptTags);
                            } else if (!entry.getCachedTags().isEmpty() && !hasPositiveTags(entry.getCachedTags(), manuscriptTags)) {
                                log.debug("Entry {} not added because tags {} defined and not match book tags {}.", entry.getName(), entry.getCachedTags(), manuscriptTags);
                            } else {
                                log.debug("Entry {} added to activated entries.", entry.getName());
                                activatedEntries.add(entry);
                            }
                            next.returnFromEvent();
                        });
                    }, () -> {
                        controller.getProperties().put(ACTIVATED_LOREBOOK_ENTRIES, activatedEntries);
                        controller.emitEvent(Events.PROCESS_ACTIVATED_ENTRIES, () -> {
                            List<LorebookEntry> finalActivatedEntries = (List<LorebookEntry>) controller.getProperties().get(ACTIVATED_LOREBOOK_ENTRIES);
                            StringBuilder builder = new StringBuilder();
                            for (LorebookEntry entry : finalActivatedEntries) {
                                builder.append(entry.getPayload());
                                builder.append("\n\n");
                            }
                            String lorebookContent = builder.toString().trim();
                            log.debug("Final lorebook content: {}", lorebookContent);
                            controller.getPrePromptData().setBackgroundLore(lorebookContent);
                            controller.getPrePromptData().setBackgroundLoreTokens(inferenceService.countTokens(lorebookContent));
                            controller.emitEvent(Events.AFTER_PROCESS_LOREBOOK, controller::next);
                        });
                    });
                });
            } else {
                controller.emitEvent(Events.AFTER_PROCESS_LOREBOOK, controller::next);
            }
        });
    }

    private boolean hasNegativeTag(List<String> cachedNegativeTags, List<String> manuscriptTags) {
        return !SetUtils.intersection(new HashSet<>(cachedNegativeTags), new HashSet<>(manuscriptTags)).isEmpty();
    }

    private boolean hasPositiveTags(List<String> cachedPositiveTags, List<String> manuscriptTags) {
        return !SetUtils.intersection(new HashSet<>(cachedPositiveTags), new HashSet<>(manuscriptTags)).isEmpty();
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.PROCESS_LOREBOOK;
    }
}
