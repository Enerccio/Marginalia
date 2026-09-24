package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Constants;
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

public class ChatMessageServiceImpl extends TreeServiceImpl<ChatMessage, ChatMessageRepository> implements ChatMessageService {
    private static final Pattern WORD_PATTERN = Pattern.compile("\\w+", Pattern.UNICODE_CHARACTER_CLASS);

    @Autowired
    private ManuscriptService manuscriptService;

    @Override
    @CommonTx
    public ChatMessage createRoot(Manuscript manuscript, ChatMessage message) throws Exception {
        message.setParent(null);
        List<String> rootTrees = getRootTrees(manuscript.getOwner());
        String rootTree;
        if (rootTrees.isEmpty()) {
            rootTree = num2tree(0L);
        } else {
            rootTree = incTree(rootTrees.getLast(), Constants.TREE_DEFAULT_ALLOC_GAP);
        }
        message.setTree(rootTree);
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
        List<ChatMessage> branch = getBranchFromLeaf(leaf.getId());
        int total = 0;
        for (ChatMessage msg : branch) {
            total += msg.getWordCount();
        }
        return total;
    }

    @Override
    @CommonTxReadOnly
    public int getBranchTokenCount(ChatMessage leaf) throws Exception {
        if (leaf == null || leaf.getId() == null) {
            return 0;
        }
        List<ChatMessage> branch = getBranchFromLeaf(leaf.getId());
        int total = 0;
        for (ChatMessage msg : branch) {
            total += msg.getTokenCount();
        }
        return total;
    }

    @Override
    @CommonTx
    public void deleteNodeAndMigrateChildren(ChatMessage message, Manuscript manuscript, boolean hard) throws Exception {
        if (message == null || message.getId() == null) {
            return;
        }

        message = find(message.getId());
        if (message == null) {
            return;
        }

        if (manuscript != null && manuscript.getId() != null) {
            manuscript = manuscriptService.find(manuscript.getId());
        }

        ChatMessage parentNode = message.getParent();
        if (parentNode == null) {
            parentNode = getParent(message);
        } else {
            parentNode = find(parentNode.getId());
        }

        List<ChatMessage> children = getChildren(message);
        ChatMessage activeLeaf = manuscript != null ? manuscript.getActiveLeaf() : null;
        boolean isDeletingActiveLeaf = activeLeaf != null && activeLeaf.getId().equals(message.getId());

        for (ChatMessage child : children) {
            if (parentNode != null) {
                moveChild(parentNode, child);
                child.setParent(parentNode);
            } else {
                child.setParent(null);
                List<String> rootTrees = getRootTrees(child.getOwner());
                String rootTree = rootTrees.isEmpty() ? num2tree(0L) : incTree(rootTrees.getLast(), Constants.TREE_DEFAULT_ALLOC_GAP);
                child.setTree(rootTree);
            }
            save(child);
        }

        if (manuscript != null && isDeletingActiveLeaf) {
            ChatMessage newActiveLeaf = null;

            if (!children.isEmpty()) {
                newActiveLeaf = children.getFirst();
            } else {
                ChatMessage leftSibling = null;
                try {
                    leftSibling = getPrevious(message);
                } catch (Exception ignored) {}

                if (leftSibling != null) {
                    ChatMessage leafCursor = leftSibling;
                    while (hasChildren(leafCursor)) {
                        leafCursor = getLastChild(leafCursor);
                    }
                    newActiveLeaf = leafCursor;
                } else {
                    newActiveLeaf = parentNode;
                }
            }

            manuscript.setActiveLeaf(newActiveLeaf);
            manuscriptService.save(manuscript);
        }

        delete(message, hard);
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