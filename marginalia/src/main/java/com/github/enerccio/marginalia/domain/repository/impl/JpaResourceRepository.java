package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.repository.ResourceRepository;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.List;

public class JpaResourceRepository extends JpaOwnedRepository<Resource> implements ResourceRepository {

    @Override
    protected Class<Resource> getEntityClass() {
        return Resource.class;
    }

    @Override
    public Resource findByHash(String hash, User owner) throws Exception {
        List<Resource> resources = getEntityManager()
                .createQuery("SELECT r FROM Resource r WHERE r.hash = :hash AND r.owner.id = :owner", Resource.class)
                .setParameter("owner", owner.getId())
                .setParameter("hash", hash)
                .getResultList();
        return resources.isEmpty() ? null : resources.getFirst();
    }
}
