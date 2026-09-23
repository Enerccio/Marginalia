package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.repository.LorebookRepository;

import java.util.List;

public interface LorebookService extends ExtendableService<Lorebook, LorebookRepository> {

    List<Lorebook> getSubbooks(Lorebook lorebook) throws Exception;

    Lorebook fillEntries(Lorebook book) throws Exception;

}