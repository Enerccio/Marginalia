package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.repository.TagRepository;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.List;

public class JpaTagRepository extends JpaExtendableRepository<Tag> implements TagRepository {

    @Override
    protected Class<Tag> getEntityClass() {
        return Tag.class;
    }

    @Override
    public List<Tag> searchByValue(String filter, User user, int offset, int limit) throws Exception {
        if (user == null || user.getId() == null) {
            return List.of();
        }
        String jpql = "SELECT t FROM Tag t WHERE t.deleted = false AND t.owner.id = :userId AND LOWER(t.value) LIKE :filter ORDER BY t.value ASC";
        return getEntityManager().createQuery(jpql, Tag.class)
                .setParameter("userId", user.getId())
                .setParameter("filter", "%" + (filter != null ? filter.toLowerCase() : "") + "%")
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public int countByValue(String filter, User user) throws Exception {
        if (user == null || user.getId() == null) {
            return 0;
        }
        String jpql = "SELECT COUNT(t) FROM Tag t WHERE t.deleted = false AND t.owner.id = :userId AND LOWER(t.value) LIKE :filter";
        Long count = getEntityManager().createQuery(jpql, Long.class)
                .setParameter("userId", user.getId())
                .setParameter("filter", "%" + (filter != null ? filter.toLowerCase() : "") + "%")
                .getSingleResult();
        return count.intValue();
    }
}