package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ChatMessageRepository;
import jakarta.persistence.TypedQuery;

import java.util.Collections;
import java.util.List;

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

        List<ChatMessage> results = getEntityManager()
                .createNativeQuery(sql, ChatMessage.class)
                .setParameter("leafId", leaf.getId())
                .getResultList();

        Collections.reverse(results);
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
    public boolean hasAnyMessages(Manuscript manuscript) throws Exception {
        return !getAllMessages(manuscript).isEmpty();
    }

    @Override
    public List<Long> getAllMessages(Manuscript manuscript) throws Exception {
        return getEntityManager().createQuery("SELECT m.id FROM ChatMessage m WHERE m.parentScript = ?1 AND m.deleted = false ORDER BY m.creation", Long.class)
                .setParameter(1, manuscript)
                .getResultList();
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