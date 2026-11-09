package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface ResourceRepository extends OwnedRepository<Resource> {

    Resource findByHash(String hash, User owner) throws Exception;

    /**
     * A resource that is linked to a part of a book the reader may view (their own, or a published one), whoever owns the book.
     *
     * @return the not deleted resource with the uuid that belongs to the owner of the book and is linked to a not deleted part of the book, null
     * when the book can't be viewed by the reader or the resource is none of these
     */
    Resource findOfViewableBook(String uuid, String manuscriptUuid, User reader) throws Exception;

    /**
     * @param sortProperty one of creation, originalName, mimeType, size; anything else sorts by creation
     */
    List<Resource> findPage(User owner, int offset, int limit, String sortProperty, boolean ascending) throws Exception;

    long count(User owner) throws Exception;

    /**
     * Resources of one object, see {@link Resource#getClazz()}.
     *
     * @param count   number of the not deleted resources of the user
     * @param size    sum of their sizes
     * @param newest  creation of the newest
     * @param clazz   null for the resources that are not linked to anything
     */
    record ResourceGroup(String clazz, Long objectId, long count, long size, Date newest) {
    }

    /**
     * The not deleted resources of the user grouped by the object that uses them (one group for all that are linked
     * to nothing).
     *
     * @param sortProperty one of creation (newest resource of the group), size (sum), anything else sorts by the class
     *                     and id of the object
     */
    List<ResourceGroup> findGroupPage(User owner, int offset, int limit, String sortProperty, boolean ascending) throws Exception;

    long countGroups(User owner) throws Exception;

    /**
     * Same as {@link #findPage} for the resources of one group, null class and id are the resources linked to nothing.
     */
    List<Resource> findPageOfGroup(User owner, String clazz, Long objectId, int offset, int limit, String sortProperty, boolean ascending) throws Exception;

    long countOfGroup(User owner, String clazz, Long objectId) throws Exception;

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
