package com.github.enerccio.marginalia.domain.service;

import java.util.Collection;

/**
 * The kinds of anchored passages the extensions brought (see {@link PassageKind}). An extension registers its kinds when it is
 * loaded and unregisters them when it is unloaded; the passages stay in the data, they are only not shown while their kind is not
 * registered.
 */
public interface PassageKinds {

    /**
     * Registers the kind, replacing the one with the same id.
     */
    void register(PassageKind kind);

    void unregister(PassageKind kind);

    /**
     * @return the kind with the id, null when no extension brings it (it is not loaded)
     */
    PassageKind get(String id);

    /**
     * @return the registered kinds
     */
    Collection<PassageKind> all();

}
