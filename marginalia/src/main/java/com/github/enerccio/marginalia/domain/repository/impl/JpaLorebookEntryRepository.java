package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.repository.LorebookEntryRepository;

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
}