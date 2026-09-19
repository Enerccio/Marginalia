package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;

import java.util.List;

public interface LorebookEntryRepository extends ExtendableRepository<LorebookEntry> {

    List<LorebookEntry> findByLorebookOrdered(Long lorebookId) throws Exception;

}