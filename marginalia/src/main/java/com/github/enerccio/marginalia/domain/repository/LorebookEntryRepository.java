package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;

import java.util.List;

public interface LorebookEntryRepository extends ExtendableRepository<LorebookEntry> {

    List<LorebookEntry> findByLorebookOrdered(Long lorebookId) throws Exception;

    /**
     * Id and {@code _fulltext} ({@code Object[]{Long, String}}) of the entries of the lorebook that are not deleted, in
     * the order of the entries. The rest of the entry is not read.
     *
     * @param literal text the {@code _fulltext} has to contain (the database compares ignoring the case of the letters
     *                it knows, so the caller checks again); null for all the entries
     */
    List<Object[]> findFulltextsContaining(Long lorebookId, String literal) throws Exception;

}
