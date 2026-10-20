package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.repository.AgentMessageRepository;

import java.util.List;

public class JpaAgentMessageRepository extends JpaExtendableRepository<AgentMessage> implements AgentMessageRepository {

    @Override
    protected Class<AgentMessage> getEntityClass() {
        return AgentMessage.class;
    }

    @Override
    public List<AgentMessage> findAllByRun(AgentRun run) throws Exception {
        if (run == null || run.getId() == null) {
            return List.of();
        }
        List<AgentMessage> messages = entityManager
                .createQuery("SELECT m FROM AgentMessage m WHERE m.deleted = false AND m.thread IS NULL AND m.run.id = :run ORDER BY m.ordinal ASC, m.id ASC", AgentMessage.class)
                .setParameter("run", run.getId())
                .getResultList();
        for (AgentMessage message : messages) {
            hydrate(message);
        }
        return messages;
    }

    @Override
    public List<AgentMessage> findLatestByRun(AgentRun run, int limit) throws Exception {
        if (run == null || run.getId() == null) {
            return List.of();
        }
        List<AgentMessage> messages = entityManager
                .createQuery("SELECT m FROM AgentMessage m WHERE m.deleted = false AND m.thread IS NULL AND m.run.id = :run ORDER BY m.ordinal DESC, m.id DESC", AgentMessage.class)
                .setParameter("run", run.getId())
                .setMaxResults(limit)
                .getResultList();
        for (AgentMessage message : messages) {
            hydrate(message);
        }
        return messages;
    }

    @Override
    public int countByRun(AgentRun run) throws Exception {
        if (run == null || run.getId() == null) {
            return 0;
        }
        return entityManager
                .createQuery("SELECT COUNT(m) FROM AgentMessage m WHERE m.deleted = false AND m.thread IS NULL AND m.run.id = :run", Long.class)
                .setParameter("run", run.getId())
                .getSingleResult().intValue();
    }

    @Override
    public List<AgentMessage> findAllByRunAndThread(AgentRun run, String thread) throws Exception {
        if (run == null || run.getId() == null || thread == null) {
            return List.of();
        }
        List<AgentMessage> messages = entityManager
                .createQuery("SELECT m FROM AgentMessage m WHERE m.deleted = false AND m.thread = :thread AND m.run.id = :run ORDER BY m.ordinal ASC, m.id ASC", AgentMessage.class)
                .setParameter("run", run.getId())
                .setParameter("thread", thread)
                .getResultList();
        for (AgentMessage message : messages) {
            hydrate(message);
        }
        return messages;
    }

    @Override
    public List<AgentMessage> findAllThreadRowsByRun(AgentRun run) throws Exception {
        if (run == null || run.getId() == null) {
            return List.of();
        }
        List<AgentMessage> messages = entityManager
                .createQuery("SELECT m FROM AgentMessage m WHERE m.deleted = false AND m.thread IS NOT NULL AND m.run.id = :run ORDER BY m.id ASC", AgentMessage.class)
                .setParameter("run", run.getId())
                .getResultList();
        for (AgentMessage message : messages) {
            hydrate(message);
        }
        return messages;
    }

    @Override
    public int countByRunAndThread(AgentRun run, String thread) throws Exception {
        if (run == null || run.getId() == null || thread == null) {
            return 0;
        }
        return entityManager
                .createQuery("SELECT COUNT(m) FROM AgentMessage m WHERE m.deleted = false AND m.thread = :thread AND m.run.id = :run", Long.class)
                .setParameter("run", run.getId())
                .setParameter("thread", thread)
                .getSingleResult().intValue();
    }

    @Override
    public List<AgentMessage> findEveryByRun(AgentRun run) throws Exception {
        if (run == null || run.getId() == null) {
            return List.of();
        }
        List<AgentMessage> messages = entityManager
                .createQuery("SELECT m FROM AgentMessage m WHERE m.deleted = false AND m.run.id = :run ORDER BY m.id ASC", AgentMessage.class)
                .setParameter("run", run.getId())
                .getResultList();
        for (AgentMessage message : messages) {
            hydrate(message);
        }
        return messages;
    }
}
