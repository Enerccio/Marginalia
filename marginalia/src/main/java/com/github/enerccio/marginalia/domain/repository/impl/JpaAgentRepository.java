package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.AgentRepository;

import java.util.List;

public class JpaAgentRepository extends JpaExtendableRepository<Agent> implements AgentRepository {

    @Override
    protected Class<Agent> getEntityClass() {
        return Agent.class;
    }

    @Override
    public Agent findByManuscript(Manuscript manuscript) throws Exception {
        List<Agent> agents = findAllByManuscript(manuscript);
        return agents.isEmpty() ? null : agents.getFirst();
    }

    @Override
    public List<Agent> findAllByManuscript(Manuscript manuscript) throws Exception {
        if (manuscript == null || manuscript.getId() == null) {
            return List.of();
        }
        List<Agent> agents = entityManager
                .createQuery("SELECT a FROM Agent a WHERE a.deleted = false AND a.manuscript.id = :manuscript ORDER BY a.id ASC", Agent.class)
                .setParameter("manuscript", manuscript.getId())
                .getResultList();
        for (Agent agent : agents) {
            hydrate(agent);
        }
        return agents;
    }
}
