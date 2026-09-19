package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ChatMessageRepository;

import java.util.List;

public interface ChatMessageService extends ExtendableService<ChatMessage, ChatMessageRepository> {

    List<ChatMessage> getBranchFromLeaf(Long leafId) throws Exception;

    List<ChatMessage> getBranchFromLeaf(ChatMessage leaf) throws Exception;

    List<ChatMessage> findAllLeavesForManuscript(Long manuscriptId) throws Exception;

    List<ChatMessage> findAllLeavesForManuscript(Manuscript manuscript) throws Exception;

}