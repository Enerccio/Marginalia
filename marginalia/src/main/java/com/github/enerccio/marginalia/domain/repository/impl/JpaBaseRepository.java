package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.repository.BaseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("SqlSourceToSinkFlow")
public abstract class JpaBaseRepository<T extends BaseEntity> implements BaseRepository<T> {

    protected EntityManager entityManager;

    protected abstract Class<T> getEntityClass();

    protected String getEntityType() {
        return getEntityClass().getSimpleName();
    }

    protected T hydrate(T t) throws Exception {
        return t;
    }

    @Override
    public T find(Long id) throws Exception {
        if (id == null) return null;
        return hydrate(entityManager.find(getEntityClass(), id));
    }

    @Override
    public Long find(String uuid) throws Exception {
        List<Long> results = entityManager.createQuery(
                "SELECT e.id FROM " + getEntityType() + " e WHERE e.uuid = :uuid",
                Long.class
        ).setParameter("uuid", uuid).setMaxResults(1).getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return results.getFirst();
    }

    @Override
    public List<T> findAll() throws Exception {
        TypedQuery<T> query = entityManager.createQuery("SELECT e FROM " + getEntityType() + " e WHERE e.deleted = false", getEntityClass());
        List<T> entities = query.getResultList();
        for (T t : entities)
            hydrate(t);
        return entities;
    }

    @Override
    public List<Long> findAllIds() throws Exception {
        TypedQuery<Long> query = entityManager.createQuery("SELECT e.id FROM " + getEntityType() + " e WHERE e.deleted = false", Long.class);
        return query.getResultList();
    }

    @Override
    public T save(T entity) throws Exception {
        if (entity.getUuid() == null) {
            entity.setUuid(UUID.randomUUID().toString());
        }
        if (entity.getCreation() == null) {
            entity.setCreation(new Date());
        }
        entity.setModification(new Date());

        if (entity.getId() == null) {
            entityManager.persist(entity);
            entityManager.flush();
            return entity;
        } else {
            entity = entityManager.merge(entity);
            entityManager.flush();
            return entity;
        }
    }

    @Override
    public T delete(T entity, boolean hard) throws Exception {
        if (!hard) {
            entity.setDeleted(true);
            return save(entity);
        }
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
        return null;
    }

    @Override
    public void evict(T entity) throws Exception {
        getEntityManager().detach(entity);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
