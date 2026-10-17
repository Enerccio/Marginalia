package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.repository.LorebookEntryRepository;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

public class LorebookEntryServiceImpl extends ExtendableServiceImpl<LorebookEntry, LorebookEntryRepository> implements LorebookEntryService {

    @Autowired
    private TagRelationService tagRelationService;

    /**
     * A soft deleted entry takes its tags with it.
     */
    @Override
    @CommonTx
    public LorebookEntry delete(LorebookEntry entry, boolean hard) throws Exception {
        if (!hard && entry != null && entry.getId() != null) {
            tagRelationService.deleteForObject(entry, false);
        }
        return super.delete(entry, hard);
    }

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

    @Override
    @CommonTxReadOnly
    public List<EntryFulltext> findFulltextsContaining(Lorebook lorebook, String literal) throws Exception {
        if (lorebook == null || lorebook.getId() == null) {
            return Collections.emptyList();
        }
        return getRepository().findFulltextsContaining(lorebook.getId(), literal).stream()
                .map(row -> new EntryFulltext((Long) row[0], (String) row[1]))
                .toList();
    }
}
