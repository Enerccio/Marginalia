package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.AgentRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public Map<String, String> findBookNames(Collection<String> agentUuids, Long ownerId) throws Exception {
        Map<String, String> names = new HashMap<>();
        if (agentUuids == null || agentUuids.isEmpty() || ownerId == null) {
            return names;
        }
        List<Object[]> rows = entityManager
                .createQuery("SELECT a.uuid, m.name FROM Agent a JOIN a.manuscript m WHERE a.owner.id = :owner AND a.uuid IN :uuids", Object[].class)
                .setParameter("owner", ownerId)
                .setParameter("uuids", agentUuids)
                .getResultList();
        for (Object[] row : rows) {
            names.put((String) row[0], (String) row[1]);
        }
        return names;
    }
}
