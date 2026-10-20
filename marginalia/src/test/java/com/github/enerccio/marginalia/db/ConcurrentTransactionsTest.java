package com.github.enerccio.marginalia.db;

import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SQLite has one writer at a time: a transaction that reads and then writes fails at once (SQLITE_BUSY_SNAPSHOT) when
 * another connection has committed between the two, unless it began with the write lock. Read-write transactions do,
 * read-only ones must not wait for them, see {@code ReadWriteRoutingDataSource}.
 */
class ConcurrentTransactionsTest extends MarginaliaTestBase {

    private static final int THREADS = 8;
    private static final int ROUNDS = 25;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void table() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS concurrent_tx_test (id INTEGER PRIMARY KEY AUTOINCREMENT, writer INTEGER)");
        jdbc.execute("DELETE FROM concurrent_tx_test");
    }

    @Test
    void transactionsThatReadAndThenWriteWaitForEachOtherInsteadOfFailing() throws Exception {
        TransactionTemplate readWrite = new TransactionTemplate(transactionManager);
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        try {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<?>> done = new ArrayList<>();
            for (int writer = 0; writer < THREADS; writer++) {
                int id = writer;
                done.add(pool.submit(() -> {
                    start.await();
                    for (int round = 0; round < ROUNDS; round++) {
                        readWrite.executeWithoutResult(_ -> {
                            // reads first: the snapshot is taken before the write
                            jdbc.queryForObject("SELECT count(*) FROM concurrent_tx_test", Integer.class);
                            jdbc.update("INSERT INTO concurrent_tx_test (writer) VALUES (?)", id);
                        });
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

        assertThat(jdbc.queryForObject("SELECT count(*) FROM concurrent_tx_test", Integer.class)).isEqualTo(THREADS * ROUNDS);
    }

    @Test
    void aReadOnlyTransactionDoesNotWaitForAWriterThatHoldsTheLock() throws Exception {
        TransactionTemplate readWrite = new TransactionTemplate(transactionManager);
        TransactionTemplate readOnly = new TransactionTemplate(transactionManager);
        readOnly.setReadOnly(true);
        ExecutorService pool = Executors.newSingleThreadExecutor();
        CountDownLatch holding = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try {
            Future<?> writer = pool.submit(() -> readWrite.executeWithoutResult(_ -> {
                jdbc.update("INSERT INTO concurrent_tx_test (writer) VALUES (0)");
                holding.countDown();
                try {
                    release.await(30, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
            assertThat(holding.await(10, TimeUnit.SECONDS)).isTrue();

            long begin = System.nanoTime();
            Integer seen = readOnly.execute(_ -> jdbc.queryForObject("SELECT count(*) FROM concurrent_tx_test", Integer.class));
            Duration took = Duration.ofNanos(System.nanoTime() - begin);

            assertThat(took).as("the reader does not queue behind the writer").isLessThan(Duration.ofSeconds(2));
            assertThat(seen).as("the write is not committed yet").isZero();
            release.countDown();
            writer.get(10, TimeUnit.SECONDS);
        } finally {
            release.countDown();
            pool.shutdownNow();
        }
    }
}
