package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.repository.LorebookEntryRepository;

import com.github.enerccio.marginalia.domain.service.search.LikePatterns;
import jakarta.persistence.TypedQuery;

import java.util.Collections;
import java.util.List;

public class JpaLorebookEntryRepository extends JpaExtendableRepository<LorebookEntry> implements LorebookEntryRepository {

    @Override
    protected Class<LorebookEntry> getEntityClass() {
        return LorebookEntry.class;
    }

    @Override
    public List<LorebookEntry> findByLorebookOrdered(Long lorebookId) throws Exception {
        if (lorebookId == null) {
            return Collections.emptyList();
        }

        String jpql = """
                SELECT e FROM LorebookEntry e
                WHERE e.lorebook.id = :lorebookId
                  AND e.deleted = false
                ORDER BY e.ordinal ASC
                """;

        return getEntityManager()
                .createQuery(jpql, LorebookEntry.class)
                .setParameter("lorebookId", lorebookId)
                .getResultList();
    }

    @Override
    public List<Object[]> findFulltextsContaining(Long lorebookId, String literal) throws Exception {
        if (lorebookId == null) {
            return Collections.emptyList();
        }
        String jpql = "SELECT e.id, e._fulltext FROM LorebookEntry e WHERE e.lorebook.id = :lorebookId AND e.deleted = false"
                + (literal == null ? "" : " AND e._fulltext LIKE :pattern ESCAPE '\\'")
                + " ORDER BY e.ordinal ASC";
        TypedQuery<Object[]> query = getEntityManager().createQuery(jpql, Object[].class)
                .setParameter("lorebookId", lorebookId);
        if (literal != null) {
            query.setParameter("pattern", "%" + LikePatterns.literal(literal) + "%");
        }
        return query.getResultList();
    }
}
