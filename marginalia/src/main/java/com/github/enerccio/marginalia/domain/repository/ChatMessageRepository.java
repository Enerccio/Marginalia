package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

import java.util.List;

public interface ChatMessageRepository extends TreeRepository<ChatMessage> {

    List<ChatMessage> findBranchFromLeaf(Long leafId) throws Exception;

    List<ChatMessage> findAllLeavesForManuscript(Long manuscriptId) throws Exception;

    boolean hasAnyMessages(Manuscript manuscript) throws Exception;

    List<Long> getAllMessages(Manuscript manuscript) throws Exception;

    int getTotalWordCount(Long manuscriptId) throws Exception;

    int getTotalTokenCount(Long manuscriptId) throws Exception;
}