package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.repository.ExtendableRepository;

public abstract class JpaExtendableRepository<T extends ExtendableEntity> extends JpaOwnedRepository<T> implements ExtendableRepository<T> {

}
