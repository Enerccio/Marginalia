package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.repository.FolderRepository;

import java.util.List;
import java.util.Map;

/**
 * The folders of the books. Every folder belongs to a user and has a name that is unique (case insensitive) among the
 * folders of its parent; the rules (name, parent of the user's own, no cycle) are checked by every {@link #save}.
 * <p>
 * Throws {@link IllegalArgumentException} for what can't be right (a blank name, a folder that is not the user's) and
 * {@link IllegalStateException} for what is wrong now (the name is taken, a folder into itself).
 * A book an agent works on blocks everything that would change what it inherits (move and delete of a folder above it),
 * see {@code ManuscriptLockedException}.
 */
public interface FolderService extends ExtendableService<Folder, FolderRepository> {

    int MAX_NAME_LENGTH = 255;

    /**
     * Separates the names in a path; a name can't contain it.
     */
    char SEPARATOR = '/';

    /**
     * What is below a folder, all levels: the number of folders (the folder itself not counted) and of books (the working
     * copies of agents not counted).
     */
    record Content(int folders, int books) {
    }

    /**
     * The folders of the current user directly in the folder (null is the top level) by name.
     */
    List<Folder> findChildren(Folder parent) throws Exception;

    /**
     * The folder of the current user with the name (case insensitive) directly in the folder (null is the top level), null
     * when there is none.
     */
    Folder findChild(Folder parent, String name) throws Exception;

    Folder create(String name, Folder parent) throws Exception;

    /**
     * Renames the folder, allowed while an agent works on a book in it. Returns null when the folder no longer exists.
     */
    Folder rename(Folder folder, String name) throws Exception;

    /**
     * Puts the folder, with everything in it, into another folder (null is the top level). Returns null when the folder no
     * longer exists.
     */
    Folder move(Folder folder, Folder newParent) throws Exception;

    /**
     * The folders above the folder, the top one first (empty for a folder of the top level).
     */
    List<Folder> getAncestors(Folder folder) throws Exception;

    /**
     * The names from the top folder to the folder, separated by {@link #SEPARATOR}; empty for null (the top level).
     */
    String getPath(Folder folder) throws Exception;

    /**
     * The folder and the folders above it, the folder itself first, as they are stored now (by id, no current user needed,
     * so a generation can ask from any thread). It stops at a folder that was deleted. Empty for null: this is the chain
     * that a book in the folder inherits its settings through.
     */
    List<Folder> findChain(Folder folder) throws Exception;

    /**
     * What is below the folder, for the question that asks before everything is deleted.
     */
    Content describeContent(Folder folder) throws Exception;

    /**
     * The number of books in each folder of the current user with the books of the folders below it (so the top folders
     * count everything); every folder is a key, an empty one has 0.
     */
    Map<Long, Long> countBooksByFolder() throws Exception;

    /**
     * Deletes the folder like {@link #delete}, but the books and folders in it are deleted (soft) with it, not moved.
     */
    void deleteWithContent(Folder folder) throws Exception;

    /**
     * {@inheritDoc}
     * <p>
     * The books and the folders directly in the folder are moved to its parent, and the folder is deleted. A folder in it
     * that has the name of a folder of the parent makes it fail (nothing is moved), as names are unique.
     */
    @Override
    Folder delete(Folder folder, boolean hard) throws Exception;
}
