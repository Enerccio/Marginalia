package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.List;

public interface ProtocolRepository extends ExtendableRepository<Protocol> {

    /**
     * The protocols that other users share with everybody: not the user's own, not deleted, of an owner that is not deleted.
     */
    List<Protocol> findAllShared(User user) throws Exception;

    /**
     * The protocol with the uuid when it is one of {@link #findAllShared}, null otherwise.
     */
    Protocol findShared(String uuid, User user) throws Exception;

}
