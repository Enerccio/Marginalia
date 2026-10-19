package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunPairKind;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.CheckpointService.CheckpointException;
import com.github.enerccio.marginalia.domain.service.CheckpointService.CheckpointFullException;
import com.github.enerccio.marginalia.domain.service.impl.CheckpointServiceImpl;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore.Info;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore.Kind;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;
import com.github.enerccio.marginalia.domain.service.agent.VirtualFileSystem;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The checkpoints of a run: restore points of the working copy and the workspace, one quota for all of them, a pinned
 * checkpoint of the start and a ring of automatic ones.
 */
class CheckpointServiceTest extends AgentTestBase {

    @Autowired
    private CheckpointService checkpointService;

    @Autowired
    private AgentRunPairService pairService;

    @Autowired
    private AgentVirtualFileSystemService fileSystemService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    private User user;
    private Manuscript book;
    private Agent agent;
    private AgentRun run;

    @BeforeEach
    void startRun() throws Exception {
        user = loginAgentUser();
        Lorebook lorebook = new Lorebook();
        lorebook.setName(uniqueName("lore"));
        lorebook.setSubbooks(new ArrayList<>());
        lorebook = lorebookService.save(lorebook);
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName("dragon");
        entry.setPayload("a red dragon");
        lorebookEntryService.save(entry);
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript.setLorebook(lorebook);
        book = manuscriptService.save(manuscript);
        ChatMessage first = new ChatMessage();
        first.setResponse("Once upon a time");
        first = chatMessageService.createRoot(book, first);
        ChatMessage second = new ChatMessage();
        second.setResponse("there was a dragon");
        chatMessageService.addChild(first, second);
        agent = agentService.save(newAgent(book));
        run = agentRunService.start(agent);
    }

    private Manuscript copy() throws Exception {
        return agentRunService.find(run.getId()).getWorkingCopy();
    }

    private List<String> texts() throws Exception {
        return chatMessageService.getAllMessages(copy()).stream().map(ChatMessage::getResponse).sorted().toList();
    }

    private ChatMessage partOfCopy(String text) throws Exception {
        return chatMessageService.getAllMessages(copy()).stream().filter(m -> text.equals(m.getResponse())).findFirst().orElseThrow();
    }

    private ChatMessage add(String parentText, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return chatMessageService.addChild(partOfCopy(parentText), message);
    }

    private void edit(String from, String to) throws Exception {
        ChatMessage message = chatMessageService.find(partOfCopy(from).getId());
        message.setResponse(to);
        chatMessageService.save(message);
    }

    private VirtualFileSystem files() throws Exception {
        return fileSystemService.open(run);
    }

    private void write(String path, String text) throws Exception {
        VirtualFileSystem fs = files();
        fs.writeText(fs.parse(path), text, false);
    }

    private CheckpointStore store() throws Exception {
        return new CheckpointStore(fileSystemService.getFolder(run).checkpoints());
    }

    // --- making ---

    @Test
    void theStartCheckpointHoldsTheCopyThePairsAndTheWorkspace() throws Exception {
        write("/notes.txt", "remember the dragon");

        Info start = checkpointService.ensureStart(run);

        assertThat(start.kind()).isEqualTo(Kind.START);
        assertThat(start.id()).isEqualTo("0001");
        assertThat(start.workspaceFiles()).isEqualTo(1);
        assertThat(start.bytes()).isPositive();
        assertThat(checkpointService.ensureStart(run)).as("made once").isEqualTo(start);
        assertThat(checkpointService.list(run)).containsExactly(start);
        try (CheckpointStore.Snapshot snapshot = store().open(start.id())) {
            assertThat(snapshot.manuscript().get("name").getAsString()).isEqualTo(book.getName());
            assertThat(snapshot.manuscript().getAsJsonArray("lorebooks")).hasSize(1);
            assertThat(snapshot.pairs()).extracting(UuidPair::kind).contains(AgentRunPairKind.MESSAGE, AgentRunPairKind.LOREBOOK,
                    AgentRunPairKind.ENTRY);
            assertThat(snapshot.zip().getEntry("workspace/notes.txt")).isNotNull();
        }
    }

    @Test
    void theNumbersAreGivenByTheCodeAndTheLabelIsCleaned() throws Exception {
        checkpointService.ensureStart(run);

        Info manual = checkpointService.makeManual(run, "  before\nthe  bulk\treplace " + "x".repeat(200)).checkpoint();

        assertThat(manual.id()).isEqualTo("0002");
        assertThat(manual.kind()).isEqualTo(Kind.MANUAL);
        assertThat(manual.label()).startsWith("before the bulk replace x").hasSize(CheckpointStore.MAX_LABEL_LENGTH).endsWith("...");
        assertThat(CheckpointStore.normalizeId("2")).isEqualTo("0002");
        assertThat(CheckpointStore.normalizeId("0002.zip")).isEqualTo("0002");
        assertThat(CheckpointStore.normalizeId("../0002")).isNull();
        assertThat(CheckpointStore.normalizeId("two")).isNull();
    }

    @Test
    void theRingKeepsTheLastFiveAutomaticOnesAndNeverTheStartOrTheOnesMadeOnPurpose() throws Exception {
        checkpointService.ensureStart(run);
        checkpointService.makeManual(run, "mine");
        List<String> made = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            made.add(checkpointService.makeAutomatic(run, "round " + i).checkpoint().id());
        }

        List<Info> all = checkpointService.list(run);

        assertThat(all).extracting(Info::kind).containsExactly(Kind.START, Kind.MANUAL, Kind.AUTOMATIC, Kind.AUTOMATIC, Kind.AUTOMATIC,
                Kind.AUTOMATIC, Kind.AUTOMATIC);
        assertThat(all.stream().filter(info -> info.kind() == Kind.AUTOMATIC).map(Info::id).toList()).isEqualTo(made.subList(2, 7));
    }

    // --- the quota ---

    private void quotaMb(int mb) {
        updatePermission(user, PermissionType.AGENTS, permission -> permission.setAgentsMaxCheckpointMb(mb));
    }

    private String random(int bytes) {
        byte[] data = new byte[bytes * 3 / 4];
        new SecureRandom().nextBytes(data);
        return Base64.getEncoder().encodeToString(data);
    }

    @Test
    void theQuotaIsTheOneOfThePermissionOrTheDefault() throws Exception {
        assertThat(checkpointService.usage(run).quotaBytes()).isEqualTo(CheckpointService.DEFAULT_QUOTA_BYTES);

        quotaMb(3);

        assertThat(checkpointService.usage(run).quotaBytes()).isEqualTo(3L * 1024 * 1024);
    }

    @Test
    void aCheckpointMadeOnPurposeThatDoesNotFitIsRefusedWithTheListAndNothingIsDeleted() throws Exception {
        quotaMb(2);
        write("/big.txt", random(250 * 1024));
        checkpointService.ensureStart(run);
        int made = 0;
        CheckpointFullException refused = null;
        while (refused == null && made < 20) {
            try {
                checkpointService.makeManual(run, "manual " + made);
                made++;
            } catch (CheckpointFullException e) {
                refused = e;
            }
        }

        assertThat(refused).as("the quota is used up after a few").isNotNull();
        assertThat(made).as("room is kept for the ring").isBetween(1, 5);
        assertThat(refused.getMessage()).contains("needs").contains("are kept for the automatic ones")
                .contains("checkpoint_delete").contains("0001 start").contains("manual 0");
        assertThat(checkpointService.list(run)).as("nothing was deleted").hasSize(made + 1);
        assertThat(store().list()).as("and nothing is left of the refused one").hasSize(made + 1);

        // what the agent does next: deletes one of its own and tries again
        checkpointService.delete(run, "0002", true);
        assertThat(checkpointService.makeManual(run, "again").checkpoint().kind()).isEqualTo(Kind.MANUAL);
    }

    @Test
    void aCheckpointThatCouldNeverFitSaysThatDeletingDoesNotHelp() throws Exception {
        quotaMb(1);
        write("/a.txt", random(700 * 1024));
        write("/b.txt", random(700 * 1024));

        assertThatThrownBy(() -> checkpointService.ensureStart(run)).isInstanceOf(CheckpointFullException.class)
                .hasMessageContaining("more than the");
        assertThatThrownBy(() -> checkpointService.makeManual(run, "x")).isInstanceOf(CheckpointFullException.class)
                .hasMessageContaining("deleting does not help");
        assertThat(store().list()).as("nothing is left of the refused ones").isEmpty();
    }

    @Test
    void anAutomaticCheckpointPushesOutOlderAutomaticOnesToFitButNotTheOnesMadeOnPurpose() throws Exception {
        quotaMb(2);
        write("/big.txt", random(250 * 1024));
        checkpointService.ensureStart(run);
        for (int i = 0; i < 4; i++) {
            checkpointService.makeAutomatic(run, "round " + i);
        }
        Info start = checkpointService.list(run).getFirst();

        // an automatic one is never refused for room the ring itself takes
        for (int i = 0; i < 12; i++) {
            checkpointService.makeAutomatic(run, "later " + i);
        }

        assertThat(checkpointService.list(run)).extracting(Info::kind).containsOnlyOnce(Kind.START);
        assertThat(checkpointService.list(run).stream().filter(info -> info.kind() == Kind.AUTOMATIC)).hasSize(CheckpointStore.RING);
        assertThat(checkpointService.list(run).getFirst()).isEqualTo(start);
        assertThat(checkpointService.usage(run).usedBytes()).isLessThanOrEqualTo(2L * 1024 * 1024);
    }

    @Test
    void theCheckpointsGoWhenTheRunLetsGoOfItsCopy() throws Exception {
        checkpointService.ensureStart(run);
        java.nio.file.Path folder = fileSystemService.getFolder(run).checkpoints();
        assertThat(folder).isDirectory();

        agentRunService.transition(run, com.github.enerccio.marginalia.domain.collections.AgentRunStatus.FAILED, "failed");

        assertThat(folder).doesNotExist();
    }

    // --- deleting ---

    @Test
    void theAgentCannotDeleteTheStartOrTheNewestButTheUserCan() throws Exception {
        Info start = checkpointService.ensureStart(run);
        Info one = checkpointService.makeManual(run, "one").checkpoint();
        Info two = checkpointService.makeManual(run, "two").checkpoint();

        assertThatThrownBy(() -> checkpointService.delete(run, start.id(), true)).isInstanceOf(CheckpointException.class)
                .hasMessageContaining("start");
        assertThatThrownBy(() -> checkpointService.delete(run, two.id(), true)).isInstanceOf(CheckpointException.class)
                .hasMessageContaining("newest");
        assertThatThrownBy(() -> checkpointService.delete(run, "0099", true)).isInstanceOf(CheckpointException.class)
                .hasMessageContaining("There is no checkpoint").hasMessageContaining("0001 start");
        assertThat(checkpointService.delete(run, one.id(), true)).isEqualTo(one);
        assertThat(checkpointService.list(run)).extracting(Info::id).containsExactly("0001", "0003");
        assertThat(checkpointService.delete(run, two.id(), false)).as("the user may").isEqualTo(two);
        assertThat(checkpointService.delete(run, start.id(), false).kind()).isEqualTo(Kind.START);
    }

    // --- restoring ---

    @Test
    void aRestoreBringsBackTheCopyTheWorkspaceAndTheHandlesOfWhatWasThere() throws Exception {
        String dragon = partOfCopy("there was a dragon").getUuid();
        String handleOfDragon = pairService.handleFor(run, AgentRunPairKind.MESSAGE, dragon);
        UuidPair dragonPair = pairService.findByCopy(run, dragon).orElseThrow();
        write("/notes.txt", "before");
        Info checkpoint = checkpointService.ensureStart(run);
        Long oldCopy = copy().getId();

        edit("there was a dragon", "there was a knight");
        ChatMessage later = add("there was a knight", "and the end");
        String handleOfLater = pairService.handleFor(run, AgentRunPairKind.MESSAGE, later.getUuid());
        write("/notes.txt", "after");
        write("/later.txt", "made after");
        assertThat(texts()).containsExactly("Once upon a time", "and the end", "there was a knight");

        CheckpointService.Restored restored = checkpointService.restore(run, checkpoint.id());

        assertThat(restored.checkpoint()).isEqualTo(checkpoint);
        assertThat(restored.removedFiles()).containsExactly("/later.txt");
        assertThat(restored.safety()).as("the state before is saved").isNotNull();
        assertThat(restored.safety().kind()).isEqualTo(Kind.AUTOMATIC);
        assertThat(restored.safety().label()).isEqualTo("before restoring 0001");
        assertThat(copy().getId()).isNotEqualTo(oldCopy);
        assertThat(restored.run().getWorkingCopy().getId()).isEqualTo(copy().getId());
        assertThat(manuscriptService.find(oldCopy)).as("the old copy is deleted for good").isNull();
        assertThat(texts()).containsExactly("Once upon a time", "there was a dragon");
        assertThat(files().readText(files().parse("/notes.txt"))).isEqualTo("before");
        assertThat(files().stat(files().parse("/later.txt"))).isNull();
        // the handle of the transcript names the same part, which has the original it had
        String newDragon = partOfCopy("there was a dragon").getUuid();
        assertThat(newDragon).isNotEqualTo(dragon);
        assertThat(pairService.findCopyUuid(run, handleOfDragon)).contains(newDragon);
        assertThat(pairService.findByCopy(run, newDragon).orElseThrow().originalUuid()).isEqualTo(dragonPair.originalUuid());
        // the handle of what was made later points at nothing and is not given again
        Optional<String> gone = pairService.findCopyUuid(run, handleOfLater);
        assertThat(gone).isPresent();
        assertThat(chatMessageService.findForUser(gone.get())).isNull();
        String next = pairService.handleFor(run, AgentRunPairKind.MESSAGE, partOfCopy("Once upon a time").getUuid());
        assertThat(next).isNotEqualTo(handleOfLater);
    }

    @Test
    void theStateBeforeARestoreCanBeRestoredToo() throws Exception {
        Info start = checkpointService.ensureStart(run);
        add("there was a dragon", "the dragon woke");

        CheckpointService.Restored back = checkpointService.restore(run, start.id());
        assertThat(texts()).containsExactly("Once upon a time", "there was a dragon");
        checkpointService.restore(run, back.safety().id());

        assertThat(texts()).containsExactly("Once upon a time", "the dragon woke", "there was a dragon");
    }

    @Test
    void aRestoreToTheOldestAutomaticOneKeepsIt() throws Exception {
        checkpointService.ensureStart(run);
        String oldest = checkpointService.makeAutomatic(run, "r0").checkpoint().id();
        for (int i = 1; i < CheckpointStore.RING; i++) {
            checkpointService.makeAutomatic(run, "r" + i);
        }
        assertThat(checkpointService.list(run).stream().filter(info -> info.kind() == Kind.AUTOMATIC)).hasSize(CheckpointStore.RING);

        CheckpointService.Restored restored = checkpointService.restore(run, oldest);

        assertThat(restored.checkpoint().id()).isEqualTo(oldest);
        assertThat(checkpointService.list(run)).extracting(Info::id).contains(oldest);
    }

    @Test
    void aRestoreOfACheckpointThatIsNotThereChangesNothing() throws Exception {
        checkpointService.ensureStart(run);
        Long id = copy().getId();

        assertThatThrownBy(() -> checkpointService.restore(run, "0042")).isInstanceOf(CheckpointException.class)
                .hasMessageContaining("There is no checkpoint 0042");
        assertThatThrownBy(() -> checkpointService.restore(run, "../../x")).isInstanceOf(CheckpointException.class);

        assertThat(copy().getId()).isEqualTo(id);
        assertThat(checkpointService.list(run)).hasSize(1);
    }

    @Test
    void remapKeepsTheHandlesOfTheItemsOfTheOldCopyAndReservesTheOthers() {
        UuidPair saved = new UuidPair(AgentRunPairKind.MESSAGE, "old-1", "orig-1", "h:m1");
        UuidPair savedPlain = new UuidPair(AgentRunPairKind.MESSAGE, "old-2", "orig-2", null);
        UuidPair now = new UuidPair(AgentRunPairKind.MESSAGE, "old-1", "orig-1", "h:m1");
        UuidPair nowLater = new UuidPair(AgentRunPairKind.MESSAGE, "later", null, "h:m7");
        UuidPair nowNoHandle = new UuidPair(AgentRunPairKind.MESSAGE, "later-2", null, null);
        // the new copy: old-1 and old-2 are items of it, an item of the checkpoint that has no pair at all
        List<UuidPair> fresh = List.of(new UuidPair(AgentRunPairKind.MESSAGE, "new-1", "old-1"),
                new UuidPair(AgentRunPairKind.MESSAGE, "new-2", "old-2"), new UuidPair(AgentRunPairKind.MESSAGE, "new-3", "old-3"));

        List<UuidPair> pairs = CheckpointServiceImpl.remap(fresh,
                List.of(saved, savedPlain), List.of(now, nowLater, nowNoHandle));

        assertThat(pairs).containsExactly(new UuidPair(AgentRunPairKind.MESSAGE, "new-1", "orig-1", "h:m1"),
                new UuidPair(AgentRunPairKind.MESSAGE, "new-2", "orig-2", null), nowLater);
    }
}
