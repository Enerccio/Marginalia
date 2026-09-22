package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.TreeEntity;
import com.github.enerccio.marginalia.domain.repository.TreeRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class JpaTreeEntityRepository<T extends TreeEntity> extends JpaExtendableRepository<T> implements TreeRepository<T> {

    @Override
    public List<Long> getChildren(T entity, int levelSize, User user) {
        String likePart = entity.getTree() + StringUtils.repeat("_", levelSize);

        try {
            return getEntityManager().createQuery("SELECT o.id FROM " + getEntityType() +
                            " o WHERE o.tree LIKE ?1 AND o.user.id = ?2 AND o.deleted = false ORDER BY o.tree", Long.class)
                    .setParameter(1, likePart)
                    .setParameter(2, user.getId())
                    .getResultList();
        }
        catch (NoResultException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public int getChildrenCount(T entity, int levelSize, User user) {
        if(entity == null) return 0;
        if(StringUtils.isBlank(entity.getTree())) return 0;

        String likePart = entity.getTree() + StringUtils.repeat("_", levelSize);
        TypedQuery<Long> q = null;
        q = getEntityManager().createQuery(
                        "SELECT count(o) FROM " + getEntityType() + " o WHERE o.tree LIKE ?1 AND o.user.id = ?2 AND o.deleted = false", Long.class)
                .setParameter(1, likePart)
                .setParameter(2, user.getId());

        return q.getSingleResult().intValue();
    }

    public List<String> getChildrenTrees(T entity, int levelSize, User user) {
        String likePart = entity.getTree() + StringUtils.repeat("_", levelSize);

        try {
            return getEntityManager().createQuery("SELECT o.tree FROM " + getEntityType() +
                            " o WHERE o.tree LIKE ?1  AND o.user.id = ?2 AND o.deleted = false ORDER BY o.tree", String.class)
                    .setParameter(1, likePart)
                    .setParameter(2, user.getId())
                    .getResultList();
        }
        catch (NoResultException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Long getParent(T entity, int levelSize, User user) {
        String tree = entity.getTree();

        if (tree == null || tree.length() <= levelSize) {
            return null;
        }

        tree = tree.substring(0, tree.length() - levelSize);

        return findByTree(tree, user);
    }

    @Override
    public Long findByTree(String tree, User user) {
        try {
            return getEntityManager().createQuery(
                            "SELECT o.id FROM " + getEntityType() + " o WHERE o.tree = ?1  AND o.user.id = ?2 AND o.deleted = false", Long.class)
                    .setParameter(1, tree)
                    .setParameter(2, user.getId())
                    .getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Long> getPath(T entity, int levelSize, User user) throws Exception {
        List<Long> path = new ArrayList<>();

        T parent = entity;
        Long parentId;
        while ((parentId = getParent(parent, levelSize, user)) != null) {
            path.add(parentId);
            parent = find(parentId);
        }

        Collections.reverse(path);

        return path;
    }

    @Override
    public Long getPrevious(T entity, int levelSize, User user) {
        String tree = entity.getTree();
        String parent = tree.substring(0, tree.length() - levelSize);

        try {
            return getEntityManager().createQuery("SELECT o.id FROM " + getEntityType() + " o WHERE o.tree < ?1 AND o.tree LIKE ?2 AND LENGTH(o.tree) = ?3 AND o.user.id = ?4 AND o.deleted = false ORDER BY o.tree DESC", Long.class)
                    .setParameter(1, tree)
                    .setParameter(2, parent + "%")
                    .setParameter(3, tree.length())
                    .setParameter(4, user.getId())
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getNext(T entity, int levelSize, User user) {
        String tree = entity.getTree();
        String parent = tree.substring(0, tree.length() - levelSize);

        try {
            return getEntityManager().createQuery("SELECT o.id FROM " + getEntityType() + " o WHERE o.tree > ?1 AND o.tree LIKE ?2 AND LENGTH(o.tree) = ?3 AND o.user.id = ?4 AND o.deleted = false ORDER BY o.tree ASC", Long.class)
                    .setParameter(1, tree)
                    .setParameter(2, parent + "%")
                    .setParameter(3, tree.length())
                    .setParameter(4, user.getId())
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void changeTree(String oldTree, String newTree, User user) {
        getEntityManager().createQuery("UPDATE " + getEntityType() + " o " +
                        "SET o.tree = CONCAT(?1, SUBSTRING(o.tree, ?2)) " +
                        "WHERE o.tree LIKE ?3 AND o.user.id = ?4 AND o.deleted = false")
                .setParameter(1, newTree)
                .setParameter(2, oldTree.length() + 1)
                .setParameter(3, oldTree + "%")
                .setParameter(4, user.getId())
                .executeUpdate();
    }

    @Override
    public T delete(T entity, boolean hard) throws Exception {
        return super.delete(entity, hard);
    }

    @Override
    public Long getRoot(int levelSize, User user) {
        String likePart = StringUtils.repeat("_", levelSize);

        return getEntityManager().createQuery("SELECT o.id FROM " + getEntityType() + " o WHERE o.tree LIKE ?1 AND o.user.id = ?2 AND o.deleted = false", Long.class)
                .setParameter(1, likePart)
                .setParameter(2, user.getId())
                .setMaxResults(1)
                .getSingleResult();
    }

    @Override
    public List<Long> getRoots(int levelSize, User user) {
        String likePart = StringUtils.repeat("_", levelSize);

        return getEntityManager().createQuery("SELECT o.id FROM " + getEntityType() + " o WHERE o.tree LIKE ?1  AND o.user.id = ?2 AND o.deleted = false ORDER BY o.tree ASC", Long.class)
                .setParameter(1, likePart)
                .setParameter(2, user.getId())
                .getResultList();
    }

    @Override
    public List<String> getRootTrees(int levelSize, User user) {
        String likePart = StringUtils.repeat("_", levelSize);

        return getEntityManager().createQuery("SELECT o.tree FROM " + getEntityType() + " o WHERE o.tree LIKE ?1 AND o.user.id = ?2 AND o.deleted = false ORDER BY o.tree ASC", String.class)
                .setParameter(1, likePart)
                .setParameter(2, user.getId())
                .getResultList();
    }

    @Override
    public String getMaxTree(int levelSize, User user) throws Exception {
        String likePart = StringUtils.repeat("_", levelSize);

        return getEntityManager().createQuery("SELECT max(o.tree) FROM " + getEntityType() + " o WHERE o.tree LIKE ?1 ", String.class)
                .setParameter(1, likePart)
                .setMaxResults(1)
                .getSingleResult();
    }

    @Override
    public String getMaxTree(T entity, int levelSize, User user) throws Exception {
        String likePart = entity.getTree() + StringUtils.repeat("_", levelSize);

        return getEntityManager().createQuery("SELECT max(o.tree) FROM " + getEntityType() + " o WHERE o.tree LIKE ?1 AND o.user.id = ?2 AND o.deleted = false", String.class)
                .setParameter(1, likePart)
                .setParameter(2, user.getId())
                .setMaxResults(1)
                .getSingleResult();
    }

    @Override
    public Long getFirstChildId(String tree, int levelSize, User user) throws Exception {
        return getMinMaxChildIds(tree, tree.length() + levelSize, "ASC", user);
    }

    @Override
    public Long getLastChildId(String tree, int levelSize, User user) throws Exception {
        return getMinMaxChildIds(tree, tree.length() + levelSize, "DESC", user);
    }

    @Override
    public String getTreeForEntity(Long entityId, User user) throws Exception {
        List<String> trees = getEntityManager().createQuery("SELECT o.tree FROM " + getEntityType() + " o WHERE o.id = ?1 AND o.user.id = ?2 AND o.deleted = false ", String.class)
                .setParameter(1, entityId)
                .setParameter(2, user.getId())
                .setMaxResults(1)
                .getResultList();
        if (trees.isEmpty())
            return null;
        return trees.getFirst();
    }

    @Override
    public List<Long> getNestedChildrenIds(String tree, Long selfId, User user) throws Exception {
        if (selfId == null)
            return Collections.emptyList();

        return getEntityManager().createQuery("SELECT o.id FROM " + getEntityType() + " o WHERE o.tree LIKE ?1 AND o.id <> ?2 AND o.user.id = ?2 AND o.deleted = false ORDER BY o.tree", Long.class)
                .setParameter(1, tree + "%")
                .setParameter(2, selfId)
                .setParameter(3, user.getId())
                .getResultList();
    }

    private Long getMinMaxChildIds(String tree, int length, String order, User user) {
        try {
            return getEntityManager().createQuery("SELECT id FROM " + getEntityType() + " o WHERE tree LIKE ?1 AND LENGTH(tree) = ?2  AND o.user.id = ?3 AND o.deleted = false ORDER BY tree " + order, Long.class)
                    .setParameter(1, tree + "%")
                    .setParameter(2, length)
                    .setParameter(3, user.getId())
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
