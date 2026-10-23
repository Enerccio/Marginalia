package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

import java.util.Collection;
import java.util.List;

public interface AnnotationRepository extends ExtendableRepository<Annotation> {

    /**
     * Non-deleted annotations of the part, in the order of their passages (the ones without a passage first), then the
     * order they were made in.
     */
    List<Annotation> findAllByPart(ChatMessage part) throws Exception;

    /**
     * Non-deleted annotations of the parts, see {@link #findAllByPart}.
     */
    List<Annotation> findAllByPartIds(Collection<Long> partIds) throws Exception;

    /**
     * Non-deleted annotations of the non-deleted parts of the manuscript, the parts in the order they were made in and the
     * annotations as {@link #findAllByPart}.
     */
    List<Annotation> findAllByManuscript(Manuscript manuscript) throws Exception;

    /**
     * Soft deletes the annotations of all parts of the manuscript with one update (they are not loaded and no listener
     * runs, their extended content stays as it is).
     *
     * @return number of the deleted annotations
     */
    int softDeleteByManuscript(Manuscript manuscript) throws Exception;

    /**
     * Soft deletes the annotations of the parts with the ids, see {@link #softDeleteByManuscript}.
     *
     * @return number of the deleted annotations
     */
    int softDeleteByPartIds(Collection<Long> partIds) throws Exception;

    /**
     * Removes the annotations of the parts with the ids from the database, deleted ones too.
     *
     * @return number of the removed annotations
     */
    int hardDeleteByPartIds(Collection<Long> partIds) throws Exception;

}
