package com.github.enerccio.marginalia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.SQLException;
import java.util.Map;

/**
 * SQLite lets one writer at a time in. A transaction that begins deferred reads first and takes the write lock at its
 * first write; when another connection has committed in between, the write fails at once with SQLITE_BUSY(_SNAPSHOT)
 * and the busy timeout does not help (the snapshot of the reads is stale). So a read-write transaction runs on a
 * connection that begins with the write lock ({@code transaction_mode=IMMEDIATE}) and the writers queue for the busy
 * timeout; a read-only transaction keeps the plain connection and never waits for a writer.
 * <p>
 * The transaction manager asks for the connection when the transaction begins, before it publishes the read-only
 * flag, so {@link Dialect} tells the choice for that moment.
 */
public class ReadWriteRoutingDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<Boolean> BEGINNING_WRITE = new ThreadLocal<>();

    public ReadWriteRoutingDataSource(javax.sql.DataSource readOnly, javax.sql.DataSource readWrite) {
        setTargetDataSources(Map.of(Boolean.FALSE, readOnly, Boolean.TRUE, readWrite));
        setDefaultTargetDataSource(readOnly);
        afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        Boolean beginning = BEGINNING_WRITE.get();
        if (beginning != null) {
            return beginning;
        }
        return TransactionSynchronizationManager.isActualTransactionActive()
                && !TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    }

    /**
     * Notes whether the transaction that begins writes, for the time its connection is taken.
     */
    public static class Dialect extends HibernateJpaDialect {

        @Override
        public Object beginTransaction(EntityManager entityManager, TransactionDefinition definition)
                throws PersistenceException, SQLException, TransactionException {
            Boolean outer = BEGINNING_WRITE.get();
            BEGINNING_WRITE.set(!definition.isReadOnly());
            try {
                return super.beginTransaction(entityManager, definition);
            } finally {
                if (outer == null) {
                    BEGINNING_WRITE.remove();
                } else {
                    BEGINNING_WRITE.set(outer);
                }
            }
        }
    }
}
