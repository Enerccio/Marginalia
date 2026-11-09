package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.service.StoryGenerationService;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationObservation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Which users have a generation running: a batch of speech must not start while the language model works for its user (the model would be
 * unloaded under the generation, or loaded again under the speech server). A generation is running from its first event to {@link Events#AFTER_CLEANUP},
 * which comes also when it was stopped or failed. Watches with observers, which cost the generation nothing and cannot change it.
 */
public class GenerationTracker {

    private final Map<Long, Set<String>> running = new ConcurrentHashMap<>();
    private final List<Registration> registrations;

    public GenerationTracker(StoryGenerationService generations) {
        registrations = List.of(
                generations.addEventObserver(Events.BEFORE_PREPARE_GENERATION, o -> change(o, true)),
                generations.addEventObserver(Events.AFTER_CLEANUP, o -> change(o, false)));
    }

    private void change(GenerationObservation observation, boolean started) {
        if (observation.getManuscript() == null || observation.getManuscript().getOwner() == null || observation.getRunId() == null) {
            return;
        }
        Long owner = observation.getManuscript().getOwner().getId();
        if (started) {
            running.computeIfAbsent(owner, o -> ConcurrentHashMap.newKeySet()).add(observation.getRunId());
        } else {
            running.computeIfPresent(owner, (o, runs) -> {
                runs.remove(observation.getRunId());
                return runs.isEmpty() ? null : runs;
            });
        }
    }

    /** Whether a generation of the user is running now. */
    public boolean isRunning(Long ownerId) {
        return ownerId != null && running.containsKey(ownerId);
    }

    public void close() {
        registrations.forEach(Registration::unregister);
        running.clear();
    }
}
