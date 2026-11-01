package com.github.enerccio.marginalia.domain.components;

import com.github.enerccio.marginalia.domain.collections.RevisionSource;

import java.util.Date;

/**
 * A version of a part as the history list shows it: everything but the text, which is not read for the list. The list is
 * ordered by {@code creation}, then {@code id}.
 */
public record RevisionRow(Long id, String uuid, RevisionSource source, boolean agent, String sourceDetail,
                          String label, int wordCount, long tokenCount, Date creation, boolean deleted) {
}
