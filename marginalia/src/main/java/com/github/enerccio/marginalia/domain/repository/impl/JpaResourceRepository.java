package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.repository.ResourceRepository;

public class JpaResourceRepository extends JpaOwnedRepository<Resource> implements ResourceRepository {

    @Override
    protected Class<Resource> getEntityClass() {
        return Resource.class;
    }

}
