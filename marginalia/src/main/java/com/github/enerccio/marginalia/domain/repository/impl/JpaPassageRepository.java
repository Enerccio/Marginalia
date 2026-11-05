package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.PassageRepository;
import com.github.enerccio.marginalia.utils.AppClock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class JpaPassageRepository extends JpaExtendableRepository<AnchoredPassage> implements PassageRepository {

    // the passages in the order of the text, the ones about the whole part before them
    private static final String ORDER = " ORDER BY COALESCE(p.start, -1) ASC, p.id ASC";
    private static final String EXTENSION_KINDS = " AND TYPE(p) = AnchoredPassage";

    @Override
    protected Class<AnchoredPassage> getEntityClass() {
        return AnchoredPassage.class;
    }

    @Override
    public List<AnchoredPassage> findAllByPart(ChatMessage part) throws Exception {
        if (part == null || part.getId() == null) {
            return List.of();
        }
        return findAllByPartIds(List.of(part.getId()));
    }

    @Override
    public List<AnchoredPassage> findAllByPartIds(Collection<Long> partIds) throws Exception {
        return byPartIds("", null, partIds);
    }

    @Override
    public List<AnchoredPassage> findKindByPartIds(String kind, Collection<Long> partIds) throws Exception {
        return byPartIds(EXTENSION_KINDS, kind, partIds);
    }

    private List<AnchoredPassage> byPartIds(String type, String kind, Collection<Long> partIds) throws Exception {
        List<AnchoredPassage> passages = new ArrayList<>();
        for (List<Long> chunk : chunks(partIds)) {
            var query = entityManager.createQuery("SELECT p FROM AnchoredPassage p WHERE p.deleted = false AND p.part.id IN :ids" + type
                    + (kind == null ? "" : " AND p.passageKind = :kind") + ORDER, AnchoredPassage.class)
                    .setParameter("ids", chunk);
            if (kind != null) {
                query.setParameter("kind", kind);
            }
            passages.addAll(query.getResultList());
        }
        for (AnchoredPassage passage : passages) {
            hydrate(passage);
        }
        return passages;
    }

    @Override
    public List<AnchoredPassage> findAllByManuscript(Manuscript manuscript) throws Exception {
        return byManuscript("", null, manuscript);
    }

    @Override
    public List<AnchoredPassage> findKindByManuscript(String kind, Manuscript manuscript) throws Exception {
        return byManuscript(EXTENSION_KINDS, kind, manuscript);
    }

    private List<AnchoredPassage> byManuscript(String type, String kind, Manuscript manuscript) throws Exception {
        if (manuscript == null || manuscript.getId() == null) {
            return List.of();
        }
        var query = entityManager
                .createQuery("SELECT p FROM AnchoredPassage p WHERE p.deleted = false AND p.part.deleted = false "
                        + "AND p.part.parentScript.id = :manuscript" + type + (kind == null ? "" : " AND p.passageKind = :kind")
                        + " ORDER BY p.part.id ASC, COALESCE(p.start, -1) ASC, p.id ASC", AnchoredPassage.class)
                .setParameter("manuscript", manuscript.getId());
        if (kind != null) {
            query.setParameter("kind", kind);
        }
        List<AnchoredPassage> passages = query.getResultList();
        for (AnchoredPassage passage : passages) {
            hydrate(passage);
        }
        return passages;
    }

    @Override
    public int softDeleteByManuscript(Manuscript manuscript) throws Exception {
        if (manuscript == null || manuscript.getId() == null) {
            return 0;
        }
        return entityManager.createQuery(
                        "UPDATE AnchoredPassage p SET p.deleted = true, p.modification = :now WHERE p.deleted = false "
                                + "AND p.part.id IN (SELECT m.id FROM ChatMessage m WHERE m.parentScript.id = :manuscript)")
                .setParameter("now", AppClock.now())
                .setParameter("manuscript", manuscript.getId())
                .executeUpdate();
    }

    @Override
    public int softDeleteByPartIds(Collection<Long> partIds) throws Exception {
        int deleted = 0;
        Date now = AppClock.now();
        for (List<Long> chunk : chunks(partIds)) {
            deleted += entityManager.createQuery(
                            "UPDATE AnchoredPassage p SET p.deleted = true, p.modification = :now WHERE p.deleted = false AND p.part.id IN :ids")
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
            deleted += entityManager.createQuery("DELETE FROM AnchoredPassage p WHERE p.part.id IN :ids")
                    .setParameter("ids", chunk)
                    .executeUpdate();
        }
        return deleted;
    }

}
