package com.github.enerccio.marginalia.domain.security.repository;

import com.github.enerccio.marginalia.domain.repository.ExtendableRepository;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;

import java.util.List;

public interface PermissionRepository extends ExtendableRepository<Permission> {

    /**
     * @return the permission of the user, whoever created it; null when there is none (or it is deleted)
     */
    Permission findByHolder(User holder, PermissionType type) throws Exception;

    /**
     * @return all permissions of the user, whoever created them
     */
    List<Permission> findAllByHolder(User holder) throws Exception;

}
