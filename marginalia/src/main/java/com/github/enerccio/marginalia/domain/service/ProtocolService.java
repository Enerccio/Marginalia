package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.repository.ProtocolRepository;

public interface ProtocolService extends ExtendableService<Protocol, ProtocolRepository> {

    /**
     * Makes a new protocol of the user with the settings of the protocol: the token limits, the sampling parameters
     * and the attributes of extensions.
     *
     * @param name the name of the clone, the name of the protocol when blank
     * @return the new protocol
     * @throws IllegalArgumentException when the protocol is not the user's or is of a type that can't be cloned
     */
    Protocol cloneProtocol(Protocol source, String name) throws Exception;

}
