package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.repository.ProtocolRepository;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.List;

public class JpaProtocolRepository extends JpaExtendableRepository<Protocol> implements ProtocolRepository {

    private static final String SHARED = "SELECT p FROM Protocol p WHERE p.deleted = false AND p.shared = true"
            + " AND p.owner.id <> :userId AND p.owner.deleted = false";

    @Override
    protected Class<Protocol> getEntityClass() {
        return Protocol.class;
    }

    @Override
    public List<Protocol> findAllShared(User user) throws Exception {
        List<Protocol> protocols = getEntityManager().createQuery(SHARED, Protocol.class)
                .setParameter("userId", user.getId())
                .getResultList();
        for (Protocol protocol : protocols) {
            hydrate(protocol);
        }
        return protocols;
    }

    @Override
    public Protocol findShared(String uuid, User user) throws Exception {
        List<Protocol> protocols = getEntityManager().createQuery(SHARED + " AND p.uuid = :uuid", Protocol.class)
                .setParameter("userId", user.getId())
                .setParameter("uuid", uuid)
                .setMaxResults(1)
                .getResultList();
        return protocols.isEmpty() ? null : hydrate(protocols.getFirst());
    }
}
