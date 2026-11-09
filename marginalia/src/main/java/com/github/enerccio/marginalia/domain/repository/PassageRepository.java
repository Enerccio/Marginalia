package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

import java.util.Collection;
import java.util.List;

/**
 * The anchored passages of every kind, the editorial {@link com.github.enerccio.marginalia.domain.model.impl.Annotation notes}
 * too (they are rows of the same table). The queries of the lists of notes are in {@link AnnotationRepository}.
 */
public interface PassageRepository extends ExtendableRepository<AnchoredPassage> {

    /**
     * Non-deleted passages of all kinds of the part, in the order of their ranges (the ones without a range first), then the
     * order they were made in.
     */
    List<AnchoredPassage> findAllByPart(ChatMessage part) throws Exception;

    /**
     * Non-deleted passages of all kinds of the parts, see {@link #findAllByPart}.
     */
    List<AnchoredPassage> findAllByPartIds(Collection<Long> partIds) throws Exception;

    /**
     * Non-deleted passages of all kinds of the non-deleted parts of the manuscript, the parts in the order they were made in
     * and the passages as {@link #findAllByPart}.
     */
    List<AnchoredPassage> findAllByManuscript(Manuscript manuscript) throws Exception;

    /**
     * Non-deleted passages of the kind (not the editorial notes) of the part, see {@link #findAllByPart}.
     *
     * @param kind {@link AnchoredPassage#getPassageKind()}, null for the passages of all the extensions' kinds
     */
    List<AnchoredPassage> findKindByPartIds(String kind, Collection<Long> partIds) throws Exception;

    /**
     * Non-deleted passages of the kind (not the editorial notes) of the non-deleted parts of the manuscript.
     *
     * @param kind {@link AnchoredPassage#getPassageKind()}, null for the passages of all the extensions' kinds
     */
    List<AnchoredPassage> findKindByManuscript(String kind, Manuscript manuscript) throws Exception;

    /**
     * Soft deletes the passages of all parts of the manuscript with one update (they are not loaded and no listener runs, their
     * extended content stays as it is).
     *
     * @return number of the deleted passages
     */
    int softDeleteByManuscript(Manuscript manuscript) throws Exception;

    /**
     * Soft deletes the passages of the parts with the ids, see {@link #softDeleteByManuscript}.
     *
     * @return number of the deleted passages
     */
    int softDeleteByPartIds(Collection<Long> partIds) throws Exception;

    /**
     * Removes the passages of the parts with the ids from the database, deleted ones too.
     *
     * @return number of the removed passages
     */
    int hardDeleteByPartIds(Collection<Long> partIds) throws Exception;

}
