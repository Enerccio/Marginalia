package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ChatMessageRepository;

import java.util.List;

public interface ChatMessageService extends ExtendableService<ChatMessage, ChatMessageRepository> {

    ChatMessage getParent(ChatMessage node) throws Exception;

    ChatMessage createRoot(Manuscript manuscript, ChatMessage message) throws Exception;

    ChatMessage addChild(ChatMessage parent, ChatMessage child) throws Exception;

    List<ChatMessage> getBranchFromLeaf(ChatMessage leaf) throws Exception;

    List<ChatMessage> getSwipesForMessage(ChatMessage message) throws Exception;

    ChatMessage swipeTo(Manuscript manuscript, ChatMessage targetMessage) throws Exception;

    int getBranchWordCount(ChatMessage leaf) throws Exception;

    long getBranchTokenCount(ChatMessage leaf) throws Exception;

    int getTotalWordCount(Manuscript manuscript) throws Exception;

    int getTotalTokenCount(Manuscript manuscript) throws Exception;

    boolean hasAnyMessages(Manuscript manuscript) throws Exception;

    void deleteNodeAndMigrateChildren(ChatMessage message, Manuscript manuscript, boolean hard) throws Exception;

    int countWords(String text);

}