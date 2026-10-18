package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ChatMessageRepository;
import com.github.enerccio.marginalia.domain.service.search.LikePatterns;
import jakarta.persistence.TypedQuery;

import java.util.*;

public class JpaChatMessageRepository extends JpaExtendableRepository<ChatMessage> implements ChatMessageRepository {

    @Override
    protected Class<ChatMessage> getEntityClass() {
        return ChatMessage.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ChatMessage> findBranchFromLeaf(ChatMessage leaf) throws Exception {
        if (leaf == null) {
            return Collections.emptyList();
        }

        String sql = """
                WITH RECURSIVE branch AS (
                    SELECT * FROM messages WHERE id = :leafId AND is_deleted = 0
                    UNION ALL
                    SELECT m.* FROM messages m
                    INNER JOIN branch b ON m.id = b.parent_id
                    WHERE m.is_deleted = 0
                )
                SELECT * FROM branch
                """;

        List<ChatMessage> rawResults = getEntityManager()
                .createNativeQuery(sql, ChatMessage.class)
                .setParameter("leafId", leaf.getId())
                .getResultList();

        List<ChatMessage> results = new ArrayList<>();
        for (ChatMessage msg : rawResults) {
            ChatMessage entity = (ChatMessage) org.hibernate.Hibernate.unproxy(msg);

            if (entity.getResponse() == null && entity.getExtendedContent() != null) {
                listener.deserialize(entity);
            }

            results.add(entity);
        }

        Collections.reverse(results);
        results.forEach(ExtendableEntity::getExtendedContent);
        return results;
    }

    @Override
    public List<ChatMessage> findChildren(ChatMessage parent) throws Exception {
        if (parent == null) {
            return List.of();
        }
        TypedQuery<ChatMessage> query = entityManager.createQuery(
                "SELECT m FROM ChatMessage m WHERE m.parent = :parent AND m.deleted = false ORDER BY m.creation ASC",
                ChatMessage.class
        );
        query.setParameter("parent", parent);
        return query.getResultList();
    }

    @Override
    public List<ChatMessage> findRootMessages(Manuscript manuscript) throws Exception {
        if (manuscript == null) {
            return List.of();
        }
        TypedQuery<ChatMessage> query = entityManager.createQuery(
                "SELECT m FROM ChatMessage m WHERE m.parentScript = :script AND m.parent IS NULL AND m.deleted = false ORDER BY m.creation ASC",
                ChatMessage.class
        );
        query.setParameter("script", manuscript);
        return query.getResultList();
    }

    @Override
    public List<ChatMessage> findAllByManuscript(Manuscript manuscript) throws Exception {
        if (manuscript == null) {
            return List.of();
        }
        TypedQuery<ChatMessage> query = entityManager.createQuery(
                "SELECT m FROM ChatMessage m WHERE m.parentScript = :script AND m.deleted = false ORDER BY m.creation ASC",
                ChatMessage.class
        );
        query.setParameter("script", manuscript);
        return query.getResultList();
    }

    @Override
    public void reparentChildren(ChatMessage targetNode, ChatMessage newParent) throws Exception {
        if (targetNode == null) {
            return;
        }
        entityManager.createQuery(
                        "UPDATE ChatMessage m SET m.parent = :newParent WHERE m.parent = :targetNode"
                )
                .setParameter("newParent", newParent)
                .setParameter("targetNode", targetNode)
                .executeUpdate();
    }

    @Override
    public List<Long> findDescendantIds(ChatMessage message) throws Exception {
        List<Long> descendants = new ArrayList<>();
        if (message == null || message.getId() == null) {
            return descendants;
        }
        List<Long> level = List.of(message.getId());
        while (!level.isEmpty()) {
            List<Long> next = new ArrayList<>();
            for (List<Long> chunk : chunks(level)) {
                next.addAll(entityManager.createQuery(
                                "SELECT m.id FROM ChatMessage m WHERE m.parent.id IN :ids AND m.deleted = false", Long.class)
                        .setParameter("ids", chunk)
                        .getResultList());
            }
            descendants.addAll(next);
            level = next;
        }
        return descendants;
    }

    @Override
    public int softDeleteByManuscript(Manuscript manuscript) throws Exception {
        if (manuscript == null || manuscript.getId() == null) {
            return 0;
        }
        return entityManager.createQuery(
                        "UPDATE ChatMessage m SET m.deleted = true, m.modification = :now WHERE m.parentScript.id = :manuscript AND m.deleted = false")
                .setParameter("now", new Date())
                .setParameter("manuscript", manuscript.getId())
                .executeUpdate();
    }

    @Override
    public int softDeleteByIds(Collection<Long> ids) throws Exception {
        int deleted = 0;
        Date now = new Date();
        for (List<Long> chunk : chunks(ids)) {
            deleted += entityManager.createQuery(
                            "UPDATE ChatMessage m SET m.deleted = true, m.modification = :now WHERE m.id IN :ids AND m.deleted = false")
                    .setParameter("now", now)
                    .setParameter("ids", chunk)
                    .executeUpdate();
        }
        return deleted;
    }

    @Override
    public boolean hasAnyMessages(Manuscript manuscript) throws Exception {
        return !getAllMessageIds(manuscript).isEmpty();
    }

    @Override
    public List<Long> getAllMessageIds(Manuscript manuscript) throws Exception {
        return getEntityManager().createQuery("SELECT m.id FROM ChatMessage m WHERE m.parentScript = ?1 AND m.deleted = false ORDER BY m.creation", Long.class)
                .setParameter(1, manuscript)
                .getResultList();
    }

    @Override
    public List<ChatMessage> getAllMessages(Manuscript manuscript) throws Exception {
        return getEntityManager().createQuery("SELECT m FROM ChatMessage m WHERE m.parentScript = ?1 AND m.deleted = false ORDER BY m.creation", ChatMessage.class)
                .setParameter(1, manuscript)
                .getResultList();
    }

    @Override
    public List<ChatMessage> getAllMessagesIncludingDeleted(Manuscript manuscript) throws Exception {
        return getEntityManager().createQuery("SELECT m FROM ChatMessage m WHERE m.parentScript = ?1 ORDER BY m.id", ChatMessage.class)
                .setParameter(1, manuscript)
                .getResultList();
    }

    @Override
    public List<Object[]> findFulltexts(Manuscript manuscript, List<List<String>> likePatterns) throws Exception {
        StringBuilder query = new StringBuilder("SELECT m.id, m._fulltext FROM ChatMessage m WHERE m.parentScript = :manuscript AND m.deleted = false");
        Map<String, Object> parameters = new HashMap<>();
        for (int word = 0; word < likePatterns.size(); word++) {
            List<String> ors = new ArrayList<>();
            for (int variant = 0; variant < likePatterns.get(word).size(); variant++) {
                String name = "p" + word + "_" + variant;
                ors.add("m._fulltext LIKE :" + name + " ESCAPE '\\'");
                parameters.put(name, likePatterns.get(word).get(variant));
            }
            query.append(" AND (").append(String.join(" OR ", ors)).append(")");
        }
        query.append(" ORDER BY m.creation");

        TypedQuery<Object[]> q = getEntityManager().createQuery(query.toString(), Object[].class)
                .setParameter("manuscript", manuscript);
        parameters.forEach(q::setParameter);
        return q.getResultList();
    }

    @Override
    public List<Object[]> findFulltextsContaining(Manuscript manuscript, String literal) throws Exception {
        return fulltextsContaining("SELECT m.id, m._fulltext FROM ChatMessage m WHERE m.parentScript = :manuscript AND m.deleted = false",
                "m._fulltext", manuscript, literal);
    }

    @Override
    public List<Object[]> findSummaryFulltextsContaining(Manuscript manuscript, String literal) throws Exception {
        return fulltextsContaining("SELECT m.id, m.summary.id, m.summary._fulltext FROM ChatMessage m WHERE m.parentScript = :manuscript"
                        + " AND m.deleted = false AND m.summary IS NOT NULL AND m.summary.deleted = false",
                "m.summary._fulltext", manuscript, literal);
    }

    private List<Object[]> fulltextsContaining(String select, String column, Manuscript manuscript, String literal) {
        StringBuilder query = new StringBuilder(select);
        if (literal != null) {
            query.append(" AND ").append(column).append(" LIKE :pattern ESCAPE '\\'");
        }
        query.append(" ORDER BY m.creation");
        TypedQuery<Object[]> q = getEntityManager().createQuery(query.toString(), Object[].class)
                .setParameter("manuscript", manuscript);
        if (literal != null) {
            q.setParameter("pattern", "%" + LikePatterns.literal(literal) + "%");
        }
        return q.getResultList();
    }

    @Override
    public int getTotalWordCount(Long manuscriptId) throws Exception {
        if (manuscriptId == null) {
            return 0;
        }
        Long result = getEntityManager().createQuery(
                        "SELECT COALESCE(SUM(m.wordCount), 0) FROM ChatMessage m WHERE m.parentScript.id = :manuscriptId AND m.deleted = false", Long.class)
                .setParameter("manuscriptId", manuscriptId)
                .getSingleResult();
        return result != null ? result.intValue() : 0;
    }

    @Override
    public int getTotalTokenCount(Long manuscriptId) throws Exception {
        if (manuscriptId == null) {
            return 0;
        }
        Long result = getEntityManager().createQuery(
                        "SELECT COALESCE(SUM(m.tokenCount), 0) FROM ChatMessage m WHERE m.parentScript.id = :manuscriptId AND m.deleted = false", Long.class)
                .setParameter("manuscriptId", manuscriptId)
                .getSingleResult();
        return result != null ? result.intValue() : 0;
    }



}