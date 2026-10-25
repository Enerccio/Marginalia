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
     * {@code String} id of this generation (a random UUID), set before the first event and the same in every event of
     * it: tells the data of generations that run at the same time apart (an extension that keeps state per
     * generation keys it by this).
     */
    public static final String RUN_ID = "RUN_ID";

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
     * {@code LorebookEntry} entry that was evaluated, set for each {@link Events#PROCESS_LOREBOOK_ENTRY}.
     */
    public static final String LOREBOOK_ENTRY = "LOREBOOK_ENTRY";

    /**
     * {@code LorebookActivationService.Verdict} the activation's decision on the entry in {@link #LOREBOOK_ENTRY}, set
     * for each {@link Events#PROCESS_LOREBOOK_ENTRY}. Read back after the event: a listener replaces it with
     * {@code new Verdict(Reason.EXTENSION_ACTIVATED / EXTENSION_SKIPPED, why)} to force the entry in or out, and the
     * entry is activated by the verdict that is left.
     */
    public static final String LOREBOOK_VERDICT = "LOREBOOK_VERDICT";

    /**
     * {@code Integer} the pass of the scan depth the entry in {@link #LOREBOOK_ENTRY} is decided in, set for each
     * {@link Events#PROCESS_LOREBOOK_ENTRY}: 0 when the filter is looked for in the prompt (every entry gets this event
     * once), n when it was found in the content of the entries that the pass n - 1 used (the event is only for such
     * entries and comes again for an entry already decided; the verdict is {@code LORE_MATCHED}, and a listener can
     * replace it as usual). See {@code Lorebook#getScanDepth}.
     */
    public static final String LOREBOOK_SCAN_DEPTH = "LOREBOOK_SCAN_DEPTH";

    /**
     * {@code List<LorebookEntryVerdict>} the final decision on every entry that was evaluated (used or not, with the
     * {@code Reason}, its detail and the pass of the scan depth), in evaluation order. Created empty at {@link Events#PROCESS_LOREBOOKS}, an entry is
     * added after its {@link Events#PROCESS_LOREBOOK_ENTRY} (with the verdict the listeners left in
     * {@link #LOREBOOK_VERDICT}), and it is complete from {@link Events#PROCESS_ACTIVATED_ENTRIES}. A record: to change
     * a decision use {@link #LOREBOOK_VERDICT}, changes made to {@link #ACTIVATED_LOREBOOK_ENTRIES} are not reflected in
     * it. Not set when manuscript has no lorebook.
     */
    public static final String LOREBOOK_VERDICTS = "LOREBOOK_VERDICTS";

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
     * {@code PromptTokenBreakdown} raw token counts of the prompt sections, available from
     * {@link Events#AFTER_PREPARE_CONTENT}. The messages of the payload are added when the new part is created and the
     * breakdown is stored with it ({@code ChatMessage#getTokenBreakdown()}).
     */
    public static final String TOKEN_BREAKDOWN = "TOKEN_BREAKDOWN";

    /**
     * {@code String} reasoning chunk, set for each {@link Events#REASONING_CHUNK_RECEIVED}. Read back after the event.
     */
    public static final String REASONING_CHUNK = "REASONING_CHUNK";

    /**
     * {@code String} response chunk, set for each {@link Events#CHUNK_RECEIVED}. Read back after the event.
     */
    public static final String CHUNK = "CHUNK";

    /**
     * {@code ChatMessage} detached copy of the regenerated part as it was before it was cleared, set for
     * {@link GenerationRequestType#REGENERATE} from {@link Events#BEFORE_GENERATE_NEW_MESSAGE} on. Cleanup restores the
     * part from it when generation ends before any text arrived.
     */
    public static final String ORIGINAL_MESSAGE = "ORIGINAL_MESSAGE";

    /**
     * {@code GenerationOutcome} how the generation ended: completed, stopped by the user or failed. Set when the cleanup
     * starts, so it is there in {@link Events#BEFORE_CLEANUP} (whose listeners do not run when the user stopped the
     * generation) and in {@link Events#AFTER_CLEANUP}. A stop wins over an error the stop caused.
     */
    public static final String OUTCOME = "OUTCOME";

    /**
     * {@code GenerationFailure} the first error reported to the user (message and exception), set together with
     * {@link #OUTCOME} when there was one, also for a stopped generation that failed first. Absent otherwise.
     */
    public static final String FAILURE = "FAILURE";

    private GenerationProperties() {

    }
}
