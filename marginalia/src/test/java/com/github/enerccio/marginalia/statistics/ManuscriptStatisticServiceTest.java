package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;
import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.*;
import com.github.enerccio.marginalia.domain.components.BookStatistic.BookState;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptSize;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptStatistic;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.ManuscriptStatisticServiceImpl;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.AopTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The data layer of the writing statistics: the counts per book, day and side, the stretches of time and the sizes. The rows hold
 * the book by its uuid and outlive it.
 */
class ManuscriptStatisticServiceTest extends MarginaliaTestBase {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 30);

    @Autowired
    private ManuscriptStatisticService statisticService;

    @Autowired
    private ActivityIntervalService intervalService;

    @Autowired
    private ManuscriptSizeService sizeService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private CleanupService cleanupService;

    @Autowired
    private TrashService trashService;

    private User user;

    @BeforeEach
    void loginUser() throws Exception {
        user = loginAdmin();
        setClock(Clock.fixed(DAY.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));
    }

    @AfterEach
    void restoreClock() {
        setClock(Clock.systemDefaultZone());
    }

    private void setClock(Clock clock) {
        ((ManuscriptStatisticServiceImpl) AopTestUtils.getUltimateTargetObject(statisticService)).setClock(clock);
    }

    private void setDay(LocalDate day) {
        setClock(Clock.fixed(day.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));
    }

    private Manuscript book(String name, Folder folder) throws Exception {
        Manuscript book = new Manuscript();
        book.setName(name);
        book.setFolder(folder);
        return manuscriptService.save(book);
    }

    private static StatisticFilter filter(LocalDate from, LocalDate to) {
        return StatisticFilter.of(from, to, StatisticSide.ALL);
    }

    private static StatisticFilter day(LocalDate day) {
        return filter(day, day);
    }

    // ------------------------------------------------------------------------------------------------------------
    // counts
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void firstCountMakesTheRowAndLaterOnesAddToIt() throws Exception {
        Manuscript book = book(uniqueName("book"), null);

        statisticService.count(book, StatisticCounts.tokens(100, 20).plus(StatisticCounts.wordsGenerated(40)));
        statisticService.count(book, StatisticCounts.tokens(50, 5));
        statisticService.count(book, StatisticCounts.wordsWritten(7).plus(StatisticCounts.edits(1)));

        List<ManuscriptStatistic> rows = statisticService.findRange(day(DAY).forBook(book.getUuid()));
        assertThat(rows).hasSize(1);
        ManuscriptStatistic row = rows.getFirst();
        assertThat(row.getCounts()).isEqualTo(new StatisticCounts(150, 25, 2, 7, 40, 0, 0, 0, 1));
        assertThat(row.getDay()).isEqualTo(DAY);
        assertThat(row.isAgent()).isFalse();
        assertThat(row.isReconstructed()).isFalse();
        assertThat(row.getBookName()).isEqualTo(book.getName());
        assertThat(row.getFolderPath()).isEmpty();
        assertThat(row.getOwner().getId()).isEqualTo(user.getId());
        assertThat(row.getLastChange()).isNotNull();
    }

    @Test
    void theTwoSidesAreTwoRows() throws Exception {
        Manuscript book = book(uniqueName("book"), null);

        statisticService.count(book, StatisticCounts.wordsWritten(10));
        statisticService.count(book, true, StatisticCounts.wordsGenerated(300));
        statisticService.count(book, true, StatisticCounts.wordsGenerated(1));

        assertThat(statisticService.find(book.getUuid(), DAY, false).getCounts()).isEqualTo(StatisticCounts.wordsWritten(10));
        assertThat(statisticService.find(book.getUuid(), DAY, true).getCounts()).isEqualTo(StatisticCounts.wordsGenerated(301));
        StatisticFilter all = day(DAY).forBook(book.getUuid());
        assertThat(statisticService.total(all)).isEqualTo(new StatisticCounts(0, 0, 0, 10, 301, 0, 0, 0, 0));
        assertThat(statisticService.total(new StatisticFilter(DAY, DAY, StatisticSide.AUTHOR, book.getUuid())).wordsAdded()).isEqualTo(10);
        assertThat(statisticService.total(new StatisticFilter(DAY, DAY, StatisticSide.AGENTS, book.getUuid())).wordsAdded()).isEqualTo(301);
    }

    @Test
    void nothingIsCountedForAnEmptyChangeOrABookThatIsNotSaved() throws Exception {
        Manuscript book = book(uniqueName("book"), null);

        statisticService.count(book, StatisticCounts.ZERO);
        statisticService.count(null, StatisticCounts.wordsWritten(1));
        statisticService.count(new Manuscript(), StatisticCounts.wordsWritten(1));
        statisticService.count(book, null);

        assertThat(statisticService.findRange(filter(DAY, DAY))).extracting(ManuscriptStatistic::getBookUuid)
                .doesNotContain(book.getUuid());
    }

    @Test
    void aNewDayIsANewRow() throws Exception {
        Manuscript book = book(uniqueName("book"), null);

        statisticService.count(book, StatisticCounts.wordsWritten(5));
        setDay(DAY.plusDays(1));
        statisticService.count(book, StatisticCounts.wordsWritten(6));

        assertThat(statisticService.today()).isEqualTo(DAY.plusDays(1));
        assertThat(statisticService.find(book.getUuid(), DAY, false).getWordsWritten()).isEqualTo(5);
        assertThat(statisticService.find(book.getUuid(), DAY.plusDays(1), false).getWordsWritten()).isEqualTo(6);
        assertThat(statisticService.find(book.getUuid(), DAY.plusDays(2), false)).isNull();
    }

    @Test
    void theRowKeepsTheNameOfTheBookAndThePathOfItsFolder() throws Exception {
        Folder scifi = folderService.create("Sci-fi", null);
        Folder expanse = folderService.create("Expanse", scifi);
        Manuscript book = book("Leviathan", expanse);

        statisticService.count(book, StatisticCounts.wordsWritten(1));

        ManuscriptStatistic row = statisticService.find(book.getUuid(), DAY, false);
        assertThat(row.getBookName()).isEqualTo("Leviathan");
        assertThat(row.getFolderPath()).isEqualTo("Sci-fi/Expanse");
        assertThat(statisticService.folderPathOf(book)).isEqualTo("Sci-fi/Expanse");
        assertThat(statisticService.folderPathOf(book(uniqueName("loose"), null))).isEmpty();
    }

    @Test
    void parallelCountsLoseNothing() throws Exception {
        Manuscript book = book(uniqueName("book"), null);
        int threads = 8;
        int rounds = 25;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<?>> done = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                boolean agent = t % 2 == 0;
                done.add(pool.submit(() -> {
                    start.await();
                    // on a thread without a session: the owner is the owner of the book
                    for (int i = 0; i < rounds; i++) {
                        statisticService.count(book, agent, StatisticCounts.tokens(10, 1).plus(StatisticCounts.wordsWritten(2)));
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : done) {
                future.get(60, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        StatisticFilter filter = day(DAY).forBook(book.getUuid());
        assertThat(statisticService.findRange(filter)).hasSize(2);
        assertThat(statisticService.total(filter)).isEqualTo(new StatisticCounts(10L * threads * rounds, (long) threads * rounds,
                (long) threads * rounds, 2L * threads * rounds, 0, 0, 0, 0, 0));
    }

    // ------------------------------------------------------------------------------------------------------------
    // sums
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void sumsOverDaysBooksAndFolders() throws Exception {
        String folderName = uniqueName("Sums");
        Folder scifi = folderService.create(folderName, null);
        Manuscript first = book(uniqueName("first"), scifi);
        Manuscript second = book(uniqueName("second"), null);
        statisticService.count(first, StatisticCounts.wordsWritten(10));
        statisticService.count(second, StatisticCounts.wordsWritten(1));
        statisticService.count(second, true, StatisticCounts.wordsGenerated(100));
        setDay(DAY.plusDays(2));
        statisticService.count(first, StatisticCounts.wordsWritten(20));

        StatisticFilter range = filter(DAY, DAY.plusDays(2));
        assertThat(statisticService.total(range.forBook(first.getUuid()))).isEqualTo(StatisticCounts.wordsWritten(30));
        assertThat(statisticService.total(range.forBook(second.getUuid())).wordsAdded()).isEqualTo(101);
        assertThat(statisticService.total(filter(DAY.minusDays(5), DAY.minusDays(1)).forBook(first.getUuid()))).isEqualTo(StatisticCounts.ZERO);
        assertThat(statisticService.total(filter(DAY.plusDays(1), DAY.plusDays(1)))).isEqualTo(StatisticCounts.ZERO);

        List<DayStatistic> days = statisticService.sumByDay(new StatisticFilter(DAY, DAY.plusDays(2), StatisticSide.ALL, first.getUuid()));
        assertThat(days).extracting(DayStatistic::day).containsExactly(DAY, DAY.plusDays(2));
        assertThat(days).extracting(d -> d.counts().wordsWritten()).containsExactly(10L, 20L);
        List<DayStatistic> authorDays = statisticService.sumByDay(new StatisticFilter(DAY, DAY, StatisticSide.AUTHOR, second.getUuid()));
        assertThat(authorDays).hasSize(1);
        assertThat(authorDays.getFirst().counts()).isEqualTo(StatisticCounts.wordsWritten(1));

        List<FolderStatistic> folders = statisticService.sumByFolder(range);
        assertThat(folders).filteredOn(f -> f.folderPath().equals(folderName)).extracting(f -> f.counts().wordsWritten()).containsExactly(30L);
        assertThat(folders).extracting(FolderStatistic::folderPath).contains("");
    }

    @Test
    void filterNeedsAValidRange() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> filter(DAY, DAY.minusDays(1))).isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> filter(null, DAY)).isInstanceOf(IllegalArgumentException.class);
        assertThat(StatisticFilter.of(DAY, DAY, null).side()).isEqualTo(StatisticSide.ALL);
    }

    @Test
    void otherUsersCannotSeeTheRows() throws Exception {
        Manuscript book = book(uniqueName("book"), null);
        statisticService.count(book, StatisticCounts.wordsWritten(10));
        statisticService.count(book, StatisticCounts.tokens(1, 1));
        setDay(DAY);

        loginAs(createUser());

        assertThat(statisticService.total(filter(DAY, DAY))).isEqualTo(StatisticCounts.ZERO);
        assertThat(statisticService.sumByBook(filter(DAY, DAY))).isEmpty();
        assertThat(statisticService.find(book.getUuid(), DAY, false)).isNull();
        assertThat(statisticService.findRange(filter(DAY, DAY))).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------------------
    // the book is gone, the history stays
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void theBooksOfTheBreakdownKnowIfTheyAreInTheTrashOrGone() throws Exception {
        Manuscript present = book(uniqueName("present"), null);
        Manuscript trashed = book(uniqueName("trashed"), null);
        Manuscript purged = book(uniqueName("purged"), null);
        for (Manuscript book : List.of(present, trashed, purged)) {
            statisticService.count(book, StatisticCounts.wordsWritten(5));
        }
        manuscriptService.delete(manuscriptService.find(trashed.getId()), false);
        manuscriptService.delete(manuscriptService.find(purged.getId()), false);
        cleanupService.purge();
        // the purge took the book that was in the trash too, make one that stays there
        Manuscript stays = book(uniqueName("stays"), null);
        statisticService.count(stays, StatisticCounts.wordsWritten(3));
        manuscriptService.delete(manuscriptService.find(stays.getId()), false);

        List<BookStatistic> books = statisticService.sumByBook(filter(DAY, DAY));

        assertThat(state(books, present)).isEqualTo(BookState.PRESENT);
        assertThat(state(books, stays)).isEqualTo(BookState.TRASHED);
        assertThat(state(books, trashed)).isEqualTo(BookState.PURGED);
        assertThat(state(books, purged)).isEqualTo(BookState.PURGED);
        BookStatistic gone = books.stream().filter(b -> b.bookUuid().equals(purged.getUuid())).findFirst().orElseThrow();
        assertThat(gone.deleted()).isTrue();
        assertThat(gone.bookName()).isEqualTo(purged.getName());
        assertThat(gone.counts()).isEqualTo(StatisticCounts.wordsWritten(5));
        assertThat(gone.lastChange()).isNotNull();
        assertThat(books.stream().filter(b -> b.bookUuid().equals(present.getUuid())).findFirst().orElseThrow().deleted()).isFalse();
        // the history of a book that is gone is still in the totals
        assertThat(statisticService.total(day(DAY).forBook(purged.getUuid()))).isEqualTo(StatisticCounts.wordsWritten(5));
    }

    private static BookState state(List<BookStatistic> books, Manuscript book) {
        return books.stream().filter(b -> b.bookUuid().equals(book.getUuid())).findFirst().orElseThrow().state();
    }

    @Test
    void aBookRestoredFromTheTrashCarriesOn() throws Exception {
        Manuscript book = book(uniqueName("book"), null);
        statisticService.count(book, StatisticCounts.wordsWritten(5));
        Manuscript stored = manuscriptService.find(book.getId());
        manuscriptService.delete(stored, false);
        assertThat(state(statisticService.sumByBook(day(DAY)), book)).isEqualTo(BookState.TRASHED);

        assertThat(trashService.restore(List.of(new CleanupService.EntityKey(Manuscript.class, book.getId()))).isBlocked()).isFalse();
        statisticService.count(manuscriptService.find(book.getId()), StatisticCounts.wordsWritten(2));

        assertThat(state(statisticService.sumByBook(day(DAY)), book)).isEqualTo(BookState.PRESENT);
        assertThat(statisticService.find(book.getUuid(), DAY, false).getWordsWritten()).isEqualTo(7);
    }

    @Test
    void clearingABookClearsAllThreeKindsOfRowsOfThatBookOnly() throws Exception {
        Manuscript cleared = book(uniqueName("cleared"), null);
        Manuscript kept = book(uniqueName("kept"), null);
        for (Manuscript book : List.of(cleared, kept)) {
            statisticService.count(book, StatisticCounts.wordsWritten(5));
            intervalService.extend(intervalService.start(book, ActivityKind.OPEN, false, new Date()).getId(), new Date());
            sizeService.set(book, DAY, 10, 15, 10, 15, null, false);
        }

        statisticService.clearBook(cleared.getUuid());

        assertThat(statisticService.find(cleared.getUuid(), DAY, false)).isNull();
        assertThat(sizeService.sizeAt(cleared.getUuid(), DAY.plusDays(30))).isNull();
        assertThat(intervalService.findRows(widest().forBook(cleared.getUuid()), null)).isEmpty();
        assertThat(statisticService.find(kept.getUuid(), DAY, false)).isNotNull();
        assertThat(sizeService.sizeAt(kept.getUuid(), DAY)).isNotNull();
        assertThat(intervalService.findRows(widest().forBook(kept.getUuid()), null)).hasSize(1);
    }

    @Test
    void clearingEverythingLeavesOtherUsersAlone() throws Exception {
        Manuscript mine = book(uniqueName("mine"), null);
        statisticService.count(mine, StatisticCounts.wordsWritten(5));
        sizeService.set(mine, DAY, 10, 15, 10, 15, null, false);
        User other = loginAs(createUser());
        Manuscript theirs = book(uniqueName("theirs"), null);
        statisticService.count(theirs, StatisticCounts.wordsWritten(9));
        loginAs(user);

        statisticService.clearAll();

        assertThat(statisticService.findRange(widest())).isEmpty();
        assertThat(sizeService.findRange(null, DAY.minusDays(1), DAY.plusDays(1))).isEmpty();
        loginAs(other);
        assertThat(statisticService.find(theirs.getUuid(), DAY, false).getWordsWritten()).isEqualTo(9);
    }

    private static StatisticFilter widest() {
        return filter(LocalDate.of(2000, 1, 1), LocalDate.of(2100, 1, 1));
    }

    // ------------------------------------------------------------------------------------------------------------
    // time
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void aStretchStartsAtTheTimeAndItsEndIsMovedForward() throws Exception {
        Manuscript book = book(uniqueName("book"), null);
        Instant begin = DAY.atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant();

        var interval = intervalService.start(book, ActivityKind.GENERATE, true, Date.from(begin));
        assertThat(interval.getDay()).isEqualTo(DAY);
        assertThat(interval.getBookName()).isEqualTo(book.getName());
        intervalService.extend(interval.getId(), Date.from(begin.plusSeconds(90)));

        List<ActivityRow> rows = intervalService.findRows(day(DAY).forBook(book.getUuid()), null);
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().kind()).isEqualTo(ActivityKind.GENERATE);
        assertThat(rows.getFirst().agent()).isTrue();
        assertThat(rows.getFirst().day()).isEqualTo(DAY);
        assertThat(rows.getFirst().startTime()).isEqualTo(Date.from(begin));
        assertThat(rows.getFirst().millis()).isEqualTo(90_000);
    }

    @Test
    void theStretchesOfAFilterAreOrderedByStartAndCanBeLimitedByKindAndSide() throws Exception {
        Manuscript first = book(uniqueName("first"), null);
        Manuscript second = book(uniqueName("second"), null);
        Instant nine = DAY.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant();
        // two books at the same time are two rows that overlap, as they happened
        intervalService.extend(intervalService.start(second, ActivityKind.RUN, true, Date.from(nine.plusSeconds(60))).getId(), Date.from(nine.plusSeconds(300)));
        intervalService.extend(intervalService.start(first, ActivityKind.OPEN, false, Date.from(nine)).getId(), Date.from(nine.plusSeconds(200)));
        intervalService.extend(intervalService.start(first, ActivityKind.EDIT, false, Date.from(nine.plusSeconds(10))).getId(), Date.from(nine.plusSeconds(100)));
        intervalService.start(first, ActivityKind.OPEN, false, Date.from(nine.plusSeconds(86_400)));

        List<String> mine = List.of(first.getUuid(), second.getUuid());
        List<ActivityRow> all = ours(intervalService.findRows(day(DAY), null), mine);
        assertThat(all).extracting(ActivityRow::kind).containsExactly(ActivityKind.OPEN, ActivityKind.EDIT, ActivityKind.RUN);
        assertThat(ours(intervalService.findRows(day(DAY), List.of(ActivityKind.OPEN, ActivityKind.EDIT)), mine)).hasSize(2);
        assertThat(ours(intervalService.findRows(day(DAY), List.of(ActivityKind.RUN)), mine)).extracting(ActivityRow::bookUuid)
                .containsExactly(second.getUuid());
        assertThat(ours(intervalService.findRows(new StatisticFilter(DAY, DAY, StatisticSide.AGENTS, null), null), mine)).hasSize(1);
        assertThat(ours(intervalService.findRows(new StatisticFilter(DAY, DAY, StatisticSide.AUTHOR, null), null), mine)).hasSize(2);
        assertThat(ours(intervalService.findRows(day(DAY.plusDays(1)), null), mine)).hasSize(1);
    }

    private static List<ActivityRow> ours(List<ActivityRow> rows, List<String> books) {
        return rows.stream().filter(r -> books.contains(r.bookUuid())).toList();
    }

    // ------------------------------------------------------------------------------------------------------------
    // size
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void theSizeOfADayIsOneRowThatIsUpdated() throws Exception {
        Manuscript book = book(uniqueName("book"), null);

        sizeService.set(book, DAY, 100, 150, 60, 90, "leaf-a", false);
        sizeService.set(book, DAY, 120, 180, 80, 120, "leaf-b", false);

        List<ManuscriptSize> rows = sizeService.findRange(book.getUuid(), DAY, DAY);
        assertThat(rows).hasSize(1);
        ManuscriptSize size = rows.getFirst();
        assertThat(size.getBookWords()).isEqualTo(120);
        assertThat(size.getBookTokens()).isEqualTo(180);
        assertThat(size.getBranchWords()).isEqualTo(80);
        assertThat(size.getBranchTokens()).isEqualTo(120);
        assertThat(size.getLeafUuid()).isEqualTo("leaf-b");
        assertThat(size.isBaseline()).isFalse();
        assertThat(size.getBookName()).isEqualTo(book.getName());
    }

    @Test
    void aDayWithoutARowHasTheLastSizeBeforeIt() throws Exception {
        Manuscript book = book(uniqueName("book"), null);
        sizeService.set(book, DAY, 100, 150, 60, 90, null, true);
        sizeService.set(book, DAY.plusDays(3), 160, 240, 90, 135, null, false);

        assertThat(sizeService.sizeAt(book.getUuid(), DAY.minusDays(1))).isNull();
        assertThat(sizeService.sizeAt(book.getUuid(), DAY).isBaseline()).isTrue();
        assertThat(sizeService.sizeAt(book.getUuid(), DAY.plusDays(2)).getBookWords()).isEqualTo(100);
        assertThat(sizeService.sizeAt(book.getUuid(), DAY.plusDays(3)).getBookWords()).isEqualTo(160);
        assertThat(sizeService.sizeAt(book.getUuid(), DAY.plusDays(40)).getBookWords()).isEqualTo(160);
        assertThat(sizeService.sizeBefore(book.getUuid(), DAY)).isNull();
        assertThat(sizeService.sizeBefore(book.getUuid(), DAY.plusDays(3)).getBookWords()).isEqualTo(100);
        assertThat(sizeService.sizeBefore(book.getUuid(), DAY.plusDays(4)).getBookWords()).isEqualTo(160);
    }

    @Test
    void theLatestSizeOfEveryBookIsFoundAtADay() throws Exception {
        Manuscript first = book(uniqueName("first"), null);
        Manuscript second = book(uniqueName("second"), null);
        sizeService.set(first, DAY, 10, 15, 10, 15, null, false);
        sizeService.set(first, DAY.plusDays(2), 30, 45, 30, 45, null, false);
        sizeService.set(second, DAY.plusDays(1), 500, 750, 100, 150, null, false);

        List<ManuscriptSize> atTheEnd = sizeService.sizesAt(DAY.plusDays(2));
        assertThat(atTheEnd).filteredOn(s -> s.getBookUuid().equals(first.getUuid())).extracting(ManuscriptSize::getBookWords).containsExactly(30L);
        assertThat(atTheEnd).filteredOn(s -> s.getBookUuid().equals(second.getUuid())).extracting(ManuscriptSize::getBookWords).containsExactly(500L);
        List<ManuscriptSize> early = sizeService.sizesAt(DAY.plusDays(1));
        assertThat(early).filteredOn(s -> s.getBookUuid().equals(first.getUuid())).extracting(ManuscriptSize::getBookWords).containsExactly(10L);
        assertThat(sizeService.findRange(null, DAY, DAY.plusDays(2))).extracting(ManuscriptSize::getBookUuid)
                .contains(first.getUuid(), second.getUuid());
        assertThat(sizeService.findRange(first.getUuid(), DAY, DAY.plusDays(1))).hasSize(1);
    }

    @Test
    void aBookThatIsGoneKeepsItsLastSize() throws Exception {
        Manuscript book = book(uniqueName("book"), null);
        sizeService.set(book, DAY, 100, 150, 60, 90, null, false);

        manuscriptService.delete(manuscriptService.find(book.getId()), false);
        cleanupService.purge();

        assertThat(manuscriptService.find(book.getId())).isNull();
        assertThat(sizeService.sizeAt(book.getUuid(), DAY.plusDays(10)).getBookWords()).isEqualTo(100);
    }

    @Test
    void sizeIsNotSetForABookThatIsNotSaved() throws Exception {
        sizeService.set(new Manuscript(), DAY, 1, 1, 1, 1, null, false);
        sizeService.set(null, DAY, 1, 1, 1, 1, null, false);
        Manuscript saved = book(uniqueName("book"), null);
        sizeService.set(saved, null, 1, 1, 1, 1, null, false);

        assertThat(sizeService.findRange(saved.getUuid(), DAY.minusDays(100), DAY.plusDays(100))).isEmpty();
    }
}
