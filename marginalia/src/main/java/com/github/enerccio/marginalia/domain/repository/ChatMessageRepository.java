package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends ExtendableRepository<ChatMessage> {

    List<ChatMessage> findBranchFromLeaf(Long leafId) throws Exception;

    List<ChatMessage> findAllLeavesForManuscript(Long manuscriptId) throws Exception;

}
