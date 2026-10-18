package com.github.enerccio.marginalia.domain.security.service;

import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.security.repository.PermissionRepository;
import com.github.enerccio.marginalia.domain.service.ExtendableService;

import java.util.List;

/**
 * What the users were allowed to do by an administrator. Permissions are looked up by the holder, not by the owner:
 * the owner is the administrator who granted it. Only administrators can change them ({@code save}, {@code delete}
 * throw {@link SecurityException} for anybody else), everybody can be asked about.
 * <p>
 * An administrator is not given special treatment here, the callers that let administrators do everything check
 * {@code AdminGuard#isAdmin()} too.
 */
public interface PermissionService extends ExtendableService<Permission, PermissionRepository> {

    /**
     * @param user may be the session's proxy of the user, it is loaded again
     * @return whether the user has a permission of the type and it is granted; false when there is none
     */
    boolean checkPermissions(User user, PermissionType type) throws Exception;

    /**
     * @param user may be the session's proxy of the user, it is loaded again
     * @return the permission of the user, null when there is none
     */
    Permission getPermission(User user, PermissionType type) throws Exception;

    /**
     * @param user may be the session's proxy of the user, it is loaded again
     * @return all the permissions of the user, granted or not
     */
    List<Permission> getPermissions(User user) throws Exception;

    /**
     * Gives the permission to the user or takes it away, administrators only: the existing permission is changed, a
     * missing one is created (owned by the current administrator).
     *
     * @return the saved permission
     */
    Permission setGranted(User user, PermissionType type, boolean granted) throws Exception;

}
