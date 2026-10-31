package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.components.SizeGrowth;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptSize;
import com.github.enerccio.marginalia.domain.repository.ManuscriptSizeRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService.BranchSize;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ManuscriptSizeService;
import com.github.enerccio.marginalia.domain.service.ManuscriptStatisticService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ManuscriptSizeServiceImpl extends OwnedServiceImpl<ManuscriptSize, ManuscriptSizeRepository>
        implements ManuscriptSizeService {

    private static final Logger log = LoggerFactory.getLogger(ManuscriptSizeServiceImpl.class);

    /** A burst of changes (a run of edits, the end of a generation, an import) is measured once, this long after the last one. */
    public static final long DEFAULT_DELAY_MILLIS = 2_000;

    @Autowired
    @Lazy
    private ManuscriptStatisticService statisticService;

    @Autowired
    @Lazy
    private ManuscriptService manuscriptService;

    @Autowired
    @Lazy
    private ChatMessageService chatMessageService;

    @Autowired
    @Qualifier("common")
    private PlatformTransactionManager transactionManager;

    /** What was touched and is not measured yet: the id of the book, when it is due, whether it was just born. */
    private record Pending(long dueAt, boolean born) {
    }

    private final Map<Long, Pending> pending = new ConcurrentHashMap<>();
    private final AtomicBoolean scheduled = new AtomicBoolean();
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "size-measure");
        thread.setDaemon(true);
        return thread;
    });
    private volatile long delayMillis = DEFAULT_DELAY_MILLIS;
    private volatile boolean manual;
    private volatile Clock clock = Clock.systemDefaultZone();

    @Override
    @CommonTx
    public void set(Manuscript book, LocalDate day, long bookWords, long bookTokens, long branchWords, long branchTokens,
                    String leafUuid, boolean baseline) {
        if (book == null || book.getUuid() == null || book.getOwner() == null || day == null) {
            return;
        }
        try {
            ManuscriptSize row = getRepository().find(book.getOwner(), book.getUuid(), day);
            if (row == null) {
                row = new ManuscriptSize();
                row.setOwner(book.getOwner());
                row.setBookUuid(book.getUuid());
                row.setDay(day);
            }
            boolean unchanged = row.getId() != null && row.getBookWords() == Math.max(0, bookWords)
                    && row.getBookTokens() == Math.max(0, bookTokens) && row.getBranchWords() == Math.max(0, branchWords)
                    && row.getBranchTokens() == Math.max(0, branchTokens) && Objects.equals(row.getLeafUuid(), leafUuid)
                    && row.isBaseline() == baseline && Objects.equals(row.getBookName(), book.getName());
            if (unchanged) {
                return;
            }
            row.setBookName(book.getName());
            String path = statisticService.folderPathOf(book);
            statisticService.noteBook(book, path);
            row.setFolderPath(path);
            row.setBookWords(Math.max(0, bookWords));
            row.setBookTokens(Math.max(0, bookTokens));
            row.setBranchWords(Math.max(0, branchWords));
            row.setBranchTokens(Math.max(0, branchTokens));
            row.setLeafUuid(leafUuid);
            row.setBaseline(baseline);
            row.setReconstructed(false);
            row.setLastChange(new Date());
            save(row);
        } catch (Exception e) {
            log.warn("Failed to set the size of book {}", book.getUuid(), e);
        }
    }

    // ---- measuring

    @Override
    public void touch(Manuscript book) {
        touch(book, false);
    }

    @Override
    public void touch(Long bookId) {
        touch(bookId, false, delayMillis);
    }

    @Override
    public void touch(Manuscript book, boolean born) {
        // the id only: the book may be a proxy that is not attached to a session any more
        touch(book == null ? null : book.getId(), born, delayMillis);
    }

    @Override
    public void touchNow(Manuscript book) {
        touchNow(book == null ? null : book.getId());
    }

    @Override
    public void touchNow(Long bookId) {
        touch(bookId, false, 0);
    }

    private void touch(Long bookId, boolean born, long delay) {
        if (bookId == null) {
            return;
        }
        long due = System.currentTimeMillis() + delay;
        pending.merge(bookId, new Pending(due, born), (old, latest) -> new Pending(latest.dueAt(), old.born() || latest.born()));
        if (!manual && (delay == 0 || scheduled.compareAndSet(false, true))) {
            schedule(delay + 20);
        }
    }

    private void schedule(long afterMillis) {
        try {
            worker.schedule(this::runDue, afterMillis, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            // the server is going down
            scheduled.set(false);
        }
    }

    /** Measures what is due; what a later touch moved on is left for the next run. */
    private void runDue() {
        scheduled.set(false);
        long now = System.currentTimeMillis();
        long next = Long.MAX_VALUE;
        List<Map.Entry<Long, Pending>> due = new ArrayList<>();
        for (Map.Entry<Long, Pending> entry : pending.entrySet()) {
            if (entry.getValue().dueAt() <= now) {
                if (pending.remove(entry.getKey(), entry.getValue())) {
                    due.add(entry);
                }
            } else {
                next = Math.min(next, entry.getValue().dueAt() - now);
            }
        }
        for (Map.Entry<Long, Pending> entry : due) {
            measure(entry.getKey(), entry.getValue().born());
        }
        if (next != Long.MAX_VALUE && scheduled.compareAndSet(false, true)) {
            schedule(next + 20);
        }
    }

    @Override
    public void flush() {
        for (Long id : new ArrayList<>(pending.keySet())) {
            Pending taken = pending.remove(id);
            if (taken != null) {
                measure(id, taken.born());
            }
        }
    }

    /** Never throws. */
    private void measure(Long bookId, boolean born) {
        try {
            Manuscript book = manuscriptService.find(bookId);
            if (book == null || book.isDeleted() || book.isAgentic() || book.getOwner() == null) {
                return;
            }
            long words = chatMessageService.getTotalWordCount(book);
            long tokens = chatMessageService.getTotalTokenCount(book);
            BranchSize branch = chatMessageService.getActiveBranchSizes(List.of(bookId)).get(bookId);
            Long leafId = book.getActiveLeaf() == null ? null : book.getActiveLeaf().getId();
            ChatMessage leaf = leafId == null ? null : chatMessageService.find(leafId);
            LocalDate day = LocalDate.now(clock);
            long branchWords = branch == null ? 0 : branch.words();
            long branchTokens = branch == null ? 0 : branch.tokens();
            String leafUuid = leaf == null ? null : leaf.getUuid();
            // a transaction of its own: the worker calls this class, not its proxy
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                try {
                    ManuscriptSize today = getRepository().find(book.getOwner(), book.getUuid(), day);
                    // the change of the day the book was first seen on is unknown, whatever happens later in it
                    boolean baseline = today != null ? today.isBaseline()
                            : !born && getRepository().findBefore(book.getOwner(), book.getUuid(), day) == null;
                    set(book, day, words, tokens, branchWords, branchTokens, leafUuid, baseline);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
        } catch (Exception e) {
            log.warn("Failed to measure the size of book {}", bookId, e);
        }
    }

    @Override
    @CommonTxReadOnly
    public SizeGrowth growth(String bookUuid, LocalDate from, LocalDate to) throws Exception {
        ManuscriptSize end = getRepository().findAtOrBefore(currentUser, bookUuid, to);
        if (end == null) {
            return SizeGrowth.ZERO;
        }
        ManuscriptSize start = getRepository().findBefore(currentUser, bookUuid, from);
        if (start == null) {
            // a book first seen in the range is measured from the row it was first seen with
            List<ManuscriptSize> rows = getRepository().findRange(currentUser, bookUuid, from, to);
            if (!rows.isEmpty() && rows.getFirst().isBaseline()) {
                start = rows.getFirst();
            }
        }
        if (end.isBaseline() && start == null || start != null && start.getId().equals(end.getId()) && end.isBaseline()) {
            return SizeGrowth.UNKNOWN;
        }
        return new SizeGrowth(end.getBookWords() - (start == null ? 0 : start.getBookWords()),
                end.getBookTokens() - (start == null ? 0 : start.getBookTokens()),
                end.getBranchWords() - (start == null ? 0 : start.getBranchWords()),
                end.getBranchTokens() - (start == null ? 0 : start.getBranchTokens()), true);
    }

    @Override
    @CommonTxReadOnly
    public ManuscriptSize sizeAt(String bookUuid, LocalDate day) throws Exception {
        return getRepository().findAtOrBefore(currentUser, bookUuid, day);
    }

    @Override
    @CommonTxReadOnly
    public ManuscriptSize sizeBefore(String bookUuid, LocalDate day) throws Exception {
        return getRepository().findBefore(currentUser, bookUuid, day);
    }

    @Override
    @CommonTxReadOnly
    public List<ManuscriptSize> sizesAt(LocalDate day) throws Exception {
        return getRepository().findAllAtOrBefore(currentUser, day);
    }

    @Override
    @CommonTxReadOnly
    public List<ManuscriptSize> findRange(String bookUuid, LocalDate from, LocalDate to) throws Exception {
        return getRepository().findRange(currentUser, bookUuid, from, to);
    }

    @Override
    @CommonTx
    public void refreshBook(User owner, String bookUuid, String bookName, String folderPath) throws Exception {
        getRepository().refreshBook(owner, bookUuid, bookName, folderPath);
    }

    @Override
    @CommonTx
    public void clearBook(String bookUuid) throws Exception {
        getRepository().deleteByBook(currentUser, bookUuid);
    }

    @Override
    @CommonTx
    public void clearAll() throws Exception {
        getRepository().deleteAll(currentUser);
    }

    // ---- for the tests and the shutdown

    /**
     * Writes what is pending and stops the worker (the destroy method of the bean).
     */
    public void stop() {
        flush();
        worker.shutdownNow();
    }

    /**
     * Gives the day to the service; a test sets its own.
     */
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void setDelayMillis(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    /**
     * Stops the worker from measuring by itself: whoever sets it calls {@link #flush}.
     */
    public void setManual(boolean manual) {
        this.manual = manual;
    }
}
