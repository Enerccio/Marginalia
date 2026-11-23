package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.repository.ExtendableRepository;
import com.github.enerccio.marginalia.domain.service.ExtendableService;

public class ExtendableServiceImpl<T extends ExtendableEntity, R extends ExtendableRepository<T>> extends OwnedServiceImpl<T, R> implements ExtendableService<T, R> {

}
