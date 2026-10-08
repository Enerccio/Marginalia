package com.github.enerccio.marginalia.domain.service.impl.generation;

/**
 * Keys of {@link GenerationController#getProperties()} (and {@link GenerationControllerEvent#getProperties()}).
 * <p>
 * Each key documents value type and the first event the value is available in. Values stay in the map for the rest
 * of the generation. Some values are read back by the pipeline after the event, so listeners can change them
 * (ie add or remove activated lorebook entries in {@link Events#PROCESS_ACTIVATED_ENTRIES}).
 */
public final class GenerationProperties {

    /**
     * {@code TemplateContext} shared by all templates of the generation. Created on first template rendering,
     * available from {@link Events#AFTER_PREPARE_CONSTANT_DATA}.
     */
    public static final String TEMPLATE_CONTEXT = "TEMPLATE_CONTEXT";

    /**
     * {@code List<Lorebook>} manuscript lorebook and all its enabled subbooks with cached entries,
     * available from {@link Events#PROCESS_LOREBOOKS}. Not set when manuscript has no lorebook. Read back after the event.
     */
    public static final String LOREBOOKS = "LOREBOOKS";

    /**
     * {@code LorebookEntry} entry being evaluated, set for each {@link Events#PROCESS_LOREBOOK_ENTRY}.
     */
    public static final String LOREBOOK_ENTRY = "LOREBOOK_ENTRY";

    /**
     * {@code List<LorebookEntry>} entries that passed activation, available from
     * {@link Events#PROCESS_ACTIVATED_ENTRIES}. Read back after the event, changes made by listeners are applied.
     */
    public static final String ACTIVATED_LOREBOOK_ENTRIES = "ACTIVATED_LOREBOOK_ENTRIES";

    /**
     * {@code LorebookTemplateData} used to render activated entries, available from {@link Events#AFTER_PROCESS_LOREBOOK}.
     */
    public static final String LOREBOOK_TEMPLATE_DATA = "LOREBOOK_TEMPLATE_DATA";

    /**
     * {@code List<String>} summaries used instead of older messages, available from {@link Events#BEFORE_SUMMARIES}.
     * Read back after the event.
     */
    public static final String SUMMARIES = "SUMMARIES";

    /**
     * {@code List<String>} story parts (summaries and messages) in prompt order, available from
     * {@link Events#AFTER_MANUSCRIPT_CONCATENATION}. Read back when payload is built.
     */
    public static final String MANUSCRIPT_CHRONICLE = "MANUSCRIPT_CHRONICLE";

    /**
     * {@code String} reasoning chunk, set for each {@link Events#REASONING_CHUNK_RECEIVED}. Read back after the event.
     */
    public static final String REASONING_CHUNK = "REASONING_CHUNK";

    /**
     * {@code String} response chunk, set for each {@link Events#CHUNK_RECEIVED}. Read back after the event.
     */
    public static final String CHUNK = "CHUNK";

    private GenerationProperties() {

    }
}
