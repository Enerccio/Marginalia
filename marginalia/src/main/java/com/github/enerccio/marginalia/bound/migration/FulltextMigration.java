package com.github.enerccio.marginalia.bound.migration;

import com.github.enerccio.marginalia.bound.Migration;
import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.traits.Fulltextable;
import com.github.enerccio.marginalia.utils.ReflectUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * App version 2 → 3: fills {@code _fulltext} (V9 adds the column) of the rows saved before it existed. Goes over every
 * entity with {@link Fulltextable} fields, deleted ones too (they can be restored), and rebuilds the column from the
 * loaded entity, so {@code extendedContent} and {@code modification} are left alone.
 */
public class FulltextMigration implements Migration {
    private static final Logger log = LoggerFactory.getLogger(FulltextMigration.class);

    private static final int BATCH = 200;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private final ExtendableEntityListener listener = new ExtendableEntityListener();

    @Override
    public int migrate(int cversion, MigrationType migrationType) throws Exception {
        if (migrationType != MigrationType.APP || cversion != 2) {
            return cversion;
        }

        for (EntityType<?> type : entityManagerFactory.getMetamodel().getEntities()) {
            Class<?> clazz = type.getJavaType();
            if (ExtendableEntity.class.isAssignableFrom(clazz)
                    && !ReflectUtils.getAnnotatedFields(clazz, Fulltextable.class).isEmpty()) {
                log.info("Filled _fulltext of {} {} rows", fill(type.getName()), type.getName());
            }
        }
        return 3;
    }

    /**
     * Walks the table by id, one transaction per batch, so memory stays flat and a failure keeps the finished batches
     * (the migration is repeated from the start on the next start, filling is idempotent).
     */
    private int fill(String entityName) throws Exception {
        int filled = 0;
        long lastId = 0;
        while (true) {
            try (EntityManager em = entityManagerFactory.createEntityManager()) {
                em.getTransaction().begin();
                try {
                    List<? extends ExtendableEntity> batch = em
                            .createQuery("select e from " + entityName + " e where e.id > :last order by e.id", ExtendableEntity.class)
                            .setParameter("last", lastId)
                            .setMaxResults(BATCH)
                            .getResultList();
                    for (ExtendableEntity entity : batch) {
                        listener.updateFulltext(entity);
                        lastId = entity.getId();
                    }
                    em.getTransaction().commit();
                    filled += batch.size();
                    if (batch.size() < BATCH) {
                        return filled;
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
