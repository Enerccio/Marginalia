package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.AgentMergeService.MergeResult;
import com.github.enerccio.marginalia.domain.service.AgentMergeService.MergeSelection;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Change;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Field;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Item;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Kind;
import com.github.enerccio.marginalia.domain.service.agent.SyncToolsImpl;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The sync of a resume: the changes the user made to the book while the run was paused come into the working copy, the changes the
 * agent made are kept, and the review still compares everything with the book as the run started.
 */
class AgentSyncTest extends AgentTestBase {

    @Autowired
    private AgentMergeService mergeService;

    @Autowired
    private AgentVirtualFileSystemService fileSystemService;

    @Autowired
    private AgentRunPairService pairService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private CheckpointService checkpointService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    private User user;
    private Manuscript book;
    private Lorebook root;
    private Lorebook places;
    private Agent agent;
    private AgentRun run;

    @BeforeEach
    void startRun() throws Exception {
        user = loginAgentUser();
        root = new Lorebook();
        root.setName("lore");
        root.setSubbooks(new ArrayList<>());
        root = lorebookService.save(root);
        entry(root, "dragon", "a red dragon");
        entry(root, "castle", "a grey castle");
        places = lorebookService.createSubbook(root, "places");
        entry(places, "village", "a small village");
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript.setDescription("a tale");
        manuscript.setLorebook(root);
        book = manuscriptService.save(manuscript);
        ChatMessage first = new ChatMessage();
        first.setResponse("Once upon a time");
        first = chatMessageService.createRoot(book, first);
        ChatMessage second = new ChatMessage();
        second.setResponse("there was a dragon");
        second = chatMessageService.addChild(first, second);
        ChatMessage third = new ChatMessage();
        third.setResponse("and a castle");
        chatMessageService.addChild(second, third);
        agent = agentService.save(newAgent(book));
        run = agentRunService.start(agent);
    }

    private LorebookEntry entry(Lorebook lorebook, String name, String payload) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setPayload(payload);
        return lorebookEntryService.save(entry);
    }

    // --- the copy (what the agent does) and the book (what the user does)

    private Manuscript copy() throws Exception {
        return agentRunService.find(run.getId()).getWorkingCopy();
    }

    private ChatMessage inCopy(String text) throws Exception {
        return chatMessageService.getAllMessages(copy()).stream().filter(m -> text.equals(m.getResponse())).findFirst().orElseThrow();
    }

    private ChatMessage inBook(String text) throws Exception {
        return chatMessageService.getAllMessages(manuscriptService.find(book.getId())).stream()
                .filter(m -> text.equals(m.getResponse())).findFirst().orElseThrow();
    }

    private void edit(ChatMessage message, String text) throws Exception {
        ChatMessage stored = chatMessageService.find(message.getId());
        stored.setResponse(text);
        chatMessageService.save(stored);
    }

    private ChatMessage addTo(Manuscript where, ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return chatMessageService.addChild(chatMessageService.find(parent.getId()), message);
    }

    private LorebookEntry entryOf(Lorebook lorebook, String name) throws Exception {
        return lorebookEntryService.getEntriesForLorebook(lorebook).stream().filter(e -> name.equals(e.getName())).findFirst().orElse(null);
    }

    private Lorebook copyRoot() throws Exception {
        return lorebookService.find(copy().getLorebook());
    }

    private Lorebook subbook(Lorebook parent, String name) throws Exception {
        return lorebookService.getSubbooks(parent).stream().filter(l -> name.equals(l.getName())).findFirst().orElse(null);
    }

    /**
     * The run is paused: the book is the user's again, and only then can they change it.
     */
    private void pause() throws Exception {
        AgentRun current = agentRunService.find(run.getId());
        run = current.getStatus() == AgentRunStatus.PAUSED ? current : agentRunService.pause(current);
    }

    /**
     * The run goes on: its copy takes over what the user changed.
     */
    private void resume() throws Exception {
        run = agentRunService.resume(agentRunService.find(run.getId()));
    }

    private void pauseAndResume() throws Exception {
        pause();
        resume();
    }

    private MergePlan plan() throws Exception {
        AgentRun current = agentRunService.find(run.getId());
        run = current.getStatus() == AgentRunStatus.DONE ? current : agentRunService.transition(current, AgentRunStatus.DONE, "report");
        return mergeService.plan(run);
    }

    private String report() throws Exception {
        return fileSystemService.readSyncReport(run);
    }

    private Item item(MergePlan plan, Kind kind, Change change, String labelPart) {
        List<Item> found = plan.items().stream().filter(i -> i.kind() == kind && i.change() == change && i.label().contains(labelPart)).toList();
        assertThat(found).as("%s %s %s in %s", kind, change, labelPart, plan.items().stream().map(Item::id).toList()).hasSize(1);
        return found.getFirst();
    }

    private Field field(Item item, String name) {
        return item.fields().stream().filter(f -> f.name().equals(name)).findFirst().orElseThrow();
    }

    private MergeResult applyAll(MergePlan plan) throws Exception {
        return mergeService.apply(run, new MergeSelection(plan.items().stream().map(Item::id).collect(Collectors.toSet()), Map.of(), false));
    }

    private List<String> bookTexts() throws Exception {
        return chatMessageService.getAllMessages(manuscriptService.find(book.getId())).stream().map(ChatMessage::getResponse).sorted().toList();
    }

    // --- what comes into the copy

    @Test
    void whatTheUserChangedAndTheAgentDidNotComesIntoTheCopyAndTheReviewHasNothingToSay() throws Exception {
        pause();
        edit(inBook("and a castle"), "and a keep");

        resume();

        assertThat(inCopy("and a keep")).isNotNull();
        assertThat(report()).contains("Taken over into your working copy").contains("changed by the user (text)").contains("and a keep")
                .doesNotContain("Not taken over");
        assertThat(plan().isEmpty()).as("copy and book agree").isTrue();
    }

    @Test
    void whenNothingChangedThereIsNothingToTellAndThePairsAreTheSame() throws Exception {
        List<?> pairs = pairService.getAll(agentRunService.find(run.getId()));

        pauseAndResume();

        assertThat(report()).isNull();
        assertThat(fileSystemService.takePendingSync(run)).isNull();
        assertThat(pairService.getAll(agentRunService.find(run.getId()))).isEqualTo(pairs);
    }

    @Test
    void whenBothChangedTheSameTextTheAgentsVersionIsKeptAndTheReviewShowsTheConflict() throws Exception {
        edit(inCopy("there was a dragon"), "there was a small dragon");
        pause();
        edit(inBook("there was a dragon"), "there lived a dragon");

        resume();

        assertThat(inCopy("there was a small dragon")).as("the agent's change is not lost").isNotNull();
        assertThat(report()).contains("Not taken over, your version is kept").contains("the user changed it too (text)");
        Item item = item(plan(), Kind.MESSAGE, Change.CHANGED, "there");
        assertThat(item.conflict()).isTrue();
        assertThat(field(item, "response").agent()).isEqualTo("there was a small dragon");
        assertThat(field(item, "response").book()).isEqualTo("there lived a dragon");
    }

    @Test
    void aConflictIsToldAgainOnEveryResumeUntilItIsResolved() throws Exception {
        edit(inCopy("there was a dragon"), "there was a small dragon");
        pause();
        edit(inBook("there was a dragon"), "there lived a dragon");
        resume();
        assertThat(fileSystemService.takePendingSync(run)).contains("the user changed it too (text)");

        pauseAndResume();

        assertThat(report()).as("nothing changed meanwhile, the conflict is still there").contains("Not taken over, your version is kept")
                .contains("the user changed it too (text)");
        assertThat(fileSystemService.takePendingSync(run)).as("and the model is told again").contains("the user changed it too (text)");

        // the user takes the agent's version into the book: nothing conflicts any more
        pause();
        edit(inBook("there lived a dragon"), "there was a small dragon");
        resume();

        assertThat(report()).isNull();
    }

    @Test
    void theNoteOfAResumeIsToldOnceAndAnotherOneFollowsTheFirst() throws Exception {
        assertThat(fileSystemService.takeResumeNote(run)).isNull();
        fileSystemService.writeResumeNote(run, "  Look at the castle first.  ");
        fileSystemService.writeResumeNote(run, "And leave the dragon.");
        assertThat(fileSystemService.takeResumeNote(run)).isEqualTo("Look at the castle first.\n\nAnd leave the dragon.");
        assertThat(fileSystemService.takeResumeNote(run)).as("told once").isNull();
    }

    @Test
    void theFieldsOfAPartAreSyncedOneByOne() throws Exception {
        edit(inCopy("there was a dragon"), "there was a small dragon");
        pause();
        ChatMessage theirs = chatMessageService.find(inBook("there was a dragon").getId());
        theirs.setSceneSetting("a cave");
        chatMessageService.save(theirs);

        resume();

        ChatMessage synced = inCopy("there was a small dragon");
        assertThat(synced.getSceneSetting()).isEqualTo("a cave");
        assertThat(report()).contains("changed by the user (scene setting)").doesNotContain("Not taken over");
        Item item = item(plan(), Kind.MESSAGE, Change.CHANGED, "there");
        assertThat(item.fields()).extracting(Field::name).containsExactly("response");
        assertThat(item.conflict()).isFalse();
    }

    @Test
    void aPartTheUserAddedComesIntoTheCopyWithAHandleAndTheAgentMayBuildOnIt() throws Exception {
        pause();
        ChatMessage added = addTo(book, inBook("and a castle"), "Then it rained.");

        resume();

        ChatMessage inTheCopy = inCopy("Then it rained.");
        assertThat(inTheCopy.getParent().getUuid()).isEqualTo(pairService.findByOriginal(run, inBook("and a castle").getUuid()).orElseThrow().copyUuid());
        assertThat(pairService.findByCopy(run, inTheCopy.getUuid()).orElseThrow().originalUuid()).isEqualTo(added.getUuid());
        String handle = pairService.findHandle(run, inTheCopy.getUuid()).orElseThrow();
        assertThat(report()).contains("part " + handle).contains("added by the user");

        // the agent edits the part the user wrote: that is its change, not a conflict
        edit(inTheCopy, "Then it poured.");
        Item item = item(plan(), Kind.MESSAGE, Change.CHANGED, "poured");
        assertThat(item.conflict()).isFalse();
        assertThat(plan().items()).hasSize(1);
    }

    @Test
    void aValueTheCopyTookFromTheBookCanBeChangedByTheAgentWithoutAConflict() throws Exception {
        pause();
        edit(inBook("and a castle"), "and a keep");
        resume();

        edit(inCopy("and a keep"), "and a tall keep");

        Item item = item(plan(), Kind.MESSAGE, Change.CHANGED, "keep");
        assertThat(item.conflict()).isFalse();
        assertThat(field(item, "response").base()).as("the base moved on for what was taken over").isEqualTo("and a keep");
    }

    @Test
    void aPartTheUserDeletedIsDeletedFromTheCopyUnlessTheAgentChangedIt() throws Exception {
        edit(inCopy("and a castle"), "and a big castle");
        pause();
        chatMessageService.deleteNodeAndMigrateChildren(inBook("and a castle"), manuscriptService.find(book.getId()), false);
        chatMessageService.deleteNodeAndMigrateChildren(inBook("Once upon a time"), manuscriptService.find(book.getId()), false);

        resume();

        assertThat(chatMessageService.getAllMessages(copy())).extracting(ChatMessage::getResponse).containsExactlyInAnyOrder("there was a dragon", "and a big castle");
        assertThat(report()).contains("deleted by the user").contains("the user deleted it and you changed it");
        Item item = item(plan(), Kind.MESSAGE, Change.CHANGED, "castle");
        assertThat(item.problem()).isEqualTo(MergePlan.PROBLEM_GONE);
    }

    @Test
    void aPartTheAgentDeletedStaysDeletedWhenTheUserChangedIt() throws Exception {
        chatMessageService.deleteNodeAndMigrateChildren(inCopy("and a castle"), copy(), false);
        pause();
        edit(inBook("and a castle"), "and a keep");

        resume();

        assertThat(chatMessageService.getAllMessages(copy())).extracting(ChatMessage::getResponse).containsExactlyInAnyOrder("Once upon a time", "there was a dragon");
        assertThat(report()).contains("you deleted it and the user changed it");
        assertThat(item(plan(), Kind.MESSAGE, Change.DELETED, "castle").conflict()).isTrue();
    }

    @Test
    void lorebooksSyncEntriesSubbooksAndTags() throws Exception {
        pause();
        LorebookEntry dragon = entryOf(root, "dragon");
        dragon.setPayload("a green dragon");
        lorebookEntryService.update(dragon);
        entry(places, "inn", "a warm inn");
        lorebookEntryService.deleteEntry(root, entryOf(root, "castle"));
        Lorebook swamp = lorebookService.createSubbook(root, "swamp");
        entry(swamp, "frog", "a loud frog");

        resume();

        Lorebook copiedRoot = copyRoot();
        assertThat(entryOf(copiedRoot, "dragon").getPayload()).isEqualTo("a green dragon");
        assertThat(entryOf(copiedRoot, "castle")).isNull();
        assertThat(entryOf(subbook(copiedRoot, "places"), "inn")).isNotNull();
        Lorebook copiedSwamp = subbook(copiedRoot, "swamp");
        assertThat(copiedSwamp).isNotNull();
        assertThat(copiedSwamp.isAgentic()).isTrue();
        assertThat(entryOf(copiedSwamp, "frog").getPayload()).isEqualTo("a loud frog");
        assertThat(pairService.findByOriginal(run, swamp.getUuid())).isPresent();
        assertThat(plan().isEmpty()).as("nothing left for the review").isTrue();
    }

    @Test
    void theSettingsTheTagsAndThePluginDataOfTheBookSync() throws Exception {
        pause();
        Manuscript theirs = manuscriptService.find(book.getId());
        theirs.setDescription("a longer tale");
        theirs.getAttributes().add("arcs", JsonParser.parseString("{\"acts\":3}"));
        manuscriptService.save(theirs);
        tagRelationService.createRelation(tagService.getOrCreateForUser("fantasy"), theirs);

        resume();

        Manuscript copied = manuscriptService.find(copy().getId());
        assertThat(copied.getDescription()).isEqualTo("a longer tale");
        assertThat(copied.getAttributes().get("arcs").toString()).isEqualTo("{\"acts\":3}");
        assertThat(tagRelationService.getTagsForObject(copied)).extracting(Tag::getValue).containsExactly("fantasy");
        assertThat(plan().isEmpty()).isTrue();
    }

    // --- the data of the attributes is merged place by place

    private void setEntryData(LorebookEntry entry, String json) throws Exception {
        LorebookEntry stored = lorebookEntryService.find(entry.getId());
        stored.getAttributes().add("tracker", JsonParser.parseString(json));
        lorebookEntryService.save(stored);
    }

    private String entryData(LorebookEntry entry) throws Exception {
        return lorebookEntryService.find(entry.getId()).getAttributes().get("tracker").toString();
    }

    private void setData(ChatMessage message, String json) throws Exception {
        ChatMessage stored = chatMessageService.find(message.getId());
        stored.getAttributes().add("templateVariables", JsonParser.parseString(json));
        chatMessageService.save(stored);
    }

    private String dataOf(ChatMessage message) throws Exception {
        return chatMessageService.find(message.getId()).getAttributes().get("templateVariables").toString();
    }

    @Test
    void theDataOfAnEntryBothChangedInOtherPlacesComesIntoTheCopyNextToTheAgentsAndTheReviewShowsTheAgentsAlone() throws Exception {
        setEntryData(entryOf(copyRoot(), "dragon"), "{\"seen\":3}");
        pause();
        setEntryData(entryOf(root, "dragon"), "{\"color\":\"red\"}");

        resume();

        assertThat(entryData(entryOf(copyRoot(), "dragon"))).isEqualTo("{\"color\":\"red\",\"seen\":3}");
        assertThat(report()).doesNotContain("Not taken over");
        MergePlan plan = plan();
        Item item = item(plan, Kind.ENTRY, Change.CHANGED, "dragon");
        assertThat(item.conflict()).isFalse();
        assertThat(field(item, "attributes_tracker").base()).isEqualTo("{\"color\":\"red\"}");

        applyAll(plan);

        assertThat(entryData(entryOf(root, "dragon"))).isEqualTo("{\"color\":\"red\",\"seen\":3}");
    }

    @Test
    void theDataOfAnEntryTheUserChangedAloneComesIntoTheCopy() throws Exception {
        pause();
        setEntryData(entryOf(root, "castle"), "{\"color\":\"grey\"}");

        resume();

        assertThat(entryData(entryOf(copyRoot(), "castle"))).isEqualTo("{\"color\":\"grey\"}");
        assertThat(plan().isEmpty()).as("copy and book agree").isTrue();
    }

    @Test
    void theDataOfAPartBothChangedTheSameKeyIsAConflictTheCopyKeepsTheAgents() throws Exception {
        setData(inCopy("there was a dragon"), "{\"mood\":\"dark\",\"a\":1}");
        pause();
        setData(inBook("there was a dragon"), "{\"mood\":\"light\",\"b\":2}");

        resume();

        assertThat(report()).contains("Not taken over");
        assertThat(dataOf(inCopy("there was a dragon"))).isEqualTo("{\"mood\":\"dark\",\"a\":1}");
        assertThat(item(plan(), Kind.MESSAGE, Change.CHANGED, "there was").conflict()).isTrue();
    }

    // --- telling the agent

    @Test
    void theReportWaitsToBeToldOnceAndStaysForTheTool() throws Exception {
        pause();
        edit(inBook("and a castle"), "and a keep");
        resume();

        assertThat(fileSystemService.takePendingSync(run)).contains("While the run was paused");
        assertThat(fileSystemService.takePendingSync(run)).as("told once").isNull();
        assertThat(report()).contains("and a keep");
        assertThat(new SyncToolsImpl(() -> {
            try {
                return report();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        })).isNotNull();
    }

    @Test
    void aResumeWithoutChangesClearsTheReportOfTheLastOne() throws Exception {
        pause();
        edit(inBook("and a castle"), "and a keep");
        resume();
        assertThat(report()).isNotNull();

        pauseAndResume();

        assertThat(report()).isNull();
    }

    @Test
    void theCopyIsSavedBeforeItTakesOverTheBook() throws Exception {
        pause();
        edit(inBook("and a castle"), "and a keep");

        resume();

        assertThat(checkpointService.list(run)).extracting(CheckpointStore.Info::label).contains("before the changes of the book");
    }

    @Test
    void aRunWithoutChangesDoesNotSaveACheckpointForTheSync() throws Exception {
        pauseAndResume();

        assertThat(checkpointService.list(run)).extracting(CheckpointStore.Info::label).doesNotContain("before the changes of the book");
    }
}
