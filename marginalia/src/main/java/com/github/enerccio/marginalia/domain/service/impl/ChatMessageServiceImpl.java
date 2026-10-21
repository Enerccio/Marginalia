package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.repository.ChatMessageRepository;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
import com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer.JavaTokkitStrategy;
import com.github.enerccio.marginalia.domain.service.search.FulltextHit;
import com.github.enerccio.marginalia.domain.service.search.FulltextQuery;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import com.github.enerccio.marginalia.utils.MarkdownArchive;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

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

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    @Autowired
    @Lazy
    private InferenceServices inferenceServices;

    /**
     * A part of a book an agent works on can't be changed, see {@link AgentRunService#checkNotLocked}.
     */
    @Override
    @CommonTx
    public ChatMessage save(ChatMessage message) throws Exception {
        agentRunService.checkNotLocked(message.getParentScript());
        return super.save(message);
    }

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
    @CommonTx
    public Manuscript branchAndSwitch(Manuscript manuscript, ChatMessage branched) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage clone = branch(book, ownPart(book, branched));
        book.setActiveLeaf(clone);
        return manuscriptService.save(book);
    }

    @Override
    @CommonTx
    public Manuscript swipeAndSwitch(Manuscript manuscript, ChatMessage target) throws Exception {
        Manuscript book = ownBook(manuscript);
        swipeTo(book, ownPart(book, target));
        return manuscriptService.save(book);
    }

    private Manuscript ownBook(Manuscript manuscript) throws Exception {
        Manuscript book = manuscript == null ? null : (Manuscript) Hibernate.unproxy(manuscriptService.findForUser(manuscript.getUuid()));
        if (book == null) {
            throw new IllegalArgumentException("The manuscript does not exist");
        }
        return book;
    }

    private ChatMessage ownPart(Manuscript book, ChatMessage message) throws Exception {
        // the part may be the lazy proxy of an association loaded earlier, its extended attributes are not read from the proxy
        ChatMessage stored = message == null ? null : (ChatMessage) Hibernate.unproxy(find(message));
        if (stored == null || stored.isDeleted() || stored.getParentScript() == null || !stored.getParentScript().getId().equals(book.getId())) {
            throw new IllegalArgumentException("The part does not exist");
        }
        return stored;
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
    public List<MessageFulltext> findFulltextsContaining(Manuscript manuscript, String literal) throws Exception {
        if (manuscript == null || manuscriptService.findForUser(manuscript.getUuid()) == null) {
            return Collections.emptyList();
        }
        return getRepository().findFulltextsContaining(manuscript, literal).stream()
                .map(row -> new MessageFulltext((Long) row[0], (String) row[1]))
                .toList();
    }

    @Override
    @CommonTxReadOnly
    public List<SummaryFulltext> findSummaryFulltextsContaining(Manuscript manuscript, String literal) throws Exception {
        if (manuscript == null || manuscriptService.findForUser(manuscript.getUuid()) == null) {
            return Collections.emptyList();
        }
        return getRepository().findSummaryFulltextsContaining(manuscript, literal).stream()
                .map(row -> new SummaryFulltext((Long) row[0], (Long) row[1], (String) row[2]))
                .toList();
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

    /**
     * A soft deleted message takes its summary and everything below it (its children and their children) with it.
     */
    @Override
    @CommonTx
    public ChatMessage delete(ChatMessage message, boolean hard) throws Exception {
        if (message != null) {
            agentRunService.checkNotLocked(message.getParentScript());
        }
        if (!hard && message != null && message.getId() != null) {
            List<Long> deleted = getRepository().findDescendantIds(message);
            getRepository().softDeleteByIds(deleted);
            deleted.add(message.getId());
            summaryService.softDeleteForMessages(deleted);
        }
        return super.delete(message, hard);
    }

    @Override
    @CommonTx
    public void hardDeleteForManuscript(Manuscript manuscript) throws Exception {
        List<ChatMessage> messages = new ArrayList<>(getRepository().getAllMessagesIncludingDeleted(manuscript));
        // the deepest first, so that no part is deleted while another still follows it (a part inserted into the story is newer
        // than the parts below it, the age tells nothing)
        Map<Long, Integer> depths = new HashMap<>();
        Map<Long, ChatMessage> byId = new HashMap<>();
        messages.forEach(message -> byId.put(message.getId(), message));
        messages.sort(Comparator.comparingInt((ChatMessage message) -> depthOf(message, byId, depths)).reversed());
        for (ChatMessage message : messages) {
            Summary summary = message.getSummary() == null ? null : summaryService.find(message.getSummary());
            if (summary != null) {
                message.setSummary(null);
                saveWithoutEvent(message);
                summaryService.delete(summary, true);
            }
            delete(message, true);
        }
    }

    private static int depthOf(ChatMessage message, Map<Long, ChatMessage> byId, Map<Long, Integer> depths) {
        Integer known = depths.get(message.getId());
        if (known != null) {
            return known;
        }
        int depth = 0;
        for (ChatMessage above = message.getParent(); above != null && byId.containsKey(above.getId()); above = byId.get(above.getId()).getParent()) {
            depth++;
        }
        depths.put(message.getId(), depth);
        return depth;
    }

    @Override
    @CommonTx
    public void softDeleteForManuscript(Manuscript manuscript) throws Exception {
        summaryService.softDeleteForManuscript(manuscript);
        getRepository().softDeleteByManuscript(manuscript);
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


    @Override
    @CommonTx
    public PartEdit updateResponse(Manuscript manuscript, ChatMessage message, String text) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage stored = ownPart(book, message);
        String newText = text == null ? "" : text;

        // the summaries of the branch the part is on that are outdated already do not count as made so by this edit
        ChatMessage leaf = findLeafFor(book, stored);
        List<ChatMessage> newestFirst = leaf == null ? List.of() : new ArrayList<>(getBranchFromLeaf(leaf).reversed());
        Set<Long> outdatedBefore = outdatedHeads(newestFirst);

        ChatMessage saved = storeResponse(book, stored, newText);

        List<SummaryBlock> outdated = new ArrayList<>();
        for (SummaryBlock block : summaryService.collectBlocks(newestFirst)) {
            if (!block.isValid() && !outdatedBefore.contains(block.head().getId())) {
                outdated.add(block);
            }
        }
        Collections.reverse(outdated);
        return new PartEdit(saved, outdated);
    }

    @Override
    @CommonTx
    public ChatMessage storeResponse(Manuscript manuscript, ChatMessage message, String text) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage stored = ownPart(book, message);
        String newText = text == null ? "" : text;
        stored.setResponse(newText);
        stored.setTokenCount(countTokens(book, newText));
        stored.setWordCount(countWords(newText));
        stored.setEdited(true);
        return save(stored);
    }

    private long countTokens(Manuscript book, String text) throws Exception {
        InferenceService inferenceService = inferenceServices.forAI(book.getAi());
        return inferenceService == null ? new JavaTokkitStrategy().countTokens(null, text) : inferenceService.countTokens(text);
    }

    @Override
    @CommonTx
    public ChatMessage createPart(Manuscript manuscript, ChatMessage parent, String text, TurnInput input) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage above = parent == null ? null : ownPart(book, parent);
        if (above == null && hasAnyMessages(book)) {
            throw new IllegalArgumentException("The book has parts already, say after which part the new one comes");
        }
        ChatMessage created = newPart(book, text, input);
        ChatMessage saved = above == null ? createRoot(book, created) : addChild(above, created);
        book.setActiveLeaf(saved);
        manuscriptService.save(book);
        return saved;
    }

    @Override
    @CommonTx
    public ChatMessage insertPart(Manuscript manuscript, ChatMessage after, String text, TurnInput input) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage above = after == null ? null : ownPart(book, after);
        if (!hasAnyMessages(book)) {
            return createPart(book, null, text, input);
        }
        ChatMessage created = newPart(book, text, input);
        ChatMessage saved = above == null ? createRoot(book, created) : addChild(above, created);
        // what followed the part follows the new one now, the new part is saved first so that it is not moved under itself
        if (above == null) {
            getRepository().reparentRootsUnder(book, saved);
        } else {
            getRepository().reparentChildrenExcept(above, saved);
        }
        if (book.getActiveLeaf() == null || above != null && book.getActiveLeaf().getId().equals(above.getId())) {
            // the part was the end of the branch: the new one is the end now
            book.setActiveLeaf(saved);
            manuscriptService.save(book);
        }
        return saved;
    }

    @Override
    @CommonTx
    public int appendFromArchive(Manuscript manuscript, byte[] archive) throws Exception {
        MarkdownArchive.Content content = MarkdownArchive.read(archive);
        Manuscript book = ownBook(manuscript);
        ChatMessage last = book.getActiveLeaf() == null ? null : ownPart(book, book.getActiveLeaf());
        if (last == null && hasAnyMessages(book)) {
            throw new IllegalArgumentException("The book has parts but no active part to continue after");
        }
        for (MarkdownArchive.Item item : content.items()) {
            ChatMessage created = newPart(book, item.text(), null);
            last = last == null ? createRoot(book, created) : addChild(last, created);
        }
        book.setActiveLeaf(last);
        manuscriptService.save(book);
        return content.items().size();
    }

    /**
     * A part that is not stored yet: the text counted, the turn input it stands for.
     */
    private ChatMessage newPart(Manuscript book, String text, TurnInput input) throws Exception {
        String newText = text == null ? "" : text;
        ChatMessage created = new ChatMessage();
        created.setResponse(newText);
        created.setTokenCount(countTokens(book, newText));
        created.setWordCount(countWords(newText));
        if (input != null) {
            created.setSceneSetting(input.sceneSetting());
            created.setPovCharacter(input.povCharacter());
            created.setPresentCharacters(input.presentCharacters());
            created.setInstructions(input.instructions());
        }
        return created;
    }

    private Set<Long> outdatedHeads(List<ChatMessage> newestFirst) throws Exception {
        Set<Long> heads = new HashSet<>();
        for (SummaryBlock block : summaryService.collectBlocks(newestFirst)) {
            if (!block.isValid()) {
                heads.add(block.head().getId());
            }
        }
        return heads;
    }

}
