package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.repository.FolderRepository;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.Callable;

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

    @Autowired
    @Lazy
    private BackupService backupService;

    @Autowired
    @Qualifier("common")
    private PlatformTransactionManager transactionManager;

    // the proxy of this service: a call through it has the transaction of the method, a call on this has none
    @Autowired
    @Lazy
    private FolderService self;

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
    @CommonTxReadOnly
    public List<Folder> search(ManuscriptFilterValues filter, Sorter... sorters) throws Exception {
        return getRepository().search(resolveScope(filter), List.of(sorters), currentUser);
    }

    @Override
    @CommonTxReadOnly
    public ManuscriptFilterValues resolveScope(ManuscriptFilterValues filter) throws Exception {
        if (filter == null) {
            return null;
        }
        ManuscriptFilterValues resolved = filter.copy();
        if (!resolved.isScoped()) {
            return resolved;
        }
        if (resolved.getFolder() == null) {
            // the top level: its own content, or everything
            resolved.setResolvedScope(resolved.isIncludeSubfolders() ? null : Set.of(), true);
            return resolved;
        }
        Folder stored = findOwned(resolved.getFolder());
        Set<Long> ids = new HashSet<>();
        if (stored != null) {
            if (resolved.isIncludeSubfolders()) {
                subtree(stored).forEach(folder -> ids.add(folder.getId()));
            } else {
                ids.add(stored.getId());
            }
        }
        resolved.setResolvedScope(ids, false);
        return resolved;
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
    public Folder update(Folder folder, Details details) throws Exception {
        Folder stored = findOwned(folder);
        if (stored == null) {
            return null;
        }
        Map<PromptField, String> changed = new LinkedHashMap<>();
        for (Map.Entry<PromptField, String> prompt : details.prompts().entrySet()) {
            String now = StringUtils.defaultIfBlank(prompt.getKey().ofFolder(stored), null);
            String next = StringUtils.defaultIfBlank(prompt.getValue(), null);
            if (!Objects.equals(now, next)) {
                changed.put(prompt.getKey(), next);
            }
        }
        boolean languageChanged = !Objects.equals(stored.getLanguage(), details.language());
        if (!changed.isEmpty() || languageChanged) {
            checkNoLockedBook(subtree(stored));
            changed.forEach((field, value) -> field.setOfFolder(stored, value));
            stored.setLanguage(details.language());
        }
        stored.setName(details.name());
        stored.setDescription(details.description());
        stored.setHistoryMaxVersions(details.historyMaxVersions() == null || details.historyMaxVersions() < 0 ? null : details.historyMaxVersions());
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
    public Map<Long, String> getPaths() throws Exception {
        Map<Long, Folder> byId = new HashMap<>();
        for (Folder folder : findAllForUser()) {
            byId.put(folder.getId(), folder);
        }
        Map<Long, String> paths = new LinkedHashMap<>();
        for (Folder folder : byId.values()) {
            LinkedList<String> names = new LinkedList<>();
            Set<Long> seen = new HashSet<>();
            for (Folder above = folder; above != null && seen.add(above.getId());
                 above = above.getParent() == null ? null : byId.get(above.getParent().getId())) {
                names.addFirst(above.getName());
            }
            paths.put(folder.getId(), String.join(String.valueOf(SEPARATOR), names));
        }
        return paths;
    }

    @Override
    @CommonTxReadOnly
    public List<Folder> findChain(Folder folder) throws Exception {
        List<Folder> chain = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        Folder current = folder == null || folder.getId() == null ? null : getRepository().find(folder.getId());
        while (current != null && !current.isDeleted() && seen.add(current.getId())) {
            chain.add(current);
            current = current.getParent() == null ? null : getRepository().find(current.getParent().getId());
        }
        return chain;
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

    @Override
    @CommonTxReadOnly
    public List<Folder> findClashes(Collection<Folder> folders, Folder target, boolean copy) throws Exception {
        Folder parent = target == null ? null : findOwned(target);
        List<Folder> clashes = new ArrayList<>();
        for (Folder folder : folders) {
            Folder stored = findOwned(folder);
            if (stored != null && getRepository().findChildByName(parent, stored.getName(), copy ? null : stored.getId(), currentUser) != null) {
                clashes.add(stored);
            }
        }
        return clashes;
    }

    /**
     * What the checks of a transfer found: the items and the target as stored, and the choices.
     */
    private record Checked(Folder target, List<Folder> folders, List<Manuscript> books, Map<Long, Choice> choices) {
    }

    @Override
    public Outcome transfer(Transfer transfer, Runnable step) throws Exception {
        if (!transfer.copy()) {
            // a move is a few bulk updates: all or nothing
            return inTx(() -> execute(transfer, check(transfer), step));
        }
        // A copy reads and writes every book whole and can take long, and the database has one writer: no transaction around it.
        // Everything is checked first (a transaction of its own, nothing is written), then every folder and every book is a step
        // with its own transactions, like the clone of one book. A failure leaves the copies made so far, each of them complete.
        Checked checked = inTx(() -> check(transfer));
        return execute(transfer, checked, step);
    }

    private <T> T inTx(Callable<T> work) throws Exception {
        try {
            return new TransactionTemplate(transactionManager).execute(_ -> {
                try {
                    return work.call();
                } catch (Exception e) {
                    // rolls back also for a checked exception
                    throw new TransferFailure(e);
                }
            });
        } catch (TransferFailure failure) {
            throw failure.cause;
        }
    }

    private static final class TransferFailure extends RuntimeException {
        private final Exception cause;

        TransferFailure(Exception cause) {
            super(cause);
            this.cause = cause;
        }
    }

    private Checked check(Transfer transfer) throws Exception {
        Folder target = null;
        if (transfer.target() != null) {
            target = findOwned(transfer.target());
            if (target == null) {
                throw new IllegalArgumentException("The target folder does not exist");
            }
        }
        List<Folder> folders = new ArrayList<>();
        for (Folder folder : transfer.folders()) {
            Folder stored = findOwned(folder);
            if (stored == null) {
                throw new IllegalArgumentException("The folder does not exist");
            }
            folders.add(stored);
        }
        List<Manuscript> books = new ArrayList<>();
        for (Manuscript book : transfer.books()) {
            Manuscript stored = book == null || book.getUuid() == null ? null : manuscriptService.findForUser(book.getUuid());
            if (stored == null || stored.isDeleted()) {
                throw new IllegalArgumentException("The book does not exist");
            }
            books.add(stored);
        }

        // everything is checked before anything changes
        Map<Long, Choice> choices = transfer.choices() == null ? Map.of() : transfer.choices();
        List<Folder> involved = new ArrayList<>();
        for (Folder folder : folders) {
            for (Folder above = target; above != null; above = above.getParent()) {
                if (above.getId().equals(folder.getId())) {
                    throw new IllegalStateException("A folder can't be put into itself or into a folder below it");
                }
            }
            Folder same = getRepository().findChildByName(target, folder.getName(), transfer.copy() ? null : folder.getId(), currentUser);
            if (same != null) {
                Choice choice = choices.get(folder.getId());
                if (choice == null || choice.resolution() == null) {
                    throw new IllegalStateException("A folder named " + folder.getName() + " already exists here");
                }
                switch (choice.resolution()) {
                    case SKIP -> {
                        continue;
                    }
                    case MERGE -> {
                        if (same.getId().equals(folder.getId())) {
                            throw new IllegalStateException("A folder can't be merged into itself");
                        }
                    }
                    case RENAME -> {
                        String name = StringUtils.trim(choice.name());
                        if (StringUtils.isEmpty(name) || getRepository().findChildByName(target, name, null, currentUser) != null) {
                            throw new IllegalStateException("A folder named " + name + " already exists here");
                        }
                    }
                }
            }
            involved.addAll(subtree(folder));
        }
        checkNoLockedBook(involved);
        for (Manuscript book : books) {
            agentRunService.checkNotLocked(book);
        }
        return new Checked(target, folders, books, choices);
    }

    private Outcome execute(Transfer transfer, Checked checked, Runnable step) throws Exception {
        Folder target = checked.target();
        List<Folder> folders = checked.folders();
        List<Manuscript> books = checked.books();
        Map<Long, Choice> choices = checked.choices();
        int[] counts = new int[3];
        if (!books.isEmpty()) {
            if (transfer.copy()) {
                for (Manuscript book : books) {
                    backupService.cloneManuscript(book, book.getName(), transfer.onlyCurrentBranch(), target);
                    step.run();
                }
            } else {
                manuscriptService.moveToFolder(books, target);
            }
            counts[0] += books.size();
        }
        for (Folder folder : folders) {
            String name = folder.getName();
            Folder same = getRepository().findChildByName(target, name, transfer.copy() ? null : folder.getId(), currentUser);
            if (same != null) {
                Choice choice = choices.get(folder.getId());
                switch (choice.resolution()) {
                    case SKIP -> {
                        counts[2]++;
                        continue;
                    }
                    case MERGE -> {
                        mergeInto(folder, same, transfer.copy(), transfer.onlyCurrentBranch(), counts, step);
                        continue;
                    }
                    case RENAME -> name = StringUtils.trim(choice.name());
                }
            }
            if (transfer.copy()) {
                copyTree(folder, target, name, transfer.onlyCurrentBranch(), counts, step);
            } else if (!sameFolder(folder.getParent(), target) || !name.equals(folder.getName())) {
                counts[0] += countBooks(subtree(folder));
                folder.setName(name);
                folder.setParent(target);
                save(folder);
                counts[1]++;
            }
        }
        return new Outcome(counts[0], counts[1], counts[2]);
    }

    private static boolean sameFolder(Folder a, Folder b) {
        return a == null ? b == null : b != null && a.getId().equals(b.getId());
    }

    private int countBooks(List<Folder> folders) throws Exception {
        Map<Long, Long> direct = manuscriptRepository.countRegularByFolders(currentUser);
        long books = 0;
        for (Folder folder : folders) {
            books += direct.getOrDefault(folder.getId(), 0L);
        }
        return (int) books;
    }

    /**
     * Puts what is in the source into the folder of the same name (a move leaves the source empty and deletes it, a copy
     * leaves it alone); the folders of the same name inside merge in turn.
     */
    private void mergeInto(Folder source, Folder existing, boolean copy, boolean onlyCurrentBranch, int[] counts, Runnable step) throws Exception {
        List<Manuscript> books = manuscriptRepository.findRegularByFolders(List.of(source.getId()), currentUser);
        if (copy) {
            for (Manuscript book : books) {
                backupService.cloneManuscript(book, book.getName(), onlyCurrentBranch, existing);
                step.run();
            }
        } else {
            manuscriptRepository.moveFolderContent(List.of(source.getId()), existing, currentUser);
        }
        counts[0] += books.size();
        for (Folder child : getRepository().findChildren(source, currentUser)) {
            Folder same = getRepository().findChildByName(existing, child.getName(), null, currentUser);
            if (same != null) {
                mergeInto(child, same, copy, onlyCurrentBranch, counts, step);
            } else if (copy) {
                copyTree(child, existing, child.getName(), onlyCurrentBranch, counts, step);
            } else {
                child.setParent(existing);
                save(child);
                counts[1]++;
            }
        }
        if (!copy) {
            tagRelationService.deleteForObject(source, false);
            super.delete(source, false);
        }
    }

    /**
     * A copy of the folder with its settings, tags, books and the folders below it, as a folder of the parent.
     */
    private Folder copyTree(Folder source, Folder parent, String name, boolean onlyCurrentBranch, int[] counts, Runnable step) throws Exception {
        Folder copy = new Folder();
        copy.setName(name);
        copy.setParent(parent);
        copy.setDescription(source.getDescription());
        copy.setLanguage(source.getLanguage());
        copy.setTemplate(source.getTemplate());
        copy.setPov(source.getPov());
        copy.setTense(source.getTense());
        copy.setStyle(source.getStyle());
        copy.setUserPrompt(source.getUserPrompt());
        copy.setSummaryPrompt(source.getSummaryPrompt());
        copy.setMetaSummaryPrompt(source.getMetaSummaryPrompt());
        copy.setRedraftSystemPrompt(source.getRedraftSystemPrompt());
        copy.setRedraftPrompt(source.getRedraftPrompt());
        copy.setAnnotateSystemPrompt(source.getAnnotateSystemPrompt());
        copy.setAnnotatePrompt(source.getAnnotatePrompt());
        copy.setImpersonatePrompt(source.getImpersonatePrompt());
        copy.setHistoryMaxVersions(source.getHistoryMaxVersions());
        copy = self.save(copy);
        for (Tag tag : tagRelationService.getTagsForObject(source)) {
            tagRelationService.createRelation(tag, copy);
        }
        counts[1]++;
        step.run();
        for (Manuscript book : manuscriptRepository.findRegularByFolders(List.of(source.getId()), currentUser)) {
            backupService.cloneManuscript(book, book.getName(), onlyCurrentBranch, copy);
            step.run();
            counts[0]++;
        }
        for (Folder child : getRepository().findChildren(source, currentUser)) {
            copyTree(child, copy, child.getName(), onlyCurrentBranch, counts, step);
        }
        return copy;
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
            try {
                agentRunService.checkNotLocked(book);
            } catch (ManuscriptLockedException e) {
                throw new FolderBookLockedException(e.getAgentName(), book.getName());
            }
        }
    }
}
