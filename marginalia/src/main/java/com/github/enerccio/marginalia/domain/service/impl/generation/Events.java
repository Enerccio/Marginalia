package com.github.enerccio.marginalia.domain.service.impl.generation;

public enum Events {

    BEFORE_PREPARE_GENERATION,
    AFTER_PREPARE_GENERATION,
    BEFORE_PREPARE_CONSTANT_DATA,
    AFTER_STATIC_TEMPLATE_DATA,
    AFTER_PREPARE_CONSTANT_DATA,
    BEFORE_PROCESS_LOREBOOK,
    PROCESS_LOREBOOKS,
    PROCESS_LOREBOOK_ENTRY,
    PROCESS_ACTIVATED_ENTRIES,
    AFTER_PROCESS_LOREBOOK,
    BEFORE_PREPARE_CONTENT,
    BEFORE_SUMMARIES,
    AFTER_MANUSCRIPT_CONCATENATION,
    AFTER_PREPARE_CONTENT,
    BEFORE_GENERATE_NEW_MESSAGE,
    AFTER_GENERATE_NEW_MESSAGE,
    BEFORE_PREPARE_PAYLOAD,
    AFTER_PREPARE_PAYLOAD,
    BEFORE_INFERENCE,
    REASONING_CHUNK_RECEIVED,
    CHUNK_RECEIVED,
    AFTER_INFERENCE,
    BEFORE_CLEANUP,
    /**
     * The last event of every generation, completed, stopped or failed: emitted when the cleanup is done. Unlike the
     * others its listeners run also when the user stopped the generation.
     */
    AFTER_CLEANUP;

    /**
     * Whether the listeners of the event run although the generation was cancelled or interrupted.
     */
    public boolean isTerminal() {
        return this == AFTER_CLEANUP;
    }

}
