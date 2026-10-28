package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessagePrompt;
import com.github.enerccio.marginalia.domain.repository.MessagePromptRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class JpaMessagePromptRepository extends JpaOwnedRepository<MessagePrompt> implements MessagePromptRepository {

    @Override
    protected Class<MessagePrompt> getEntityClass() {
        return MessagePrompt.class;
    }

    @Override
    public MessagePrompt findByPart(ChatMessage part) throws Exception {
        if (part == null || part.getId() == null) {
            return null;
        }
        List<MessagePrompt> found = findAllByPartIds(List.of(part.getId()));
        return found.isEmpty() ? null : found.getFirst();
    }

    @Override
    public List<MessagePrompt> findAllByPartIds(Collection<Long> partIds) throws Exception {
        List<MessagePrompt> prompts = new ArrayList<>();
        for (List<Long> chunk : chunks(partIds)) {
            prompts.addAll(entityManager
                    .createQuery("SELECT p FROM MessagePrompt p WHERE p.deleted = false AND p.message.id IN :ids ORDER BY p.id ASC", MessagePrompt.class)
                    .setParameter("ids", chunk)
                    .getResultList());
        }
        return prompts;
    }

    @Override
    public int softDeleteByManuscript(Manuscript manuscript) throws Exception {
        if (manuscript == null || manuscript.getId() == null) {
            return 0;
        }
        return entityManager.createQuery(
                        "UPDATE MessagePrompt p SET p.deleted = true, p.modification = :now WHERE p.deleted = false "
                                + "AND p.message.id IN (SELECT m.id FROM ChatMessage m WHERE m.parentScript.id = :manuscript)")
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
                            "UPDATE MessagePrompt p SET p.deleted = true, p.modification = :now WHERE p.deleted = false AND p.message.id IN :ids")
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
            deleted += entityManager.createQuery("DELETE FROM MessagePrompt p WHERE p.message.id IN :ids")
                    .setParameter("ids", chunk)
                    .executeUpdate();
        }
        return deleted;
    }
}
