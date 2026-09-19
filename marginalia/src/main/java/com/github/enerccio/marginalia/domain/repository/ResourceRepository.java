package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.security.model.User;

public interface ResourceRepository extends OwnedRepository<Resource> {

    Resource findByHash(String hash, User owner) throws Exception;

}
