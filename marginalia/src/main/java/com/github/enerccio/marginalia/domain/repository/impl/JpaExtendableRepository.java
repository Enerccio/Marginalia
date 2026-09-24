package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.repository.ExtendableRepository;

public abstract class JpaExtendableRepository<T extends ExtendableEntity> extends JpaOwnedRepository<T> implements ExtendableRepository<T> {

    protected final ExtendableEntityListener listener = new ExtendableEntityListener();

    @Override
    public T save(T entity) throws Exception {
        listener.serialize(entity);
        entity = super.save(entity);
        listener.deserialize(entity);
        return entity;
    }
}
