package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.bound.Migration.MigrationType;
import com.github.enerccio.marginalia.bound.migration.StatisticsBackfillMigration;
import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.components.StatisticReport;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.ManuscriptSizeServiceImpl;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.test.util.AopTestUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link StatisticsBackfillMigration}: the statistics of the books that were written before they were kept, rebuilt from the parts
 * and their versions and marked as an estimate, and the day the time is recorded from.
 */
class StatisticsBackfillMigrationTest extends MarginaliaTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ManuscriptStatisticService statisticService;

    @Autowired
    private ManuscriptSizeService sizeService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private StatisticReportService reportService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    private User user;
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() throws Exception {
        user = login();
        // the size worker would write rows of its own
        ((ManuscriptSizeServiceImpl) AopTestUtils.getUltimateTargetObject(sizeService)).setManual(true);
    }

    @AfterEach
    void tearDown() {
        ((ManuscriptSizeServiceImpl) AopTestUtils.getUltimateTargetObject(sizeService)).setManual(false);
    }

    private Date at(int daysAgo) {
        return Date.from(today.minusDays(daysAgo).atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant());
    }

    private Manuscript book(String name, Folder folder) throws Exception {
        Manuscript book = new Manuscript();
        book.setName(name);
        book.setFolder(folder);
        return manuscriptService.save(book);
    }

    /**
     * Makes the history of the part older: all its versions on the day, and the part itself.
     */
    private void age(ChatMessage part, int daysAgo) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("update MessageRevision r set r.creation = :when where r.message.id = :id")
                    .setParameter("when", at(daysAgo)).setParameter("id", part.getId()).executeUpdate();
            em.createQuery("update ChatMessage m set m.creation = :when, m.modification = :when where m.id = :id")
                    .setParameter("when", at(daysAgo)).setParameter("id", part.getId()).executeUpdate();
            em.getTransaction().commit();
        }
    }

    private void ageVersions(ChatMessage part, int first, int... later) {
        List<Long> ids;
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            ids = em.createQuery("select r.id from MessageRevision r where r.message.id = :id order by r.id", Long.class)
                    .setParameter("id", part.getId()).getResultList();
            em.getTransaction().begin();
            for (int i = 0; i < ids.size(); i++) {
                int ago = i == 0 ? first : later[Math.min(i - 1, later.length - 1)];
                em.createQuery("update MessageRevision r set r.creation = :when where r.id = :id")
                        .setParameter("when", at(ago)).setParameter("id", ids.get(i)).executeUpdate();
            }
            em.getTransaction().commit();
        }
    }

    private void migrate() throws Exception {
        StatisticsBackfillMigration migration = new StatisticsBackfillMigration();
        beanFactory.autowireBean(migration);
        assertThat(migration.migrate(8, MigrationType.APP)).isEqualTo(9);
    }

    private List<ManuscriptStatistic> statistics(Manuscript book) throws Exception {
        return statisticService.findRange(StatisticFilter.of(today.minusDays(60), today, StatisticSide.ALL).forBook(book.getUuid()));
    }

    private List<ManuscriptSize> sizes(Manuscript book) throws Exception {
        return sizeService.findRange(book.getUuid(), today.minusDays(60), today);
    }

    /**
     * A book as the version before the statistics had it: parts with versions, no rows.
     */
    private Manuscript legacyBook(String name, Folder folder) throws Exception {
        Manuscript book = book(name, folder);
        ChatMessage first = chatMessageService.createPart(book, null, "one two three", null);
        ChatMessage second = chatMessageService.createPart(book, first, "four five", null);
        chatMessageService.updateResponse(book, second, "four five six seven");
        age(first, 5);
        ageVersions(second, 4, 3);
        statisticService.clearBook(book.getUuid());
        return manuscriptService.find(book.getId());
    }

    @Test
    void theCountsAndTheSizeOfABookAreRebuiltFromItsVersionsAndMarkedAsAnEstimate() throws Exception {
        Folder scifi = folderService.create("Sci-fi", null);
        Folder expanse = folderService.create("Expanse", scifi);
        Manuscript book = legacyBook("The Old Book", expanse);
        assertThat(statistics(book)).isEmpty();

        migrate();

        List<ManuscriptStatistic> rows = statistics(book);
        assertThat(rows).hasSize(3).allMatch(ManuscriptStatistic::isReconstructed).allMatch(row -> !row.isAgent());
        assertThat(rows).extracting(ManuscriptStatistic::getBookName).containsOnly("The Old Book");
        assertThat(rows).extracting(ManuscriptStatistic::getFolderPath).containsOnly("Sci-fi/Expanse");
        assertThat(rows.get(0).getDay()).isEqualTo(today.minusDays(5));
        assertThat(rows.get(0).getCounts().wordsWritten()).isEqualTo(3);
        assertThat(rows.get(0).getCounts().partsCreated()).isEqualTo(1);
        assertThat(rows.get(1).getCounts().wordsWritten()).isEqualTo(2);
        assertThat(rows.get(1).getCounts().partsCreated()).isEqualTo(1);
        assertThat(rows.get(2).getCounts().wordsWritten()).as("two more words").isEqualTo(2);
        assertThat(rows.get(2).getCounts().edits()).isEqualTo(1);

        List<ManuscriptSize> sizes = sizes(book);
        assertThat(sizes).extracting(ManuscriptSize::getDay).containsExactly(today.minusDays(5), today.minusDays(4), today.minusDays(3));
        assertThat(sizes).extracting(ManuscriptSize::getBookWords).containsExactly(3L, 5L, 7L);
        assertThat(sizes).extracting(ManuscriptSize::getBranchWords).containsExactly(3L, 5L, 7L);
        assertThat(sizes).allMatch(ManuscriptSize::isReconstructed).allMatch(size -> !size.isBaseline());
        assertThat(sizes.getFirst().getLeafUuid()).isEqualTo(manuscriptService.find(book.getId()).getActiveLeaf().getUuid());
        assertThat(sizeService.growth(book.getUuid(), today.minusDays(5), today.minusDays(3)).bookWords()).isEqualTo(7);
    }

    @Test
    void theMigrationIsRunOnceForABookAndLeavesABookWithRowsAlone() throws Exception {
        Manuscript old = legacyBook("Old", null);
        Manuscript live = book("Live", null);
        chatMessageService.createPart(live, null, "a live part", null);
        List<ManuscriptStatistic> liveRows = statistics(live);
        assertThat(liveRows).isNotEmpty();

        migrate();
        int rebuilt = statistics(old).size();
        migrate();

        assertThat(statistics(old)).hasSize(rebuilt);
        assertThat(sizes(old)).hasSize(3);
        assertThat(statistics(live)).hasSameSizeAs(liveRows).noneMatch(ManuscriptStatistic::isReconstructed);
    }

    @Test
    void theDaysOfTheLiveCountingAreNotRebuilt() throws Exception {
        Manuscript book = book("Today", null);
        ChatMessage part = chatMessageService.createPart(book, null, "written today", null);
        statisticService.clearBook(book.getUuid());

        migrate();

        assertThat(part.getId()).isNotNull();
        assertThat(statistics(book)).isEmpty();
        assertThat(sizes(book)).isEmpty();
    }

    @Test
    void aBookInTheTrashIsRebuiltToo() throws Exception {
        Manuscript book = legacyBook("Trashed", null);
        manuscriptService.delete(manuscriptService.find(book.getId()), false);

        migrate();

        assertThat(statistics(book)).hasSize(3);
        assertThat(statistics(book).stream().map(ManuscriptStatistic::getCounts).map(StatisticCounts::partsDeleted).reduce(0L, Long::sum))
                .as("the parts went with the book, they were not deleted one by one").isZero();
    }

    @Test
    void aDeletedPartIsADeleteOnTheDayItWasChanged() throws Exception {
        Manuscript book = legacyBook("With a delete", null);
        ChatMessage second = chatMessageService.getAllMessages(book).stream().filter(m -> m.getParent() != null).findFirst().orElseThrow();
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("update ChatMessage m set m.deleted = true, m.modification = :when where m.id = :id")
                    .setParameter("when", at(2)).setParameter("id", second.getId()).executeUpdate();
            em.getTransaction().commit();
        }

        migrate();

        ManuscriptStatistic deleted = statistics(book).stream().filter(row -> row.getDay().equals(today.minusDays(2))).findFirst().orElseThrow();
        assertThat(deleted.getCounts().partsDeleted()).isEqualTo(1);
        assertThat(deleted.getCounts().wordsRemoved()).as("the four words of the part").isEqualTo(4);
        assertThat(sizes(book)).extracting(ManuscriptSize::getBookWords).containsExactly(3L, 5L, 7L, 3L);
    }

    @Test
    void theNextLiveSizeIsMeasuredAgainstTheRebuiltOneAndNotABaseline() throws Exception {
        Manuscript book = legacyBook("Continues", null);
        migrate();

        sizeService.touchNow(book);
        sizeService.flush();

        ManuscriptSize latest = sizeService.sizeAt(book.getUuid(), today);
        assertThat(latest.getDay()).isEqualTo(today);
        assertThat(latest.isBaseline()).isFalse();
        assertThat(latest.isReconstructed()).isFalse();
        assertThat(sizeService.growth(book.getUuid(), today, today).known()).isTrue();
    }

    @Test
    void everyUserGetsTheDayTheTimeIsRecordedFrom() throws Exception {
        migrate();

        UserSetting setting = settingService.find(UserSetting.class, user);
        assertThat(setting.getStatisticsSince()).isEqualTo(today.toString());
    }

    @Test
    void theDayTheTimeIsRecordedFromIsNotChangedBySecondRun() throws Exception {
        UserSetting setting = settingService.getOrCreate(UserSetting.class, user);
        setting.setStatisticsSince("2026-01-05");
        settingService.save(setting);

        migrate();

        assertThat(settingService.find(UserSetting.class, user).getStatisticsSince()).isEqualTo("2026-01-05");
    }

    @Test
    void theReportMarksTheRebuiltDaysAndKnowsWhereTheTimeBegins() throws Exception {
        Manuscript book = legacyBook("Reported", null);
        migrate();

        StatisticReport report = reportService.report(StatisticFilter.of(today.minusDays(7), today, StatisticSide.ALL).forBook(book.getUuid()));

        assertThat(report.days()).hasSize(3).allMatch(StatisticReport.DayLine::reconstructed);
        assertThat(report.reconstructedDays()).isEqualTo(3);
        assertThat(report.timeSince()).isEqualTo(today);
        assertThat(report.timeKnown(today.minusDays(1))).isFalse();
        assertThat(report.timeKnown(today)).isTrue();
        assertThat(report.sizes().get(1).bookWords()).as("the day before the first rebuilt row has no size").isZero();
        assertThat(report.sizes().get(1).present()).isFalse();
        assertThat(report.sizes().get(2).bookWords()).isEqualTo(3);
        assertThat(report.growth().bookWords()).isEqualTo(7);
    }

    @Test
    void aLiveDayIsNotMarkedAndAUserWithoutTheDayKnowsAllTheTimes() throws Exception {
        Manuscript book = book("Live book", null);
        chatMessageService.createPart(book, null, "live words", null);

        StatisticReport report = reportService.report(StatisticFilter.of(today, today, StatisticSide.ALL).forBook(book.getUuid()));

        assertThat(report.days()).isNotEmpty().noneMatch(StatisticReport.DayLine::reconstructed);
        assertThat(report.timeSince()).isNull();
        assertThat(report.timeKnown(today.minusDays(400))).isTrue();
    }

    @Test
    void anotherVersionIsNotMigrated() throws Exception {
        StatisticsBackfillMigration migration = new StatisticsBackfillMigration();
        beanFactory.autowireBean(migration);

        assertThat(migration.migrate(7, MigrationType.APP)).isEqualTo(7);
        assertThat(migration.migrate(9, MigrationType.APP)).isEqualTo(9);
        assertThat(migration.migrate(8, MigrationType.DB)).isEqualTo(8);
    }

}
