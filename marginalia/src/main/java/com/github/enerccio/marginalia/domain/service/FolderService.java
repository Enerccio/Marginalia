package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.FolderRepository;

import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;

import java.util.Collection;
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
     * What to do with a folder that has the name of one in the target.
     */
    enum Resolution {
        /** The folder gets another name ({@link Choice#name()}). */
        RENAME,
        /** The folder is left where it is (and not copied). */
        SKIP,
        /** What is in it is put into the folder of the target, the subfolders of the same name merge in turn. */
        MERGE
    }

    /**
     * The decision for one folder; the name is only for {@link Resolution#RENAME}.
     */
    record Choice(Resolution resolution, String name) {
    }

    /**
     * A copy or a move of books and folders into a folder.
     *
     * @param target           the folder to put them into, null is the top level
     * @param copy             copy (books are cloned, folders are copied with all that is in them) instead of move
     * @param onlyCurrentBranch for the books that are copied: only the branch of the story the book is set to
     * @param choices          for each folder (by id) that has the name of a folder in the target, what to do, see
     *                         {@link #findClashes}; books never clash (their names may repeat)
     */
    record Transfer(List<Manuscript> books, List<Folder> folders, Folder target, boolean copy, boolean onlyCurrentBranch,
                    Map<Long, Choice> choices) {
    }

    /**
     * What a transfer did: the books and the folders moved or copied (folders merged into others not counted) and the
     * folders left out.
     */
    record Outcome(int books, int folders, int skipped) {
    }

    /**
     * The folders, out of the given ones, that a transfer to the target would find a folder of their name in (a move to the
     * folder they are in is no clash: it changes nothing).
     */
    List<Folder> findClashes(Collection<Folder> folders, Folder target, boolean copy) throws Exception;

    /**
     * Moves or copies books and folders. Everything is checked before anything is changed: the target and the items must
     * be the user's, a folder can't go into itself or a folder below it, a clash needs a choice, and a book an agent works
     * on (also in a folder that is moved or copied) refuses the whole transfer.
     * <p>
     * A copy of a folder has the settings, tags and description of the folder, and copies of the books in it (and of the
     * folders below it).
     * <p>
     * A move is one transaction. A copy is not (it can take long and the database has one writer): the checks are, then every
     * folder and every book is a step of its own, so when one fails the copies made before it stay, each of them complete.
     *
     * @throws IllegalStateException when the rules are broken
     */
    default Outcome transfer(Transfer transfer) throws Exception {
        return transfer(transfer, () -> {
        });
    }

    /**
     * Like {@link #transfer(Transfer)}, and tells after every step of a copy that is done: a folder made, a book copied (the
     * progress to show; the steps of a move are not counted).
     */
    Outcome transfer(Transfer transfer, Runnable step) throws Exception;

    /**
     * The folders of the current user directly in the folder (null is the top level) by name.
     */
    List<Folder> findChildren(Folder parent) throws Exception;

    /**
     * The folder of the current user with the name (case insensitive) directly in the folder (null is the top level), null
     * when there is none.
     */
    Folder findChild(Folder parent, String name) throws Exception;

    /**
     * The folders of the current user that match the filter, in its folder scope (the folders directly in the folder, or
     * all below it) by the sorters (attributes name, creation, modification; by name when none).
     */
    List<Folder> search(ManuscriptFilterValues filter, Sorter... sorters) throws Exception;

    /**
     * A copy of the filter with its folder scope worked out (the folder and the ones below it), ready for the repositories;
     * a filter that is not scoped, or a folder that does not exist (nothing matches), is handled as well.
     */
    ManuscriptFilterValues resolveScope(ManuscriptFilterValues filter) throws Exception;

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
     * The path of every folder of the current user ({@link #getPath} for all of them at once, from the folder tree in
     * memory), by the id of the folder: for the pickers and the *Folder* column of the list.
     */
    Map<Long, String> getPaths() throws Exception;

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
