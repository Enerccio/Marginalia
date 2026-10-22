package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.AgentVirtualFileSystemService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.CheckpointService;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore.Info;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore.Kind;
import com.github.enerccio.marginalia.domain.service.agent.VirtualFileSystem;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The checkpoints of a run as the user sees them: who made each, and saving, restoring and deleting them.
 */
class AgentCheckpointBrowserTest extends AgentTestBase {

    @Autowired
    private CheckpointService checkpointService;

    @Autowired
    private AgentVirtualFileSystemService fileSystemService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private AgentMessageService messageService;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;
    private Manuscript book;
    private AgentRun run;
    private final AtomicBoolean busy = new AtomicBoolean();
    private final AtomicInteger restored = new AtomicInteger();

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        loginAgentUser();
        book = manuscript();
        ChatMessage first = new ChatMessage();
        first.setResponse("Once upon a time");
        chatMessageService.createRoot(book, first);
        Agent agent = agentService.save(newAgent(book));
        run = agentRunService.start(agent);
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private AgentCheckpointBrowser browser() {
        AgentCheckpointBrowser browser = new AgentCheckpointBrowser(() -> run, busy::get, restored::incrementAndGet);
        browser.create();
        return browser;
    }

    @Test
    void aPhoneShowsACheckpointAsACardWithItsButtons() throws Exception {
        checkpointService.ensureStart(run);
        AgentCheckpointBrowser browser = new AgentCheckpointBrowser(() -> run, busy::get, restored::incrementAndGet);
        browser.setPhone(true);
        browser.create();

        assertThat(browser.getGrid().getColumns()).hasSize(1);
        com.vaadin.flow.component.Component card = browser.card(rows(browser).getFirst());
        assertThat(com.github.enerccio.marginalia.test.ComponentTree.all(card, com.vaadin.flow.component.button.Button.class).toList())
                .as("restore, rewind, download, delete").hasSize(4);
    }

    private static List<Info> rows(AgentCheckpointBrowser browser) {
        return browser.getGrid().getDataProvider().fetch(new Query<>()).toList();
    }

    @Test
    void noCheckpointsSaysSoAndASavedOneIsListed() throws Exception {
        AgentCheckpointBrowser browser = browser();
        assertThat(rows(browser)).isEmpty();
        assertThat(browser.getUsageLabel().getText()).isEmpty();

        browser.save("by hand");

        assertThat(rows(browser)).extracting(Info::kind).containsExactly(Kind.MANUAL);
        assertThat(rows(browser).getFirst().label()).isEqualTo("by hand");
        assertThat(browser.getUsageLabel().getText()).startsWith("The checkpoints take");
    }

    @Test
    void aRestoreBringsTheCopyBackAndTellsTheConsole() throws Exception {
        checkpointService.ensureStart(run);
        AgentCheckpointBrowser browser = browser();
        Long copy = agentRunService.find(run.getId()).getWorkingCopy().getId();
        VirtualFileSystem files = fileSystemService.open(run);
        files.writeText(files.parse("/later.txt"), "after", false);

        browser.restore(rows(browser).getFirst());

        assertThat(restored.get()).isEqualTo(1);
        assertThat(agentRunService.find(run.getId()).getWorkingCopy().getId()).isNotEqualTo(copy);
        assertThat(fileSystemService.open(run).stat(files.parse("/later.txt"))).isNull();
        assertThat(rows(browser)).as("the state before is saved too").extracting(Info::kind).containsExactly(Kind.START, Kind.AUTOMATIC);
    }

    @Test
    void aRewindOfAPausedRunGoesBackInTheConversationAndTellsTheConsole() throws Exception {
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "task")));
        checkpointService.ensureStart(run);
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.ASSISTANT, "work")));
        run = agentRunService.pause(agentRunService.find(run.getId()));
        AtomicInteger rewound = new AtomicInteger();
        AgentCheckpointBrowser browser = new AgentCheckpointBrowser(() -> run, busy::get, restored::incrementAndGet, rewound::incrementAndGet);
        browser.create();
        assertThat(browser.getRewindable()).containsExactly(rows(browser).getFirst().id());

        browser.rewind(rows(browser).getFirst());

        assertThat(rewound.get()).isEqualTo(1);
        assertThat(restored.get()).isZero();
        assertThat(messageService.transcript(run)).extracting(LLMChatMessage::getRole).containsExactly(LLMRole.USER, LLMRole.USER);
    }

    @Test
    void aRunThatWorksIsNotRewound() throws Exception {
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "task")));
        checkpointService.ensureStart(run);
        AtomicInteger rewound = new AtomicInteger();
        AgentCheckpointBrowser browser = new AgentCheckpointBrowser(() -> run, busy::get, restored::incrementAndGet, rewound::incrementAndGet);
        browser.create();

        browser.rewind(rows(browser).getFirst());

        assertThat(rewound.get()).isZero();
        assertThat(messageService.transcript(run)).hasSize(1);
    }

    @Test
    void whileTheAgentWorksNothingIsSavedOrRestored() throws Exception {
        checkpointService.ensureStart(run);
        AgentCheckpointBrowser browser = browser();
        busy.set(true);
        browser.refresh();

        assertThat(browser.getSaveButton().isEnabled()).isFalse();
        browser.save("no");
        browser.restore(rows(browser).getFirst());

        assertThat(rows(browser)).hasSize(1);
        assertThat(restored.get()).isZero();
    }

    @Test
    void theUserMayDeleteTheStartToo() throws Exception {
        checkpointService.ensureStart(run);
        AgentCheckpointBrowser browser = browser();

        browser.delete(rows(browser).getFirst());

        assertThat(rows(browser)).isEmpty();
    }
}
