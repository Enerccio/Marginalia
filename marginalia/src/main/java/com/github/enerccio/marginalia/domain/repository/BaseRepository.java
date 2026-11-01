package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.BaseEntity;

import java.util.List;

public interface BaseRepository<T extends BaseEntity> {

    T find(Long id) throws Exception;
    Long find(String uuid) throws Exception;
    List<T> findAll() throws Exception;
    List<Long> findAllIds() throws Exception;
    T save(T t) throws Exception;
    T saveWithoutEvent(T t) throws Exception;

    /**
     * Saves the entity as it is: {@code modification} is not set to now, and nothing that follows a change of the entity
     * (see {@code JpaBaseRepository#changed}, a book changes when its part does) happens. For a save that is not a change of
     * the content, like moving to another branch. A new entity still gets its first {@code modification}.
     */
    T saveWithoutModification(T t) throws Exception;
    T delete(T entity, boolean hard) throws Exception;
    void evict(T entity) throws Exception;

}
