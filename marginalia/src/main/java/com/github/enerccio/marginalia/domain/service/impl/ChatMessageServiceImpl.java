package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ChatMessageRepository;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.search.FulltextHit;
import com.github.enerccio.marginalia.domain.service.search.FulltextQuery;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatMessageServiceImpl extends ExtendableServiceImpl<ChatMessage, ChatMessageRepository> implements ChatMessageService {
    private static final int SNIPPET_RADIUS = 60;
    private static final Pattern WORD_PATTERN = Pattern.compile("\\w+", Pattern.UNICODE_CHARACTER_CLASS);

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SummaryService summaryService;

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

    @Override
    @CommonTx
    public ChatMessage branch(Manuscript manuscript, ChatMessage branched) throws Exception {
        ChatMessage clone = new ChatMessage();
        clone.loadFrom(branched);
        clone.setSummary(summaryService.copySummary(branched.getSummary()));
        return save(clone);
    }

    @Override
    @CommonTxReadOnly
    public List<ChatMessage> getAllMessages(Manuscript manuscript) throws Exception {
        return getRepository().getAllMessages(manuscript);
    }

    @Override
    @CommonTxReadOnly
    public List<FulltextHit> searchFulltext(Manuscript manuscript, String query) throws Exception {
        FulltextQuery parsed = FulltextQuery.parse(query);
        if (manuscript == null || parsed.isEmpty() || manuscriptService.findForUser(manuscript.getUuid()) == null) {
            return Collections.emptyList();
        }

        List<FulltextHit> hits = new ArrayList<>();
        for (Object[] row : getRepository().findFulltexts(manuscript, parsed.likePatterns())) {
            hits.add(new FulltextHit((Long) row[0], parsed.snippet((String) row[1], SNIPPET_RADIUS)));
        }
        return hits;
    }

    @Override
    @CommonTxReadOnly
    public ChatMessage findLeafFor(Manuscript manuscript, ChatMessage target) throws Exception {
        if (manuscript == null || target == null || manuscriptService.findForUser(manuscript.getUuid()) == null) {
            return null;
        }

        Map<Long, ChatMessage> byId = new HashMap<>();
        Map<Long, List<ChatMessage>> children = new HashMap<>();
        for (ChatMessage message : getRepository().getAllMessages(manuscript)) {
            byId.put(message.getId(), message);
            if (message.getParent() != null) {
                children.computeIfAbsent(message.getParent().getId(), k -> new ArrayList<>()).add(message);
            }
        }
        if (!byId.containsKey(target.getId())) {
            return null;
        }

        ChatMessage active = manuscript.getActiveLeaf();
        for (Long id = active == null ? null : active.getId(); id != null && byId.containsKey(id); ) {
            if (id.equals(target.getId())) {
                return active;
            }
            ChatMessage parent = byId.get(id).getParent();
            id = parent == null ? null : parent.getId();
        }

        // breadth first, the newest child first: the first end found is the nearest, the newest of the nearest
        Deque<ChatMessage> queue = new ArrayDeque<>();
        queue.add(byId.get(target.getId()));
        while (!queue.isEmpty()) {
            ChatMessage current = queue.poll();
            List<ChatMessage> next = children.get(current.getId());
            if (next == null) {
                return current;
            }
            for (int i = next.size() - 1; i >= 0; i--) {
                queue.add(next.get(i));
            }
        }
        return null;
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
    public Manuscript deleteNodeAndMigrateChildren(ChatMessage message, Manuscript manuscript, boolean hard) throws Exception {
        if (message == null) {
            return manuscript;
        }

        ChatMessage parent = message.getParent();

        getRepository().reparentChildren(message, parent);

        if (manuscript != null && manuscript.getActiveLeaf() != null) {
            if (manuscript.getActiveLeaf().getId().equals(message.getId())) {
                manuscript.setActiveLeaf(parent);
            }
        }

        delete(message, hard);
        // the active leaf may have moved - it must not stay on the deleted part in the database
        return manuscript != null ? manuscriptService.save(manuscript) : null;
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