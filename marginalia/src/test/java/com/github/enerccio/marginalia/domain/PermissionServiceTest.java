package com.github.enerccio.marginalia.domain;

import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Permissions are looked up by the holder whoever created them; only administrators change them.
 */
class PermissionServiceTest extends MarginaliaTestBase {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void noPermissionObjectMeansNoPermission() throws Exception {
        User user = createUser();

        assertThat(permissionService.checkPermissions(user, PermissionType.HELPER)).isFalse();
        assertThat(permissionService.getPermission(user, PermissionType.HELPER)).isNull();
    }

    @Test
    void grantedPermissionIsFoundAndRevokedOneIsNot() throws Exception {
        User user = createUser();
        // the test users are given CREATE_AI
        jdbc.update("DELETE FROM permissions WHERE holder_id = ?", user.getId());
        loginAdmin();

        Permission permission = permissionService.setGranted(user, PermissionType.HELPER, true);
        assertThat(permissionService.checkPermissions(user, PermissionType.HELPER)).isTrue();
        assertThat(permissionService.getPermission(user, PermissionType.HELPER).getId()).isEqualTo(permission.getId());
        assertThat(permissionService.checkPermissions(user, PermissionType.CREATE_AI)).as("another type").isFalse();

        Permission revoked = permissionService.setGranted(user, PermissionType.HELPER, false);
        assertThat(revoked.getId()).as("the same permission is changed").isEqualTo(permission.getId());
        assertThat(permissionService.checkPermissions(user, PermissionType.HELPER)).isFalse();
        assertThat(permissionService.getPermission(user, PermissionType.HELPER)).isNotNull().extracting(Permission::isGranted).isEqualTo(false);
    }

    @Test
    void ownerOfThePermissionDoesNotMatter() throws Exception {
        User user = createUser();
        User admin = loginAdmin();
        Permission permission = permissionService.setGranted(user, PermissionType.HELPER, true);
        assertThat(permission.getOwner().getId()).isEqualTo(admin.getId());

        // another administrator, a user, and nobody asking: the answer is the same
        loginAdmin();
        assertThat(permissionService.checkPermissions(user, PermissionType.HELPER)).isTrue();
        loginAs(user);
        assertThat(permissionService.checkPermissions(user, PermissionType.HELPER)).isTrue();

        // a permission without an owner (the migration creates those)
        jdbc.update("UPDATE permissions SET userId = NULL WHERE id = ?", permission.getId());
        assertThat(permissionService.checkPermissions(user, PermissionType.HELPER)).isTrue();
    }

    @Test
    void sessionProxyOfTheUserIsRehydrated() throws Exception {
        User user = createUser();
        setPermission(user, PermissionType.HELPER, true);

        // the session's copy only has the id, login and name; the other users' ids do not leak into the answer
        loginAs(user);
        assertThat(permissionService.checkPermissions(currentUser, PermissionType.HELPER)).isTrue();
        assertThat(permissionService.getPermission(currentUser, PermissionType.HELPER).getPermissionHolder().getId())
                .isEqualTo(user.getId());

        User other = new User();
        other.setId(user.getId());
        assertThat(permissionService.checkPermissions(other, PermissionType.HELPER)).isTrue();
    }

    @Test
    void unknownOrMissingUserHasNoPermission() throws Exception {
        assertThat(permissionService.checkPermissions(null, PermissionType.HELPER)).isFalse();
        assertThat(permissionService.checkPermissions(new User(), PermissionType.HELPER)).isFalse();
        User unknown = new User();
        unknown.setId(Long.MAX_VALUE);
        assertThat(permissionService.checkPermissions(unknown, PermissionType.HELPER)).isFalse();
        assertThat(permissionService.getPermission(createUser(), null)).isNull();
    }

    @Test
    void deletedPermissionAndDeletedUserDoNotCount() throws Exception {
        User user = createUser();
        setPermission(user, PermissionType.HELPER, true);
        loginAdmin();
        permissionService.delete(permissionService.getPermission(user, PermissionType.HELPER), false);

        assertThat(permissionService.checkPermissions(user, PermissionType.HELPER)).isFalse();
        assertThat(permissionService.getPermission(user, PermissionType.HELPER)).isNull();

        // granting again creates a new one next to the deleted
        permissionService.setGranted(user, PermissionType.HELPER, true);
        assertThat(permissionService.checkPermissions(user, PermissionType.HELPER)).isTrue();

        userService.deleteUser(user);
        assertThat(permissionService.checkPermissions(user, PermissionType.HELPER)).isFalse();
    }

    @Test
    void permissionsOfAUserAreListed() throws Exception {
        User user = createUser();
        loginAdmin();
        permissionService.setGranted(user, PermissionType.HELPER, true);
        permissionService.setGranted(user, PermissionType.CREATE_AI, false);

        assertThat(permissionService.getPermissions(user))
                .extracting(Permission::getPermissionType, Permission::isGranted)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(PermissionType.HELPER, true),
                        org.assertj.core.groups.Tuple.tuple(PermissionType.CREATE_AI, false));
    }

    @Test
    void settingsOfThePermissionTypeAreExtendedAttributes() throws Exception {
        User user = createUser();
        loginAdmin();
        Permission permission = permissionService.setGranted(user, PermissionType.HELPER, true);
        assertThat(permission.getHelperApplyLimits()).as("off by default").isFalse();
        assertThat(permission.getHelperInteractions()).isFalse();

        permission.setHelperApplyLimits(true);
        permission.setHelperInteractions(true);
        permissionService.save(permission);

        Permission loaded = permissionService.getPermission(user, PermissionType.HELPER);
        assertThat(loaded.getHelperApplyLimits()).isTrue();
        assertThat(loaded.getHelperInteractions()).isTrue();
    }

    @Test
    void typeIsStoredByName() throws Exception {
        User user = createUser();
        loginAdmin();
        Permission permission = permissionService.setGranted(user, PermissionType.HELPER, true);

        assertThat(jdbc.queryForObject("SELECT permissionType FROM permissions WHERE id = ?", String.class, permission.getId()))
                .isEqualTo("HELPER");
    }

    @Test
    void onlyAdministratorsChangePermissions() throws Exception {
        User user = login();
        Permission own = new Permission();
        own.setPermissionHolder(user);
        own.setPermissionType(PermissionType.HELPER);
        own.setGranted(true);

        assertThatThrownBy(() -> permissionService.save(own)).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> permissionService.saveWithoutEvent(own)).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> permissionService.setGranted(user, PermissionType.HELPER, true)).isInstanceOf(SecurityException.class);
        assertThat(permissionService.checkPermissions(user, PermissionType.HELPER)).isFalse();

        loginAdmin();
        Permission saved = permissionService.save(own);
        assertThat(saved.getId()).isNotNull();
        assertThat(permissionService.checkPermissions(user, PermissionType.HELPER)).isTrue();

        loginAs(user);
        assertThatThrownBy(() -> permissionService.delete(saved, false)).isInstanceOf(SecurityException.class);
        assertThat(permissionService.checkPermissions(user, PermissionType.HELPER)).isTrue();
    }
}
