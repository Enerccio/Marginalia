package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.components.BranchTokens;
import com.github.enerccio.marginalia.domain.components.PartMerge;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.ImageAttachment;
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
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
    private AnnotationService annotationService;

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    @Autowired
    @Lazy
    private InferenceServices inferenceServices;

    @Autowired
    @Qualifier("common")
    private PlatformTransactionManager transactionManager;

    @FunctionalInterface
    private interface Work<T> {
        T run() throws Exception;
    }

    private static final class WorkFailure extends RuntimeException {
        private final Exception cause;

        private WorkFailure(Exception cause) {
            super(cause);
            this.cause = cause;
        }
    }

    /**
     * Runs the work in a transaction, joining the one of the caller. Counting the tokens may be a call to the model server
     * (which can be loading the model first), and a read-write transaction holds the database write lock from its begin, so
     * the methods count first with {@link #counted} and write in here, never the other way round.
     */
    private <T> T inTx(boolean readOnly, Work<T> work) throws Exception {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setReadOnly(readOnly);
        try {
            return template.execute(status -> {
                try {
                    return work.run();
                } catch (Exception e) {
                    throw new WorkFailure(e);
                }
            });
        } catch (WorkFailure failure) {
            throw failure.cause;
        }
    }

    /**
     * The tokens of the text as the AI of the book counts them, in a transaction that does not write.
     */
    private long counted(Manuscript manuscript, String text) throws Exception {
        return inTx(true, () -> countTokens(ownBook(manuscript), text == null ? "" : text));
    }

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
        ChatMessage saved = save(clone);
        annotationService.copyForBranch(branched, saved);
        return saved;
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
    public Set<Long> findManuscriptsWithText(Collection<Long> manuscriptIds, String query, boolean allBranches) throws Exception {
        FulltextQuery parsed = FulltextQuery.parse(query);
        if (parsed.isEmpty() || manuscriptIds == null) {
            return Collections.emptySet();
        }
        Set<Long> own = new HashSet<>();
        for (Manuscript book : manuscriptService.findAllForUser()) {
            if (manuscriptIds.contains(book.getId())) {
                own.add(book.getId());
            }
        }

        Map<Long, Set<Long>> hits = new HashMap<>();
        for (Object[] row : getRepository().findFulltextHits(own, parsed.likePatterns())) {
            hits.computeIfAbsent((Long) row[1], k -> new HashSet<>()).add((Long) row[0]);
        }
        if (allBranches) {
            return new HashSet<>(hits.keySet());
        }
        Set<Long> found = new HashSet<>();
        hits.forEach((book, parts) -> {
            try {
                if (!Collections.disjoint(parts, getRepository().findActiveBranchIds(book))) {
                    found.add(book);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        return found;
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

    @Override
    @CommonTxReadOnly
    public BranchTokens getBranchTokens(ChatMessage leaf) throws Exception {
        if (leaf == null || leaf.getId() == null) {
            return new BranchTokens(0, 0, 0, 0, 0, 0, 0);
        }
        List<ChatMessage> branch = getBranchFromLeaf(leaf);
        List<SummaryService.SummaryBlock> blocks = summaryService.collectBlocks(new ArrayList<>(branch.reversed()));

        int summaries = 0;
        long summaryTokens = 0;
        int stale = 0;
        Long newestSummarized = null;
        // newest first: the first valid block ends the summarized part
        for (SummaryService.SummaryBlock block : blocks) {
            if (!block.isValid()) {
                stale++;
                continue;
            }
            summaries++;
            summaryTokens += summaryTokens(block.summary());
            if (newestSummarized == null) {
                newestSummarized = block.head().getId();
            }
        }

        int parts = 0;
        long partTokens = 0;
        int summarizedParts = 0;
        long summarizedTokens = 0;
        // the branch goes from the root, the parts up to the head of the newest valid summary are summarized
        boolean summarized = newestSummarized != null;
        for (ChatMessage message : branch) {
            if (StringUtils.isNotBlank(message.getResponse())) {
                parts++;
                partTokens += message.getTokenCount();
                if (summarized) {
                    summarizedParts++;
                    summarizedTokens += message.getTokenCount();
                }
            }
            if (summarized && message.getId().equals(newestSummarized)) {
                summarized = false;
            }
        }
        return new BranchTokens(parts, partTokens, summaries, summaryTokens, summarizedParts, summarizedTokens, stale);
    }

    private static long summaryTokens(Summary summary) {
        if (summary.getSummaryTokens() != null && summary.getSummaryTokens() > 0) {
            return summary.getSummaryTokens();
        }
        // not counted yet: about four characters to a token
        return StringUtils.length(summary.getSummary()) / 4;
    }

    /**
     * A soft deleted message takes its summary, its annotations and everything below it (its children and their children) with
     * it. A message deleted for good takes its annotations.
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
            annotationService.softDeleteForParts(deleted);
        } else if (hard && message != null && message.getId() != null) {
            annotationService.hardDeleteForParts(List.of(message.getId()));
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
        annotationService.softDeleteForManuscript(manuscript);
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
    public PartEdit updateResponse(Manuscript manuscript, ChatMessage message, String text) throws Exception {
        long tokens = counted(manuscript, text);
        return inTx(false, () -> updateResponse(manuscript, message, text, tokens));
    }

    private PartEdit updateResponse(Manuscript manuscript, ChatMessage message, String text, long tokens) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage stored = ownPart(book, message);
        String newText = text == null ? "" : text;

        // the summaries of the branch the part is on that are outdated already do not count as made so by this edit
        ChatMessage leaf = findLeafFor(book, stored);
        List<ChatMessage> newestFirst = leaf == null ? List.of() : new ArrayList<>(getBranchFromLeaf(leaf).reversed());
        Set<Long> outdatedBefore = outdatedHeads(newestFirst);

        ChatMessage saved = storeResponse(book, stored, newText, tokens);

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
    public ChatMessage storeResponse(Manuscript manuscript, ChatMessage message, String text) throws Exception {
        long tokens = counted(manuscript, text);
        return inTx(false, () -> storeResponse(manuscript, message, text, tokens));
    }

    private ChatMessage storeResponse(Manuscript manuscript, ChatMessage message, String text, long tokens) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage stored = ownPart(book, message);
        String newText = text == null ? "" : text;
        String oldText = stored.getResponse();
        stored.setResponse(newText);
        stored.setTokenCount(tokens);
        stored.setWordCount(countWords(newText));
        stored.setEdited(true);
        ChatMessage saved = save(stored);
        // the passages of the annotations move with the text
        annotationService.shift(book, saved, oldText, newText);
        return saved;
    }

    @Override
    @CommonTxReadOnly
    public MergePreview previewMergeIntoPrevious(Manuscript manuscript, ChatMessage part) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage later = ownPart(book, part);
        ChatMessage previous = mergeTarget(book, later);
        TurnInput input = new TurnInput(
                PartMerge.combineText(previous.getSceneSetting(), later.getSceneSetting()),
                PartMerge.combineText(previous.getPovCharacter(), later.getPovCharacter()),
                PartMerge.combineText(previous.getPresentCharacters(), later.getPresentCharacters()),
                PartMerge.combineText(previous.getInstructions(), later.getInstructions()));
        return new MergePreview(previous, later, input, PartMerge.Join.suggest(previous.getResponse(), later.getResponse()),
                PartMerge.diffAttributes(previous.getAttributes(), later.getAttributes()));
    }

    @Override
    @CommonTx
    public ChatMessage mergeIntoPrevious(Manuscript manuscript, ChatMessage part, TurnInput input, PartMerge.Join join,
                                         Set<List<String>> fromPrevious, Set<List<String>> dropped) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage later = ownPart(book, part);
        ChatMessage previous = mergeTarget(book, later);
        PartMerge.Join joined = join == null ? PartMerge.Join.suggest(previous.getResponse(), later.getResponse()) : join;

        String text = PartMerge.join(previous.getResponse(), later.getResponse(), joined);
        // where the text of the later part starts in the joined one
        int laterOffset = StringUtils.isBlank(previous.getResponse()) ? 0 : previous.getResponse().length() + joined.separator().length();
        if (previous.getOriginalResponse() != null || later.getOriginalResponse() != null) {
            // the part that was not generated has no original text: its text is what the author added to the generated one
            previous.setOriginalResponse(PartMerge.join(previous.getOriginalResponse(), later.getOriginalResponse(), joined));
        }
        previous.setResponse(text);
        previous.setTokenCount(countTokens(book, text));
        previous.setWordCount(countWords(text));
        previous.setEdited(previous.isEdited() || later.isEdited());
        previous.setResponseReasoning(blankToNull(PartMerge.join(previous.getResponseReasoning(), later.getResponseReasoning(), PartMerge.Join.PARAGRAPH)));
        if (previous.getTokenReasoningCount() != null || later.getTokenReasoningCount() != null) {
            previous.setTokenReasoningCount((previous.getTokenReasoningCount() == null ? 0 : previous.getTokenReasoningCount())
                    + (later.getTokenReasoningCount() == null ? 0 : later.getTokenReasoningCount()));
        }
        List<ImageAttachment> images = previous.getImages();
        images.addAll(later.getImages());
        previous.setImages(images);
        if (input != null) {
            previous.setSceneSetting(input.sceneSetting());
            previous.setPovCharacter(input.povCharacter());
            previous.setPresentCharacters(input.presentCharacters());
            previous.setInstructions(input.instructions());
        }
        previous.setAttributes(PartMerge.mergeAttributes(previous.getAttributes(), later.getAttributes(),
                fromPrevious == null ? Set.of() : fromPrevious, dropped == null ? Set.of() : dropped));
        ChatMessage saved = save(previous);
        annotationService.moveToPrevious(book, later, saved, laterOffset);

        // what follows the part follows the previous one now, the active leaf moves to it when the part was the leaf
        deleteNodeAndMigrateChildren(later, book, false);
        return saved;
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text;
    }

    /**
     * The part {@code later} is merged into.
     */
    private ChatMessage mergeTarget(Manuscript book, ChatMessage later) throws Exception {
        if (later.getParent() == null) {
            throw new IllegalStateException("The first part of the book has no part before it to be merged into");
        }
        ChatMessage previous = ownPart(book, later.getParent());
        for (ChatMessage child : getRepository().findChildren(previous)) {
            if (!child.getId().equals(later.getId())) {
                throw new IllegalStateException("The part before has other continuations (swipes of the part), delete them or make them a branch first");
            }
        }
        return previous;
    }

    private long countTokens(Manuscript book, String text) throws Exception {
        InferenceService inferenceService = inferenceServices.forAI(book.getAi());
        return inferenceService == null ? new JavaTokkitStrategy().countTokens(null, text) : inferenceService.countTokens(text);
    }

    @Override
    public ChatMessage createPart(Manuscript manuscript, ChatMessage parent, String text, TurnInput input) throws Exception {
        long tokens = counted(manuscript, text);
        return inTx(false, () -> createPart(manuscript, parent, text, input, tokens));
    }

    private ChatMessage createPart(Manuscript manuscript, ChatMessage parent, String text, TurnInput input, long tokens) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage above = parent == null ? null : ownPart(book, parent);
        if (above == null && hasAnyMessages(book)) {
            throw new IllegalArgumentException("The book has parts already, say after which part the new one comes");
        }
        ChatMessage created = newPart(text, input, tokens);
        ChatMessage saved = above == null ? createRoot(book, created) : addChild(above, created);
        book.setActiveLeaf(saved);
        manuscriptService.save(book);
        return saved;
    }

    @Override
    public ChatMessage addPart(Manuscript manuscript, ChatMessage parent, ChatMessage part) throws Exception {
        long tokens = counted(manuscript, part.getResponse());
        return inTx(false, () -> addPart(manuscript, parent, part, tokens));
    }

    private ChatMessage addPart(Manuscript manuscript, ChatMessage parent, ChatMessage part, long tokens) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage above = parent == null ? null : ownPart(book, parent);
        String text = part.getResponse() == null ? "" : part.getResponse();
        part.setResponse(text);
        part.setTokenCount(tokens);
        part.setWordCount(countWords(text));
        return above == null ? createRoot(book, part) : addChild(above, part);
    }

    @Override
    public ChatMessage insertPart(Manuscript manuscript, ChatMessage after, String text, TurnInput input) throws Exception {
        long tokens = counted(manuscript, text);
        return inTx(false, () -> insertPart(manuscript, after, text, input, tokens));
    }

    private ChatMessage insertPart(Manuscript manuscript, ChatMessage after, String text, TurnInput input, long tokens) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage above = after == null ? null : ownPart(book, after);
        if (!hasAnyMessages(book)) {
            return createPart(book, null, text, input, tokens);
        }
        ChatMessage created = newPart(text, input, tokens);
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

    /**
     * A part that is not stored yet: the text counted, the turn input it stands for.
     */
    private ChatMessage newPart(String text, TurnInput input, long tokens) {
        String newText = text == null ? "" : text;
        ChatMessage created = new ChatMessage();
        created.setResponse(newText);
        created.setTokenCount(tokens);
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
