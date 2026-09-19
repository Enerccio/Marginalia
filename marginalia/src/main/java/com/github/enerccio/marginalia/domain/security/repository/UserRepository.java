package com.github.enerccio.marginalia.domain.security.repository;

import com.github.enerccio.marginalia.domain.repository.BaseRepository;
import com.github.enerccio.marginalia.domain.security.model.User;

public interface UserRepository extends BaseRepository<User> {

    Long findByName(String name) throws Exception;

    boolean existsUsers() throws Exception;

}
