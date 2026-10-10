package com.github.enerccio.marginalia.domain.service.search;

/**
 * A message of a full-text search: its id and a piece of its text around the match.
 */
public record FulltextHit(Long id, String snippet) {
}
