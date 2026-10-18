package com.github.enerccio.marginalia.bound.migration;

import com.github.enerccio.marginalia.bound.Migration;
import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.impl.settings.AppSettings;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.SettingService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * App version 4 → 5: the application wide switches {@code userAisAllowed} and {@code helpEnabled} became the
 * permissions {@code CREATE_AI} and {@code HELPER} of the users. A switch that was on grants the permission to every
 * existing user (the permission has no owner, no administrator gave it). The settings of the help assistant that
 * depended on the user (apply limits, navigating the interface) are copied into the new {@code HELPER} permissions.
 * Users that already have the permission are left alone, so the migration can be repeated.
 */
public class PermissionsMigration implements Migration {
    private static final Logger log = LoggerFactory.getLogger(PermissionsMigration.class);

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private SettingService settingService;

    private final ExtendableEntityListener listener = new ExtendableEntityListener();

    @Override
    @SuppressWarnings("deprecation")
    public int migrate(int cversion, MigrationType migrationType) throws Exception {
        if (migrationType != MigrationType.APP || cversion != 4) {
            return cversion;
        }

        AppSettings settings = settingService.getOrCreateApp(AppSettings.class);
        Set<PermissionType> grant = EnumSet.noneOf(PermissionType.class);
        if (settings.getUserAisAllowed()) {
            grant.add(PermissionType.CREATE_AI);
        }
        if (settings.getHelpEnabled()) {
            grant.add(PermissionType.HELPER);
        }

        log.info("Granted {} permissions of {}", grant.isEmpty() ? 0 : grant(grant, settings), grant);
        return 5;
    }

    /**
     * Reads and writes in separate sessions: the id generator of a new entity updates its sequence table on another
     * connection, SQLite refuses that (database is locked) while the session already holds a read lock.
     */
    @SuppressWarnings("deprecation")
    private int grant(Set<PermissionType> types, AppSettings settings) throws Exception {
        record Grant(Long user, PermissionType type) {}

        List<Grant> missing = new ArrayList<>();
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            List<Long> users = em.createQuery("select u.id from User u where u.deleted = false", Long.class).getResultList();
            Set<Grant> existing = new HashSet<>();
            for (Object[] row : em.createQuery("select p.permissionHolder.id, p.permissionType from Permission p where p.deleted = false", Object[].class)
                    .getResultList()) {
                existing.add(new Grant((Long) row[0], (PermissionType) row[1]));
            }
            for (Long user : users) {
                for (PermissionType type : types) {
                    Grant grant = new Grant(user, type);
                    if (!existing.contains(grant)) {
                        missing.add(grant);
                    }
                }
            }
        }
        if (missing.isEmpty()) {
            return 0;
        }
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            try {
                for (Grant grant : missing) {
                    Permission permission = new Permission();
                    permission.setPermissionHolder(em.getReference(User.class, grant.user()));
                    permission.setPermissionType(grant.type());
                    permission.setGranted(true);
                    if (grant.type() == PermissionType.HELPER) {
                        permission.setHelperApplyLimits(settings.getHelpApplyLimits());
                        permission.setHelperInteractions(settings.getHelpInteractions());
                    }
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
