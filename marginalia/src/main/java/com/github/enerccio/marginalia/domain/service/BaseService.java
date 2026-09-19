package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.repository.BaseRepository;

import java.util.List;

public interface BaseService<T extends BaseEntity, R extends BaseRepository<T>> {

    T find(Long id) throws Exception;
    Long find(String uuid) throws Exception;
    List<T> findAll() throws Exception;
    List<Long> findAllIds() throws Exception;
    T save(T t) throws Exception;
    T delete(T entity, boolean hard) throws Exception;

}
