package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ChatMessageRepository extends ExtendableRepository<ChatMessage> {

    List<ChatMessage> findBranchFromLeaf(ChatMessage leaf) throws Exception;

    List<ChatMessage> findChildren(ChatMessage parent) throws Exception;

    List<ChatMessage> findRootMessages(Manuscript manuscript) throws Exception;

    List<ChatMessage> findAllByManuscript(Manuscript manuscript) throws Exception;

    void reparentChildren(ChatMessage targetNode, ChatMessage newParent) throws Exception;

    /**
     * Makes the part {@code newParent} the parent of the children of {@code targetNode} (the deleted ones too), except itself:
     * what a part inserted below {@code targetNode} takes over.
     */
    void reparentChildrenExcept(ChatMessage targetNode, ChatMessage newParent) throws Exception;

    /**
     * Makes the part {@code newRoot} the parent of the other first parts of the manuscript (the roots, the deleted ones too):
     * what a part inserted before the story takes over.
     */
    void reparentRootsUnder(Manuscript manuscript, ChatMessage newRoot) throws Exception;

    boolean hasAnyMessages(Manuscript manuscript) throws Exception;

    List<Long> getAllMessageIds(Manuscript manuscript) throws Exception;

    List<ChatMessage> getAllMessages(Manuscript manuscript) throws Exception;

    /**
     * Every message of the manuscript, the soft deleted ones too, for deleting the manuscript for good.
     */
    List<ChatMessage> getAllMessagesIncludingDeleted(Manuscript manuscript) throws Exception;

    /**
     * Id and {@code _fulltext} ({@code Object[]{Long, String}}) of the messages of the manuscript that are not deleted
     * and match the query, oldest first. The rest of the message, including {@code extendedContent}, is not read.
     *
     * @param likePatterns for each word the {@code LIKE} patterns (escape character {@code \}) of which one must
     *                     match, see {@link com.github.enerccio.marginalia.domain.service.search.FulltextQuery#likePatterns()}
     */
    List<Object[]> findFulltexts(Manuscript manuscript, List<List<String>> likePatterns) throws Exception;

    /**
     * Id of the message and id of its manuscript ({@code Object[]{Long, Long}}) for the messages of the manuscripts
     * that are not deleted and match the query, the text of the messages is not read.
     *
     * @param likePatterns see {@link #findFulltexts}
     */
    List<Object[]> findFulltextHits(Collection<Long> manuscriptIds, List<List<String>> likePatterns) throws Exception;

    /**
     * Ids of the parts from the first one to the active part of the manuscript (the branch the story shows), empty
     * when the manuscript has no active part.
     */
    Set<Long> findActiveBranchIds(Long manuscriptId) throws Exception;

    /**
     * The words and the tokens ({@code long[]{words, tokens}}) of the active branch (the parts from the first one to the
     * active leaf, deleted parts not counted) of each of the manuscripts, summed in one query. A manuscript without an
     * active part is not a key.
     */
    Map<Long, long[]> sumActiveBranches(Collection<Long> manuscriptIds) throws Exception;

    /**
     * Id and {@code _fulltext} ({@code Object[]{Long, String}}) of the messages of the manuscript that are not deleted,
     * oldest first. The rest of the message is not read.
     *
     * @param literal text the {@code _fulltext} has to contain (the database compares ignoring the case of the letters
     *                it knows, so the caller checks again); null for all the messages
     */
    List<Object[]> findFulltextsContaining(Manuscript manuscript, String literal) throws Exception;

    /**
     * Id of the message, id of its summary and {@code _fulltext} of the summary ({@code Object[]{Long, Long, String}})
     * of the messages of the manuscript that are not deleted and have a summary that is not deleted, oldest first.
     *
     * @param literal see {@link #findFulltextsContaining}
     */
    List<Object[]> findSummaryFulltextsContaining(Manuscript manuscript, String literal) throws Exception;

    /**
     * Ids of the messages below the message (its children, their children...) that are not deleted, the message itself
     * is not among them.
     */
    List<Long> findDescendantIds(ChatMessage message) throws Exception;

    /**
     * Soft deletes all messages of the manuscript with one update. The messages are not loaded and no listener runs,
     * so their extended content stays as it is.
     *
     * @return number of the deleted messages
     */
    int softDeleteByManuscript(Manuscript manuscript) throws Exception;

    /**
     * Soft deletes the messages with one update per chunk of ids, see {@link #softDeleteByManuscript}.
     *
     * @return number of the deleted messages
     */
    int softDeleteByIds(Collection<Long> ids) throws Exception;

    int getTotalWordCount(Long manuscriptId) throws Exception;

    int getTotalTokenCount(Long manuscriptId) throws Exception;

}