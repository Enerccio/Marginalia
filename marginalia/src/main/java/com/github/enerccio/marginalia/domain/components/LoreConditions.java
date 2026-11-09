package com.github.enerccio.marginalia.domain.components;

import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService.Verdict;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * What the conditions of the lorebook entries of one activation (a generation, a preview) are rendered against, and how
 * much time they may take together.
 * <p>
 * The conditions are rendered with a {@linkplain LorebookTemplateData#fork() fork} of the data, so a {@code setvar} of a
 * condition does not reach the prompt or the other entries. An entry is decided once: the answer is kept, so the scan of
 * the content of other entries (scan depth) does not render the condition again and a condition that uses
 * {@code random} does not change its mind within one activation.
 * <p>
 * All the conditions share one time budget ({@link #TIME_BUDGET_SECONDS} by default): when it is spent, the conditions
 * that are still to be decided are not rendered and count as errors (false). A condition that is running is not
 * interrupted, the budget is checked before each one.
 */
public final class LoreConditions {

    /**
     * The time all the conditions of one activation may take together.
     */
    public static final long TIME_BUDGET_SECONDS = 60;

    private final LorebookTemplateData data;
    private final long budgetNanos;
    private final Map<LorebookEntry, Verdict> decided = new IdentityHashMap<>();
    private long spentNanos;

    /**
     * @param data what the conditions are rendered against: the turn (POV, present characters, instructions...) and its
     *             context (the variables as they are when the lorebook is processed)
     */
    public LoreConditions(LorebookTemplateData data) {
        this(data, TimeUnit.SECONDS.toNanos(TIME_BUDGET_SECONDS));
    }

    public LoreConditions(LorebookTemplateData data, long budgetNanos) {
        this.data = data == null ? new LorebookTemplateData() : data;
        this.budgetNanos = budgetNanos;
    }

    /**
     * Data for one condition: a fork of the data, nothing a condition does stays.
     */
    public LorebookTemplateData forCondition() {
        return data.fork();
    }

    public synchronized boolean exhausted() {
        return spentNanos >= budgetNanos;
    }

    public synchronized void spend(long nanos) {
        spentNanos += Math.max(0, nanos);
    }

    public synchronized long spentNanos() {
        return spentNanos;
    }

    /**
     * The answer already given for the entry, null when the condition was not decided yet.
     */
    public synchronized Verdict decision(LorebookEntry entry) {
        return decided.get(entry);
    }

    public synchronized void keep(LorebookEntry entry, Verdict verdict) {
        decided.put(entry, verdict);
    }
}
