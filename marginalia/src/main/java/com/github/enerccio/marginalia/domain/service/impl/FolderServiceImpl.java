package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.FolderRepository;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.*;

public class FolderServiceImpl extends ExtendableServiceImpl<Folder, FolderRepository> implements FolderService {

    @Autowired
    private ManuscriptRepository manuscriptRepository;

    @Autowired
    @Lazy
    private ManuscriptService manuscriptService;

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    @Autowired
    private TagRelationService tagRelationService;

    /**
     * Checks the rules before the folder is written, whoever saves it.
     */
    @Override
    @CommonTx
    public Folder save(Folder folder) throws Exception {
        validate(folder);
        return super.save(folder);
    }

    private void validate(Folder folder) throws Exception {
        String name = StringUtils.trim(folder.getName());
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("The name of a folder must not be blank");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("The name of a folder can have at most " + MAX_NAME_LENGTH + " characters");
        }
        if (name.indexOf(SEPARATOR) >= 0) {
            throw new IllegalArgumentException("The name of a folder can't contain " + SEPARATOR);
        }
        folder.setName(name);

        if (folder.getParent() != null) {
            Folder parent = findOwned(folder.getParent());
            if (parent == null) {
                throw new IllegalArgumentException("The parent folder does not exist");
            }
            for (Folder above = parent; above != null; above = above.getParent()) {
                if (folder.getId() != null && folder.getId().equals(above.getId())) {
                    throw new IllegalStateException("A folder can't be put into itself or into a folder below it");
                }
            }
            folder.setParent(parent);
        }
        if (getRepository().findChildByName(folder.getParent(), name, folder.getId(), currentUser) != null) {
            throw new IllegalStateException("A folder named " + name + " already exists here");
        }
    }

    @Override
    @CommonTxReadOnly
    public List<Folder> findChildren(Folder parent) throws Exception {
        return getRepository().findChildren(parent, currentUser);
    }

    @Override
    @CommonTxReadOnly
    public Folder findChild(Folder parent, String name) throws Exception {
        return getRepository().findChildByName(parent, StringUtils.trim(name), null, currentUser);
    }

    @Override
    @CommonTx
    public Folder create(String name, Folder parent) throws Exception {
        Folder folder = new Folder();
        folder.setName(name);
        folder.setParent(parent);
        return save(folder);
    }

    @Override
    @CommonTx
    public Folder rename(Folder folder, String name) throws Exception {
        Folder stored = findOwned(folder);
        if (stored == null) {
            return null;
        }
        stored.setName(name);
        return save(stored);
    }

    @Override
    @CommonTx
    public Folder move(Folder folder, Folder newParent) throws Exception {
        Folder stored = findOwned(folder);
        if (stored == null) {
            return null;
        }
        Folder parent = null;
        if (newParent != null) {
            parent = findOwned(newParent);
            if (parent == null) {
                throw new IllegalArgumentException("The target folder does not exist");
            }
        }
        checkNoLockedBook(subtree(stored));
        stored.setParent(parent);
        return save(stored);
    }

    @Override
    @CommonTxReadOnly
    public List<Folder> getAncestors(Folder folder) throws Exception {
        Folder stored = findOwned(folder);
        LinkedList<Folder> ancestors = new LinkedList<>();
        if (stored != null) {
            for (Folder above = stored.getParent(); above != null; above = above.getParent()) {
                ancestors.addFirst(above);
            }
        }
        return ancestors;
    }

    @Override
    @CommonTxReadOnly
    public String getPath(Folder folder) throws Exception {
        Folder stored = findOwned(folder);
        if (stored == null) {
            return "";
        }
        StringJoiner path = new StringJoiner(String.valueOf(SEPARATOR));
        for (Folder above : getAncestors(stored)) {
            path.add(above.getName());
        }
        path.add(stored.getName());
        return path.toString();
    }

    @Override
    @CommonTxReadOnly
    public Content describeContent(Folder folder) throws Exception {
        Folder stored = findOwned(folder);
        if (stored == null) {
            return new Content(0, 0);
        }
        List<Folder> subtree = subtree(stored);
        Map<Long, Long> direct = manuscriptRepository.countRegularByFolders(currentUser);
        long books = 0;
        for (Folder inside : subtree) {
            books += direct.getOrDefault(inside.getId(), 0L);
        }
        return new Content(subtree.size() - 1, (int) books);
    }

    @Override
    @CommonTxReadOnly
    public Map<Long, Long> countBooksByFolder() throws Exception {
        Map<Long, Long> direct = manuscriptRepository.countRegularByFolders(currentUser);
        Map<Long, Folder> byId = new HashMap<>();
        Map<Long, Long> totals = new HashMap<>();
        for (Folder folder : findAllForUser()) {
            byId.put(folder.getId(), folder);
            totals.put(folder.getId(), 0L);
        }
        direct.forEach((folderId, count) -> {
            // the folder and every one above it
            for (Folder above = byId.get(folderId); above != null; above = above.getParent() == null ? null : byId.get(above.getParent().getId())) {
                totals.merge(above.getId(), count, Long::sum);
            }
        });
        return totals;
    }

    @Override
    @CommonTx
    public Folder delete(Folder folder, boolean hard) throws Exception {
        Folder stored = findOwned(folder);
        if (stored == null) {
            return null;
        }
        checkNoLockedBook(subtree(stored));

        Folder parent = stored.getParent();
        Set<String> taken = new HashSet<>();
        for (Folder sibling : getRepository().findChildren(parent, currentUser)) {
            if (!sibling.getId().equals(stored.getId())) {
                taken.add(sibling.getName().toLowerCase());
            }
        }
        for (Folder child : getRepository().findChildren(stored, currentUser)) {
            if (taken.contains(child.getName().toLowerCase())) {
                throw new IllegalStateException("A folder named " + child.getName() + " already exists in the folder above");
            }
        }

        manuscriptRepository.moveFolderContent(List.of(stored.getId()), parent, currentUser);
        getRepository().reparentChildren(List.of(stored.getId()), parent, currentUser);
        tagRelationService.deleteForObject(stored, hard);
        return super.delete(stored, hard);
    }

    @Override
    @CommonTx
    public void deleteWithContent(Folder folder) throws Exception {
        Folder stored = findOwned(folder);
        if (stored == null) {
            return;
        }
        List<Folder> subtree = subtree(stored);
        checkNoLockedBook(subtree);

        for (Manuscript book : booksIn(subtree)) {
            manuscriptService.delete(book, false);
        }
        // the deepest first, so no folder is deleted before the ones in it
        for (Folder inside : subtree.reversed()) {
            tagRelationService.deleteForObject(inside, false);
            super.delete(inside, false);
        }
    }

    /**
     * The persisted non-deleted folder for the (possibly detached) one, only when it belongs to the current user.
     */
    private Folder findOwned(Folder folder) throws Exception {
        if (folder == null || folder.getId() == null) {
            return null;
        }
        Folder found = getRepository().find(folder.getId());
        boolean usable = found != null && !found.isDeleted() && found.getOwner() != null
                && found.getOwner().getId().equals(currentUser.getId());
        return usable ? found : null;
    }

    /**
     * The folder and every folder below it, a folder before the ones in it.
     */
    private List<Folder> subtree(Folder root) throws Exception {
        Map<Long, List<Folder>> children = new HashMap<>();
        for (Folder folder : findAllForUser()) {
            if (folder.getParent() != null) {
                children.computeIfAbsent(folder.getParent().getId(), _ -> new ArrayList<>()).add(folder);
            }
        }
        List<Folder> subtree = new ArrayList<>();
        subtree.add(root);
        for (int i = 0; i < subtree.size(); i++) {
            subtree.addAll(children.getOrDefault(subtree.get(i).getId(), List.of()));
        }
        return subtree;
    }

    private List<Manuscript> booksIn(List<Folder> folders) throws Exception {
        return manuscriptRepository.findRegularByFolders(folders.stream().map(Folder::getId).toList(), currentUser);
    }

    /**
     * What a book inherits changes with the folders above it, which a book an agent works on does not tolerate.
     */
    private void checkNoLockedBook(List<Folder> folders) throws Exception {
        for (Manuscript book : booksIn(folders)) {
            agentRunService.checkNotLocked(book);
        }
    }
}
