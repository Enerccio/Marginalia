package com.github.enerccio.marginalia.domain.service.search;

import com.github.enerccio.marginalia.domain.collections.RevisionSource;

import java.util.Date;

/**
 * A message of a full-text search: its id and a piece of its text around the match. A message found only through an
 * older version of its text (a search with versions included) says which version: its uuid, the time it was made and where it came from.
 *
 * @param version the uuid of the version the snippet is from, null when it is from the text of the message itself
 */
public record FulltextHit(Long id, String snippet, String version, Date versionTime, RevisionSource versionSource) {

    public FulltextHit(Long id, String snippet) {
        this(id, snippet, null, null, null);
    }

    /**
     * Whether the snippet is from an older version of the message, not from its text.
     */
    public boolean isVersion() {
        return version != null;
    }
}
