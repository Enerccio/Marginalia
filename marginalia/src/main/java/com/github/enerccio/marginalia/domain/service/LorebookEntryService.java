package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.repository.LorebookEntryRepository;

import java.util.List;

public interface LorebookEntryService extends ExtendableService<LorebookEntry, LorebookEntryRepository> {

    List<LorebookEntry> getEntriesForLorebook(Long lorebookId) throws Exception;

    List<LorebookEntry> getEntriesForLorebook(Lorebook lorebook) throws Exception;

}