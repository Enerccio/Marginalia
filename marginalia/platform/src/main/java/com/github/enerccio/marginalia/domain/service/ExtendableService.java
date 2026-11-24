package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.repository.ExtendableRepository;

public interface ExtendableService<T extends ExtendableEntity, R extends ExtendableRepository<T>> extends OwnedService<T, R> {

}
