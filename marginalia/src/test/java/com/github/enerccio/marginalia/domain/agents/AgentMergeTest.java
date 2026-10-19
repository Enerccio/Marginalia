package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.AgentMergeService.MergeResult;
import com.github.enerccio.marginalia.domain.service.AgentMergeService.MergeSelection;
import com.github.enerccio.marginalia.domain.service.agent.MergeDiff;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Change;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Field;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Item;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Kind;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Taking over what a run changed: the plan (the three-way comparison of the base, the copy and the book as it is now) and the
 * apply of the items the user picked, in place.
 */
class AgentMergeTest extends AgentTestBase {

    @Autowired
    private AgentMergeService mergeService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private SummaryService summaryService;

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
     * The agent is done; the book is the user's again, and only then can they change it.
     */
    private AgentRun finish() throws Exception {
        AgentRun current = agentRunService.find(run.getId());
        run = current.getStatus() == AgentRunStatus.DONE ? current : agentRunService.transition(current, AgentRunStatus.DONE, "report");
        return run;
    }

    private MergePlan plan() throws Exception {
        finish();
        return mergeService.plan(run);
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

    // --- the plan

    @Test
    void aCopyThatWasNotChangedHasNothingToTakeOver() throws Exception {
        MergePlan plan = plan();

        assertThat(plan.isEmpty()).isTrue();
        assertThat(plan.bookChanged()).isFalse();
    }

    @Test
    void anEditedPartIsAChangeWithTheThreeValues() throws Exception {
        edit(inCopy("there was a dragon"), "there was a small dragon");

        MergePlan plan = plan();

        assertThat(plan.items()).hasSize(1);
        Item item = item(plan, Kind.MESSAGE, Change.CHANGED, "there was");
        Field response = field(item, "response");
        assertThat(response.base()).isEqualTo("there was a dragon");
        assertThat(response.agent()).isEqualTo("there was a small dragon");
        assertThat(response.book()).isEqualTo("there was a dragon");
        assertThat(item.conflict()).isFalse();
        assertThat(item.label()).startsWith("2: ");
    }

    @Test
    void whatTheUserChangedAloneIsNotAChangeOfTheRun() throws Exception {
        finish();
        edit(inBook("and a castle"), "and a keep");

        MergePlan plan = plan();

        assertThat(plan.isEmpty()).isTrue();
        assertThat(plan.bookChanged()).isTrue();
    }

    @Test
    void bothChangingAPartToDifferentTextsIsAConflict() throws Exception {
        edit(inCopy("there was a dragon"), "there was a small dragon");
        finish();
        edit(inBook("there was a dragon"), "there lived a dragon");

        MergePlan plan = plan();

        Item item = item(plan, Kind.MESSAGE, Change.CHANGED, "there");
        assertThat(item.conflict()).isTrue();
        assertThat(field(item, "response").conflict()).isTrue();
        assertThat(field(item, "response").book()).isEqualTo("there lived a dragon");
    }

    @Test
    void bothChangingAPartToTheSameTextIsNotAChange() throws Exception {
        edit(inCopy("there was a dragon"), "there lived a dragon");
        finish();
        edit(inBook("there was a dragon"), "there lived a dragon");

        assertThat(plan().isEmpty()).isTrue();
    }

    @Test
    void theAgentAndTheUserChangingOtherFieldsOfAPartIsNoConflict() throws Exception {
        ChatMessage copied = chatMessageService.find(inCopy("there was a dragon").getId());
        copied.setSceneSetting("a cave");
        chatMessageService.save(copied);
        finish();
        edit(inBook("there was a dragon"), "there lived a dragon");

        Item item = item(plan(), Kind.MESSAGE, Change.CHANGED, "there");

        assertThat(item.conflict()).isFalse();
        assertThat(item.fields()).extracting(Field::name).containsExactly("sceneSetting");
    }

    @Test
    void aNewPartFollowsItsParentAndANewPartUnderItDependsOnIt() throws Exception {
        ChatMessage fourth = addTo(copy(), inCopy("and a castle"), "The end.");
        addTo(copy(), fourth, "Or not.");

        MergePlan plan = plan();

        Item end = item(plan, Kind.MESSAGE, Change.ADDED, "The end");
        Item or = item(plan, Kind.MESSAGE, Change.ADDED, "Or not");
        assertThat(end.dependsOn()).isNull();
        assertThat(or.dependsOn()).isEqualTo(end.id());
        assertThat(plan.items().indexOf(end)).isLessThan(plan.items().indexOf(or));
        assertThat(MergeDiff.dependents(plan, end.id())).containsExactly(or.id());
        assertThat(MergeDiff.chain(plan, or)).containsExactly(end.id(), or.id());
    }

    @Test
    void aNewPartWhoseParentTheUserDeletedCannotBeTakenOver() throws Exception {
        addTo(copy(), inCopy("and a castle"), "The end.");
        finish();
        ChatMessage castle = inBook("and a castle");
        chatMessageService.deleteNodeAndMigrateChildren(castle, manuscriptService.find(book.getId()), false);

        Item item = item(plan(), Kind.MESSAGE, Change.ADDED, "The end");

        assertThat(item.problem()).isEqualTo(MergePlan.PROBLEM_NEEDS_PARENT);
        assertThat(item.applicable()).isFalse();
    }

    @Test
    void aDeletedPartChangedByTheUserIsAConflict() throws Exception {
        chatMessageService.deleteNodeAndMigrateChildren(inCopy("and a castle"), copy(), false);
        finish();
        edit(inBook("and a castle"), "and a keep");

        Item item = item(plan(), Kind.MESSAGE, Change.DELETED, "and a castle");

        assertThat(item.conflict()).isTrue();
    }

    @Test
    void lorebookChangesAreChangesOfEntriesAndLorebooks() throws Exception {
        LorebookEntry dragon = entryOf(copyRoot(), "dragon");
        dragon.setPayload("a green dragon");
        lorebookEntryService.update(dragon);
        lorebookEntryService.deleteEntry(copyRoot(), entryOf(copyRoot(), "castle"));
        lorebookEntryService.create(subbook(copyRoot(), "places"), newEntry("inn", "a warm inn"));
        Lorebook swamp = lorebookService.createSubbook(copyRoot(), "swamp");

        MergePlan plan = plan();

        assertThat(field(item(plan, Kind.ENTRY, Change.CHANGED, "dragon"), "payload").agent()).isEqualTo("a green dragon");
        assertThat(item(plan, Kind.ENTRY, Change.DELETED, "castle").conflict()).isFalse();
        Item inn = item(plan, Kind.ENTRY, Change.ADDED, "inn");
        assertThat(inn.label()).isEqualTo("places / inn");
        assertThat(inn.dependsOn()).isNull();
        Item added = item(plan, Kind.LOREBOOK, Change.ADDED, "swamp");
        assertThat(added.sourceUuid()).isEqualTo(swamp.getUuid());
        Item parent = item(plan, Kind.LOREBOOK, Change.CHANGED, "lore");
        assertThat(field(parent, "subbooks").agent()).contains(MergeDiff.NEW + swamp.getUuid());
    }

    private LorebookEntry newEntry(String name, String payload) {
        LorebookEntry entry = new LorebookEntry();
        entry.setName(name);
        entry.setPayload(payload);
        return entry;
    }

    @Test
    void anEntryOfANewLorebookDependsOnTheLorebook() throws Exception {
        Lorebook swamp = lorebookService.createSubbook(copyRoot(), "swamp");
        lorebookEntryService.create(swamp, newEntry("frog", "a loud frog"));

        MergePlan plan = plan();

        Item frog = item(plan, Kind.ENTRY, Change.ADDED, "frog");
        assertThat(frog.dependsOn()).isEqualTo(item(plan, Kind.LOREBOOK, Change.ADDED, "swamp").id());
    }

    @Test
    void aLorebookOfAnotherBookIsReportedAsShared() throws Exception {
        Manuscript other = new Manuscript();
        other.setName("the other book");
        other.setLorebook(root);
        manuscriptService.save(other);
        LorebookEntry dragon = entryOf(copyRoot(), "dragon");
        dragon.setPayload("a green dragon");
        lorebookEntryService.update(dragon);

        Item item = item(plan(), Kind.ENTRY, Change.CHANGED, "dragon");

        assertThat(item.sharedWith()).containsExactly("the other book");
    }

    @Test
    void theSettingsTheTagsAndThePluginDataOfTheBookAreChanges() throws Exception {
        Manuscript copied = manuscriptService.find(copy().getId());
        copied.setDescription("a longer tale");
        copied.getAttributes().add("arcs", JsonParser.parseString("{\"acts\":3}"));
        manuscriptService.save(copied);
        tagRelationService.createRelation(tagService.getOrCreateForUser("fantasy"), copied);

        MergePlan plan = plan();

        assertThat(field(item(plan, Kind.BOOK, Change.CHANGED, "description"), "description").agent()).isEqualTo("a longer tale");
        assertThat(field(item(plan, Kind.BOOK, Change.CHANGED, "attributes_arcs"), "attributes_arcs").agent()).isEqualTo("{\"acts\":3}");
        assertThat(field(item(plan, Kind.BOOK, Change.CHANGED, "tags"), "tags").agent()).isEqualTo("fantasy");
    }

    @Test
    void theCopyWritesWithTheModelOfTheAgentAndTheBookKeepsItsOwn() throws Exception {
        // the book is the user's again only when the run is over
        edit(inCopy("there was a dragon"), "there was a small dragon");
        finish();
        AI bookAi = createAI();
        Protocol bookProtocol = protocol();
        Manuscript stored = manuscriptService.find(book.getId());
        stored.setAi(bookAi);
        stored.setProtocol(bookProtocol);
        manuscriptService.save(stored);

        agentRunService.useAgentModel(run);

        var main = agentService.resolveProfile(agentService.findForUser(agent.getUuid()).getMainProfile());
        Manuscript copied = manuscriptService.find(copy().getId());
        assertThat(copied.getAi().getUuid()).isEqualTo(main.ai().getUuid()).isNotEqualTo(bookAi.getUuid());
        assertThat(copied.getProtocol().getUuid()).isEqualTo(main.protocol().getUuid()).isNotEqualTo(bookProtocol.getUuid());

        MergePlan plan = plan();
        assertThat(plan.items()).as("the model of the copy is not a change of the run").extracting(Item::kind).containsOnly(Kind.MESSAGE);
        applyAll(plan);

        Manuscript after = manuscriptService.find(book.getId());
        assertThat(after.getAi().getUuid()).isEqualTo(bookAi.getUuid());
        assertThat(after.getProtocol().getUuid()).isEqualTo(bookProtocol.getUuid());
    }

    @Test
    void anAgentThatDoesNotUseItsModelLeavesTheCopyWithTheModelOfTheBook() throws Exception {
        finish();
        AI bookAi = createAI();
        Protocol bookProtocol = protocol();
        Manuscript stored = manuscriptService.find(book.getId());
        stored.setAi(bookAi);
        stored.setProtocol(bookProtocol);
        manuscriptService.save(stored);
        var main = agentService.resolveProfile(agentService.findForUser(agent.getUuid()).getMainProfile());

        agentRunService.useAgentModel(run);
        assertThat(manuscriptService.find(copy().getId()).getAi().getUuid()).as("the flag is on").isEqualTo(main.ai().getUuid());

        Agent changed = agentService.findForUser(agent.getUuid());
        changed.setUseAgentModel(false);
        agentService.save(changed);
        agentRunService.useAgentModel(run);

        Manuscript copied = manuscriptService.find(copy().getId());
        assertThat(copied.getAi().getUuid()).as("back to the book's").isEqualTo(bookAi.getUuid());
        assertThat(copied.getProtocol().getUuid()).isEqualTo(bookProtocol.getUuid());
    }

    // --- the apply

    @Test
    void anEditedPartIsTakenOverInPlaceAndTheRunIsApplied() throws Exception {
        String uuid = inBook("there was a dragon").getUuid();
        edit(inCopy("there was a dragon"), "there was a small dragon");
        MergePlan plan = plan();

        MergeResult result = applyAll(plan);

        assertThat(inBook("there was a small dragon").getUuid()).as("the part is the same one").isEqualTo(uuid);
        assertThat(bookTexts()).containsExactly("Once upon a time", "and a castle", "there was a small dragon");
        assertThat(inBook("there was a small dragon").isEdited()).isTrue();
        assertThat(result.run().getStatus()).isEqualTo(AgentRunStatus.APPLIED);
        assertThat(result.run().getWorkingCopy()).as("the copy is gone").isNull();
        assertThat(result.applied()).containsEntry(Kind.MESSAGE, 1);
        assertThat(result.total()).isEqualTo(1);
        assertThat(result.skipped()).isEmpty();
        assertThat(result.backup()).as("the book was saved first").isNotNull();
        assertThat(new File(result.backup())).exists();
    }

    @Test
    void whatTheUserChangedMeanwhileSurvivesAnApplyThatDoesNotTouchIt() throws Exception {
        edit(inCopy("there was a dragon"), "there was a small dragon");
        finish();
        edit(inBook("and a castle"), "and a keep");
        MergePlan plan = plan();

        applyAll(plan);

        assertThat(bookTexts()).containsExactly("Once upon a time", "and a keep", "there was a small dragon");
    }

    @Test
    void anAgentPartIsAddedAfterTheMappedParentAndTheNewPartsFollowEachOther() throws Exception {
        ChatMessage fourth = addTo(copy(), inCopy("and a castle"), "The end.");
        addTo(copy(), fourth, "Or not.");
        MergePlan plan = plan();

        MergeResult result = applyAll(plan);

        assertThat(result.applied()).containsEntry(Kind.MESSAGE, 2);
        ChatMessage end = inBook("The end.");
        assertThat(end.getParent().getUuid()).isEqualTo(inBook("and a castle").getUuid());
        assertThat(inBook("Or not.").getParent().getUuid()).isEqualTo(end.getUuid());
        assertThat(end.getParentScript().getId()).isEqualTo(book.getId());
    }

    @Test
    void aDeletedPartIsDeletedFromTheBookAndItsChildrenMoveUp() throws Exception {
        chatMessageService.deleteNodeAndMigrateChildren(inCopy("there was a dragon"), copy(), false);
        MergePlan plan = plan();

        applyAll(plan);

        assertThat(bookTexts()).containsExactly("Once upon a time", "and a castle");
        assertThat(inBook("and a castle").getParent().getUuid()).isEqualTo(inBook("Once upon a time").getUuid());
    }

    @Test
    void thePickedItemsAreAppliedAndTheOthersAreLeft() throws Exception {
        edit(inCopy("there was a dragon"), "there was a small dragon");
        edit(inCopy("and a castle"), "and a big castle");
        MergePlan plan = plan();
        Item castle = item(plan, Kind.MESSAGE, Change.CHANGED, "castle");

        MergeResult result = mergeService.apply(run, new MergeSelection(Set.of(castle.id()), Map.of(), false));

        assertThat(bookTexts()).containsExactly("Once upon a time", "and a big castle", "there was a dragon");
        assertThat(result.total()).isEqualTo(1);
    }

    @Test
    void theTextTheUserWroteIsAppliedInPlaceOfTheAgents() throws Exception {
        edit(inCopy("there was a dragon"), "there was a small dragon");
        finish();
        edit(inBook("there was a dragon"), "there lived a dragon");
        MergePlan plan = plan();
        Item item = item(plan, Kind.MESSAGE, Change.CHANGED, "there");
        assertThat(item.conflict()).isTrue();

        mergeService.apply(run, new MergeSelection(Set.of(item.id()),
                Map.of(item.id(), Map.of("response", "there lived a small dragon")), false));

        assertThat(bookTexts()).containsExactly("Once upon a time", "and a castle", "there lived a small dragon");
    }

    @Test
    void aPartThatNeedsAPartThatIsNotPickedCannotBeAppliedAlone() throws Exception {
        ChatMessage fourth = addTo(copy(), inCopy("and a castle"), "The end.");
        addTo(copy(), fourth, "Or not.");
        MergePlan plan = plan();
        Item or = item(plan, Kind.MESSAGE, Change.ADDED, "Or not");

        assertThatThrownBy(() -> mergeService.apply(run, new MergeSelection(Set.of(or.id()), Map.of(), false)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(agentRunService.find(run.getId()).getStatus()).as("nothing happened").isEqualTo(AgentRunStatus.DONE);
        assertThat(bookTexts()).hasSize(3);
    }

    @Test
    void theBookCanSwitchToTheBranchTheAgentEndedOn() throws Exception {
        ChatMessage fourth = addTo(copy(), inCopy("and a castle"), "The end.");
        Manuscript copied = manuscriptService.find(copy().getId());
        copied.setActiveLeaf(chatMessageService.find(fourth.getId()));
        manuscriptService.save(copied);
        MergePlan plan = plan();

        mergeService.apply(run, new MergeSelection(plan.items().stream().map(Item::id).collect(Collectors.toSet()), Map.of(), true));

        assertThat(manuscriptService.find(book.getId()).getActiveLeaf().getResponse()).isEqualTo("The end.");
    }

    @Test
    void theLorebookIsChangedInPlaceSoOtherBooksKeepTheirLinks() throws Exception {
        Manuscript other = new Manuscript();
        other.setName("the other book");
        other.setLorebook(root);
        manuscriptService.save(other);
        LorebookEntry dragon = entryOf(copyRoot(), "dragon");
        String dragonUuid = entryOf(root, "dragon").getUuid();
        dragon.setPayload("a green dragon");
        dragon.setName("wyrm");
        lorebookEntryService.update(dragon);
        lorebookEntryService.deleteEntry(copyRoot(), entryOf(copyRoot(), "castle"));
        lorebookEntryService.create(subbook(copyRoot(), "places"), newEntry("inn", "a warm inn"));
        Lorebook swamp = lorebookService.createSubbook(copyRoot(), "swamp");
        lorebookEntryService.create(swamp, newEntry("frog", "a loud frog"));
        MergePlan plan = plan();

        MergeResult result = applyAll(plan);

        assertThat(manuscriptService.find(other.getId()).getLorebook().getId()).isEqualTo(root.getId());
        assertThat(entryOf(root, "wyrm").getUuid()).isEqualTo(dragonUuid);
        assertThat(entryOf(root, "wyrm").getPayload()).isEqualTo("a green dragon");
        assertThat(entryOf(root, "castle")).isNull();
        assertThat(entryOf(subbook(root, "places"), "inn")).isNotNull();
        Lorebook madeSwamp = subbook(root, "swamp");
        assertThat(madeSwamp).isNotNull();
        assertThat(madeSwamp.isAgentic()).isFalse();
        assertThat(entryOf(madeSwamp, "frog").getPayload()).isEqualTo("a loud frog");
        assertThat(result.skipped()).isEmpty();
    }

    @Test
    void aDeletedLorebookIsRemovedFromItsParentAndDeleted() throws Exception {
        Lorebook copiedPlaces = subbook(copyRoot(), "places");
        lorebookEntryService.deleteEntry(copiedPlaces, entryOf(copiedPlaces, "village"));
        lorebookService.removeSubbook(copyRoot(), copiedPlaces);
        lorebookService.delete(copiedPlaces, false);
        MergePlan plan = plan();

        applyAll(plan);

        assertThat(subbook(root, "places")).isNull();
        assertThat(lorebookService.find(places.getId()).isDeleted()).as("to the trash").isTrue();
    }

    @Test
    void theSettingsTheTagsAndThePluginDataAreTakenOver() throws Exception {
        Manuscript copied = manuscriptService.find(copy().getId());
        copied.setDescription("a longer tale");
        copied.getAttributes().add("arcs", JsonParser.parseString("{\"acts\":3}"));
        manuscriptService.save(copied);
        tagRelationService.createRelation(tagService.getOrCreateForUser("fantasy"), copied);
        MergePlan plan = plan();

        applyAll(plan);

        Manuscript taken = manuscriptService.find(book.getId());
        assertThat(taken.getDescription()).isEqualTo("a longer tale");
        assertThat(taken.getAttributes().get("arcs").toString()).isEqualTo("{\"acts\":3}");
        assertThat(tagRelationService.getTagsForObject(taken)).extracting(Tag::getValue).containsExactly("fantasy");
    }

    @Test
    void aSummaryTheAgentWroteIsTakenOver() throws Exception {
        Summary summary = new Summary();
        summary.setSummary("A dragon appears.");
        summary.setSummaryMessageHash("hash");
        summary = summaryService.save(summary);
        ChatMessage message = chatMessageService.find(inCopy("there was a dragon").getId());
        message.setSummary(summary);
        chatMessageService.save(message);
        MergePlan plan = plan();
        Item item = item(plan, Kind.MESSAGE, Change.CHANGED, "there was");
        assertThat(field(item, "summary").agent()).isEqualTo("A dragon appears.");

        mergeService.apply(run, new MergeSelection(Set.of(item.id()), Map.of(item.id(), Map.of("summary", "A red dragon appears.")), false));

        Summary taken = summaryService.find(inBook("there was a dragon").getSummary());
        assertThat(taken.getSummary()).isEqualTo("A red dragon appears.");
        assertThat(taken.getSummaryMessageHash()).isEqualTo("hash");
    }

    @Test
    void aRunThatIsNotFinishedHasNothingToTakeOver() throws Exception {
        assertThatThrownBy(() -> mergeService.plan(run)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void applyingNothingIsRefused() throws Exception {
        finish();

        assertThatThrownBy(() -> mergeService.apply(run, new MergeSelection(Set.of(), Map.of(), false)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aStoppedAndKeptRunCanBeAppliedToo() throws Exception {
        edit(inCopy("there was a dragon"), "there was a small dragon");
        run = agentRunService.stop(agentRunService.pause(agentRunService.find(run.getId())), true);
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.STOPPED_KEPT);
        MergePlan plan = mergeService.plan(run);

        applyAll(plan);

        assertThat(bookTexts()).contains("there was a small dragon");
    }

    @Test
    void anotherUserCannotTakeOverTheRun() throws Exception {
        edit(inCopy("there was a dragon"), "there was a small dragon");
        finish();
        loginAgentUser();

        assertThatThrownBy(() -> mergeService.plan(run)).isInstanceOf(IllegalArgumentException.class);
    }
}
