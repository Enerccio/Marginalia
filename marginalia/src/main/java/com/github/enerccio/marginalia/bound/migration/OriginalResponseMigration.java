package com.github.enerccio.marginalia.bound.migration;

import com.github.enerccio.marginalia.bound.Migration;
import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * App version 3 → 4: copies {@code response} of every part into the new extended attribute {@code originalResponse},
 * deleted parts too (they can be restored). Nothing is known about what was edited before, so all existing text counts
 * as generated. Parts that already have an original response are left alone; {@code modification} is not touched.
 */
public class OriginalResponseMigration implements Migration {
    private static final Logger log = LoggerFactory.getLogger(OriginalResponseMigration.class);

    private static final int BATCH = 200;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private final ExtendableEntityListener listener = new ExtendableEntityListener();

    @Override
    public int migrate(int cversion, MigrationType migrationType) throws Exception {
        if (migrationType != MigrationType.APP || cversion != 3) {
            return cversion;
        }

        log.info("Copied response to originalResponse of {} messages", copyResponses());
        return 4;
    }

    /**
     * Walks the table by id, one transaction per batch, so memory stays flat and a failure keeps the finished batches
     * (the migration is repeated from the start on the next start, copying is idempotent).
     */
    private int copyResponses() throws Exception {
        int copied = 0;
        long lastId = 0;
        while (true) {
            try (EntityManager em = entityManagerFactory.createEntityManager()) {
                em.getTransaction().begin();
                try {
                    List<ChatMessage> batch = em
                            .createQuery("select m from ChatMessage m where m.id > :last order by m.id", ChatMessage.class)
                            .setParameter("last", lastId)
                            .setMaxResults(BATCH)
                            .getResultList();
                    for (ChatMessage message : batch) {
                        lastId = message.getId();
                        if (message.getResponse() != null && message.getOriginalResponse() == null) {
                            message.setOriginalResponse(message.getResponse());
                            listener.serialize(message);
                            copied++;
                        }
                    }
                    em.getTransaction().commit();
                    if (batch.size() < BATCH) {
                        return copied;
                    }
                } catch (Exception e) {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    throw e;
                }
            }
        }
    }
}
