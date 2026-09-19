package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.List;

public interface OwnedRepository<T extends OwnedEntity> extends BaseRepository<T> {

    List<T> findAll(User user) throws Exception;
    List<Long> findAllIds(User user) throws Exception;

}
