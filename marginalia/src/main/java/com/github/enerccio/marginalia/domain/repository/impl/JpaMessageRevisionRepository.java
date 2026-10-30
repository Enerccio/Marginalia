package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.repository.MessageRevisionRepository;

import jakarta.persistence.TypedQuery;

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
        return part == null ? List.of() : findRows(part.getId(), includeDeleted);
    }

    @Override
    public List<RevisionRow> findRows(Long partId, boolean includeDeleted) throws Exception {
        if (partId == null) {
            return List.of();
        }
        return entityManager
                .createQuery("SELECT new com.github.enerccio.marginalia.domain.components.RevisionRow(r.id, r.uuid, r.source, "
                        + "r.agent, r.sourceDetail, r.label, r.wordCount, r.tokenCount, r.creation, r.deleted) FROM MessageRevision r "
                        + "WHERE r.message.id = :part" + (includeDeleted ? "" : " AND r.deleted = false")
                        + " ORDER BY r.creation DESC, r.id DESC", RevisionRow.class)
                .setParameter("part", partId)
                .getResultList();
    }

    @Override
    public List<Long> findPartIdsWithVersions(Manuscript manuscript) throws Exception {
        if (manuscript == null || manuscript.getId() == null) {
            return List.of();
        }
        return entityManager.createQuery("SELECT DISTINCT r.message.id FROM MessageRevision r WHERE r.message.parentScript.id = :manuscript "
                        + "AND r.message.deleted = false", Long.class)
                .setParameter("manuscript", manuscript.getId())
                .getResultList();
    }

    @Override
    public int hardDeleteByIds(Collection<Long> ids) throws Exception {
        int deleted = 0;
        for (List<Long> chunk : chunks(ids)) {
            deleted += entityManager.createQuery("DELETE FROM MessageRevision r WHERE r.id IN :ids")
                    .setParameter("ids", chunk)
                    .executeUpdate();
        }
        return deleted;
    }

    @Override
    public long sizeOf(Collection<Long> ids) throws Exception {
        long size = 0;
        for (List<Long> chunk : chunks(ids)) {
            size += entityManager.createQuery("SELECT COALESCE(SUM(OCTET_LENGTH(r.extendedContent)), 0) FROM MessageRevision r WHERE r.id IN :ids", Number.class)
                    .setParameter("ids", chunk)
                    .getSingleResult()
                    .longValue();
        }
        return size;
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
    public int softDeleteNewerThan(MessageRevision revision) throws Exception {
        if (revision == null || revision.getId() == null || revision.getMessageId() == null) {
            return 0;
        }
        return entityManager.createQuery(
                        "UPDATE MessageRevision r SET r.deleted = true, r.modification = :now WHERE r.deleted = false AND r.message.id = :part "
                                + "AND (r.creation > :creation OR r.creation = :creation AND r.id > :id)")
                .setParameter("now", new Date())
                .setParameter("part", revision.getMessageId())
                .setParameter("creation", revision.getCreation())
                .setParameter("id", revision.getId())
                .executeUpdate();
    }

    @Override
    public int hardDeleteTrashByPart(ChatMessage part) throws Exception {
        if (part == null || part.getId() == null) {
            return 0;
        }
        return entityManager.createQuery("DELETE FROM MessageRevision r WHERE r.deleted = true AND r.message.id = :part")
                .setParameter("part", part.getId())
                .executeUpdate();
    }

    @Override
    public int hardDeleteTrashByManuscript(Manuscript manuscript) throws Exception {
        if (manuscript == null || manuscript.getId() == null) {
            return 0;
        }
        return entityManager.createQuery("DELETE FROM MessageRevision r WHERE r.deleted = true AND r.message.id IN "
                        + "(SELECT m.id FROM ChatMessage m WHERE m.parentScript.id = :manuscript AND m.deleted = false)")
                .setParameter("manuscript", manuscript.getId())
                .executeUpdate();
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

    @Override
    public List<Object[]> findFulltexts(Manuscript manuscript, List<List<String>> likePatterns) throws Exception {
        if (manuscript == null || manuscript.getId() == null) {
            return List.of();
        }
        StringBuilder query = new StringBuilder("SELECT r.uuid, r.message.id, r.creation, r.source, r._fulltext FROM MessageRevision r "
                + "WHERE r.message.parentScript.id = :manuscript AND r.message.deleted = false AND r.deleted = false");
        Map<String, Object> parameters = new HashMap<>();
        appendLikes(query, parameters, likePatterns);
        query.append(" ORDER BY r.creation DESC, r.id DESC");

        TypedQuery<Object[]> q = entityManager.createQuery(query.toString(), Object[].class)
                .setParameter("manuscript", manuscript.getId());
        parameters.forEach(q::setParameter);
        return q.getResultList();
    }

    @Override
    public List<Object[]> findFulltextHits(Collection<Long> manuscriptIds, List<List<String>> likePatterns) throws Exception {
        if (manuscriptIds.isEmpty()) {
            return List.of();
        }
        StringBuilder query = new StringBuilder("SELECT DISTINCT r.message.id, r.message.parentScript.id FROM MessageRevision r "
                + "WHERE r.message.parentScript.id IN :ids AND r.message.deleted = false AND r.deleted = false");
        Map<String, Object> parameters = new HashMap<>();
        appendLikes(query, parameters, likePatterns);

        TypedQuery<Object[]> q = entityManager.createQuery(query.toString(), Object[].class)
                .setParameter("ids", manuscriptIds);
        parameters.forEach(q::setParameter);
        return q.getResultList();
    }

    /**
     * Every word has to be in the text: one condition per word, the variants of the word are alternatives.
     */
    private static void appendLikes(StringBuilder query, Map<String, Object> parameters, List<List<String>> likePatterns) {
        for (int word = 0; word < likePatterns.size(); word++) {
            List<String> ors = new ArrayList<>();
            for (int variant = 0; variant < likePatterns.get(word).size(); variant++) {
                String name = "p" + word + "_" + variant;
                ors.add("r._fulltext LIKE :" + name + " ESCAPE '\\'");
                parameters.put(name, likePatterns.get(word).get(variant));
            }
            query.append(" AND (").append(String.join(" OR ", ors)).append(")");
        }
    }

}
