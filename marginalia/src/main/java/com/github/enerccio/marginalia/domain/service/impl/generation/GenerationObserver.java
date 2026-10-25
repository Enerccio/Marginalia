package com.github.enerccio.marginalia.domain.service.impl.generation;

/**
 * Read-only, synchronous watcher of an event of a generation, registered with
 * {@code StoryGenerationService.addEventObserver}. Unlike a {@link GenerationEvent} it cannot change the generation,
 * does not have to call {@code chain.next()} and costs no hop to another thread, so it is the way to follow the hot
 * events ({@link Events#CHUNK_RECEIVED}, {@link Events#REASONING_CHUNK_RECEIVED}).
 * <p>
 * It is called on the thread that emits the event (for a chunk the thread of the inference stream, not a generation
 * thread, and not necessarily one with a request context: do not call services that need the current user) before the
 * listeners of the event run, so it sees the values as the step produced them. The generation waits for it: it must
 * return quickly, hand slow work over to another thread. What it throws is logged and ignored. It is called also for
 * a generation that was stopped.
 */
@FunctionalInterface
public interface GenerationObserver {

    void observe(GenerationObservation observation);

}
