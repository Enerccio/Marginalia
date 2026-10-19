package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.AgentMergeService;
import com.github.enerccio.marginalia.domain.service.AgentRunPairService;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.AgentVirtualFileSystemService;
import com.github.enerccio.marginalia.domain.service.BackupService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.domain.service.agent.MergeDiff;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Change;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Field;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Item;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Kind;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            case LOREBOOK -> item.originalUuid();
            case ENTRY -> {
                LorebookEntry entry = item.originalUuid() == null ? null : lorebookEntryService.findForUser(item.originalUuid());
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

    private Applied applyChanges(AgentRun run, MergeSelection selection) throws Exception {
        Review review = open(run);
        if (selection == null || selection.items().isEmpty()) {
            throw new IllegalArgumentException("Nothing is picked");
        }
        MergePlan plan = plan(review);
        return new Applier(review, plan, selection).apply();
    }

    /**
     * One apply: the state it builds up while it goes through the picked items.
     */
    private final class Applier {
        private final Review review;
        private final MergePlan plan;
        private final MergeSelection selection;
        private Manuscript original;

        // the pairs of the run, and what the apply made: the uuid in the copy to the uuid of the item in the book
        private final Map<String, String> toOriginal;
        private final Map<String, Lorebook> createdLorebooks = new HashMap<>();
        // the summaries written to the book by the uuid of the summary of the copy they were made from
        private final Map<String, Summary> writtenSummaries = new HashMap<>();

        private final Set<String> applied = new LinkedHashSet<>();
        private final Map<Kind, Integer> counts = new EnumMap<>(Kind.class);
        private final List<Skipped> skipped = new ArrayList<>();

        Applier(Review review, MergePlan plan, MergeSelection selection) {
            this.review = review;
            this.plan = plan;
            this.selection = selection;
            this.original = review.original();
            this.toOriginal = new HashMap<>(review.copyToOriginal());
        }

        Applied apply() throws Exception {
            List<Item> chosen = new ArrayList<>();
            Set<String> willApply = new HashSet<>();
            for (Item item : plan.items()) {
                if (!selection.items().contains(item.id())) {
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
            if (chosen.isEmpty()) {
                throw new IllegalArgumentException("Nothing that is picked can be applied");
            }

            // the book as it is now, before anything is changed in it (lorebooks and the story are in it)
            BackupService.ManuscriptBackup backup = backupService.takeBackup(original);

            applyBook(chosen);
            each(chosen, Kind.LOREBOOK, Change.ADDED, this::createLorebook);
            each(chosen, Kind.LOREBOOK, Change.CHANGED, this::changeLorebook);
            for (Item item : chosen) {
                if (item.kind() == Kind.LOREBOOK && item.change() != Change.DELETED && applied.contains(item.id())) {
                    relinkSubbooks(item);
                }
            }
            each(chosen, Kind.ENTRY, Change.ADDED, this::createEntry);
            each(chosen, Kind.ENTRY, Change.CHANGED, this::changeEntry);
            each(chosen, Kind.ENTRY, Change.DELETED, this::deleteEntry);
            each(chosen, Kind.LOREBOOK, Change.DELETED, this::deleteLorebook);
            each(chosen, Kind.MESSAGE, Change.ADDED, this::createMessage);
            each(chosen, Kind.MESSAGE, Change.CHANGED, this::changeMessage);
            each(chosen, Kind.MESSAGE, Change.DELETED, this::deleteMessage);
            relinkSummaries();
            if (selection.switchToAgentBranch()) {
                switchBranch();
            }
            return new Applied(review.run(), counts, skipped, backup == null ? null : backup.getFile());
        }

        @FunctionalInterface
        private interface Action {
            void apply(Item item) throws Exception;
        }

        /**
         * Goes through the picked items of the kind (of the change; any for null) in the order of the plan.
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

        private String value(Item item, Field field) {
            Map<String, String> edits = selection.edits().get(item.id());
            if (field.editable() && edits != null && edits.containsKey(field.name())) {
                return edits.get(field.name());
            }
            return field.agent();
        }

        private Field field(Item item, String name) {
            return item.fields().stream().filter(field -> field.name().equals(name)).findFirst().orElse(null);
        }

        private Manuscript reload() throws Exception {
            original = (Manuscript) Hibernate.unproxy(manuscriptService.findForUser(original.getUuid()));
            return original;
        }

        /**
         * The uuid in the book of an item of the copy: the item it was made from or the one this apply made of it.
         */
        private String originalOf(String copyUuid) {
            String uuid = toOriginal.get(copyUuid);
            if (uuid == null) {
                throw new IllegalStateException("The item " + copyUuid + " of the copy is not in the book");
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
                manuscriptService.updateSettings(original, settings);
                reload();
            }
            if (!attributes.isEmpty()) {
                for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                    String name = attribute.getKey().substring(MergeDiff.ATTRIBUTES.length());
                    if (StringUtils.isBlank(attribute.getValue())) {
                        original.getAttributes().remove(name);
                    } else {
                        original.getAttributes().add(name, JsonParser.parseString(attribute.getValue()));
                    }
                }
                manuscriptService.save(original);
                reload();
            }
            if (tags != null) {
                Field field = tags.fields().get(0);
                retag(original, field, false);
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

        private Lorebook lorebookByKey(String key) throws Exception {
            Lorebook found = key.startsWith(MergeDiff.NEW) ? createdLorebooks.get(key.substring(MergeDiff.NEW.length()))
                    : lorebookService.findForUser(key);
            if (found == null || found.isDeleted()) {
                throw new IllegalStateException("The lorebook " + key + " is not in the book");
            }
            return (Lorebook) Hibernate.unproxy(found);
        }

        private void createLorebook(Item item) throws Exception {
            Lorebook lorebook = new Lorebook();
            lorebook.setName(value(item, field(item, "name")));
            Field enabled = field(item, "enabled");
            lorebook.setEnabled(enabled == null || Boolean.parseBoolean(enabled.agent()));
            lorebook = lorebookService.save(lorebook);
            createdLorebooks.put(item.copyUuid(), lorebook);
            toOriginal.put(item.copyUuid(), lorebook.getUuid());
            Field tags = field(item, "tags");
            if (tags != null) {
                retag(lorebook, tags, false);
            }
        }

        private void changeLorebook(Item item) throws Exception {
            Lorebook lorebook = (Lorebook) Hibernate.unproxy(lorebookService.findForUser(item.originalUuid()));
            Field name = field(item, "name");
            Field enabled = field(item, "enabled");
            if (name != null && StringUtils.isNotBlank(value(item, name))) {
                lorebook.setName(value(item, name));
            }
            if (enabled != null) {
                lorebook.setEnabled(Boolean.parseBoolean(enabled.agent()));
            }
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
            Lorebook lorebook = item.change() == Change.ADDED ? lorebookByKey(MergeDiff.NEW + item.copyUuid())
                    : (Lorebook) Hibernate.unproxy(lorebookService.findForUser(item.originalUuid()));
            Set<String> before = lines(subbooks.base());
            Set<String> after = lines(subbooks.agent());
            Set<Long> present = new HashSet<>();
            for (Lorebook sub : lorebookService.getSubbooks(lorebook)) {
                present.add(sub.getId());
            }
            for (String key : before) {
                if (!after.contains(key) && !key.startsWith(MergeDiff.NEW)) {
                    Lorebook sub = lorebookService.findForUser(key);
                    if (sub != null && present.contains(sub.getId())) {
                        lorebookService.removeSubbook(lorebook, sub);
                    }
                }
            }
            for (String key : after) {
                if (before.contains(key) || key.startsWith(MergeDiff.NEW) && !createdLorebooks.containsKey(key.substring(MergeDiff.NEW.length()))) {
                    continue;
                }
                Lorebook sub = lorebookByKey(key);
                if (!present.contains(sub.getId()) && !lorebookService.wouldCycle(lorebook, sub)) {
                    lorebookService.addSubbook(lorebook, sub);
                }
            }
        }

        private void deleteLorebook(Item item) throws Exception {
            Lorebook lorebook = lorebookService.findForUser(item.originalUuid());
            Lorebook root = original.getLorebook();
            if (lorebook == null || root == null || lorebook.getId().equals(root.getId())) {
                throw new IllegalStateException("The lorebook " + item.originalUuid() + " can't be deleted");
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
            toOriginal.put(item.copyUuid(), saved.getUuid());
            retagEntry(item, saved);
        }

        private void changeEntry(Item item) throws Exception {
            LorebookEntry entry = (LorebookEntry) Hibernate.unproxy(lorebookEntryService.findForUser(item.originalUuid()));
            setEntryFields(item, entry);
            entry = lorebookEntryService.update(entry).entry();
            retagEntry(item, entry);
            Field lorebook = field(item, "lorebook");
            if (lorebook != null) {
                lorebookEntryService.move(entry, lorebookByKey(lorebook.agent()));
            }
        }

        private void deleteEntry(Item item) throws Exception {
            LorebookEntry entry = (LorebookEntry) Hibernate.unproxy(lorebookEntryService.findForUser(item.originalUuid()));
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
                    || !message.getParentScript().getId().equals(original.getId())) {
                throw new IllegalStateException("The part " + uuid + " is not in the book");
            }
            return (ChatMessage) Hibernate.unproxy(message);
        }

        private ChatMessage copyMessage(String copyUuid) throws Exception {
            ChatMessage message = chatMessageService.findForUser(copyUuid);
            if (message == null || message.getParentScript() == null || !message.getParentScript().getId().equals(review.copy().getId())) {
                throw new IllegalStateException("The part " + copyUuid + " is not in the copy");
            }
            return (ChatMessage) Hibernate.unproxy(message);
        }

        private void createMessage(Item item) throws Exception {
            ChatMessage source = copyMessage(item.copyUuid());
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
                    }
                }
            }
            ChatMessage parent = source.getParent();
            ChatMessage saved = parent == null ? chatMessageService.createRoot(original, created)
                    : chatMessageService.addChild(messageOf(originalOf(parent.getUuid())), created);
            toOriginal.put(item.copyUuid(), saved.getUuid());
            if (text != null && !text.equals(source.getResponse())) {
                storeResponse(saved, source, text);
            }
            Field summary = field(item, "summary");
            if (summary != null && source.getSummary() != null) {
                writeSummary(saved, source.getSummary(), value(item, summary));
            }
        }

        private void changeMessage(Item item) throws Exception {
            ChatMessage message = messageOf(item.originalUuid());
            ChatMessage source = copyMessage(item.copyUuid());
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
                    }
                }
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
         * Stores the text of the part. The text of the agent comes with the counts of the part it was written in; a text the
         * user wrote is counted again (it needs the tokenizer of the book's model, which is slow to start).
         */
        private ChatMessage storeResponse(ChatMessage message, ChatMessage source, String text) throws Exception {
            if (!text.equals(source.getResponse())) {
                return chatMessageService.storeResponse(original, message, text);
            }
            message.setResponse(text);
            message.setTokenCount(source.getTokenCount());
            message.setWordCount(source.getWordCount());
            message.setEdited(true);
            return chatMessageService.save(message);
        }

        private void deleteMessage(Item item) throws Exception {
            ChatMessage message = messageOf(item.originalUuid());
            original = chatMessageService.deleteNodeAndMigrateChildren(message, original, false);
        }

        /**
         * Puts the summary of the copy on the part: into the summary it has when it has one (the uuid stays, meta summaries
         * point to it), else as a new one.
         */
        private void writeSummary(ChatMessage message, Summary source, String edited) throws Exception {
            Summary target = message.getSummary() == null ? null : summaryService.find(message.getSummary());
            if (target == null) {
                target = summaryService.copySummary(source);
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
                target = summaryService.updateSummaryText(original, target, edited);
            }
            writtenSummaries.put(source.getUuid(), target);
        }

        /**
         * The meta summaries the apply wrote point to summaries by uuid, in the words of the copy; they are told the uuids of
         * the book now that every summary is written.
         */
        private void relinkSummaries() throws Exception {
            Map<String, String> uuids = new HashMap<>(toOriginal);
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
            ChatMessage leaf = review.copy().getActiveLeaf();
            String uuid = leaf == null ? null : toOriginal.get(leaf.getUuid());
            if (uuid == null) {
                return;
            }
            ChatMessage message = messageOf(uuid);
            original.setActiveLeaf(message);
            original = manuscriptService.save(original);
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
