package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ChatMessageRepository;

import com.github.enerccio.marginalia.domain.service.search.FulltextHit;
import com.github.enerccio.marginalia.domain.service.search.FulltextQuery;

import java.util.List;

public interface ChatMessageService extends ExtendableService<ChatMessage, ChatMessageRepository> {

    ChatMessage getParent(ChatMessage node) throws Exception;

    ChatMessage createRoot(Manuscript manuscript, ChatMessage message) throws Exception;

    ChatMessage addChild(ChatMessage parent, ChatMessage child) throws Exception;

    List<ChatMessage> getBranchFromLeaf(ChatMessage leaf) throws Exception;

    List<ChatMessage> getSwipesForMessage(ChatMessage message) throws Exception;

    ChatMessage swipeTo(Manuscript manuscript, ChatMessage targetMessage) throws Exception;

    ChatMessage branch(Manuscript manuscript, ChatMessage branched) throws Exception;

    List<ChatMessage> getAllMessages(Manuscript manuscript) throws Exception;

    /**
     * Full-text search over the parts of all branches of the book, oldest first.
     *
     * @see FulltextQuery
     */
    List<FulltextHit> searchFulltext(Manuscript manuscript, String query) throws Exception;

    /**
     * The end of the branch to show to display the part: the active leaf when the part is on the active branch,
     * otherwise the nearest end of a branch below the part (the newer one when there are two). Null when the part
     * isn't a part of the book.
     */
    ChatMessage findLeafFor(Manuscript manuscript, ChatMessage target) throws Exception;

    int getBranchWordCount(ChatMessage leaf) throws Exception;

    long getBranchTokenCount(ChatMessage leaf) throws Exception;

    int getTotalWordCount(Manuscript manuscript) throws Exception;

    int getTotalTokenCount(Manuscript manuscript) throws Exception;

    boolean hasAnyMessages(Manuscript manuscript) throws Exception;

    Manuscript deleteNodeAndMigrateChildren(ChatMessage message, Manuscript manuscript, boolean hard) throws Exception;

    int countWords(String text);

}