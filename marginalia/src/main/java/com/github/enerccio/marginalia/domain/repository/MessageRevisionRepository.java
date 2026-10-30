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
     * {@link #findRows(ChatMessage, boolean)} for the part with the id.
     */
    List<RevisionRow> findRows(Long partId, boolean includeDeleted) throws Exception;

    /**
     * The ids of the parts of the manuscript that are not deleted and have a version (one in the trash too).
     */
    List<Long> findPartIdsWithVersions(Manuscript manuscript) throws Exception;

    /**
     * Deletes for good the versions with the ids.
     *
     * @return number of the deleted versions
     */
    int hardDeleteByIds(Collection<Long> ids) throws Exception;

    /**
     * How much room the stored texts of the versions with the ids take, in bytes.
     */
    long sizeOf(Collection<Long> ids) throws Exception;

    /**
     * How many versions that are not in the trash each of the parts has, by the id of the part; the parts without one
     * are not in the map.
     */
    Map<Long, Long> countByPartIds(Collection<Long> partIds) throws Exception;

    /**
     * Puts the versions of the part that are newer than this one (made after it) and not in the trash into the trash.
     *
     * @return number of the versions
     */
    int softDeleteNewerThan(MessageRevision revision) throws Exception;

    /**
     * Deletes for good the versions of the part that are in the trash.
     *
     * @return number of the deleted versions
     */
    int hardDeleteTrashByPart(ChatMessage part) throws Exception;

    /**
     * Deletes for good the versions in the trash of the parts of the manuscript that are not deleted themselves.
     *
     * @return number of the deleted versions
     */
    int hardDeleteTrashByManuscript(Manuscript manuscript) throws Exception;

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

    /**
     * The versions of the parts of the manuscript that have every word of the search ({@code Object[]{String versionUuid,
     * Long partId, Date creation, RevisionSource source, String _fulltext}}), newest first. Only the versions that are not in the
     * trash, of the parts that are not deleted.
     *
     * @param likePatterns see {@link com.github.enerccio.marginalia.domain.service.search.FulltextQuery#likePatterns()}
     */
    List<Object[]> findFulltexts(Manuscript manuscript, List<List<String>> likePatterns) throws Exception;

    /**
     * Id of the part and id of its book ({@code Object[]{Long, Long}}) of the parts of the manuscripts with the ids that have a
     * version with every word of the search, see {@link #findFulltexts}.
     */
    List<Object[]> findFulltextHits(Collection<Long> manuscriptIds, List<List<String>> likePatterns) throws Exception;

}
