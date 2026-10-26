package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.List;
import java.util.Set;

public interface ResourceRepository extends OwnedRepository<Resource> {

    Resource findByHash(String hash, User owner) throws Exception;

    /**
     * @param sortProperty one of creation, originalName, mimeType, size; anything else sorts by creation
     */
    List<Resource> findPage(User owner, int offset, int limit, String sortProperty, boolean ascending) throws Exception;

    long count(User owner) throws Exception;

    /**
     * @return the stored file names of all resources of all users, the soft deleted ones included
     */
    Set<String> findAllPaths();

    /**
     * @return true if there is a not deleted entity of the class with the id; false also for a class that is not an
     * entity of the application
     */
    boolean entityExists(String clazz, Long id);

}
