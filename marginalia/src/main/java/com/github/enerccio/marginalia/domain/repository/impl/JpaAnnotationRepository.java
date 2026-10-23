package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.AnnotationRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class JpaAnnotationRepository extends JpaExtendableRepository<Annotation> implements AnnotationRepository {

    // the passages in the order of the text, the ones about the whole part before them
    private static final String ORDER = " ORDER BY COALESCE(a.start, -1) ASC, a.id ASC";

    @Override
    protected Class<Annotation> getEntityClass() {
        return Annotation.class;
    }

    @Override
    public List<Annotation> findAllByPart(ChatMessage part) throws Exception {
        if (part == null || part.getId() == null) {
            return List.of();
        }
        return findAllByPartIds(List.of(part.getId()));
    }

    @Override
    public List<Annotation> findAllByPartIds(Collection<Long> partIds) throws Exception {
        List<Annotation> annotations = new ArrayList<>();
        for (List<Long> chunk : chunks(partIds)) {
            annotations.addAll(entityManager
                    .createQuery("SELECT a FROM Annotation a WHERE a.deleted = false AND a.part.id IN :ids" + ORDER, Annotation.class)
                    .setParameter("ids", chunk)
                    .getResultList());
        }
        for (Annotation annotation : annotations) {
            hydrate(annotation);
        }
        return annotations;
    }

    @Override
    public List<Annotation> findAllByManuscript(Manuscript manuscript) throws Exception {
        if (manuscript == null || manuscript.getId() == null) {
            return List.of();
        }
        List<Annotation> annotations = entityManager
                .createQuery("SELECT a FROM Annotation a WHERE a.deleted = false AND a.part.deleted = false "
                        + "AND a.part.parentScript.id = :manuscript ORDER BY a.part.id ASC, COALESCE(a.start, -1) ASC, a.id ASC", Annotation.class)
                .setParameter("manuscript", manuscript.getId())
                .getResultList();
        for (Annotation annotation : annotations) {
            hydrate(annotation);
        }
        return annotations;
    }

    @Override
    public int softDeleteByManuscript(Manuscript manuscript) throws Exception {
        if (manuscript == null || manuscript.getId() == null) {
            return 0;
        }
        return entityManager.createQuery(
                        "UPDATE Annotation a SET a.deleted = true, a.modification = :now WHERE a.deleted = false "
                                + "AND a.part.id IN (SELECT m.id FROM ChatMessage m WHERE m.parentScript.id = :manuscript)")
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
                            "UPDATE Annotation a SET a.deleted = true, a.modification = :now WHERE a.deleted = false AND a.part.id IN :ids")
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
            deleted += entityManager.createQuery("DELETE FROM Annotation a WHERE a.part.id IN :ids")
                    .setParameter("ids", chunk)
                    .executeUpdate();
        }
        return deleted;
    }

}
