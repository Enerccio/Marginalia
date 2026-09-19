package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.repository.OwnedRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import jakarta.persistence.TypedQuery;

import java.util.List;

public abstract class JpaOwnedRepository<T extends OwnedEntity> extends JpaBaseRepository<T> implements OwnedRepository<T> {

    @Override
    public List<T> findAll(User user) throws Exception {
        TypedQuery<T> query = entityManager.createQuery("SELECT e FROM " + getEntityType() + " e WHERE e.deleted = false AND e.owner = ?1", getEntityClass())
                .setParameter("owner", user);
        List<T> entities = query.getResultList();
        for (T t : entities)
            hydrate(t);
        return entities;
    }

    @Override
    public List<Long> findAllIds(User user) throws Exception {
        TypedQuery<Long> query = entityManager.createQuery("SELECT e.id FROM " + getEntityType() + " e WHERE e.deleted = false AND e.owner = ?1", Long.class)
                .setParameter("owner", user);
        return query.getResultList();
    }

}
