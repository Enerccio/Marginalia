package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.repository.ProtocolRepository;

import java.util.List;

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

    /**
     * The protocols that other users share (the flag {@link Protocol#isShared()}), what "Import from other user" lists: not the
     * user's own and not deleted. They are for reading and for {@link #importShared}, the current user can't change them. The
     * owner of each is {@link Protocol#getOwner()}.
     */
    List<Protocol> findAllShared() throws Exception;

    /**
     * Makes a new, private protocol of the current user with the settings of a protocol another user shares, as
     * {@link #cloneProtocol} does.
     *
     * @param name the name of the copy, the name of the protocol when blank
     * @return the new protocol
     * @throws IllegalArgumentException when the protocol is not shared (any more), is the user's own, does not exist or is of
     *                                  a type that can't be copied
     */
    Protocol importShared(Protocol source, String name) throws Exception;

}
