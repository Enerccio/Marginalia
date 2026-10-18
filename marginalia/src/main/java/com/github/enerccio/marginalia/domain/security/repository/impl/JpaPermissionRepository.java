package com.github.enerccio.marginalia.domain.security.repository.impl;

import com.github.enerccio.marginalia.domain.repository.impl.JpaExtendableRepository;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.security.repository.PermissionRepository;

import java.util.List;

public class JpaPermissionRepository extends JpaExtendableRepository<Permission> implements PermissionRepository {

    @Override
    protected Class<Permission> getEntityClass() {
        return Permission.class;
    }

    @Override
    public Permission findByHolder(User holder, PermissionType type) throws Exception {
        List<Permission> results = getEntityManager()
                .createQuery("SELECT p FROM Permission p WHERE p.permissionHolder.id = :holder AND p.permissionType = :type AND p.deleted = false", Permission.class)
                .setParameter("holder", holder.getId())
                .setParameter("type", type)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? null : hydrate(results.getFirst());
    }

    @Override
    public List<Permission> findAllByHolder(User holder) throws Exception {
        List<Permission> results = getEntityManager()
                .createQuery("SELECT p FROM Permission p WHERE p.permissionHolder.id = :holder AND p.deleted = false", Permission.class)
                .setParameter("holder", holder.getId())
                .getResultList();
        for (Permission permission : results) {
            hydrate(permission);
        }
        return results;
    }
}
