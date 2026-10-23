package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.*;
import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.agent.*;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.*;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonMerge;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonText;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AgentMergeServiceImpl implements AgentMergeService {

    private static final Logger log = LoggerFactory.getLogger(AgentMergeServiceImpl.class);

    private static final Set<AgentRunStatus> REVIEWABLE = Set.of(AgentRunStatus.DONE, AgentRunStatus.STOPPED_KEPT);

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    @Autowired
    @Lazy
    private AgentRunPairService pairService;

    @Autowired
    @Lazy
    private AgentVirtualFileSystemService fileSystemService;

    @Autowired
    @Lazy
    private ManuscriptService manuscriptService;

    @Autowired
    @Lazy
    private ChatMessageService chatMessageService;

    @Autowired
    @Lazy
    private SummaryService summaryService;

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    @Lazy
    private LorebookService lorebookService;

    @Autowired
    @Lazy
    private LorebookEntryService lorebookEntryService;

    @Autowired
    @Lazy
    private TagService tagService;

    @Autowired
    @Lazy
    private TagRelationService tagRelationService;

    @Autowired
    @Lazy
    private BackupService backupService;

    @Autowired
    @Lazy
    private CheckpointService checkpointService;

    @Autowired
    @Qualifier("common")
    private PlatformTransactionManager transactionManager;

    /**
     * The run, its copy and the book, checked.
     */
    private record Review(AgentRun run, Manuscript copy, Manuscript original, JsonObject base, Map<String, String> copyToOriginal) {
    }

    private Review open(AgentRun run) throws Exception {
        AgentRun stored = run == null ? null : agentRunService.findForUser(run.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        if (!REVIEWABLE.contains(stored.getStatus()) || stored.getWorkingCopy() == null) {
            throw new IllegalStateException("A run that is " + stored.getStatus() + " has no result to take over");
        }
        Manuscript book = stored.getAgent() == null ? null : stored.getAgent().getManuscript();
        Manuscript original = book == null ? null : (Manuscript) Hibernate.unproxy(manuscriptService.findForUser(book.getUuid()));
        if (original == null || original.isDeleted()) {
            throw new IllegalArgumentException("The book of the agent does not exist");
        }
        JsonObject base = fileSystemService.readBase(stored);
        if (base == null) {
            throw new IllegalStateException("The run has no record of the book as it was when it started");
        }
        Map<String, String> pairs = new HashMap<>();
        for (UuidPair pair : pairService.getAll(stored)) {
            if (pair.originalUuid() != null) {
                pairs.put(pair.copyUuid(), pair.originalUuid());
            }
        }
        return new Review(stored, (Manuscript) Hibernate.unproxy(stored.getWorkingCopy()), original, base, pairs);
    }

    private JsonObject snapshotOfBook(Manuscript book) throws Exception {
        JsonObject snapshot = manuscriptService.createBackup(book);
        Lorebook lorebook = book.getLorebook() == null ? null : lorebookService.find(book.getLorebook());
        if (lorebook != null) {
            snapshot.add("lorebooks", lorebookService.marshalLorebooks(lorebook, true));
        }
        return snapshot;
    }

    @Override
    @CommonTxReadOnly
    public MergePlan plan(AgentRun run) throws Exception {
        return plan(open(run));
    }

    private MergePlan plan(Review review) throws Exception {
        MergePlan plan = MergeDiff.compute(review.base(), manuscriptService.snapshotAgenticCopy(review.copy()),
                snapshotOfBook(review.original()), review.copyToOriginal());
        Map<String, List<String>> shared = sharedLorebooks(review.original());
        if (shared.isEmpty()) {
            return plan;
        }
        List<Item> items = new ArrayList<>();
        for (Item item : plan.items()) {
            items.add(item.withShared(sharedWith(item, shared)));
        }
        return new MergePlan(items, plan.names(), plan.bookChanged());
    }

    /**
     * The books that use a lorebook of the book besides it, by the uuid of the lorebook.
     */
    private Map<String, List<String>> sharedLorebooks(Manuscript original) throws Exception {
        Map<String, List<String>> shared = new HashMap<>();
        for (Manuscript other : manuscriptService.findAllRegularForUser()) {
            if (other.getId().equals(original.getId()) || other.getLorebook() == null) {
                continue;
            }
            for (Lorebook lorebook : tree(other.getLorebook())) {
                shared.computeIfAbsent(lorebook.getUuid(), uuid -> new ArrayList<>()).add(other.getName());
            }
        }
        return shared;
    }

    private List<String> sharedWith(Item item, Map<String, List<String>> shared) throws Exception {
        String lorebook = switch (item.kind()) {
            case LOREBOOK -> item.targetUuid();
            case ENTRY -> {
                LorebookEntry entry = item.targetUuid() == null ? null : lorebookEntryService.findForUser(item.targetUuid());
                yield entry != null ? entry.getLorebook().getUuid() : item.fields().stream()
                        .filter(field -> field.name().equals("lorebook")).map(Field::agent).findFirst().orElse(null);
            }
            default -> null;
        };
        return lorebook == null ? List.of() : shared.getOrDefault(lorebook, List.of());
    }

    private List<Lorebook> tree(Lorebook root) throws Exception {
        List<Lorebook> books = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        Deque<Lorebook> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Lorebook book = queue.remove();
            if (book == null || !seen.add(book.getId())) {
                continue;
            }
            books.add(book);
            queue.addAll(lorebookService.getSubbooks(book));
        }
        return books;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // apply
    // ---------------------------------------------------------------------------------------------------------------

    @Override
    public MergeResult apply(AgentRun run, MergeSelection selection) throws Exception {
        // the changes are one transaction, all or nothing; deleting the copy (a book of its own, as big as the book) is another,
        // so the database is not locked for both of them at once. When that one fails the changes are in the book and the run
        // still waits, discarding it ends it.
        Applied applied;
        try {
            applied = new TransactionTemplate(transactionManager).execute(status -> {
                try {
                    return applyChanges(run, selection);
                } catch (Exception e) {
                    throw new ApplyFailure(e);
                }
            });
        } catch (ApplyFailure failure) {
            throw failure.cause;
        }
        AgentRun ended = agentRunService.transition(applied.run(), AgentRunStatus.APPLIED, null);
        log.info("Agent run {}: {} changes applied, {} skipped", ended.getUuid(), applied.counts().values().stream()
                .mapToInt(Integer::intValue).sum(), applied.skipped().size());
        return new MergeResult(ended, applied.counts(), applied.skipped(), applied.backup());
    }

    private static final class ApplyFailure extends RuntimeException {
        private final Exception cause;

        ApplyFailure(Exception cause) {
            super(cause);
            this.cause = cause;
        }
    }

    private record Applied(AgentRun run, Map<Kind, Integer> counts, List<Skipped> skipped, String backup) {
    }

    /**
     * The items without the fields the user did not pick; an item the agent changed that has none left is dropped. The place of
     * a part is not a choice, and what the agent made or deleted is taken or left as a whole.
     */
    private static List<Item> withPickedFields(List<Item> chosen, Map<String, Set<String>> skippedFields) {
        if (skippedFields.isEmpty()) {
            return chosen;
        }
        List<Item> picked = new ArrayList<>();
        for (Item item : chosen) {
            Set<String> skippedOfItem = skippedFields.get(item.id());
            if (item.change() != Change.CHANGED || skippedOfItem == null || skippedOfItem.isEmpty()) {
                picked.add(item);
                continue;
            }
            List<Field> fields = item.fields().stream()
                    .filter(field -> MergeDiff.PARENT.equals(field.name()) || !skippedOfItem.contains(field.name())).toList();
            if (fields.stream().anyMatch(field -> !MergeDiff.PARENT.equals(field.name()))) {
                picked.add(new Item(item.id(), item.kind(), item.change(), item.sourceUuid(), item.targetUuid(), item.label(),
                        item.dependsOn(), fields, item.conflict(), item.problem(), item.sharedWith()));
            }
        }
        return picked;
    }

    private Applied applyChanges(AgentRun run, MergeSelection selection) throws Exception {
        Review review = open(run);
        if (selection == null || selection.items().isEmpty()) {
            throw new IllegalArgumentException("Nothing is picked");
        }
        MergePlan plan = plan(review);
        List<Skipped> skipped = new ArrayList<>();
        List<Item> chosen = withPickedFields(applicable(plan, selection.items(), skipped), selection.skippedFields());
        if (chosen.isEmpty()) {
            throw new IllegalArgumentException("Nothing that is picked can be applied");
        }
        // the book as it is now, before anything is changed in it (lorebooks and the story are in it)
        BackupService.ManuscriptBackup backup = backupService.takeBackup(review.original());
        Direction direction = new Direction(review.copy(), review.original(), review.copyToOriginal(),
                key -> key.startsWith(MergeDiff.NEW) ? null : key);
        Outcome outcome = new Applier(direction, chosen, selection.edits(), selection.switchToAgentBranch(), skipped).apply();
        return new Applied(review.run(), outcome.counts(), outcome.skipped(), backup == null ? null : backup.getFile());
    }

    // ---------------------------------------------------------------------------------------------------------------
    // sync
    // ---------------------------------------------------------------------------------------------------------------

    @Override
    @CommonTx
    public SyncReport sync(AgentRun run) throws Exception {
        AgentRun stored = run == null ? null : agentRunService.findForUser(run.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        if (!stored.getStatus().isResumable() || stored.getWorkingCopy() == null) {
            throw new IllegalStateException("A run that is " + stored.getStatus() + " has no copy to bring the book into");
        }
        Manuscript bookRef = stored.getAgent() == null ? null : stored.getAgent().getManuscript();
        Manuscript original = bookRef == null ? null : (Manuscript) Hibernate.unproxy(manuscriptService.findForUser(bookRef.getUuid()));
        if (original == null || original.isDeleted()) {
            throw new IllegalArgumentException("The book of the agent does not exist");
        }
        JsonObject base = fileSystemService.readBase(stored);
        if (base == null) {
            return SyncReport.EMPTY;
        }
        Manuscript copy = (Manuscript) Hibernate.unproxy(stored.getWorkingCopy());
        Map<String, String> copyToOriginal = new HashMap<>();
        Map<String, String> originalToCopy = new HashMap<>();
        for (UuidPair pair : pairService.getAll(stored)) {
            if (pair.originalUuid() != null) {
                copyToOriginal.put(pair.copyUuid(), pair.originalUuid());
                originalToCopy.putIfAbsent(pair.originalUuid(), pair.copyUuid());
            }
        }
        JsonObject bookSnapshot = snapshotOfBook(original);
        MergePlan plan = MergeDiff.computeSync(base, bookSnapshot, manuscriptService.snapshotAgenticCopy(copy), copyToOriginal);

        // what the copy can take over: the fields the agent did not change too; the rest is told as a conflict
        List<SyncReport.Line> conflicts = new ArrayList<>();
        List<Item> clean = new ArrayList<>();
        for (Item item : plan.items()) {
            if (!item.applicable()) {
                conflicts.add(conflictLine(stored, item, item.change() == Change.CHANGED ? SyncReport.Reason.YOU_DELETED : SyncReport.Reason.NOT_ADDED, List.of()));
            } else if (item.change() == Change.DELETED) {
                if (item.conflict()) {
                    conflicts.add(conflictLine(stored, item, SyncReport.Reason.USER_DELETED, List.of()));
                } else {
                    clean.add(item);
                }
            } else if (item.change() == Change.CHANGED && item.conflict()) {
                List<String> both = item.fields().stream().filter(Field::conflict).map(Field::name).toList();
                conflicts.add(conflictLine(stored, item, SyncReport.Reason.BOTH_CHANGED, both));
                List<Field> fields = item.fields().stream().filter(field -> !field.conflict()).toList();
                if (!fields.isEmpty()) {
                    clean.add(new Item(item.id(), item.kind(), item.change(), item.sourceUuid(), item.targetUuid(), item.label(),
                            item.dependsOn(), fields, false, null, List.of()));
                }
            } else {
                clean.add(item);
            }
        }
        MergePlan cleanPlan = new MergePlan(clean, plan.names(), plan.bookChanged());
        List<Skipped> skipped = new ArrayList<>();
        List<Item> chosen = applicable(cleanPlan, clean.stream().map(Item::id).collect(Collectors.toSet()), skipped);
        for (Skipped notTaken : skipped) {
            for (Item item : clean) {
                if (item.id().equals(notTaken.id())) {
                    conflicts.add(conflictLine(stored, item, SyncReport.Reason.NOT_ADDED, List.of()));
                }
            }
        }

        List<SyncReport.Line> taken = new ArrayList<>();
        if (!chosen.isEmpty()) {
            try {
                checkpointService.makeAutomatic(stored, "before the changes of the book");
            } catch (Exception e) {
                log.warn("Agent run {}: no checkpoint before the sync: {}", stored.getUuid(), e.getMessage());
            }
            Direction direction = new Direction(original, copy, originalToCopy, originalToCopy::get);
            Outcome outcome = new Applier(direction, chosen, Map.of(), false, new ArrayList<>()).apply();
            List<UuidPair> pairs = new ArrayList<>(pairService.getAll(stored));
            Map<String, String> madeFrom = new HashMap<>();
            for (Created made : outcome.created()) {
                pairs.add(new UuidPair(made.kind(), made.targetUuid(), made.sourceUuid()));
                madeFrom.put(made.sourceUuid(), made.targetUuid());
            }
            pairService.replace(stored, pairs);
            BaseAdvance.advance(base, bookSnapshot, chosen);
            fileSystemService.updateBase(stored, base);
            for (Item item : chosen) {
                String copyUuid = item.change() == Change.ADDED ? madeFrom.get(item.sourceUuid()) : item.targetUuid();
                taken.add(new SyncReport.Line(item.kind(), item.change(), handle(stored, item, copyUuid), item.label(),
                        item.fields().stream().map(Field::name).toList(), null));
            }
        }
        SyncReport report = new SyncReport(taken, conflicts);
        fileSystemService.writeSyncReport(stored, report.isEmpty() ? null : report.text());
        if (!report.isEmpty()) {
            log.info("Agent run {}: the sync took over {} change(s) of the book, {} conflict(s)", stored.getUuid(), taken.size(), conflicts.size());
        }
        return report;
    }

    private SyncReport.Line conflictLine(AgentRun run, Item item, SyncReport.Reason reason, List<String> fields) throws Exception {
        return new SyncReport.Line(item.kind(), item.change(), handle(run, item, item.targetUuid()), item.label(), fields, reason);
    }

    /**
     * The handle of the item in the copy, given when it has none; none for the settings of the book and for an item that
     * is not in the copy (a handle it had before is kept for it).
     */
    private String handle(AgentRun run, Item item, String copyUuid) throws Exception {
        AgentRunPairKind kind = switch (item.kind()) {
            case MESSAGE -> AgentRunPairKind.MESSAGE;
            case LOREBOOK -> AgentRunPairKind.LOREBOOK;
            case ENTRY -> AgentRunPairKind.ENTRY;
            case BOOK -> null;
        };
        if (kind == null || copyUuid == null) {
            return null;
        }
        if (item.change() == Change.DELETED && item.conflict() == false || item.problem() != null && item.change() == Change.CHANGED) {
            // gone from the copy: only a handle it already had
            return pairService.findHandle(run, copyUuid).orElse(null);
        }
        return pairService.handleFor(run, kind, copyUuid);
    }

    /**
     * What a change is applied to and from: the review applies the working copy to the book, the sync of a resume applies the
     * book to the working copy.
     *
     * @param source        the book the changes come from
     * @param target        the book that takes them over
     * @param toTarget      the uuid of an item of the source to the uuid of its item in the target (the pairs, in the direction needed)
     * @param keyToTarget   the uuid in the target of the item a key of the plan names; null for an item that is not in the target
     */
    private record Direction(Manuscript source, Manuscript target, Map<String, String> toTarget, Function<String, String> keyToTarget) {
    }

    /**
     * An item the apply made in the target.
     */
    private record Created(AgentRunPairKind kind, String sourceUuid, String targetUuid) {
    }

    private record Outcome(Map<Kind, Integer> counts, List<Skipped> skipped, List<Created> created) {
    }

    /**
     * The items of the plan that can be applied, from the ones that were picked: the ones that can't (they need something
     * the target no longer has) and the ones that need an item that is not picked are told to {@code skipped}.
     */
    private static List<Item> applicable(MergePlan plan, Set<String> picked, List<Skipped> skipped) {
        List<Item> chosen = new ArrayList<>();
        Set<String> willApply = new HashSet<>();
        for (Item item : plan.items()) {
            if (!picked.contains(item.id())) {
                continue;
            }
            if (!item.applicable()) {
                skipped.add(new Skipped(item.id(), item.label(), item.problem()));
            } else if (item.dependsOn() != null && !willApply.contains(item.dependsOn())) {
                // the plan lists what an item needs before it
                skipped.add(new Skipped(item.id(), item.label(), "needs " + item.dependsOn()));
            } else {
                chosen.add(item);
                willApply.add(item.id());
            }
        }
        return chosen;
    }

    /**
     * One apply: the state it builds up while it goes through the items it is given.
     */
    private final class Applier {
        private final Direction direction;
        private final List<Item> chosen;
        private final Map<String, Map<String, String>> edits;
        private final boolean switchToSourceBranch;
        private Manuscript targetBook;
        private final Manuscript sourceBook;

        // the pairs of the run, and what the apply made: the uuid in the source to the uuid of the item in the target
        private final Map<String, String> toTarget;
        // the lorebooks the apply made, by the key of their item in the plan
        private final Map<String, Lorebook> createdLorebooks = new HashMap<>();
        // the summaries written to the target by the uuid of the summary of the source they were made from
        private final Map<String, Summary> writtenSummaries = new HashMap<>();

        private final Set<String> applied = new LinkedHashSet<>();
        private final Map<Kind, Integer> counts = new EnumMap<>(Kind.class);
        private final List<Skipped> skipped;
        private final List<Created> created = new ArrayList<>();

        Applier(Direction direction, List<Item> chosen, Map<String, Map<String, String>> edits, boolean switchToSourceBranch,
                List<Skipped> skipped) {
            this.direction = direction;
            this.chosen = chosen;
            this.edits = edits;
            this.switchToSourceBranch = switchToSourceBranch;
            this.skipped = skipped;
            this.sourceBook = direction.source();
            this.targetBook = direction.target();
            this.toTarget = new HashMap<>(direction.toTarget());
        }

        Outcome apply() throws Exception {
            applyBook(chosen);
            each(chosen, Kind.LOREBOOK, Change.ADDED, this::createLorebook);
            each(chosen, Kind.LOREBOOK, Change.CHANGED, this::changeLorebook);
            for (Item item : chosen) {
                if (item.kind() == Kind.LOREBOOK && item.change() != Change.DELETED && applied.contains(item.id())) {
                    relinkSubbooks(item);
                }
            }
            adoptRoot();
            each(chosen, Kind.ENTRY, Change.ADDED, this::createEntry);
            each(chosen, Kind.ENTRY, Change.CHANGED, this::changeEntry);
            each(chosen, Kind.ENTRY, Change.DELETED, this::deleteEntry);
            each(chosen, Kind.LOREBOOK, Change.DELETED, this::deleteLorebook);
            each(chosen, Kind.MESSAGE, Change.ADDED, this::createMessage);
            each(chosen, Kind.MESSAGE, Change.CHANGED, this::changeMessage);
            each(chosen, Kind.MESSAGE, Change.DELETED, this::deleteMessage);
            relinkSummaries();
            if (switchToSourceBranch) {
                switchBranch();
            }
            return new Outcome(counts, skipped, created);
        }

        @FunctionalInterface
        private interface Action {
            void apply(Item item) throws Exception;
        }

        /**
         * Goes through the items of the kind (of the change; any for null) in the order of the plan.
         */
        private void each(List<Item> chosen, Kind kind, Change change, Action action) throws Exception {
            for (Item item : chosen) {
                if (item.kind() != kind || change != null && item.change() != change) {
                    continue;
                }
                action.apply(item);
                applied.add(item.id());
                counts.merge(kind, 1, Integer::sum);
            }
        }

        // -----------------------------------------------------------------------------------------------------------

        /**
         * The key of the item in the plan, the uuid in the book or {@code new:} and the uuid in the copy.
         */
        private String keyOf(Item item) {
            return item.id().substring(2);
        }

        private void made(AgentRunPairKind kind, String sourceUuid, String targetUuid) {
            toTarget.put(sourceUuid, targetUuid);
            created.add(new Created(kind, sourceUuid, targetUuid));
        }

        private String value(Item item, Field field) {
            Map<String, String> edits = this.edits.get(item.id());
            if (field.editable() && edits != null && edits.containsKey(field.name())) {
                return edits.get(field.name());
            }
            if (field.type() == Type.JSON) {
                // the changes of both sides, the places both changed are the agent's unless the user decided them
                return JsonMerge.merge(field.base(), field.agent(), field.book());
            }
            return field.agent();
        }

        private Field field(Item item, String name) {
            return item.fields().stream().filter(field -> field.name().equals(name)).findFirst().orElse(null);
        }

        private Manuscript reload() throws Exception {
            targetBook = (Manuscript) Hibernate.unproxy(manuscriptService.findForUser(targetBook.getUuid()));
            return targetBook;
        }

        /**
         * The uuid in the target of an item of the source: the item it was made from or the one this apply made of it.
         */
        private String targetOf(String sourceUuid) {
            String uuid = toTarget.get(sourceUuid);
            if (uuid == null) {
                throw new IllegalStateException("The item " + sourceUuid + " is not in the other book");
            }
            return uuid;
        }

        // --- the settings of the book

        private void applyBook(List<Item> chosen) throws Exception {
            ManuscriptService.BookSettings settings = new ManuscriptService.BookSettings();
            boolean settingsChanged = false;
            Map<String, String> attributes = new HashMap<>();
            Item tags = null;
            for (Item item : chosen) {
                if (item.kind() != Kind.BOOK) {
                    continue;
                }
                Field field = item.fields().get(0);
                String text = value(item, field);
                switch (field.name()) {
                    case "name" -> settings.name(StringUtils.isBlank(text) ? null : text);
                    case "description" -> settings.description(text);
                    case "language" -> settings.language(StringUtils.isBlank(text) ? null : Language.valueOf(text.strip()));
                    case "pov" -> settings.pov(text);
                    case "tense" -> settings.tense(text);
                    case "style" -> settings.style(text);
                    case "template" -> settings.masterTemplate(text);
                    case "userPrompt" -> settings.userPrompt(text);
                    case "summaryPrompt" -> settings.summaryPrompt(text);
                    case "metaSummaryPrompt" -> settings.metaSummaryPrompt(text);
                    case MergeDiff.TAGS -> tags = item;
                    default -> {
                        attributes.put(field.name(), text);
                        applied.add(item.id());
                        counts.merge(Kind.BOOK, 1, Integer::sum);
                        continue;
                    }
                }
                if (!field.name().equals(MergeDiff.TAGS)) {
                    settingsChanged = true;
                }
                applied.add(item.id());
                counts.merge(Kind.BOOK, 1, Integer::sum);
            }
            if (settingsChanged) {
                manuscriptService.updateSettings(targetBook, settings);
                reload();
            }
            if (!attributes.isEmpty()) {
                for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                    setAttribute(targetBook, attribute.getKey(), attribute.getValue());
                }
                manuscriptService.save(targetBook);
                reload();
            }
            if (tags != null) {
                Field field = tags.fields().get(0);
                retag(targetBook, field, false);
            }
        }

        /**
         * Adds the tags the agent added and removes the ones it removed, the tags the book got meanwhile stay.
         */
        private void retag(BaseEntity object, Field field, boolean negative) throws Exception {
            Set<String> before = lines(field.base());
            Set<String> after = lines(field.agent());
            List<Tag> current = tagRelationService.getTagsForObject(object, negative);
            for (String value : before) {
                if (!after.contains(value)) {
                    for (Tag tag : current) {
                        if (tag.getValue().equalsIgnoreCase(value)) {
                            tagRelationService.removeRelation(tag, object, negative);
                        }
                    }
                }
            }
            for (String value : after) {
                if (!before.contains(value) && current.stream().noneMatch(tag -> tag.getValue().equalsIgnoreCase(value))) {
                    tagRelationService.createRelation(tagService.getOrCreateForUser(value), object, negative);
                }
            }
        }

        // --- lorebooks and their entries

        /**
         * The lorebook of the target a key of the plan names: one this apply made, or the one the key stands for.
         */
        private Lorebook lorebookByKey(String key) throws Exception {
            Lorebook found = createdLorebooks.get(key);
            if (found == null) {
                String uuid = direction.keyToTarget().apply(key);
                found = uuid == null ? null : lorebookService.findForUser(uuid);
            }
            if (found == null || found.isDeleted()) {
                throw new IllegalStateException("The lorebook " + key + " is not in the book");
            }
            return (Lorebook) Hibernate.unproxy(found);
        }

        /**
         * Whether the key names a lorebook the target has or this apply made.
         */
        private boolean knownLorebook(String key) {
            return createdLorebooks.containsKey(key) || direction.keyToTarget().apply(key) != null;
        }

        private void createLorebook(Item item) throws Exception {
            Lorebook lorebook = new Lorebook();
            // a lorebook of the working copy is a copy as well, the two kinds can't be mixed
            lorebook.setAgentic(targetBook.isAgentic());
            lorebook.setSubbooks(new ArrayList<>());
            lorebook.setName(value(item, field(item, "name")));
            Field enabled = field(item, "enabled");
            lorebook.setEnabled(enabled == null || Boolean.parseBoolean(enabled.agent()));
            setAttributes(item, lorebook);
            lorebook = lorebookService.save(lorebook);
            createdLorebooks.put(keyOf(item), lorebook);
            made(AgentRunPairKind.LOREBOOK, item.sourceUuid(), lorebook.getUuid());
            Field tags = field(item, "tags");
            if (tags != null) {
                retag(lorebook, tags, false);
            }
        }

        /**
         * A book that has no lorebook takes the root of the source as its own, the lorebooks of the source were made
         * without anything above them. Otherwise they would be in no book at all.
         */
        private void adoptRoot() throws Exception {
            Lorebook current = targetBook.getLorebook() == null ? null : lorebookService.find(targetBook.getLorebook());
            if (current != null && !current.isDeleted() || sourceBook.getLorebook() == null) {
                return;
            }
            String root = sourceBook.getLorebook().getUuid();
            for (Item item : chosen) {
                Lorebook made = item.kind() == Kind.LOREBOOK && item.change() == Change.ADDED && root.equals(item.sourceUuid())
                        ? createdLorebooks.get(keyOf(item)) : null;
                if (made != null) {
                    targetBook.setLorebook(made);
                    manuscriptService.save(targetBook);
                    reload();
                }
            }
        }

        private void changeLorebook(Item item) throws Exception {
            Lorebook lorebook = (Lorebook) Hibernate.unproxy(lorebookService.findForUser(item.targetUuid()));
            Field name = field(item, "name");
            Field enabled = field(item, "enabled");
            if (name != null && StringUtils.isNotBlank(value(item, name))) {
                lorebook.setName(value(item, name));
            }
            if (enabled != null) {
                lorebook.setEnabled(Boolean.parseBoolean(enabled.agent()));
            }
            setAttributes(item, lorebook);
            lorebook = lorebookService.save(lorebook);
            Field tags = field(item, "tags");
            if (tags != null) {
                retag(lorebook, tags, false);
            }
        }

        /**
         * The subbooks the agent added to the lorebook and the ones it removed from it, the subbooks the book got meanwhile
         * stay. After all the lorebooks the agent made exist.
         */
        private void relinkSubbooks(Item item) throws Exception {
            Field subbooks = field(item, "subbooks");
            if (subbooks == null) {
                return;
            }
            Lorebook lorebook = item.change() == Change.ADDED ? lorebookByKey(keyOf(item))
                    : (Lorebook) Hibernate.unproxy(lorebookService.findForUser(item.targetUuid()));
            Set<String> before = lines(subbooks.base());
            Set<String> after = lines(subbooks.agent());
            Set<Long> present = new HashSet<>();
            for (Lorebook sub : lorebookService.getSubbooks(lorebook)) {
                present.add(sub.getId());
            }
            for (String key : before) {
                if (!after.contains(key) && knownLorebook(key)) {
                    Lorebook sub = lorebookByKey(key);
                    if (sub != null && present.contains(sub.getId())) {
                        lorebookService.removeSubbook(lorebook, sub);
                    }
                }
            }
            for (String key : after) {
                if (before.contains(key) || !knownLorebook(key)) {
                    continue;
                }
                Lorebook sub = lorebookByKey(key);
                if (!present.contains(sub.getId()) && !lorebookService.wouldCycle(lorebook, sub)) {
                    lorebookService.addSubbook(lorebook, sub);
                }
            }
        }

        private void deleteLorebook(Item item) throws Exception {
            Lorebook lorebook = lorebookService.findForUser(item.targetUuid());
            Lorebook root = targetBook.getLorebook();
            if (lorebook == null || root == null || lorebook.getId().equals(root.getId())) {
                throw new IllegalStateException("The lorebook " + item.targetUuid() + " can't be deleted");
            }
            for (Lorebook parent : tree(root)) {
                if (lorebookService.getSubbooks(parent).stream().anyMatch(sub -> sub.getId().equals(lorebook.getId()))) {
                    lorebookService.removeSubbook(parent, lorebook);
                }
            }
            lorebookService.delete(lorebook, false);
        }

        private void createEntry(Item item) throws Exception {
            LorebookEntry fields = new LorebookEntry();
            setEntryFields(item, fields);
            Lorebook target = lorebookByKey(field(item, "lorebook").agent());
            LorebookEntry saved = lorebookEntryService.create(target, fields).entry();
            if (setAttributes(item, saved)) {
                saved = lorebookEntryService.save(saved);
            }
            made(AgentRunPairKind.ENTRY, item.sourceUuid(), saved.getUuid());
            retagEntry(item, saved);
        }

        private void changeEntry(Item item) throws Exception {
            LorebookEntry entry = (LorebookEntry) Hibernate.unproxy(lorebookEntryService.findForUser(item.targetUuid()));
            setEntryFields(item, entry);
            entry = lorebookEntryService.update(entry).entry();
            if (setAttributes(item, entry)) {
                entry = lorebookEntryService.save(entry);
            }
            retagEntry(item, entry);
            Field lorebook = field(item, "lorebook");
            if (lorebook != null) {
                lorebookEntryService.move(entry, lorebookByKey(lorebook.agent()));
            }
        }

        private void deleteEntry(Item item) throws Exception {
            LorebookEntry entry = (LorebookEntry) Hibernate.unproxy(lorebookEntryService.findForUser(item.targetUuid()));
            lorebookEntryService.deleteEntry(entry.getLorebook(), entry);
        }

        private void setEntryFields(Item item, LorebookEntry entry) {
            for (Field field : item.fields()) {
                String text = value(item, field);
                switch (field.name()) {
                    case "name" -> entry.setName(text);
                    case "payload" -> entry.setPayload(text);
                    case "comment" -> entry.setComment(text);
                    case "enabled" -> entry.setEnabled(Boolean.parseBoolean(text));
                    case "order" -> entry.setOrder(StringUtils.isBlank(text) ? 100 : Integer.parseInt(text.strip()));
                    case "filtering" -> entry.setFiltering(text);
                    case "filteringMode" -> entry.setFilteringMode(StringUtils.isBlank(text) ? null : FilteringMode.valueOf(text));
                    case "insertionMode" -> entry.setInsertionMode(StringUtils.isBlank(text) ? null : InsertionMode.valueOf(text));
                    default -> {
                    }
                }
            }
        }

        private void retagEntry(Item item, LorebookEntry entry) throws Exception {
            Field tags = field(item, "tags");
            if (tags != null) {
                retag(entry, tags, false);
            }
            Field negative = field(item, "negativeTags");
            if (negative != null) {
                retag(entry, negative, true);
            }
        }

        // --- parts

        private ChatMessage messageOf(String uuid) throws Exception {
            ChatMessage message = chatMessageService.findForUser(uuid);
            if (message == null || message.isDeleted() || message.getParentScript() == null
                    || !message.getParentScript().getId().equals(targetBook.getId())) {
                throw new IllegalStateException("The part " + uuid + " is not in the book");
            }
            return (ChatMessage) Hibernate.unproxy(message);
        }

        private ChatMessage sourceMessage(String copyUuid) throws Exception {
            ChatMessage message = chatMessageService.findForUser(copyUuid);
            if (message == null || message.getParentScript() == null || !message.getParentScript().getId().equals(sourceBook.getId())) {
                throw new IllegalStateException("The part " + copyUuid + " is not in the copy");
            }
            return (ChatMessage) Hibernate.unproxy(message);
        }

        private void createMessage(Item item) throws Exception {
            ChatMessage source = sourceMessage(item.sourceUuid());
            ChatMessage created = ChatMessage.copyOf(source);
            created.setSummary(null);
            String text = null;
            for (Field field : item.fields()) {
                String edited = value(item, field);
                switch (field.name()) {
                    case "response" -> text = edited;
                    case "instructions" -> created.setInstructions(edited);
                    case "sceneSetting" -> created.setSceneSetting(edited);
                    case "povCharacter" -> created.setPovCharacter(edited);
                    case "presentCharacters" -> created.setPresentCharacters(edited);
                    default -> {
                        if (field.name().startsWith(MergeDiff.ATTRIBUTES)) {
                            setAttribute(created, field.name(), edited);
                        }
                    }
                }
            }
            ChatMessage parent = source.getParent();
            ChatMessage saved = parent == null ? chatMessageService.createRoot(targetBook, created)
                    : chatMessageService.addChild(messageOf(targetOf(parent.getUuid())), created);
            made(AgentRunPairKind.MESSAGE, item.sourceUuid(), saved.getUuid());
            if (text != null && !text.equals(source.getResponse())) {
                storeResponse(saved, source, text);
            }
            Field summary = field(item, "summary");
            if (summary != null && source.getSummary() != null) {
                writeSummary(saved, source.getSummary(), value(item, summary));
            }
        }

        private void changeMessage(Item item) throws Exception {
            ChatMessage message = messageOf(item.targetUuid());
            ChatMessage source = sourceMessage(item.sourceUuid());
            String text = null;
            boolean fields = false;
            for (Field field : item.fields()) {
                String edited = value(item, field);
                switch (field.name()) {
                    case "response" -> text = edited;
                    case "instructions" -> {
                        message.setInstructions(edited);
                        fields = true;
                    }
                    case "sceneSetting" -> {
                        message.setSceneSetting(edited);
                        fields = true;
                    }
                    case "povCharacter" -> {
                        message.setPovCharacter(edited);
                        fields = true;
                    }
                    case "presentCharacters" -> {
                        message.setPresentCharacters(edited);
                        fields = true;
                    }
                    default -> {
                        if (field.name().startsWith(MergeDiff.ATTRIBUTES)) {
                            setAttribute(message, field.name(), edited);
                            fields = true;
                        }
                    }
                }
            }
            if (field(item, MergeDiff.PARENT) != null) {
                // the agent inserted a part above this one: it follows the new part now (made before, items are in the plan's order)
                message.setParent(source.getParent() == null ? null : messageOf(targetOf(source.getParent().getUuid())));
                fields = true;
            }
            if (fields) {
                message = chatMessageService.save(message);
            }
            if (text != null) {
                message = storeResponse(message, source, text);
            }
            Field summary = field(item, "summary");
            if (summary != null) {
                if (source.getSummary() == null) {
                    summaryService.removeSummary(message, false);
                } else {
                    writeSummary(message, source.getSummary(), value(item, summary));
                }
            }
        }

        /**
         * Puts the data of one key of the attributes of the object (the JSON of the field), removes it when the other side has none.
         *
         * @throws JsonText.InvalidJsonException the text is not JSON
         */
        private void setAttribute(ExtendableEntity entity, String field, String json) {
            String name = field.substring(MergeDiff.ATTRIBUTES.length());
            JsonElement data = JsonText.parse(json);
            if (data == null) {
                entity.getAttributes().remove(name);
            } else {
                entity.getAttributes().add(name, data);
            }
        }

        /**
         * Puts the data of the attributes the item changed into the object; whether there were any.
         */
        private boolean setAttributes(Item item, ExtendableEntity entity) {
            boolean any = false;
            for (Field field : item.fields()) {
                if (field.name().startsWith(MergeDiff.ATTRIBUTES)) {
                    setAttribute(entity, field.name(), value(item, field));
                    any = true;
                }
            }
            return any;
        }

        /**
         * Stores the text of the part. The text of the agent comes with the counts of the part it was written in; a text the
         * user wrote is counted again (it needs the tokenizer of the book's model, which is slow to start).
         */
        private ChatMessage storeResponse(ChatMessage message, ChatMessage source, String text) throws Exception {
            if (!text.equals(source.getResponse())) {
                return chatMessageService.storeResponse(targetBook, message, text);
            }
            String before = message.getResponse();
            message.setResponse(text);
            message.setTokenCount(source.getTokenCount());
            message.setWordCount(source.getWordCount());
            message.setEdited(true);
            ChatMessage saved = chatMessageService.save(message);
            annotationService.shift(targetBook, saved, before, text);
            return saved;
        }

        private void deleteMessage(Item item) throws Exception {
            ChatMessage message = messageOf(item.targetUuid());
            targetBook = chatMessageService.deleteNodeAndMigrateChildren(message, targetBook, false);
        }

        /**
         * Puts the summary of the copy on the part: into the summary it has when it has one (the uuid stays, meta summaries
         * point to it), else as a new one.
         */
        private void writeSummary(ChatMessage message, Summary source, String edited) throws Exception {
            Summary target = message.getSummary() == null ? null : summaryService.find(message.getSummary());
            if (target == null) {
                target = summaryService.copySummary(source);
                created.add(new Created(AgentRunPairKind.SUMMARY, source.getUuid(), target.getUuid()));
                message.setSummary(target);
                chatMessageService.save(message);
            } else {
                target.setSummary(source.getSummary());
                target.setSummaryTokens(source.getSummaryTokens());
                target.setReasoning(source.getReasoning());
                target.setReasoningTokens(source.getReasoningTokens());
                target.setSummaryMessageHash(source.getSummaryMessageHash());
                target.setSummaryType(source.getSummaryType());
                target.setNextSummaryUuid(source.getNextSummaryUuid());
                target.setReplacedSummary(source.getReplacedSummary());
                target = summaryService.save(target);
            }
            if (edited != null && !edited.equals(source.getSummary())) {
                target = summaryService.updateSummaryText(targetBook, target, edited);
            }
            writtenSummaries.put(source.getUuid(), target);
        }

        /**
         * The meta summaries the apply wrote point to summaries by uuid, in the words of the copy; they are told the uuids of
         * the book now that every summary is written.
         */
        private void relinkSummaries() throws Exception {
            Map<String, String> uuids = new HashMap<>(toTarget);
            writtenSummaries.forEach((copyUuid, written) -> uuids.put(copyUuid, written.getUuid()));
            for (Summary written : writtenSummaries.values()) {
                String next = MergeDiff.translate(written.getNextSummaryUuid(), uuids);
                String replaced = MergeDiff.translate(written.getReplacedSummary(), uuids);
                if (!StringUtils.equals(next, written.getNextSummaryUuid()) || !StringUtils.equals(replaced, written.getReplacedSummary())) {
                    written.setNextSummaryUuid(next);
                    written.setReplacedSummary(replaced);
                    summaryService.save(written);
                }
            }
        }

        private void switchBranch() throws Exception {
            ChatMessage leaf = sourceBook.getActiveLeaf();
            String uuid = leaf == null ? null : toTarget.get(leaf.getUuid());
            if (uuid == null) {
                return;
            }
            ChatMessage message = messageOf(uuid);
            targetBook.setActiveLeaf(message);
            targetBook = manuscriptService.save(targetBook);
        }
    }

    private static Set<String> lines(String text) {
        Set<String> values = new LinkedHashSet<>();
        if (text != null) {
            for (String line : text.split("\n")) {
                if (!line.isEmpty()) {
                    values.add(line);
                }
            }
        }
        return values;
    }
}
