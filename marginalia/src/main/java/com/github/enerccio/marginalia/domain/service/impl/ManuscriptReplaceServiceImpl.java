package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.SummaryService.BlockTexts;
import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
import com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer.JavaTokkitStrategy;
import com.github.enerccio.marginalia.domain.service.search.replace.EntityTextSearch;
import com.github.enerccio.marginalia.domain.service.search.replace.EntityTextSearch.FieldChange;
import com.github.enerccio.marginalia.domain.service.search.replace.TaskProgress;
import com.github.enerccio.marginalia.domain.service.search.replace.TextHit;
import com.github.enerccio.marginalia.domain.service.search.replace.TextSearch;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.utils.ReflectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.ToLongFunction;

public class ManuscriptReplaceServiceImpl implements ManuscriptReplaceService {

    private static final String RESPONSE_FIELD = "response";
    private static final String SUMMARY_FIELD = "summary";
    private static final String REPLACED_SUMMARY_FIELD = "replacedSummary";

    private final ExtendableEntityListener extendableListener = new ExtendableEntityListener();

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    /**
     * A part or a summary to change.
     */
    private record EntityChange(Kind kind, Long id, List<FieldChange> fields) {

        int hits() {
            return fields.stream().mapToInt(f -> f.hits().size()).sum();
        }
    }

    private record PlanImpl(Long manuscriptId, TextSearch search, List<Item> items, List<EntityChange> changes,
                            Map<Long, String> summaryHashes, int outdatedSummaries) implements Plan {

        @Override
        public int hitCount() {
            return items.stream().mapToInt(item -> item.hits().size()).sum();
        }
    }

    @Override
    @CommonTxReadOnly
    public Plan plan(Manuscript manuscript, Options options, TaskProgress progress) throws Exception {
        Manuscript owned = owned(manuscript);
        TextSearch search = TextSearch.of(options.search(), options.replacement(), options.regex(), options.caseSensitive());

        // the active branch, oldest first
        List<ChatMessage> branch = owned.getActiveLeaf() == null ? List.of()
                : chatMessageService.getBranchFromLeaf(owned.getActiveLeaf());
        Map<Long, Integer> branchOrder = new HashMap<>();
        for (int i = 0; i < branch.size(); i++) {
            branchOrder.put(branch.get(i).getId(), i);
        }
        Set<Long> scope = options.allBranches() ? null : branchOrder.keySet();

        // the database narrows the parts down by _fulltext, the fields of those are then searched
        List<ChatMessageService.MessageFulltext> messageRows = !options.story() ? List.<ChatMessageService.MessageFulltext>of()
                : chatMessageService.findFulltextsContaining(owned, search.literal()).stream()
                .filter(row -> scope == null || scope.contains(row.messageId()))
                .filter(row -> search.matches(row.fulltext()))
                .toList();
        progress.checkCancelled();
        // the summaries a meta summary stands in for are inside of it, the database can't tell which ones hold the text
        List<ChatMessageService.SummaryFulltext> summaryRows = !options.summaries() ? List.<ChatMessageService.SummaryFulltext>of()
                : chatMessageService.findSummaryFulltextsContaining(owned, null).stream()
                .filter(row -> scope == null || scope.contains(row.messageId()))
                .toList();
        progress.checkCancelled();

        long total = messageRows.size() + summaryRows.size();
        long done = 0;
        List<Sorted> found = new ArrayList<>();
        Map<Long, String> responseOverlay = new HashMap<>();
        Map<Long, String> summaryOverlay = new HashMap<>();

        for (ChatMessageService.MessageFulltext row : messageRows) {
            progress.checkCancelled();
            ChatMessage message = chatMessageService.find(row.messageId());
            List<FieldChange> fields = EntityTextSearch.search(message, search);
            if (!fields.isEmpty()) {
                found.add(new Sorted(message, 0, new EntityChange(Kind.MESSAGE, message.getId(), fields)));
                fields.stream().filter(f -> f.field().getName().equals(RESPONSE_FIELD))
                        .findFirst().ifPresent(f -> responseOverlay.put(message.getId(), f.newText()));
            }
            progress.update(++done, total);
        }
        for (ChatMessageService.SummaryFulltext row : summaryRows) {
            progress.checkCancelled();
            Summary summary = summaryService.find(row.summaryId());
            List<FieldChange> fields = new ArrayList<>(search.matches(row.fulltext()) ? EntityTextSearch.search(summary, search) : List.of());
            Nested replaced = searchReplaced(summary.getReplacedSummary(), search, null);
            if (replaced != null) {
                fields.add(new FieldChange(ReflectUtils.getField(Summary.class, REPLACED_SUMMARY_FIELD),
                        summary.getReplacedSummary(), replaced.text(), replaced.hits()));
            }
            if (!fields.isEmpty()) {
                found.add(new Sorted(chatMessageService.find(row.messageId()), 1, new EntityChange(Kind.SUMMARY, summary.getId(), fields)));
                fields.stream().filter(f -> f.field().getName().equals(SUMMARY_FIELD))
                        .findFirst().ifPresent(f -> summaryOverlay.put(summary.getId(), f.newText()));
            }
            progress.update(++done, total);
        }

        Comparator<Sorted> order = scope != null
                ? Comparator.comparing((Sorted s) -> branchOrder.get(s.message().getId()))
                : Comparator.comparing((Sorted s) -> s.message().getCreation(), Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(s -> s.message().getId());
        found.sort(order.thenComparingInt(Sorted::rank));

        List<EntityChange> changes = found.stream().map(Sorted::change).toList();
        List<Item> items = changes.stream().map(change -> new Item(change.kind(), change.id(),
                change.fields().stream().flatMap(f -> f.hits().stream()).toList())).toList();

        Map<Long, String> hashes = new LinkedHashMap<>();
        int outdated = 0;
        if (!changes.isEmpty()) {
            outdated = planHashes(owned, scope, options, responseOverlay, summaryOverlay, hashes, progress);
        }
        return new PlanImpl(owned.getId(), search, items, changes, hashes, outdated);
    }

    /**
     * The text of a summary a meta summary stands in for, with the matches replaced.
     *
     * @param text serialized summary, with the summaries it replaced inside
     * @param hits the matches in all of them
     */
    private record Nested(String text, List<TextHit> hits) {
    }

    /**
     * Searches the summaries inside a meta summary: the one it replaced on its part, and the one that one replaced
     * before and so on. They are not parts of the story, so the hash of no summary is made of them.
     *
     * @param serialized {@link Summary#getReplacedSummary()}
     * @param tokens     counts the tokens of the new text of a summary, null to leave the count as it is
     * @return null when there is no match in them
     */
    private Nested searchReplaced(String serialized, TextSearch search, ToLongFunction<String> tokens) throws Exception {
        if (StringUtils.isBlank(serialized)) {
            return null;
        }
        Summary replaced = new Summary();
        replaced.setExtendedContent(serialized.getBytes(StandardCharsets.UTF_8));
        extendableListener.deserialize(replaced);

        List<FieldChange> fields = EntityTextSearch.search(replaced, search);
        Nested deeper = searchReplaced(replaced.getReplacedSummary(), search, tokens);
        if (fields.isEmpty() && deeper == null) {
            return null;
        }
        List<TextHit> hits = new ArrayList<>(fields.stream().flatMap(f -> f.hits().stream()).toList());
        if (!fields.isEmpty()) {
            EntityTextSearch.apply(replaced, fields);
            if (tokens != null) {
                replaced.setSummaryTokens(tokens.applyAsLong(replaced.getSummary()));
            }
        }
        if (deeper != null) {
            replaced.setReplacedSummary(deeper.text());
            hits.addAll(deeper.hits());
        }
        extendableListener.serialize(replaced);
        return new Nested(new String(replaced.getExtendedContent(), StandardCharsets.UTF_8), hits);
    }

    private record Sorted(ChatMessage message, int rank, EntityChange change) {
    }

    /**
     * Works out the hash of every summary of the book with the changed texts. The summaries the replace looks into and
     * that were valid get the new hash, those it does not look into and that were valid are counted. When the
     * summaries are revalidated, every summary whose hash is not the one it should have gets it.
     *
     * @param scope ids of the parts the replace looks into, null for all
     * @return number of the summaries that become outdated
     */
    private int planHashes(Manuscript manuscript, Set<Long> scope, Options options, Map<Long, String> responseOverlay,
                           Map<Long, String> summaryOverlay, Map<Long, String> hashes, TaskProgress progress) throws Exception {
        Map<Long, ChatMessage> messages = new HashMap<>();
        List<ChatMessage> withSummary = new ArrayList<>();
        for (ChatMessage message : chatMessageService.getAllMessages(manuscript)) {
            messages.put(message.getId(), message);
            if (message.getSummary() != null) {
                withSummary.add(message);
            }
        }

        BlockTexts changed = new BlockTexts() {
            @Override
            public String response(ChatMessage message) {
                return responseOverlay.getOrDefault(message.getId(), message.getResponse());
            }

            @Override
            public String summary(Summary summary) {
                return summaryOverlay.getOrDefault(summary.getId(), summary.getSummary());
            }
        };

        int outdated = 0;
        long done = 0;
        for (ChatMessage head : withSummary) {
            progress.checkCancelled();
            progress.update(done++, withSummary.size());
            SummaryBlock current = summaryService.collectFirstBlock(branchOf(head, messages), BlockTexts.CURRENT);
            if (current == null) {
                continue;
            }
            SummaryBlock after = summaryService.collectFirstBlock(branchOf(head, messages), changed);
            if (after == null) {
                continue;
            }
            String stored = current.summary().getSummaryMessageHash();
            if (options.revalidateSummaries()) {
                if (!after.hash().equals(stored)) {
                    hashes.put(current.summary().getId(), after.hash());
                }
                continue;
            }
            if (!current.isValid()) {
                // already outdated, stays as it is
                continue;
            }
            if (after.hash().equals(current.hash())) {
                continue;
            }
            if (options.summaries() && (scope == null || scope.contains(head.getId()))) {
                hashes.put(current.summary().getId(), after.hash());
            } else {
                outdated++;
            }
        }
        progress.update(withSummary.size(), withSummary.size());
        return outdated;
    }

    /**
     * The part and its ancestors, newest first, as far as they are not deleted.
     */
    private static Iterator<ChatMessage> branchOf(ChatMessage head, Map<Long, ChatMessage> messages) {
        return new Iterator<>() {
            private ChatMessage next = head;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public ChatMessage next() {
                ChatMessage current = next;
                next = current.getParent() == null ? null : messages.get(current.getParent().getId());
                return current;
            }
        };
    }

    @Override
    @CommonTx
    public Result apply(Manuscript manuscript, Plan plan, TaskProgress progress) throws Exception {
        Manuscript owned = owned(manuscript);
        agentRunService.checkNotLocked(owned);
        PlanImpl planned = (PlanImpl) plan;
        if (!Objects.equals(planned.manuscriptId(), owned.getId())) {
            throw new IllegalArgumentException("The plan is of another book");
        }

        Map<Long, String> hashes = new LinkedHashMap<>(planned.summaryHashes());
        long total = planned.changes().size() + hashes.size();
        long done = 0;
        int changed = 0;
        int hits = 0;
        int skipped = 0;

        for (EntityChange change : planned.changes()) {
            progress.update(done++, total);
            if (change.kind() == Kind.MESSAGE) {
                // saving serializes the fields of the object it gets, a lazy proxy of the part has none
                ChatMessage message = Hibernate.unproxy(chatMessageService.find(change.id()), ChatMessage.class);
                if (!unchanged(message, change)) {
                    skipped++;
                    continue;
                }
                String responseBefore = message.getResponse();
                EntityTextSearch.apply(message, change.fields());
                String response = message.getResponse();
                message.setTokenCount(countTokens(owned, response));
                message.setWordCount(chatMessageService.countWords(response));
                message.setEdited(true);
                chatMessageService.save(message);
                // the passages of the annotations move with the text
                annotationService.shift(owned, message, responseBefore, response);
            } else {
                Summary summary = Hibernate.unproxy(summaryService.find(change.id()), Summary.class);
                if (!unchanged(summary, change)) {
                    skipped++;
                    hashes.remove(change.id());
                    continue;
                }
                List<FieldChange> own = change.fields().stream().filter(f -> !f.field().getName().equals(REPLACED_SUMMARY_FIELD)).toList();
                if (!own.isEmpty()) {
                    EntityTextSearch.apply(summary, own);
                    summary.setSummaryTokens(countTokens(owned, summary.getSummary()));
                }
                if (own.size() < change.fields().size()) {
                    // the tokens of the summaries inside are counted with the model of the book as well
                    Nested replaced = searchReplaced(summary.getReplacedSummary(), planned.search(), text -> countTokensUnchecked(owned, text));
                    if (replaced != null) {
                        summary.setReplacedSummary(replaced.text());
                    }
                }
                String hash = hashes.remove(change.id());
                if (hash != null) {
                    summary.setSummaryMessageHash(hash);
                    done++;
                }
                summaryService.save(summary);
            }
            changed++;
            hits += change.hits();
        }

        for (Map.Entry<Long, String> entry : hashes.entrySet()) {
            progress.update(done++, total);
            Summary summary = Hibernate.unproxy(summaryService.find(entry.getKey()), Summary.class);
            if (summary != null) {
                summary.setSummaryMessageHash(entry.getValue());
                summaryService.save(summary);
            }
        }
        progress.update(total, total);
        return new Result(changed, hits, skipped);
    }

    /**
     * The entity still has the texts the plan was made of: it was not deleted or edited since.
     */
    private static boolean unchanged(com.github.enerccio.marginalia.domain.model.ExtendableEntity entity, EntityChange change) throws IllegalAccessException {
        if (entity == null || entity.isDeleted()) {
            return false;
        }
        return EntityTextSearch.hasTexts(entity, change.fields());
    }

    private long countTokens(Manuscript manuscript, String text) throws Exception {
        AI ai = manuscript.getAi();
        InferenceService inferenceService = ai == null ? null : inferenceServices.forAI(ai);
        if (inferenceService == null) {
            return new JavaTokkitStrategy().countTokens(null, text);
        }
        return inferenceService.countTokens(text);
    }

    private long countTokensUnchecked(Manuscript manuscript, String text) {
        try {
            return countTokens(manuscript, text);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Manuscript owned(Manuscript manuscript) throws Exception {
        Manuscript owned = manuscript == null ? null : manuscriptService.findForUser(manuscript.getUuid());
        if (owned == null) {
            throw new IllegalArgumentException("The book is not available");
        }
        return owned;
    }

}
