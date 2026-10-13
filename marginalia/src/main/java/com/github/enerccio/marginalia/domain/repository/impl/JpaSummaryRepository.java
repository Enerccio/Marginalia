package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.repository.SummaryRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public class JpaSummaryRepository extends JpaExtendableRepository<Summary> implements SummaryRepository {

    @Override
    protected Class<Summary> getEntityClass() {
        return Summary.class;
    }

    @Override
    public int softDeleteByManuscript(Manuscript manuscript) throws Exception {
        if (manuscript == null || manuscript.getId() == null) {
            return 0;
        }
        return entityManager.createQuery(
                        "UPDATE Summary s SET s.deleted = true, s.modification = :now WHERE s.deleted = false "
                                + "AND s.id IN (SELECT m.summary.id FROM ChatMessage m WHERE m.parentScript.id = :manuscript)")
                .setParameter("now", new Date())
                .setParameter("manuscript", manuscript.getId())
                .executeUpdate();
    }

    @Override
    public int softDeleteByMessageIds(Collection<Long> messageIds) throws Exception {
        int deleted = 0;
        Date now = new Date();
        for (List<Long> chunk : chunks(messageIds)) {
            deleted += entityManager.createQuery(
                            "UPDATE Summary s SET s.deleted = true, s.modification = :now WHERE s.deleted = false "
                                    + "AND s.id IN (SELECT m.summary.id FROM ChatMessage m WHERE m.id IN :ids)")
                    .setParameter("now", now)
                    .setParameter("ids", chunk)
                    .executeUpdate();
        }
        return deleted;
    }

}
