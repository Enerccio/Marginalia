package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.repository.LorebookEntryRepository;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;

import java.util.Collections;
import java.util.List;

public class LorebookEntryServiceImpl extends ExtendableServiceImpl<LorebookEntry, LorebookEntryRepository> implements LorebookEntryService {

    @Override
    @CommonTxReadOnly
    public List<LorebookEntry> getEntriesForLorebook(Long lorebookId) throws Exception {
        if (lorebookId == null) {
            return Collections.emptyList();
        }
        return getRepository().findByLorebookOrdered(lorebookId);
    }

    @Override
    @CommonTxReadOnly
    public List<LorebookEntry> getEntriesForLorebook(Lorebook lorebook) throws Exception {
        if (lorebook == null || lorebook.getId() == null) {
            return Collections.emptyList();
        }
        return getEntriesForLorebook(lorebook.getId());
    }
}