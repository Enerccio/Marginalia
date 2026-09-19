package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.repository.ChatMessageRepository;

import java.util.Collections;
import java.util.List;

public class JpaChatMessageRepository extends JpaExtendableRepository<ChatMessage> implements ChatMessageRepository {

    @Override
    protected Class<ChatMessage> getEntityClass() {
        return ChatMessage.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ChatMessage> findBranchFromLeaf(Long leafId) throws Exception {
        if (leafId == null) {
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
                .setParameter("leafId", leafId)
                .getResultList();

        // Native CTE traverses bottom-up; reverse to return chronological root-to-leaf order
        Collections.reverse(results);
        return results;
    }

    @Override
    public List<ChatMessage> findAllLeavesForManuscript(Long manuscriptId) throws Exception {
        if (manuscriptId == null) {
            return Collections.emptyList();
        }

        String jpql = """
                SELECT m FROM ChatMessage m
                WHERE m.parentScript.id = :manuscriptId
                  AND m.deleted = false
                  AND NOT EXISTS (
                      SELECT c FROM ChatMessage c
                      WHERE c.parent.id = m.id
                        AND c.deleted = false
                  )
                ORDER BY m.id ASC
                """;

        return getEntityManager()
                .createQuery(jpql, ChatMessage.class)
                .setParameter("manuscriptId", manuscriptId)
                .getResultList();
    }

}