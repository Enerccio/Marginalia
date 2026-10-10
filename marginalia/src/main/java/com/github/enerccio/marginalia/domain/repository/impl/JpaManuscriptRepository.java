package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.search.LikePatterns;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import jakarta.persistence.TypedQuery;

import java.util.*;

public class JpaManuscriptRepository extends JpaExtendableRepository<Manuscript> implements ManuscriptRepository {

    @Override
    protected Class<Manuscript> getEntityClass() {
        return Manuscript.class;
    }

    @Override
    public Manuscript findViewable(String uuid, User user) throws Exception {
        List<Manuscript> results = getEntityManager().createQuery(
                        "SELECT m FROM Manuscript m WHERE m.uuid = :uuid AND m.deleted = false AND (m.owner.id = :ownerId OR m.published = true)",
                        Manuscript.class)
                .setParameter("uuid", uuid)
                .setParameter("ownerId", user.getId())
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? null : hydrate(results.getFirst());
    }

    @Override
    public void markOpened(Long id, User user) throws Exception {
        // bulk update, so opening a book doesn't touch modification or extended content
        getEntityManager().createQuery("UPDATE Manuscript m SET m.lastOpened = :now WHERE m.id = :id AND m.owner.id = :ownerId")
                .setParameter("now", new Date())
                .setParameter("id", id)
                .setParameter("ownerId", user.getId())
                .executeUpdate();
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @Override
    public List<Long> searchManuscripts(ManuscriptFilterValues filterValues, List<Sorter> sorters, User user) throws Exception {
        if (filterValues == null) {
            return getEntityManager().createQuery("SELECT m.id FROM Manuscript m WHERE m.deleted = false AND m.owner.id = :ownerId " + Sorter.toOrderBy("m", sorters), Long.class)
                    .setParameter("ownerId", user.getId())
                    .getResultList();
        } else {
            String base = "SELECT DISTINCT (m.id) FROM Manuscript m LEFT JOIN TagRelation tr ON tr.objectId = m.id LEFT JOIN Tag t ON tr.tag = t WHERE ";
            Map<String, Object> parameters = new HashMap<>();

            List<String> ands = new ArrayList<>();

            ands.add("m.deleted = false");
            ands.add("m.owner.id = :ownerId");
            parameters.put("ownerId", user.getId());

            if (filterValues.getName() != null) {
                String nameFilter = filterValues.getName();
                nameFilter = LikePatterns.fromWildcards(nameFilter);
                ands.add("m.name LIKE :name ESCAPE '\\'");
                parameters.put("name", nameFilter + "%");
            }
            if (filterValues.getTags() != null && !filterValues.getTags().isEmpty()) {
                ands.add("tr.clazz = :clazz");
                parameters.put("clazz", Manuscript.class.getName());

                List<String> ors = new ArrayList<>();
                for (int i=0; i<filterValues.getTags().size(); i++) {
                    String tagKey = "tag" + i;
                    ors.add("t.value = :" + tagKey);
                    parameters.put(tagKey, filterValues.getTags().get(i));
                }
                ands.add("(" + String.join(" OR ", ors) +")");
            }

            String query = base + String.join(" AND ", ands);

            if (filterValues.getTags() != null && !filterValues.getTags().isEmpty()) {
                query += " GROUP BY m.id HAVING COUNT(DISTINCT t.value) = :tagCount";
                parameters.put("tagCount", filterValues.getTags().size());
            }

            query += Sorter.toOrderBy("m", sorters);

            TypedQuery<Long> q = getEntityManager().createQuery(query, Long.class);

            for (String key : parameters.keySet()) {
                q.setParameter(key, parameters.get(key));
            }

            return q.getResultList();
        }
    }
}
