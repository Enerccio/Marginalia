package com.github.enerccio.marginalia.domain.service.impl.generation.dto;

import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService.Verdict;

/**
 * The decision on one lorebook entry of a generation: the entry and why it was used or left out.
 *
 * @param depth 0 when the entry was decided by the prompt, n when it was used by the content of the entries of the pass
 *              n - 1 (the scan depth of the lorebook); an entry that was not used is 0
 */
public record LorebookEntryVerdict(LorebookEntry entry, Verdict verdict, int depth) {

    public LorebookEntryVerdict(LorebookEntry entry, Verdict verdict) {
        this(entry, verdict, 0);
    }
}
