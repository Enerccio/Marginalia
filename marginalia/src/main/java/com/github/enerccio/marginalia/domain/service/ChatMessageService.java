package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ChatMessageRepository;

import java.util.List;

public interface ChatMessageService extends TreeService<ChatMessage, ChatMessageRepository> {

    ChatMessage createRoot(Manuscript manuscript, ChatMessage message) throws Exception;

    List<ChatMessage> getBranchFromLeaf(Long leafId) throws Exception;

    List<ChatMessage> getBranchFromLeaf(ChatMessage leaf) throws Exception;

    List<ChatMessage> findAllLeavesForManuscript(Long manuscriptId) throws Exception;

    List<ChatMessage> findAllLeavesForManuscript(Manuscript manuscript) throws Exception;

    boolean hasAnyMessages(Manuscript manuscript) throws Exception;

    int getTotalWordCount(Manuscript manuscript) throws Exception;

    int getTotalTokenCount(Manuscript manuscript) throws Exception;

    int getBranchWordCount(ChatMessage leaf) throws Exception;

    int getBranchTokenCount(ChatMessage leaf) throws Exception;

    void deleteNodeAndMigrateChildren(ChatMessage message, Manuscript manuscript, boolean hard) throws Exception;

    int countWords(String text);
}