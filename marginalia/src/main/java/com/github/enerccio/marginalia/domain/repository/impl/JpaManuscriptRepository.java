package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import jakarta.persistence.TypedQuery;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpaManuscriptRepository extends JpaExtendableRepository<Manuscript> implements ManuscriptRepository {

    @Override
    protected Class<Manuscript> getEntityClass() {
        return Manuscript.class;
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @Override
    public List<Long> searchManuscripts(ManuscriptFilterValues filterValues, List<Sorter> sorters, User user) throws Exception {
        if (filterValues == null) {
            return getEntityManager().createQuery("SELECT m.id FROM Manuscript m WHERE m.owner.id = :id " + Sorter.toOrderBy("m", sorters), Long.class)
                    .setParameter("ownerId", user.getId())
                    .getResultList();
        } else {
            String base = "SELECT DISTINCT (m.id) FROM Manuscript m LEFT JOIN TagRelation tr ON tr.objectId = m.id LEFT JOIN Tag t ON tr.tag = t WHERE ";
            Map<String, Object> parameters = new HashMap<>();

            List<String> ands = new ArrayList<>();

            ands.add("m.owner.id = :ownerId");
            parameters.put("ownerId", user.getId());

            if (filterValues.getName() != null) {
                String nameFilter = filterValues.getName();
                nameFilter = StringUtils.replaceEach(nameFilter,
                        new String[] {"\\", "_", "%", "*"},
                        new String[] {"\\\\", "\\_", "\\%", "%"}
                        );
                ands.add("m.name LIKE :name");
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
