package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.components.*;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptStatistic;
import com.github.enerccio.marginalia.domain.repository.FolderRepository;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.repository.ManuscriptStatisticRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ManuscriptStatisticServiceImpl extends OwnedServiceImpl<ManuscriptStatistic, ManuscriptStatisticRepository>
        implements ManuscriptStatisticService {

    private static final Logger log = LoggerFactory.getLogger(ManuscriptStatisticServiceImpl.class);

    @Autowired
    @Lazy
    private FolderService folderService;

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    @Autowired
    @Lazy
    private ActivityIntervalService intervalService;

    @Autowired
    @Lazy
    private ManuscriptSizeService sizeService;

    @Autowired
    @Lazy
    private ManuscriptService manuscriptService;

    @Autowired
    @Lazy
    private ManuscriptRepository manuscriptRepository;

    @Autowired
    @Lazy
    private FolderRepository folderRepository;

    /** The name and the path of a book as the last writer told them, to find a rename that nobody announced. */
    private record Names(String name, String path) {
    }

    private static final int MAX_REMEMBERED = 5_000;
    private final Map<String, Names> names = new ConcurrentHashMap<>();

    /**
     * The clock that tells the day, the one of the server's zone; a test sets its own.
     */
    private Clock clock = Clock.systemDefaultZone();

    @Override
    @CommonTx
    public void count(Manuscript book, StatisticCounts delta) {
        count(book, false, delta);
    }

    @Override
    @CommonTx
    public void count(Manuscript book, boolean agent, StatisticCounts delta) {
        if (book == null || book.getUuid() == null || book.getOwner() == null || delta == null || delta.isZero()) {
            return;
        }
        try {
            Manuscript target = book;
            boolean agentSide = agent;
            if (book.isAgentic()) {
                // the hidden working copy of an agent is not a book of the author: what happens to it is what the agent did to
                // the original, counted for the agents' side. A copy that no run has (it is being made or dropped) is not counted.
                target = agentRunService.findOriginalOfCopy(book);
                agentSide = true;
                if (target == null || target.getOwner() == null) {
                    return;
                }
            }
            Date now = new Date(clock.millis());
            LocalDate day = LocalDate.now(clock);
            String path = folderPathOf(target);
            noteBook(target, path);
            if (getRepository().add(target.getOwner(), target.getUuid(), day, agentSide, delta, target.getName(), path, now) > 0) {
                return;
            }
            // the writers go one at a time (a transaction that writes takes the write lock when it begins), so nobody makes the row between the update and this
            ManuscriptStatistic row = new ManuscriptStatistic();
            row.setOwner(target.getOwner());
            row.setBookUuid(target.getUuid());
            row.setBookName(target.getName());
            row.setFolderPath(path);
            row.setDay(day);
            row.setAgent(agentSide);
            row.setCounts(delta);
            row.setLastChange(now);
            save(row);
        } catch (Exception e) {
            log.warn("Failed to count the statistics of book {}", book.getUuid(), e);
        }
    }

    @Override
    @CommonTx
    public void refreshBook(Manuscript book) {
        if (book == null || book.getUuid() == null || book.getOwner() == null || book.isDeleted() || book.isAgentic()) {
            return;
        }
        try {
            refreshRows(book.getOwner(), book.getUuid(), book.getName(), folderPathOf(book));
        } catch (Exception e) {
            log.warn("Failed to refresh the name of book {} in the statistics", book.getUuid(), e);
        }
    }

    @Override
    @CommonTx
    public void refreshBooks(Collection<Manuscript> books, Folder folder) {
        try {
            String path = pathOf(folder);
            for (Manuscript book : books) {
                if (book != null && book.getUuid() != null && book.getOwner() != null && !book.isDeleted() && !book.isAgentic()) {
                    refreshRows(book.getOwner(), book.getUuid(), book.getName(), path);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to refresh the folder of the books in the statistics", e);
        }
    }

    @Override
    @CommonTx
    public void refreshFolders() {
        try {
            Map<Long, FolderPlacement> folders = new HashMap<>();
            for (FolderPlacement placement : folderRepository.findPlacements(currentUser)) {
                folders.put(placement.id(), placement);
            }
            Map<Long, String> paths = new HashMap<>();
            for (BookPlacement book : manuscriptRepository.findPlacements(currentUser)) {
                String path = book.folderId() == null ? "" : paths.computeIfAbsent(book.folderId(), id -> pathOf(id, folders));
                refreshRows(currentUser, book.uuid(), book.name(), path);
            }
        } catch (Exception e) {
            log.warn("Failed to refresh the folders of the books in the statistics", e);
        }
    }

    @Override
    @CommonTx
    public void noteBook(Manuscript book, String folderPath) {
        if (book == null || book.getUuid() == null || book.getOwner() == null || book.isAgentic()) {
            return;
        }
        try {
            Names now = new Names(StringUtils.defaultString(book.getName()), StringUtils.defaultString(folderPath));
            if (names.size() > MAX_REMEMBERED) {
                names.clear();
            }
            Names before = names.putIfAbsent(book.getUuid(), now);
            if (before == null || before.equals(now)) {
                return;
            }
            // the caller may hold an old copy of the book: what is stored decides
            Manuscript stored = manuscriptService.find(book.getId());
            if (stored != null && !stored.isDeleted()) {
                Names truth = new Names(StringUtils.defaultString(stored.getName()), StringUtils.defaultString(folderPathOf(stored)));
                if (!truth.equals(before)) {
                    refreshRows(stored.getOwner(), stored.getUuid(), truth.name(), truth.path());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to check the name of book {} in the statistics", book.getUuid(), e);
        }
    }

    private void refreshRows(User owner, String uuid, String name, String path) throws Exception {
        getRepository().refreshBook(owner, uuid, name, path);
        intervalService.refreshBook(owner, uuid, name, path);
        sizeService.refreshBook(owner, uuid, name, path);
        names.put(uuid, new Names(StringUtils.defaultString(name), StringUtils.defaultString(path)));
    }

    /** The path of a folder from the placements of all of them; a cycle ends the path. */
    private static String pathOf(Long folderId, Map<Long, FolderPlacement> folders) {
        LinkedList<String> parts = new LinkedList<>();
        Set<Long> seen = new HashSet<>();
        for (FolderPlacement above = folders.get(folderId); above != null && seen.add(above.id());
             above = above.parentId() == null ? null : folders.get(above.parentId())) {
            parts.addFirst(above.name());
        }
        return String.join(String.valueOf(FolderService.SEPARATOR), parts);
    }

    @Override
    @CommonTxReadOnly
    public ManuscriptStatistic find(String bookUuid, LocalDate day, boolean agent) throws Exception {
        return getRepository().find(currentUser, bookUuid, day, agent);
    }

    @Override
    @CommonTxReadOnly
    public List<ManuscriptStatistic> findRange(StatisticFilter filter) throws Exception {
        return getRepository().findRange(currentUser, filter);
    }

    @Override
    @CommonTxReadOnly
    public StatisticCounts total(StatisticFilter filter) throws Exception {
        return getRepository().sum(currentUser, filter);
    }

    @Override
    @CommonTxReadOnly
    public List<DayStatistic> sumByDay(StatisticFilter filter) throws Exception {
        return getRepository().sumByDay(currentUser, filter);
    }

    @Override
    @CommonTxReadOnly
    public List<BookStatistic> sumByBook(StatisticFilter filter) throws Exception {
        return getRepository().sumByBook(currentUser, filter);
    }

    @Override
    @CommonTxReadOnly
    public List<FolderStatistic> sumByFolder(StatisticFilter filter) throws Exception {
        return getRepository().sumByFolder(currentUser, filter);
    }

    @Override
    @CommonTx
    public void clearBook(String bookUuid) throws Exception {
        if (bookUuid == null) {
            return;
        }
        getRepository().deleteByBook(currentUser, bookUuid);
        intervalService.clearBook(bookUuid);
        sizeService.clearBook(bookUuid);
    }

    @Override
    @CommonTx
    public void clearAll() throws Exception {
        getRepository().deleteAll(currentUser);
        intervalService.clearAll();
        sizeService.clearAll();
    }

    @Override
    public LocalDate today() {
        return LocalDate.now(clock);
    }

    @Override
    @CommonTxReadOnly
    public String folderPathOf(Manuscript book) throws Exception {
        return pathOf(book == null ? null : book.getFolder());
    }

    private String pathOf(Folder folder) throws Exception {
        if (folder == null) {
            return "";
        }
        List<Folder> chain = folderService.findChain(folder);
        Collections.reverse(chain);
        StringBuilder path = new StringBuilder();
        for (Folder f : chain) {
            if (!path.isEmpty()) {
                path.append(FolderService.SEPARATOR);
            }
            path.append(f.getName());
        }
        return path.toString();
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
