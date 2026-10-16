package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.service.manual.ManualHit;
import com.github.enerccio.marginalia.domain.service.manual.ManualPage;

import java.util.List;
import java.util.Optional;

/**
 * The user manual packed in the WAR ({@code classpath:/manual/}, Markdown pages without pictures), kept in memory.
 * Global read-only data: no user, no transaction. Page paths are relative to the manual and end with {@code .md},
 * e.g. {@code books/story-editor.md}, the same form the pages use to link to each other.
 */
public interface ManualService {

    /** All pages ordered by path. */
    List<ManualPage> listPages();

    /**
     * @param path path of the page; a leading slash, an {@code #anchor} and a missing {@code .md} are tolerated
     */
    Optional<ManualPage> getPage(String path);

    /**
     * Full-text search over the sections of all pages (English text; a heading counts more than the body).
     *
     * @param query free text, e.g. a user's question
     * @param limit maximum number of hits
     * @return best hits first, empty when nothing matches
     */
    List<ManualHit> search(String query, int limit);
}
