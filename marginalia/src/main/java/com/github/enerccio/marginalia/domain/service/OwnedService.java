package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.repository.OwnedRepository;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.List;

public interface OwnedService<T extends OwnedEntity, R extends OwnedRepository<T>> extends BaseService<T, R> {

    List<T> findAll(User user) throws Exception;
    List<Long> findAllIds(User user) throws Exception;
    List<T> findAllForUser() throws Exception;
    List<Long> findAllIdsForUser() throws Exception;

}
