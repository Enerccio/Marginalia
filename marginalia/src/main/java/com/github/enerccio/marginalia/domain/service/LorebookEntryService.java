package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.repository.LorebookEntryRepository;

import java.util.List;

public interface LorebookEntryService extends ExtendableService<LorebookEntry, LorebookEntryRepository> {

    List<LorebookEntry> getEntriesForLorebook(Long lorebookId) throws Exception;

    List<LorebookEntry> getEntriesForLorebook(Lorebook lorebook) throws Exception;

    /**
     * The id and {@code _fulltext} of the entries of the lorebook that are not deleted, in the order of the entries. It
     * does not check that the lorebook is the current user's, the caller does.
     *
     * @param literal the text the {@code _fulltext} has to contain (the database does not tell the case of every
     *                letter apart, check again), null for all the entries
     */
    List<EntryFulltext> findFulltextsContaining(Lorebook lorebook, String literal) throws Exception;

    record EntryFulltext(Long entryId, String fulltext) {
    }

}
