package com.github.enerccio.marginalia.db;

import com.github.enerccio.marginalia.bound.Migration.MigrationType;
import com.github.enerccio.marginalia.bound.migration.VersionHistoryPermissionMigration;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.EffectiveHistoryLimit;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The app migration 7 → 8 gives every user, administrators included, the permission {@code VERSION_HISTORY} with the default
 * limit.
 */
class VersionHistoryPermissionMigrationTest extends MarginaliaTestBase {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private JdbcTemplate jdbc;

    private VersionHistoryPermissionMigration migration() {
        return context.getAutowireCapableBeanFactory().createBean(VersionHistoryPermissionMigration.class);
    }

    private int permissionsOf(User user) {
        return jdbc.queryForObject("SELECT count(*) FROM permissions WHERE holder_id = ? AND permissionType = 'VERSION_HISTORY'", Integer.class, user.getId());
    }

    @Test
    void everybodyGetsItIncludingAdministrators() throws Exception {
        User user = createUser();
        User admin = createUser(uniqueName("admin"), "secret", true);

        assertThat(migration().migrate(7, MigrationType.APP)).isEqualTo(8);

        for (User holder : new User[]{user, admin}) {
            Permission permission = permissionService.getPermission(holder, PermissionType.VERSION_HISTORY);
            assertThat(permission.isGranted()).isTrue();
            assertThat(permission.getHistoryMaxVersions()).isEqualTo(EffectiveHistoryLimit.DEFAULT_MAX_VERSIONS);
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM permissions WHERE holder_id = ? AND userId IS NULL AND permissionType = 'VERSION_HISTORY'", Integer.class, user.getId()))
                .as("no administrator gave it").isEqualTo(1);
    }

    @Test
    void leavesWhatAnAdministratorSetAndDeletedUsersAlone() throws Exception {
        User tuned = createUser();
        loginAdmin();
        permissionService.setGranted(tuned, PermissionType.VERSION_HISTORY, false);
        User deleted = createUser();
        userService.deleteUser(deleted);

        migration().migrate(7, MigrationType.APP);
        migration().migrate(7, MigrationType.APP);

        assertThat(permissionService.checkPermissions(tuned, PermissionType.VERSION_HISTORY)).as("taken away by an administrator").isFalse();
        assertThat(permissionsOf(tuned)).isOne();
        assertThat(permissionsOf(deleted)).isZero();
    }

    @Test
    void onlyRunsForAppVersionSeven() throws Exception {
        User user = createUser();

        assertThat(migration().migrate(6, MigrationType.APP)).isEqualTo(6);
        assertThat(migration().migrate(8, MigrationType.APP)).isEqualTo(8);
        assertThat(migration().migrate(7, MigrationType.DB)).isEqualTo(7);
        assertThat(permissionsOf(user)).isZero();
    }
}
