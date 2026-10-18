package com.github.enerccio.marginalia.domain.security.service.impl;

import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.security.repository.PermissionRepository;
import com.github.enerccio.marginalia.domain.security.service.PermissionService;
import com.github.enerccio.marginalia.domain.service.impl.ExtendableServiceImpl;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class PermissionServiceImpl extends ExtendableServiceImpl<Permission, PermissionRepository> implements PermissionService {

    @Autowired
    private AdminGuard adminGuard;

    @Override
    @CommonTxReadOnly
    public boolean checkPermissions(User user, PermissionType type) throws Exception {
        Permission permission = getPermission(user, type);
        return permission != null && permission.isGranted();
    }

    @Override
    @CommonTxReadOnly
    public Permission getPermission(User user, PermissionType type) throws Exception {
        User holder = rehydrate(user);
        if (holder == null || type == null) {
            return null;
        }
        return getRepository().findByHolder(holder, type);
    }

    @Override
    @CommonTxReadOnly
    public List<Permission> getPermissions(User user) throws Exception {
        User holder = rehydrate(user);
        return holder == null ? List.of() : getRepository().findAllByHolder(holder);
    }

    @Override
    @CommonTx
    public Permission setGranted(User user, PermissionType type, boolean granted) throws Exception {
        adminGuard.requireAdmin();
        User holder = rehydrate(user);
        if (holder == null) {
            throw new IllegalArgumentException("The user does not exist");
        }
        Permission permission = getRepository().findByHolder(holder, type);
        if (permission == null) {
            permission = new Permission();
            permission.setPermissionHolder(holder);
            permission.setPermissionType(type);
        }
        permission.setGranted(granted);
        return save(permission);
    }

    @Override
    @CommonTx
    public Permission save(Permission permission) throws Exception {
        adminGuard.requireAdmin();
        return super.save(permission);
    }

    @Override
    @CommonTx
    public Permission saveWithoutEvent(Permission permission) throws Exception {
        adminGuard.requireAdmin();
        return super.saveWithoutEvent(permission);
    }

    @Override
    @CommonTx
    public Permission delete(Permission permission, boolean hard) throws Exception {
        adminGuard.requireAdmin();
        return super.delete(permission, hard);
    }

    /**
     * The user can be the session's proxy, which has no more than the id, login and name: the entity is loaded
     * again, null when there is no such user or it was deleted.
     */
    private User rehydrate(User user) throws Exception {
        if (user == null || user.getId() == null) {
            return null;
        }
        User loaded = userService.find(user.getId());
        return loaded == null || loaded.isDeleted() ? null : loaded;
    }
}
