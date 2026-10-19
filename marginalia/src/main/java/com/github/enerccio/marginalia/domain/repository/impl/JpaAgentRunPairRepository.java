package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.AgentRunPair;
import com.github.enerccio.marginalia.domain.repository.AgentRunPairRepository;
import jakarta.persistence.EntityManager;

import java.util.Collection;
import java.util.List;

public class JpaAgentRunPairRepository implements AgentRunPairRepository {

    protected EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void insertAll(Collection<AgentRunPair> pairs) throws Exception {
        for (AgentRunPair pair : pairs) {
            entityManager.persist(pair);
        }
        entityManager.flush();
    }

    @Override
    public List<AgentRunPair> findAllByRun(Long runId) throws Exception {
        return entityManager
                .createQuery("SELECT p FROM AgentRunPair p WHERE p.runId = :run ORDER BY p.id ASC", AgentRunPair.class)
                .setParameter("run", runId)
                .getResultList();
    }

    @Override
    public AgentRunPair findByCopy(Long runId, String copyUuid) throws Exception {
        return entityManager
                .createQuery("SELECT p FROM AgentRunPair p WHERE p.runId = :run AND p.copyUuid = :uuid", AgentRunPair.class)
                .setParameter("run", runId)
                .setParameter("uuid", copyUuid)
                .setMaxResults(1)
                .getResultStream().findFirst().orElse(null);
    }

    @Override
    public AgentRunPair findByOriginal(Long runId, String originalUuid) throws Exception {
        return entityManager
                .createQuery("SELECT p FROM AgentRunPair p WHERE p.runId = :run AND p.originalUuid = :uuid ORDER BY p.id ASC", AgentRunPair.class)
                .setParameter("run", runId)
                .setParameter("uuid", originalUuid)
                .setMaxResults(1)
                .getResultStream().findFirst().orElse(null);
    }

    @Override
    public AgentRunPair findByHandle(Long runId, String handle) throws Exception {
        return entityManager
                .createQuery("SELECT p FROM AgentRunPair p WHERE p.runId = :run AND p.handle = :handle", AgentRunPair.class)
                .setParameter("run", runId)
                .setParameter("handle", handle)
                .setMaxResults(1)
                .getResultStream().findFirst().orElse(null);
    }

    @Override
    public int maxHandleNumber(Long runId, char kind) throws Exception {
        int max = 0;
        String prefix = "h:" + kind;
        List<String> handles = entityManager
                .createQuery("SELECT p.handle FROM AgentRunPair p WHERE p.runId = :run AND p.handle LIKE :prefix", String.class)
                .setParameter("run", runId)
                .setParameter("prefix", prefix + "%")
                .getResultList();
        for (String handle : handles) {
            try {
                max = Math.max(max, Integer.parseInt(handle.substring(prefix.length())));
            } catch (NumberFormatException ignored) {
                // not one of ours
            }
        }
        return max;
    }

    @Override
    public long countByRun(Long runId) throws Exception {
        return entityManager
                .createQuery("SELECT COUNT(p) FROM AgentRunPair p WHERE p.runId = :run", Long.class)
                .setParameter("run", runId)
                .getSingleResult();
    }

    @Override
    public int deleteByRun(Long runId) throws Exception {
        // rows of the run loaded earlier in the transaction would be stale, nobody holds them across this call
        entityManager.flush();
        return entityManager
                .createQuery("DELETE FROM AgentRunPair p WHERE p.runId = :run")
                .setParameter("run", runId)
                .executeUpdate();
    }
}
