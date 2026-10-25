package com.github.enerccio.marginalia.domain.service.impl.generation.dto;

import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService.Verdict;

/**
 * The decision on one lorebook entry of a generation: the entry and why it was used or left out.
 */
public record LorebookEntryVerdict(LorebookEntry entry, Verdict verdict) {
}
