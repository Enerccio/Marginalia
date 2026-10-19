package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.AgentRunRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import jakarta.persistence.Query;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public class JpaAgentRunRepository extends JpaExtendableRepository<AgentRun> implements AgentRunRepository {

    @Override
    protected Class<AgentRun> getEntityClass() {
        return AgentRun.class;
    }

    @Override
    public List<AgentRun> findAllByAgent(Agent agent) throws Exception {
        if (agent == null || agent.getId() == null) {
            return List.of();
        }
        List<AgentRun> runs = entityManager
                .createQuery("SELECT r FROM AgentRun r WHERE r.deleted = false AND r.agent.id = :agent ORDER BY r.creation DESC, r.id DESC", AgentRun.class)
                .setParameter("agent", agent.getId())
                .getResultList();
        for (AgentRun run : runs) {
            hydrate(run);
        }
        return runs;
    }

    @Override
    public AgentRun findByAgent(Agent agent, Collection<AgentRunStatus> statuses) throws Exception {
        if (agent == null || agent.getId() == null || statuses.isEmpty()) {
            return null;
        }
        List<AgentRun> runs = entityManager
                .createQuery("SELECT r FROM AgentRun r WHERE r.deleted = false AND r.agent.id = :agent AND r.status IN :statuses ORDER BY r.id ASC", AgentRun.class)
                .setParameter("agent", agent.getId())
                .setParameter("statuses", statuses)
                .setMaxResults(1)
                .getResultList();
        return runs.isEmpty() ? null : hydrate(runs.getFirst());
    }

    @Override
    public AgentRun findByManuscript(Manuscript manuscript, Collection<AgentRunStatus> statuses) throws Exception {
        if (manuscript == null || manuscript.getId() == null || statuses.isEmpty()) {
            return null;
        }
        List<AgentRun> runs = entityManager
                .createQuery("SELECT r FROM AgentRun r WHERE r.deleted = false AND r.agent.deleted = false "
                        + "AND r.agent.manuscript.id = :manuscript AND r.status IN :statuses ORDER BY r.id ASC", AgentRun.class)
                .setParameter("manuscript", manuscript.getId())
                .setParameter("statuses", statuses)
                .setMaxResults(1)
                .getResultList();
        return runs.isEmpty() ? null : hydrate(runs.getFirst());
    }

    @Override
    public List<AgentRun> findAllByStatus(Collection<AgentRunStatus> statuses) throws Exception {
        if (statuses.isEmpty()) {
            return List.of();
        }
        List<AgentRun> runs = entityManager
                .createQuery("SELECT r FROM AgentRun r WHERE r.deleted = false AND r.status IN :statuses ORDER BY r.id ASC", AgentRun.class)
                .setParameter("statuses", statuses)
                .getResultList();
        for (AgentRun run : runs) {
            hydrate(run);
        }
        return runs;
    }

    @Override
    public long countByStatus(User user, Collection<AgentRunStatus> statuses) throws Exception {
        if (user == null || user.getId() == null || statuses.isEmpty()) {
            return 0;
        }
        return entityManager
                .createQuery("SELECT COUNT(r) FROM AgentRun r WHERE r.deleted = false AND r.owner.id = :owner AND r.status IN :statuses", Long.class)
                .setParameter("owner", user.getId())
                .setParameter("statuses", statuses)
                .getSingleResult();
    }

    @Override
    public long countWithWorkingCopy(User user) throws Exception {
        if (user == null || user.getId() == null) {
            return 0;
        }
        return entityManager
                .createQuery("SELECT COUNT(r) FROM AgentRun r WHERE r.deleted = false AND r.owner.id = :owner AND r.workingCopy IS NOT NULL", Long.class)
                .setParameter("owner", user.getId())
                .getSingleResult();
    }

    @Override
    public int updateStatus(Long runId, Long ownerId, AgentRunStatus from, AgentRunStatus to, String reason, Date finishedAt) throws Exception {
        return statusUpdate("r.id = :id AND r.status = :from AND r.deleted = false"
                + (ownerId != null ? " AND r.owner.id = :owner" : ""), runId, ownerId, from, to, reason, finishedAt);
    }

    @Override
    public List<AgentRun> findNeedingAttention(User user) throws Exception {
        if (user == null || user.getId() == null) {
            return List.of();
        }
        List<AgentRun> runs = entityManager
                .createQuery("SELECT r FROM AgentRun r WHERE r.deleted = false AND r.agent.deleted = false AND r.owner.id = :owner "
                        + "AND r.attention = true ORDER BY r.modification ASC, r.id ASC", AgentRun.class)
                .setParameter("owner", user.getId())
                .getResultList();
        for (AgentRun run : runs) {
            hydrate(run);
        }
        return runs;
    }

    @Override
    public int clearAttention(Long runId, Long ownerId) throws Exception {
        return entityManager
                .createQuery("UPDATE AgentRun r SET r.attention = false WHERE r.id = :id AND r.owner.id = :owner AND r.attention = true")
                .setParameter("id", runId)
                .setParameter("owner", ownerId)
                .executeUpdate();
    }

    @Override
    public int addUsage(Long runId, long prefillTokens, long generatedTokens) throws Exception {
        return entityManager
                .createQuery("UPDATE AgentRun r SET r.prefillTokens = r.prefillTokens + :prefill, "
                        + "r.generatedTokens = r.generatedTokens + :generated, r.rounds = r.rounds + 1, r.modification = :now "
                        + "WHERE r.id = :id AND r.deleted = false")
                .setParameter("prefill", prefillTokens)
                .setParameter("generated", generatedTokens)
                .setParameter("now", new Date())
                .setParameter("id", runId)
                .executeUpdate();
    }

    @Override
    public int updateWorkingCopy(Long runId, Manuscript workingCopy) throws Exception {
        return entityManager
                .createQuery("UPDATE AgentRun r SET r.workingCopy = :copy, r.modification = :now WHERE r.id = :id")
                .setParameter("copy", workingCopy)
                .setParameter("now", new Date())
                .setParameter("id", runId)
                .executeUpdate();
    }

    @Override
    public int forceStatus(Long runId, AgentRunStatus to, String reason, Date finishedAt) throws Exception {
        return statusUpdate("r.id = :id", runId, null, null, to, reason, finishedAt);
    }

    private int statusUpdate(String where, Long runId, Long ownerId, AgentRunStatus from, AgentRunStatus to, String reason, Date finishedAt) {
        Query query = entityManager.createQuery("UPDATE AgentRun r SET r.status = :to, r.statusReason = :reason, "
                        + "r.finishedAt = :finishedAt, r.attention = :attention, r.modification = :now WHERE " + where)
                .setParameter("to", to)
                .setParameter("reason", reason)
                .setParameter("finishedAt", finishedAt)
                .setParameter("attention", to.needsAttention(reason))
                .setParameter("now", new Date())
                .setParameter("id", runId);
        if (from != null) {
            query.setParameter("from", from);
        }
        if (ownerId != null) {
            query.setParameter("owner", ownerId);
        }
        return query.executeUpdate();
    }
}
