package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

import java.util.List;

public interface ChatMessageRepository extends ExtendableRepository<ChatMessage> {

    List<ChatMessage> findBranchFromLeaf(ChatMessage leaf) throws Exception;

    List<ChatMessage> findChildren(ChatMessage parent) throws Exception;

    List<ChatMessage> findRootMessages(Manuscript manuscript) throws Exception;

    List<ChatMessage> findAllByManuscript(Manuscript manuscript) throws Exception;

    void reparentChildren(ChatMessage targetNode, ChatMessage newParent) throws Exception;

    boolean hasAnyMessages(Manuscript manuscript) throws Exception;

    List<Long> getAllMessageIds(Manuscript manuscript) throws Exception;

    List<ChatMessage> getAllMessages(Manuscript manuscript) throws Exception;

    /**
     * Id and {@code _fulltext} ({@code Object[]{Long, String}}) of the messages of the manuscript that are not deleted
     * and match the query, oldest first. The rest of the message, including {@code extendedContent}, is not read.
     *
     * @param likePatterns for each word the {@code LIKE} patterns (escape character {@code \}) of which one must
     *                     match, see {@link com.github.enerccio.marginalia.domain.service.search.FulltextQuery#likePatterns()}
     */
    List<Object[]> findFulltexts(Manuscript manuscript, List<List<String>> likePatterns) throws Exception;

    int getTotalWordCount(Long manuscriptId) throws Exception;

    int getTotalTokenCount(Long manuscriptId) throws Exception;

}