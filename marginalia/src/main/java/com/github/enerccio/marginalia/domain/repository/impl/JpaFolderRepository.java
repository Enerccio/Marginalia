package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.repository.FolderRepository;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.Collection;
import java.util.List;

public class JpaFolderRepository extends JpaExtendableRepository<Folder> implements FolderRepository {

    @Override
    protected Class<Folder> getEntityClass() {
        return Folder.class;
    }

    @Override
    public List<Folder> findChildren(Folder parent, User user) throws Exception {
        String where = parent == null ? "f.parent IS NULL" : "f.parent.id = :parentId";
        var query = getEntityManager().createQuery(
                        "SELECT f FROM Folder f WHERE f.deleted = false AND f.owner.id = :ownerId AND " + where
                                + " ORDER BY LOWER(f.name), f.id", Folder.class)
                .setParameter("ownerId", user.getId());
        if (parent != null) {
            query.setParameter("parentId", parent.getId());
        }
        List<Folder> folders = query.getResultList();
        for (Folder folder : folders) {
            hydrate(folder);
        }
        return folders;
    }

    @Override
    public Folder findChildByName(Folder parent, String name, Long excludeId, User user) throws Exception {
        if (name == null) {
            return null;
        }
        String where = parent == null ? "f.parent IS NULL" : "f.parent.id = :parentId";
        var query = getEntityManager().createQuery(
                        "SELECT f FROM Folder f WHERE f.deleted = false AND f.owner.id = :ownerId AND " + where
                                + " AND LOWER(f.name) = :name AND f.id <> :excludeId ORDER BY f.id", Folder.class)
                .setParameter("ownerId", user.getId())
                .setParameter("name", name.toLowerCase())
                .setParameter("excludeId", excludeId == null ? -1L : excludeId)
                .setMaxResults(1);
        if (parent != null) {
            query.setParameter("parentId", parent.getId());
        }
        List<Folder> folders = query.getResultList();
        return folders.isEmpty() ? null : hydrate(folders.getFirst());
    }

    @Override
    public int reparentChildren(Collection<Long> fromParentIds, Folder to, User user) throws Exception {
        int moved = 0;
        for (List<Long> chunk : chunks(fromParentIds)) {
            moved += getEntityManager().createQuery(
                            "UPDATE Folder f SET f.parent = :to WHERE f.parent.id IN :ids AND f.owner.id = :ownerId")
                    .setParameter("to", to)
                    .setParameter("ids", chunk)
                    .setParameter("ownerId", user.getId())
                    .executeUpdate();
        }
        return moved;
    }
}
