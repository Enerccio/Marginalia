package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;

public interface ExtendableRepository<T extends ExtendableEntity> extends OwnedRepository<T> {

}
