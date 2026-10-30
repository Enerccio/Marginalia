package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.repository.MessageRevisionRepository;

import java.util.*;

public class JpaMessageRevisionRepository extends JpaExtendableRepository<MessageRevision> implements MessageRevisionRepository {

    @Override
    protected Class<MessageRevision> getEntityClass() {
        return MessageRevision.class;
    }

    @Override
    public MessageRevision findLatest(ChatMessage part) throws Exception {
        if (part == null || part.getId() == null) {
            return null;
        }
        List<MessageRevision> found = entityManager
                .createQuery("SELECT r FROM MessageRevision r WHERE r.deleted = false AND r.message.id = :part ORDER BY r.creation DESC, r.id DESC", MessageRevision.class)
                .setParameter("part", part.getId())
                .setMaxResults(1)
                .getResultList();
        return found.isEmpty() ? null : hydrate(found.getFirst());
    }

    @Override
    public List<RevisionRow> findRows(ChatMessage part, boolean includeDeleted) throws Exception {
        if (part == null || part.getId() == null) {
            return List.of();
        }
        return entityManager
                .createQuery("SELECT new com.github.enerccio.marginalia.domain.components.RevisionRow(r.id, r.uuid, r.source, "
                        + "r.agent, r.sourceDetail, r.label, r.wordCount, r.tokenCount, r.creation, r.deleted) FROM MessageRevision r "
                        + "WHERE r.message.id = :part" + (includeDeleted ? "" : " AND r.deleted = false")
                        + " ORDER BY r.creation DESC, r.id DESC", RevisionRow.class)
                .setParameter("part", part.getId())
                .getResultList();
    }

    @Override
    public Map<Long, Long> countByPartIds(Collection<Long> partIds) throws Exception {
        Map<Long, Long> counts = new HashMap<>();
        for (List<Long> chunk : chunks(partIds)) {
            for (Object[] row : entityManager
                    .createQuery("SELECT r.message.id, COUNT(r) FROM MessageRevision r WHERE r.deleted = false AND r.message.id IN :ids GROUP BY r.message.id", Object[].class)
                    .setParameter("ids", chunk)
                    .getResultList()) {
                counts.put((Long) row[0], (Long) row[1]);
            }
        }
        return counts;
    }

    @Override
    public int softDeleteByManuscript(Manuscript manuscript) throws Exception {
        if (manuscript == null || manuscript.getId() == null) {
            return 0;
        }
        return entityManager.createQuery(
                        "UPDATE MessageRevision r SET r.deleted = true, r.modification = :now WHERE r.deleted = false "
                                + "AND r.message.id IN (SELECT m.id FROM ChatMessage m WHERE m.parentScript.id = :manuscript)")
                .setParameter("now", new Date())
                .setParameter("manuscript", manuscript.getId())
                .executeUpdate();
    }

    @Override
    public int softDeleteByPartIds(Collection<Long> partIds) throws Exception {
        int deleted = 0;
        Date now = new Date();
        for (List<Long> chunk : chunks(partIds)) {
            deleted += entityManager.createQuery(
                            "UPDATE MessageRevision r SET r.deleted = true, r.modification = :now WHERE r.deleted = false AND r.message.id IN :ids")
                    .setParameter("now", now)
                    .setParameter("ids", chunk)
                    .executeUpdate();
        }
        return deleted;
    }

    @Override
    public int hardDeleteByPartIds(Collection<Long> partIds) throws Exception {
        int deleted = 0;
        for (List<Long> chunk : chunks(partIds)) {
            deleted += entityManager.createQuery("DELETE FROM MessageRevision r WHERE r.message.id IN :ids")
                    .setParameter("ids", chunk)
                    .executeUpdate();
        }
        return deleted;
    }

}
