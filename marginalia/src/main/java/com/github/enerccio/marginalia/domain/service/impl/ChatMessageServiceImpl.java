package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ChatMessageRepository;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;

import java.util.Collections;
import java.util.List;

public class ChatMessageServiceImpl extends TreeServiceImpl<ChatMessage, ChatMessageRepository> implements ChatMessageService {

    @Override
    @CommonTx
    public ChatMessage createRoot(Manuscript manuscript, ChatMessage message) throws Exception {
        message.setParent(null);
        message.setTree(getRepository().getMaxTree(getLevelSize(), manuscript.getOwner()));
        message.setParentScript(manuscript);
        return save(message);
    }

    @Override
    @CommonTxReadOnly
    public List<ChatMessage> getBranchFromLeaf(Long leafId) throws Exception {
        if (leafId == null) {
            return Collections.emptyList();
        }
        return getRepository().findBranchFromLeaf(leafId);
    }

    @Override
    @CommonTxReadOnly
    public List<ChatMessage> getBranchFromLeaf(ChatMessage leaf) throws Exception {
        if (leaf == null || leaf.getId() == null) {
            return Collections.emptyList();
        }
        return getBranchFromLeaf(leaf.getId());
    }

    @Override
    @CommonTxReadOnly
    public List<ChatMessage> findAllLeavesForManuscript(Long manuscriptId) throws Exception {
        if (manuscriptId == null) {
            return Collections.emptyList();
        }
        return getRepository().findAllLeavesForManuscript(manuscriptId);
    }

    @Override
    @CommonTxReadOnly
    public List<ChatMessage> findAllLeavesForManuscript(Manuscript manuscript) throws Exception {
        if (manuscript == null || manuscript.getId() == null) {
            return Collections.emptyList();
        }
        return findAllLeavesForManuscript(manuscript.getId());
    }

    @Override
    @CommonTxReadOnly
    public boolean hasAnyMessages(Manuscript manuscript) throws Exception {
        return getRepository().hasAnyMessages(manuscript);
    }

}