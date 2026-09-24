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

    List<Long> getAllMessages(Manuscript manuscript) throws Exception;

    int getTotalWordCount(Long manuscriptId) throws Exception;

    int getTotalTokenCount(Long manuscriptId) throws Exception;

}