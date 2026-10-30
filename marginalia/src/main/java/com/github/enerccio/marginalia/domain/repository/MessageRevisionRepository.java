package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MessageRevisionRepository extends ExtendableRepository<MessageRevision> {

    /**
     * The newest version of the part that is not in the trash, null when it has none.
     */
    MessageRevision findLatest(ChatMessage part) throws Exception;

    /**
     * The versions of the part, newest first, without their text (it is not read).
     *
     * @param includeDeleted whether the versions in the trash are listed too
     */
    List<RevisionRow> findRows(ChatMessage part, boolean includeDeleted) throws Exception;

    /**
     * How many versions that are not in the trash each of the parts has, by the id of the part; the parts without one
     * are not in the map.
     */
    Map<Long, Long> countByPartIds(Collection<Long> partIds) throws Exception;

    /**
     * Soft deletes the versions of all parts of the manuscript with one update (they are not loaded and no listener
     * runs, their extended content stays as it is).
     *
     * @return number of the deleted versions
     */
    int softDeleteByManuscript(Manuscript manuscript) throws Exception;

    /**
     * Soft deletes the versions of the parts with the ids, see {@link #softDeleteByManuscript}.
     *
     * @return number of the deleted versions
     */
    int softDeleteByPartIds(Collection<Long> partIds) throws Exception;

    /**
     * Deletes for good the versions of the parts with the ids, the ones in the trash too.
     *
     * @return number of the deleted versions
     */
    int hardDeleteByPartIds(Collection<Long> partIds) throws Exception;

}
