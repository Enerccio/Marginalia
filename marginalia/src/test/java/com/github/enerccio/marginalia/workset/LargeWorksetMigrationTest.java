package com.github.enerccio.marginalia.workset;

import com.github.enerccio.marginalia.bound.Migration.MigrationType;
import com.github.enerccio.marginalia.bound.migration.PartRevisionMigration;
import com.github.enerccio.marginalia.bound.migration.StatisticsBackfillMigration;
import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptSize;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptStatistic;
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
 * The two data migrations of the upgrade to 1.7.0 ({@link PartRevisionMigration}, {@link StatisticsBackfillMigration}) run over a book of a real size, the book of the
 * dataset (see {@link WorksetFixture}): 103 parts, 360 versions, a history of months. The unit tests of the migrations use books of a few parts; these show that the result
 * is right for a long one and how long it takes. Every test restores its own copy of the book (about 5 s), the migrations change the data.
 */
class LargeWorksetMigrationTest extends MarginaliaTestBase {

    /** Far above the 1 to 2 seconds a laptop needs; it only guards against a migration that became quadratic. */
    private static final long LIMIT_MILLIS = 120_000;

    @Autowired
    private BackupService backupService;
    @Autowired
    private LorebookService lorebookService;
    @Autowired
    private ManuscriptService manuscriptService;
    @Autowired
    private ManuscriptStatisticService statisticService;
    @Autowired
    private ManuscriptSizeService sizeService;
    @Autowired
    private MessageRevisionService messageRevisionService;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    private Manuscript book;
    private List<Long> partIds;

    @BeforeEach
    void restoreTheBook() throws Exception {
        login();
        // the size worker would write rows of its own
        ((ManuscriptSizeServiceImpl) AopTestUtils.getUltimateTargetObject(sizeService)).setManual(true);
        book = WorksetFixture.restore(backupService, lorebookService, uniqueName("Unregistered"), true);
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            partIds = em.createQuery("select m.id from ChatMessage m where m.parentScript.id = :book", Long.class).setParameter("book", book.getId()).getResultList();
        }
        assertThat(partIds).hasSize(103);
    }

    @AfterEach
    void tearDown() {
        ((ManuscriptSizeServiceImpl) AopTestUtils.getUltimateTargetObject(sizeService)).setManual(false);
    }

    private long revisions() {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return em.createQuery("select count(r) from MessageRevision r where r.message.parentScript.id = :book", Long.class).setParameter("book", book.getId()).getSingleResult();
        }
    }

    @Test
    void theFirstVersionsOfAHundredPartsAreRebuiltAndItIsFast() throws Exception {
        long before = revisions();
        assertThat(before).as("versions the restore brought").isEqualTo(360);
        // the book as the version before the history had it: no versions at all
        messageRevisionService.hardDeleteForParts(partIds);
        assertThat(revisions()).isZero();

        PartRevisionMigration migration = new PartRevisionMigration();
        beanFactory.autowireBean(migration);
        long start = System.nanoTime();
        assertThat(migration.migrate(6, MigrationType.APP)).isEqualTo(7);
        long millis = (System.nanoTime() - start) / 1_000_000;

        long after = revisions();
        System.out.println("[large workset] PartRevisionMigration: " + after + " versions for " + partIds.size() + " parts in " + millis + " ms (over the whole database of the test)");
        // one version for a part written and left, two for one that was edited since; every part of this book has text
        assertThat(after).isBetween((long) partIds.size(), 2L * partIds.size());
        assertThat(millis).isLessThan(LIMIT_MILLIS);
        // repeating it changes nothing
        assertThat(migration.migrate(6, MigrationType.APP)).isEqualTo(7);
        assertThat(revisions()).isEqualTo(after);
    }

    /** The parts and their versions spread over 90 days, as the dataset has them in the harness (the restore stamps everything with today). */
    private void spreadOverDays() {
        LocalDate today = LocalDate.now();
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            List<Long> ordered = em.createQuery("select m.id from ChatMessage m where m.parentScript.id = :book order by m.id", Long.class)
                    .setParameter("book", book.getId()).getResultList();
            em.getTransaction().begin();
            for (int i = 0; i < ordered.size(); i++) {
                int daysAgo = 90 - (int) Math.round(i * 90.0 / ordered.size());
                Date when = Date.from(today.minusDays(Math.max(daysAgo, 1)).atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant());
                em.createQuery("update MessageRevision r set r.creation = :when where r.message.id = :id").setParameter("when", when).setParameter("id", ordered.get(i)).executeUpdate();
                em.createQuery("update ChatMessage m set m.creation = :when, m.modification = :when where m.id = :id").setParameter("when", when).setParameter("id", ordered.get(i)).executeUpdate();
            }
            em.getTransaction().commit();
        }
    }

    @Test
    void theStatisticsOfALongBookAreRebuiltFromItsHistory() throws Exception {
        spreadOverDays();
        statisticService.clearBook(book.getUuid());
        LocalDate today = LocalDate.now();
        StatisticFilter filter = StatisticFilter.of(today.minusDays(120), today, StatisticSide.ALL).forBook(book.getUuid());
        assertThat(statisticService.findRange(filter)).isEmpty();

        StatisticsBackfillMigration migration = new StatisticsBackfillMigration();
        beanFactory.autowireBean(migration);
        long start = System.nanoTime();
        assertThat(migration.migrate(8, MigrationType.APP)).isEqualTo(9);
        long millis = (System.nanoTime() - start) / 1_000_000;

        List<ManuscriptStatistic> rows = statisticService.findRange(filter);
        List<ManuscriptSize> sizes = sizeService.findRange(book.getUuid(), today.minusDays(120), today);
        long words = rows.stream().mapToLong(r -> r.getCounts().wordsWritten()).sum();
        long parts = rows.stream().mapToLong(r -> r.getCounts().partsCreated()).sum();
        System.out.println("[large workset] StatisticsBackfillMigration: " + rows.size() + " days of counts (" + words + " words written, " + parts + " parts created), "
                + sizes.size() + " size rows, in " + millis + " ms (over the whole database of the test)");
        assertThat(rows).hasSizeGreaterThan(30).allMatch(ManuscriptStatistic::isReconstructed);
        assertThat(parts).as("every part is counted once, on the day it was written").isEqualTo(103);
        assertThat(words).isPositive();
        assertThat(sizes).isNotEmpty().allMatch(ManuscriptSize::isReconstructed);
        assertThat(sizes.getLast().getBookWords()).as("the size of the book on the last day it grew").isPositive();
        assertThat(millis).isLessThan(LIMIT_MILLIS);
    }
}
