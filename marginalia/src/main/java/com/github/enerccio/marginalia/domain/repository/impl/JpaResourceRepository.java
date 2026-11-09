package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.repository.ResourceRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import jakarta.persistence.metamodel.EntityType;

import java.util.*;

public class JpaResourceRepository extends JpaOwnedRepository<Resource> implements ResourceRepository {

    @Override
    protected Class<Resource> getEntityClass() {
        return Resource.class;
    }

    @Override
    public Resource findByHash(String hash, User owner) throws Exception {
        List<Resource> resources = getEntityManager()
                .createQuery("SELECT r FROM Resource r WHERE r.hash = :hash AND r.owner.id = :owner", Resource.class)
                .setParameter("owner", owner.getId())
                .setParameter("hash", hash)
                .getResultList();
        return resources.isEmpty() ? null : resources.getFirst();
    }

    @Override
    public Resource findOfViewableBook(String uuid, String manuscriptUuid, User reader) throws Exception {
        List<Resource> resources = getEntityManager()
                .createQuery("SELECT r FROM Resource r, Manuscript m WHERE r.uuid = :uuid AND r.deleted = false AND m.uuid = :book AND m.deleted = false"
                        + " AND (m.owner.id = :reader OR m.published = true) AND r.owner.id = m.owner.id AND r.clazz = :clazz"
                        + " AND r.objectId IN (SELECT c.id FROM ChatMessage c WHERE c.parentScript.id = m.id AND c.deleted = false)", Resource.class)
                .setParameter("uuid", uuid)
                .setParameter("book", manuscriptUuid)
                .setParameter("reader", reader.getId())
                .setParameter("clazz", ChatMessage.class.getName())
                .setMaxResults(1)
                .getResultList();
        return resources.isEmpty() ? null : resources.getFirst();
    }

    @Override
    public List<Resource> findPage(User owner, int offset, int limit, String sortProperty, boolean ascending) {
        return findPageOfGroup(owner, null, null, offset, limit, sortProperty, ascending, false);
    }

    @Override
    public List<Resource> findPageOfGroup(User owner, String clazz, Long objectId, int offset, int limit, String sortProperty, boolean ascending) {
        return findPageOfGroup(owner, clazz, objectId, offset, limit, sortProperty, ascending, true);
    }

    private List<Resource> findPageOfGroup(User owner, String clazz, Long objectId, int offset, int limit, String sortProperty,
                                           boolean ascending, boolean group) {
        // the property is put into the query, so only known ones are accepted
        String property = switch (String.valueOf(sortProperty)) {
            case "originalName", "mimeType", "size" -> sortProperty;
            default -> "creation";
        };
        var query = getEntityManager()
                .createQuery("SELECT r FROM Resource r WHERE r.deleted = false AND r.owner.id = :owner"
                        + (group ? groupCondition(clazz, objectId) : "") + " ORDER BY r."
                        + property + (ascending ? " ASC" : " DESC") + ", r.id " + (ascending ? "ASC" : "DESC"), Resource.class)
                .setParameter("owner", owner.getId());
        if (group) {
            bindGroup(query, clazz, objectId);
        }
        return query.setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    private static String groupCondition(String clazz, Long objectId) {
        return (clazz == null ? " AND r.clazz IS NULL" : " AND r.clazz = :clazz")
                + (objectId == null ? " AND r.objectId IS NULL" : " AND r.objectId = :objectId");
    }

    private static void bindGroup(jakarta.persistence.Query query, String clazz, Long objectId) {
        if (clazz != null) {
            query.setParameter("clazz", clazz);
        }
        if (objectId != null) {
            query.setParameter("objectId", objectId);
        }
    }

    @Override
    public long countOfGroup(User owner, String clazz, Long objectId) {
        var query = getEntityManager()
                .createQuery("SELECT COUNT(r) FROM Resource r WHERE r.deleted = false AND r.owner.id = :owner"
                        + groupCondition(clazz, objectId), Long.class)
                .setParameter("owner", owner.getId());
        bindGroup(query, clazz, objectId);
        return query.getSingleResult();
    }

    @Override
    public List<ResourceGroup> findGroupPage(User owner, int offset, int limit, String sortProperty, boolean ascending) {
        String direction = ascending ? " ASC" : " DESC";
        String order = switch (String.valueOf(sortProperty)) {
            case "size" -> "SUM(r.size)" + direction;
            case "originalName", "mimeType" -> "r.clazz" + direction + ", r.objectId" + direction;
            default -> "MAX(r.creation)" + direction;
        };
        List<Object[]> rows = getEntityManager()
                .createQuery("SELECT r.clazz, r.objectId, COUNT(r), SUM(r.size), MAX(r.creation) FROM Resource r"
                        + " WHERE r.deleted = false AND r.owner.id = :owner GROUP BY r.clazz, r.objectId ORDER BY "
                        + order + ", r.clazz" + direction + ", r.objectId" + direction, Object[].class)
                .setParameter("owner", owner.getId())
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
        return rows.stream().map(row -> new ResourceGroup((String) row[0], (Long) row[1], (Long) row[2],
                row[3] == null ? 0 : ((Number) row[3]).longValue(), (Date) row[4])).toList();
    }

    @Override
    public long countGroups(User owner) {
        return getEntityManager()
                .createQuery("SELECT r.clazz, r.objectId FROM Resource r WHERE r.deleted = false AND r.owner.id = :owner"
                        + " GROUP BY r.clazz, r.objectId", Object[].class)
                .setParameter("owner", owner.getId())
                .getResultList().size();
    }

    @Override
    public long count(User owner) {
        return getEntityManager()
                .createQuery("SELECT COUNT(r) FROM Resource r WHERE r.deleted = false AND r.owner.id = :owner", Long.class)
                .setParameter("owner", owner.getId())
                .getSingleResult();
    }

    @Override
    public Set<String> findAllPaths() {
        return new HashSet<>(getEntityManager()
                .createQuery("SELECT r.path FROM Resource r WHERE r.path IS NOT NULL", String.class)
                .getResultList());
    }

    @Override
    public boolean entityExists(String clazz, Long id) {
        if (clazz == null || id == null) {
            return false;
        }
        // the class name is matched against the entities of the persistence unit, it is never loaded by name
        Optional<EntityType<?>> entity = getEntityManager().getMetamodel().getEntities().stream()
                .filter(e -> e.getJavaType().getName().equals(clazz) && BaseEntity.class.isAssignableFrom(e.getJavaType()))
                .findFirst();
        if (entity.isEmpty()) {
            return false;
        }
        return getEntityManager()
                .createQuery("SELECT COUNT(e) FROM " + entity.get().getName() + " e WHERE e.id = :id AND e.deleted = false", Long.class)
                .setParameter("id", id)
                .getSingleResult() > 0;
    }
}
