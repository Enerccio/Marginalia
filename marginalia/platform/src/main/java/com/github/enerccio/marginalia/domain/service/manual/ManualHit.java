package com.github.enerccio.marginalia.domain.service.manual;

/**
 * A search result: the section and its relevance (higher is better, only comparable within one search).
 */
public record ManualHit(ManualSection section, double score) {
}
