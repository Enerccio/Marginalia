package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ChatMessageRepository;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatMessageServiceImpl extends ExtendableServiceImpl<ChatMessage, ChatMessageRepository> implements ChatMessageService {
    private static final Pattern WORD_PATTERN = Pattern.compile("\\w+", Pattern.UNICODE_CHARACTER_CLASS);

    @Autowired
    private ManuscriptService manuscriptService;

    @Override
    @CommonTx
    public ChatMessage getParent(ChatMessage node) throws Exception {
        if (node == null)
            return null;
        return find(find(node).getParent());
    }

    @Override
    @CommonTx
    public ChatMessage createRoot(Manuscript manuscript, ChatMessage message) throws Exception {
        message.setParentScript(manuscript);
        message.setParent(null);
        return save(message);
    }

    @Override
    @CommonTx
    public ChatMessage addChild(ChatMessage parent, ChatMessage child) throws Exception {
        child.setParent(parent);
        child.setParentScript(parent.getParentScript());
        return save(child);
    }

    @Override
    @CommonTxReadOnly
    public List<ChatMessage> getBranchFromLeaf(ChatMessage leaf) throws Exception {
        if (leaf == null) {
            return Collections.emptyList();
        }
        return getRepository().findBranchFromLeaf(leaf);
    }

    @Override
    @CommonTxReadOnly
    public List<ChatMessage> getSwipesForMessage(ChatMessage message) throws Exception {
        if (message == null) {
            return Collections.emptyList();
        }

        if (message.getParent() != null) {
            return getRepository().findChildren(message.getParent());
        } else if (message.getParentScript() != null) {
            return getRepository().findRootMessages(message.getParentScript());
        }

        return Collections.emptyList();
    }

    @Override
    @CommonTx
    public ChatMessage swipeTo(Manuscript manuscript, ChatMessage targetMessage) throws Exception {
        if (manuscript == null || targetMessage == null) {
            return null;
        }

        ChatMessage deepestLeaf = findDeepestActiveLeaf(targetMessage);
        manuscript.setActiveLeaf(deepestLeaf);
        return deepestLeaf;
    }

    private ChatMessage findDeepestActiveLeaf(ChatMessage node) throws Exception {
        ChatMessage current = node;
        while (true) {
            List<ChatMessage> children = getRepository().findChildren(current);
            if (children.isEmpty()) {
                break;
            }
            current = children.get(children.size() - 1);
        }
        return current;
    }

    @Override
    @CommonTxReadOnly
    public boolean hasAnyMessages(Manuscript manuscript) throws Exception {
        return getRepository().hasAnyMessages(manuscript);
    }

    @Override
    @CommonTxReadOnly
    public int getTotalWordCount(Manuscript manuscript) throws Exception {
        if (manuscript == null || manuscript.getId() == null) {
            return 0;
        }
        return getRepository().getTotalWordCount(manuscript.getId());
    }

    @Override
    @CommonTxReadOnly
    public int getTotalTokenCount(Manuscript manuscript) throws Exception {
        if (manuscript == null || manuscript.getId() == null) {
            return 0;
        }
        return getRepository().getTotalTokenCount(manuscript.getId());
    }

    @Override
    @CommonTxReadOnly
    public int getBranchWordCount(ChatMessage leaf) throws Exception {
        if (leaf == null || leaf.getId() == null) {
            return 0;
        }
        List<ChatMessage> branch = getBranchFromLeaf(leaf);
        int total = 0;
        for (ChatMessage msg : branch) {
            total += msg.getWordCount();
        }
        return total;
    }

    @Override
    @CommonTxReadOnly
    public long getBranchTokenCount(ChatMessage leaf) throws Exception {
        if (leaf == null || leaf.getId() == null) {
            return 0;
        }
        List<ChatMessage> branch = getBranchFromLeaf(leaf);
        long total = 0;
        for (ChatMessage msg : branch) {
            total += msg.getTokenCount();
        }
        return total;
    }

    @Override
    @CommonTx
    public void deleteNodeAndMigrateChildren(ChatMessage message, Manuscript manuscript, boolean softDelete) throws Exception {
        if (message == null) {
            return;
        }

        ChatMessage parent = message.getParent();

        getRepository().reparentChildren(message, parent);

        if (manuscript != null && manuscript.getActiveLeaf() != null) {
            if (manuscript.getActiveLeaf().getId().equals(message.getId())) {
                manuscript.setActiveLeaf(parent);
            }
        }

        delete(message, softDelete);
    }

    @Override
    @NoTx
    public int countWords(String text) {
        Matcher matcher = WORD_PATTERN.matcher(text);
        int count = 0;
        while (matcher.find()) count++;
        return count;
    }

}