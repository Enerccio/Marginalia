package com.github.enerccio.marginalia.db;

import com.github.enerccio.marginalia.bound.Migration.MigrationType;
import com.github.enerccio.marginalia.bound.migration.PermissionsMigration;
import com.github.enerccio.marginalia.domain.model.impl.settings.AppSettings;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The app migration 4 → 5 gives the permissions {@code CREATE_AI} and {@code HELPER} to the existing users when the
 * application wide switches they replace were on.
 */
@SuppressWarnings("deprecation")
class PermissionsMigrationTest extends MarginaliaTestBase {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private SettingService settingService;

    private PermissionsMigration migration() {
        return context.getAutowireCapableBeanFactory().createBean(PermissionsMigration.class);
    }

    private void switches(boolean userAis, boolean help) throws Exception {
        AppSettings settings = settingService.getOrCreateApp(AppSettings.class);
        settings.setUserAisAllowed(userAis);
        settings.setHelpEnabled(help);
        settings.setHelpApplyLimits(false);
        settings.setHelpInteractions(false);
        settingService.save(settings);
    }

    /** The application settings are shared by all tests. */
    @AfterEach
    void defaultSwitches() throws Exception {
        switches(true, false);
    }

    /** A user as they were before the permissions: none (the test base would give CREATE_AI). */
    private User oldUser() throws Exception {
        User user = createUser();
        jdbc.update("DELETE FROM permissions WHERE holder_id = ?", user.getId());
        return user;
    }

    @Test
    void grantsWhatWasOnForEverybody() throws Exception {
        User user = oldUser();
        switches(true, true);

        assertThat(migration().migrate(4, MigrationType.APP)).isEqualTo(5);

        assertThat(permissionService.checkPermissions(user, PermissionType.CREATE_AI)).isTrue();
        assertThat(permissionService.checkPermissions(user, PermissionType.HELPER)).isTrue();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM permissions WHERE holder_id = ? AND userId IS NULL", Integer.class, user.getId()))
                .as("no administrator gave them").isEqualTo(2);
    }

    @Test
    void copiesTheSettingsOfTheHelpAssistantIntoTheHelperPermission() throws Exception {
        User user = oldUser();
        switches(true, true);
        AppSettings settings = settingService.getOrCreateApp(AppSettings.class);
        settings.setHelpApplyLimits(true);
        settings.setHelpInteractions(true);
        settingService.save(settings);

        migration().migrate(4, MigrationType.APP);

        Permission helper = permissionService.getPermission(user, PermissionType.HELPER);
        assertThat(helper.getHelperApplyLimits()).isTrue();
        assertThat(helper.getHelperInteractions()).isTrue();
    }

    @Test
    void helperPermissionKeepsTheDefaultsWhenTheSettingsWereOff() throws Exception {
        User user = oldUser();
        switches(true, true);

        migration().migrate(4, MigrationType.APP);

        Permission helper = permissionService.getPermission(user, PermissionType.HELPER);
        assertThat(helper.getHelperApplyLimits()).isFalse();
        assertThat(helper.getHelperInteractions()).isFalse();
    }

    @Test
    void doesNotGrantWhatWasOff() throws Exception {
        User user = oldUser();
        switches(false, false);

        migration().migrate(4, MigrationType.APP);

        assertThat(permissionService.checkPermissions(user, PermissionType.CREATE_AI)).isFalse();
        assertThat(permissionService.checkPermissions(user, PermissionType.HELPER)).isFalse();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM permissions WHERE holder_id = ?", Integer.class, user.getId())).isZero();
    }

    @Test
    void grantsOnlyTheSwitchesThatWereOn() throws Exception {
        User user = oldUser();
        switches(false, true);

        migration().migrate(4, MigrationType.APP);

        assertThat(permissionService.checkPermissions(user, PermissionType.CREATE_AI)).isFalse();
        assertThat(permissionService.checkPermissions(user, PermissionType.HELPER)).isTrue();
    }

    @Test
    void leavesExistingPermissionsAndDeletedUsersAlone() throws Exception {
        User revoked = oldUser();
        loginAdmin();
        permissionService.setGranted(revoked, PermissionType.CREATE_AI, false);
        User deleted = oldUser();
        userService.deleteUser(deleted);
        switches(true, false);

        migration().migrate(4, MigrationType.APP);
        migration().migrate(4, MigrationType.APP);

        assertThat(permissionService.checkPermissions(revoked, PermissionType.CREATE_AI)).as("revoked by an administrator").isFalse();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM permissions WHERE holder_id = ?", Integer.class, revoked.getId())).isOne();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM permissions WHERE holder_id = ?", Integer.class, deleted.getId())).isZero();
    }

    @Test
    void onlyRunsForAppVersionFour() throws Exception {
        User user = oldUser();
        switches(true, true);

        assertThat(migration().migrate(3, MigrationType.APP)).isEqualTo(3);
        assertThat(migration().migrate(5, MigrationType.APP)).isEqualTo(5);
        assertThat(migration().migrate(4, MigrationType.DB)).isEqualTo(4);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM permissions WHERE holder_id = ?", Integer.class, user.getId())).isZero();
    }
}
