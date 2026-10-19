package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunPairKind;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.enerccio.marginalia.domain.collections.AgentRunStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Which item of the working copy is which item of the original ({@link AgentRunPair}), and the base snapshot of the
 * original the copy was made from.
 */
class AgentRunPairsTest extends AgentTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private AgentRunPairService pairService;

    @Autowired
    private AgentVirtualFileSystemService fileSystemService;

    @Autowired
    private JdbcTemplate jdbc;

    private Manuscript book;
    private Lorebook root;
    private Lorebook sub;
    private ChatMessage first;
    private ChatMessage second;
    private ChatMessage swipe;
    private Agent agent;

    @BeforeEach
    void createBook() throws Exception {
        loginAgentUser();
        sub = lorebook("sub");
        entry(sub, "sub entry");
        root = lorebook("root");
        entry(root, "first entry");
        entry(root, "second entry");
        root.setSubbooks(new ArrayList<>(List.of(sub)));
        root = lorebookService.save(root);

        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript.setLorebook(root);
        book = manuscriptService.save(manuscript);

        first = message(null, "Once upon a time");
        second = message(first, "there was a dragon");
        swipe = message(first, "there was a knight");
        Summary summary = new Summary();
        summary.setSummary("a dragon");
        summary = summaryService.save(summary);
        ChatMessage stored = chatMessageService.find(second.getId());
        stored.setSummary(summary);
        chatMessageService.save(stored);
        book = manuscriptService.find(book.getId());
        book.setActiveLeaf(chatMessageService.find(second.getId()));
        book = manuscriptService.save(book);

        agent = agentService.save(newAgent(book));
    }

    private ChatMessage message(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
    }

    private Lorebook lorebook(String name) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(uniqueName(name));
        lorebook.setSubbooks(new ArrayList<>());
        return lorebookService.save(lorebook);
    }

    private LorebookEntry entry(Lorebook lorebook, String name) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setPayload(name + " payload");
        return lorebookEntryService.save(entry);
    }

    private AgentRun reload(AgentRun run) throws Exception {
        return agentRunService.find(run.getId());
    }

    private Map<String, UuidPair> byCopy(AgentRun run, AgentRunPairKind kind) throws Exception {
        return pairService.getAll(run).stream().filter(pair -> pair.kind() == kind)
                .collect(Collectors.toMap(UuidPair::copyUuid, pair -> pair));
    }

    @Test
    void everyItemOfTheCopyHasExactlyOneOriginal() throws Exception {
        AgentRun run = reload(agentRunService.start(agent));
        Manuscript copy = run.getWorkingCopy();

        // messages, including the swipe
        List<ChatMessage> copiedMessages = chatMessageService.getAllMessages(copy);
        Map<String, UuidPair> messages = byCopy(run, AgentRunPairKind.MESSAGE);
        assertThat(messages.keySet()).containsExactlyInAnyOrderElementsOf(copiedMessages.stream().map(ChatMessage::getUuid).toList());
        Map<String, String> originalResponses = new HashMap<>();
        for (ChatMessage original : List.of(first, second, swipe)) {
            originalResponses.put(original.getUuid(), original.getResponse());
        }
        for (ChatMessage copied : copiedMessages) {
            String originalUuid = messages.get(copied.getUuid()).originalUuid();
            assertThat(originalResponses).as("the original of %s", copied.getResponse()).containsKey(originalUuid);
            assertThat(originalResponses.get(originalUuid)).isEqualTo(copied.getResponse());
        }
        assertThat(messages.values().stream().map(UuidPair::originalUuid)).doesNotHaveDuplicates()
                .containsExactlyInAnyOrder(first.getUuid(), second.getUuid(), swipe.getUuid());

        // the summary
        Map<String, UuidPair> summaries = byCopy(run, AgentRunPairKind.SUMMARY);
        Summary copiedSummary = copiedMessages.stream().map(ChatMessage::getSummary).filter(Objects::nonNull).findFirst().orElseThrow();
        Summary originalSummary = chatMessageService.find(second.getId()).getSummary();
        assertThat(summaries).containsOnlyKeys(copiedSummary.getUuid());
        assertThat(summaries.get(copiedSummary.getUuid()).originalUuid()).isEqualTo(originalSummary.getUuid());
        assertThat(copiedSummary.getUuid()).isNotEqualTo(originalSummary.getUuid());

        // the lorebooks and their entries
        Lorebook copiedRoot = lorebookService.find(copy.getLorebook());
        Lorebook copiedSub = lorebookService.getSubbooks(copiedRoot).getFirst();
        Map<String, UuidPair> lorebooks = byCopy(run, AgentRunPairKind.LOREBOOK);
        assertThat(lorebooks).containsOnlyKeys(copiedRoot.getUuid(), copiedSub.getUuid());
        assertThat(lorebooks.get(copiedRoot.getUuid()).originalUuid()).isEqualTo(root.getUuid());
        assertThat(lorebooks.get(copiedSub.getUuid()).originalUuid()).isEqualTo(sub.getUuid());

        Map<String, UuidPair> entries = byCopy(run, AgentRunPairKind.ENTRY);
        List<LorebookEntry> copiedEntries = new ArrayList<>(lorebookEntryService.getEntriesForLorebook(copiedRoot));
        copiedEntries.addAll(lorebookEntryService.getEntriesForLorebook(copiedSub));
        assertThat(entries.keySet()).containsExactlyInAnyOrderElementsOf(copiedEntries.stream().map(LorebookEntry::getUuid).toList());
        List<LorebookEntry> originalEntries = new ArrayList<>(lorebookEntryService.getEntriesForLorebook(root));
        originalEntries.addAll(lorebookEntryService.getEntriesForLorebook(sub));
        Map<String, String> originalNames = originalEntries.stream().collect(Collectors.toMap(LorebookEntry::getUuid, LorebookEntry::getName));
        for (LorebookEntry copied : copiedEntries) {
            assertThat(originalNames.get(entries.get(copied.getUuid()).originalUuid())).isEqualTo(copied.getName());
        }
        assertThat(entries.values().stream().map(UuidPair::originalUuid)).doesNotHaveDuplicates().hasSize(3);

        assertThat(pairService.count(run)).isEqualTo(3 + 1 + 2 + 3);
    }

    @Test
    void pairsAreFoundBothWays() throws Exception {
        AgentRun run = reload(agentRunService.start(agent));
        ChatMessage copied = chatMessageService.getAllMessages(run.getWorkingCopy()).stream()
                .filter(m -> "there was a knight".equals(m.getResponse())).findFirst().orElseThrow();

        UuidPair byCopy = pairService.findByCopy(run, copied.getUuid()).orElseThrow();
        UuidPair byOriginal = pairService.findByOriginal(run, swipe.getUuid()).orElseThrow();

        assertThat(byCopy).isEqualTo(byOriginal);
        assertThat(byCopy.kind()).isEqualTo(AgentRunPairKind.MESSAGE);
        assertThat(pairService.findByCopy(run, swipe.getUuid())).as("an uuid of the original is not one of the copy").isEmpty();
        assertThat(pairService.findByOriginal(run, copied.getUuid())).isEmpty();
    }

    @Test
    void bookWithoutALorebookHasPairsOfItsPartsOnly() throws Exception {
        Manuscript plain = manuscript();
        ChatMessage part = new ChatMessage();
        part.setResponse("alone");
        chatMessageService.createRoot(plain, part);
        AgentRun run = reload(agentRunService.start(agentService.save(newAgent(plain))));

        assertThat(pairService.getAll(run)).extracting(UuidPair::kind).containsExactly(AgentRunPairKind.MESSAGE);
    }

    @Test
    void lorebookSharedByTwoBooksIsCopiedForTheRunAlone() throws Exception {
        Manuscript other = new Manuscript();
        other.setName(uniqueName("other"));
        other.setLorebook(root);
        manuscriptService.save(other);

        AgentRun run = reload(agentRunService.start(agent));

        assertThat(byCopy(run, AgentRunPairKind.LOREBOOK).values()).extracting(UuidPair::originalUuid)
                .containsExactlyInAnyOrder(root.getUuid(), sub.getUuid());
        assertThat(lorebookService.find(root.getId()).isAgentic()).isFalse();
    }

    @Test
    void baseIsTheOriginalAsItWasCopied() throws Exception {
        AgentRun run = reload(agentRunService.start(agent));

        JsonObject base = fileSystemService.readBase(run);

        assertThat(base).isNotNull();
        assertThat(base.get("uuid").getAsString()).isEqualTo(book.getUuid());
        assertThat(base.get("activeLeafUuid").getAsString()).isEqualTo(second.getUuid());
        assertThat(uuids(base.getAsJsonArray("messages"))).containsExactlyInAnyOrder(first.getUuid(), second.getUuid(), swipe.getUuid());
        JsonArray lorebooks = base.getAsJsonArray("lorebooks");
        assertThat(uuids(lorebooks)).containsExactlyInAnyOrder(root.getUuid(), sub.getUuid());
        List<String> entryUuids = new ArrayList<>();
        for (JsonElement lorebook : lorebooks) {
            entryUuids.addAll(uuids(lorebook.getAsJsonObject().getAsJsonArray("entries")));
        }
        List<String> originalEntryUuids = new ArrayList<>();
        originalEntryUuids.addAll(lorebookEntryService.getEntriesForLorebook(root).stream().map(LorebookEntry::getUuid).toList());
        originalEntryUuids.addAll(lorebookEntryService.getEntriesForLorebook(sub).stream().map(LorebookEntry::getUuid).toList());
        assertThat(entryUuids).containsExactlyInAnyOrderElementsOf(originalEntryUuids);
    }

    @Test
    void baseKeepsWhatTheLorebookSaidWhenTheOriginalChangesLater() throws Exception {
        AgentRun run = reload(agentRunService.start(agent));

        // the lorebooks of the original are locked while the run works, so the user edits it while the run is paused
        agentRunService.pause(run);
        LorebookEntry changed = lorebookEntryService.getEntriesForLorebook(root).getFirst();
        String before = changed.getPayload();
        changed.setPayload("changed by the user");
        lorebookEntryService.save(changed);

        JsonObject base = fileSystemService.readBase(run);
        JsonObject rootBook = null;
        for (JsonElement lorebook : base.getAsJsonArray("lorebooks")) {
            if (root.getUuid().equals(lorebook.getAsJsonObject().get("uuid").getAsString())) {
                rootBook = lorebook.getAsJsonObject();
            }
        }
        assertThat(rootBook).isNotNull();
        String payload = null;
        for (JsonElement entry : rootBook.getAsJsonArray("entries")) {
            if (changed.getUuid().equals(entry.getAsJsonObject().get("uuid").getAsString())) {
                payload = entry.getAsJsonObject().get("payload").getAsString();
            }
        }
        assertThat(payload).isEqualTo(before);
    }

    private long rowsOf(AgentRun run) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM agent_run_pairs WHERE run_id = ?", Long.class, run.getId());
    }

    private static List<String> uuids(JsonArray array) {
        List<String> uuids = new ArrayList<>();
        for (JsonElement element : array) {
            uuids.add(element.getAsJsonObject().get("uuid").getAsString());
        }
        return uuids;
    }

    @Test
    void pairsAndBaseGoWithTheCopy() throws Exception {
        AgentRun run = reload(agentRunService.start(agent));
        assertThat(pairService.count(run)).isPositive();
        assertThat(fileSystemService.readBase(run)).isNotNull();

        AgentRun done = agentRunService.transition(run, DONE, "report");

        assertThat(done.getWorkingCopy()).as("a finished run keeps its copy for the review").isNotNull();
        assertThat(pairService.count(done)).isPositive();

        AgentRun ended = agentRunService.stop(done, false);

        assertThat(ended.getWorkingCopy()).isNull();
        assertThat(pairService.count(ended)).isZero();
        assertThat(fileSystemService.readBase(ended)).isNull();
    }

    @Test
    void deletedRunLeavesNoPairs() throws Exception {
        AgentRun run = reload(agentRunService.start(agent));

        agentRunService.delete(run, true);

        assertThat(rowsOf(run)).isZero();
    }

    @Test
    void pausedRunKeepsItsPairsAndResumeDoesNotMakeNewOnes() throws Exception {
        AgentRun run = reload(agentRunService.start(agent));
        List<UuidPair> before = pairService.getAll(run);

        AgentRun resumed = agentRunService.resume(agentRunService.pause(run));

        assertThat(pairService.getAll(resumed)).containsExactlyElementsOf(before);
        assertThat(fileSystemService.readBase(resumed)).isNotNull();
    }

    @Test
    void resumeMakingANewCopyMakesItsPairs() throws Exception {
        AgentRun run = new AgentRun();
        run.setAgent(agent);
        run.setStatus(PAUSED);
        run = agentRunService.save(run);
        assertThat(pairService.count(run)).isZero();

        AgentRun resumed = agentRunService.resume(reload(run));

        assertThat(pairService.count(resumed)).isEqualTo(9);
        assertThat(fileSystemService.readBase(resumed)).isNotNull();
        Set<String> copied = chatMessageService.getAllMessages(resumed.getWorkingCopy()).stream().map(ChatMessage::getUuid).collect(Collectors.toSet());
        assertThat(byCopy(resumed, AgentRunPairKind.MESSAGE).keySet()).isEqualTo(copied);
    }

    @Test
    void everyRunHasPairsOfItsOwn() throws Exception {
        AgentRun failed = reload(agentRunService.start(agent));
        agentRunService.transition(failed, FAILED, "failed");

        AgentRun second = reload(agentRunService.start(agent));

        assertThat(pairService.count(second)).isEqualTo(9);
        assertThat(pairService.count(reload(failed))).isZero();
    }

    @Test
    void replaceRefusesAnItemOfTheCopyPairedTwice() throws Exception {
        AgentRun run = reload(agentRunService.start(agent));
        List<UuidPair> before = pairService.getAll(run);

        assertThatThrownBy(() -> pairService.replace(run, List.of(
                new UuidPair(AgentRunPairKind.MESSAGE, "copy", "one"),
                new UuidPair(AgentRunPairKind.MESSAGE, "copy", "two")))).isInstanceOf(IllegalArgumentException.class);

        assertThat(pairService.getAll(run)).containsExactlyElementsOf(before);
    }

    @Test
    void itemMadeByTheAgentHasNoOriginal() throws Exception {
        AgentRun run = reload(agentRunService.start(agent));

        pairService.replace(run, List.of(new UuidPair(AgentRunPairKind.MESSAGE, "made-by-the-agent", null)));

        assertThat(pairService.findByCopy(run, "made-by-the-agent").orElseThrow().originalUuid()).isNull();
        assertThat(pairService.count(run)).isEqualTo(1);
    }

    @Test
    void pairsOfARunAreOfItsOwnerOnly() throws Exception {
        AgentRun run = reload(agentRunService.start(agent));
        User other = createUser();
        loginAs(other);

        assertThatThrownBy(() -> pairService.getAll(run)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> pairService.findByOriginal(run, first.getUuid())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> pairService.replace(run, List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> fileSystemService.readBase(run)).isInstanceOf(IllegalArgumentException.class);

        loginAs(agent.getOwner());
        assertThat(pairService.count(run)).isEqualTo(9);
    }
}
