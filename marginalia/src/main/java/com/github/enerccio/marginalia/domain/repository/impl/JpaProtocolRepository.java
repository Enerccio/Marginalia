package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.repository.ProtocolRepository;

public class JpaProtocolRepository extends JpaExtendableRepository<Protocol> implements ProtocolRepository {

    @Override
    protected Class<Protocol> getEntityClass() {
        return Protocol.class;
    }
}
