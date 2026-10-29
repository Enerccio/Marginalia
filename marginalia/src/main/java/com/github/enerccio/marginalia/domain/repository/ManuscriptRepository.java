package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptSearchCriteria;
import com.github.enerccio.marginalia.domain.service.search.Sorter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ManuscriptRepository extends ExtendableRepository<Manuscript> {

    List<Long> searchManuscripts(ManuscriptFilterValues filterValues, List<Sorter> sorters, User user) throws Exception;

    /**
     * @param agentic true for the working copies of agents, false for the books of the user, null for both
     */
    List<Long> searchManuscripts(ManuscriptFilterValues filterValues, List<Sorter> sorters, User user, Boolean agentic) throws Exception;

    /**
     * Ids of the books of the user that pass the checks of the criteria that the database can make (everything but the
     * text), the working copies of agents are left out.
     */
    List<Long> searchManuscripts(ManuscriptSearchCriteria criteria, List<Sorter> sorters, User user) throws Exception;

    /**
     * @param agentic true for the working copies of agents, false for the books of the user
     */
    List<Manuscript> findAllByAgentic(boolean agentic, User user) throws Exception;

    Manuscript findViewable(String uuid, User user) throws Exception;

    void markOpened(Long id, User user) throws Exception;

    /**
     * The books of the user in the folders (the working copies of agents are left out), not deleted.
     */
    List<Manuscript> findRegularByFolders(Collection<Long> folderIds, User user) throws Exception;

    /**
     * How many books of the user are in each folder, keyed by folder id (a folder without a book is not in the map); the
     * working copies of agents are left out, not deleted.
     */
    Map<Long, Long> countRegularByFolders(User user) throws Exception;

    /**
     * The uuids, out of the given ones, of the books of the user that exist (not deleted, no agent working copies).
     */
    Set<String> findExistingRegularUuids(Collection<String> uuids, User user) throws Exception;

    /**
     * Puts the books of the user on the folder (null is the top level) with a bulk update: neither the book nor its
     * {@code modification} is saved. Returns the number of books.
     */
    int moveToFolder(Collection<Long> bookIds, Folder folder, User user) throws Exception;

    /**
     * Moves all books (the working copies of agents too) that are in one of the folders to another folder.
     */
    int moveFolderContent(Collection<Long> fromFolderIds, Folder to, User user) throws Exception;

}
