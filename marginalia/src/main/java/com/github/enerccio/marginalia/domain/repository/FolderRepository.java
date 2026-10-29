package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.security.model.User;

import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;

import java.util.Collection;
import java.util.List;

public interface FolderRepository extends ExtendableRepository<Folder> {

    /**
     * The folders of the user directly in the folder (null is the top level), not deleted, by name.
     */
    List<Folder> findChildren(Folder parent, User user) throws Exception;

    /**
     * The folders of the user that match the filter (name starts with, carries all the tags, in the resolved scope of
     * {@link ManuscriptFilterValues#inFolder}: the scope applies to the parent of the folder), not deleted, ordered by the
     * sorters (the attributes of the folder: name, creation, modification).
     */
    List<Folder> search(ManuscriptFilterValues filter, List<Sorter> sorters, User user) throws Exception;

    /**
     * The folder of the user with exactly this name (case insensitive) directly in the folder (null is the top level),
     * not deleted, other than the folder with the given id (null for none).
     */
    Folder findChildByName(Folder parent, String name, Long excludeId, User user) throws Exception;

    /**
     * Moves the folders that are directly in one of the folders to another folder (null is the top level) with a bulk
     * update. Returns the number of folders.
     */
    int reparentChildren(Collection<Long> fromParentIds, Folder to, User user) throws Exception;
}
