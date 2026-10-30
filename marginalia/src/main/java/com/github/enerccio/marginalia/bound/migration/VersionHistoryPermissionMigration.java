package com.github.enerccio.marginalia.bound.migration;

import com.github.enerccio.marginalia.bound.Migration;
import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.EffectiveHistoryLimit;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * App version 7 → 8: every user, administrators included, gets the permission {@code VERSION_HISTORY}, granted, with the
 * default limit of {@value EffectiveHistoryLimit#DEFAULT_MAX_VERSIONS} versions of a part, so that the administrator sees and can
 * change it. A user without the permission has the same (the permission is theirs by default), so the migration changes no
 * behavior; it only makes the setting visible. A user that has the permission already is left alone, so it can be repeated.
 */
public class VersionHistoryPermissionMigration implements Migration {
    private static final Logger log = LoggerFactory.getLogger(VersionHistoryPermissionMigration.class);

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private final ExtendableEntityListener listener = new ExtendableEntityListener();

    @Override
    public int migrate(int cversion, MigrationType migrationType) throws Exception {
        if (migrationType != MigrationType.APP || cversion != 7) {
            return cversion;
        }
        log.info("Granted the permission {} to {} users", PermissionType.VERSION_HISTORY, grant());
        return 8;
    }

    /**
     * Reads and writes in separate sessions: the id generator of a new entity updates its sequence table on another
     * connection, SQLite refuses that (database is locked) while the session already holds a read lock.
     */
    private int grant() throws Exception {
        List<Long> missing;
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            missing = new ArrayList<>(em.createQuery("select u.id from User u where u.deleted = false", Long.class).getResultList());
            missing.removeAll(em.createQuery("select p.permissionHolder.id from Permission p where p.deleted = false and p.permissionType = :type", Long.class)
                    .setParameter("type", PermissionType.VERSION_HISTORY)
                    .getResultList());
        }
        if (missing.isEmpty()) {
            return 0;
        }
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            try {
                for (Long user : missing) {
                    Permission permission = new Permission();
                    permission.setPermissionHolder(em.getReference(User.class, user));
                    permission.setPermissionType(PermissionType.VERSION_HISTORY);
                    permission.setGranted(true);
                    permission.setHistoryMaxVersions(EffectiveHistoryLimit.DEFAULT_MAX_VERSIONS);
                    permission.setUuid(UUID.randomUUID().toString());
                    permission.setCreation(new Date());
                    permission.setModification(new Date());
                    listener.serialize(permission);
                    em.persist(permission);
                }
                em.getTransaction().commit();
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
        return missing.size();
    }
}
