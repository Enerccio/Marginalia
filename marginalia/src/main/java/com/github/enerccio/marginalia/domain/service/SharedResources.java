package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.security.model.User;

/**
 * The resources a user may see although somebody else owns them, because they belong to something shared with them (a
 * published book, in Marginalia: the files of its parts). {@link ResourceService#findImageForReader} and
 * {@link ResourceService#readForReader} ask the bean of this type; without one nothing of others is shown.
 */
public interface SharedResources {

    /**
     * @param resourceUuid the uuid of the resource
     * @param sharedUuid   the uuid of what is shared (the book)
     * @param reader       the user who asks
     * @param image        true for an image (only when the shared thing shows its images to readers), false for any
     *                     other file
     * @return the resource (not deleted, of the owner of the shared thing), null when the reader may not see it
     */
    Resource findForReader(String resourceUuid, String sharedUuid, User reader, boolean image) throws Exception;
}
