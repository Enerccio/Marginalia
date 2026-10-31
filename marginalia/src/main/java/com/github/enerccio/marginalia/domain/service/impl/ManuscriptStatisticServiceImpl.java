package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.components.*;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptStatistic;
import com.github.enerccio.marginalia.domain.repository.ManuscriptStatisticRepository;
import com.github.enerccio.marginalia.domain.service.ActivityIntervalService;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptSizeService;
import com.github.enerccio.marginalia.domain.service.ManuscriptStatisticService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ManuscriptStatisticServiceImpl extends OwnedServiceImpl<ManuscriptStatistic, ManuscriptStatisticRepository>
        implements ManuscriptStatisticService {

    private static final Logger log = LoggerFactory.getLogger(ManuscriptStatisticServiceImpl.class);

    @Autowired
    @Lazy
    private FolderService folderService;

    @Autowired
    @Lazy
    private ActivityIntervalService intervalService;

    @Autowired
    @Lazy
    private ManuscriptSizeService sizeService;

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
        if (book.isAgentic()) {
            // the hidden working copy of an agent is not a book of the author, what happens to it is counted for the original
            // with the agents' side, which is not resolved yet
            return;
        }
        try {
            Date now = new Date(clock.millis());
            LocalDate day = LocalDate.now(clock);
            String path = folderPathOf(book);
            if (getRepository().add(book.getOwner(), book.getUuid(), day, agent, delta, book.getName(), path, now) > 0) {
                return;
            }
            // the writers go one at a time (a transaction that writes takes the write lock when it begins), so nobody makes the row between the update and this
            ManuscriptStatistic row = new ManuscriptStatistic();
            row.setOwner(book.getOwner());
            row.setBookUuid(book.getUuid());
            row.setBookName(book.getName());
            row.setFolderPath(path);
            row.setDay(day);
            row.setAgent(agent);
            row.setCounts(delta);
            row.setLastChange(now);
            save(row);
        } catch (Exception e) {
            log.warn("Failed to count the statistics of book {}", book.getUuid(), e);
        }
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
        Folder folder = book == null ? null : book.getFolder();
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
