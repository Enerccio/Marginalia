package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptLockedException;
import com.github.enerccio.marginalia.domain.service.StoryGenerationService;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The book of an agent is locked while a run of the agent works or waits for the user: reading is allowed, every
 * service that changes the book refuses, and a paused, stopped or finished run lets go.
 */
class ManuscriptLockTest extends AgentTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private StoryGenerationService storyGenerationService;

    private Manuscript book;
    private Agent agent;
    private ChatMessage part;

    @BeforeEach
    void setUp() throws Exception {
        loginAgentUser();
        book = manuscript();
        part = new ChatMessage();
        part.setResponse("Once upon a time");
        part = chatMessageService.createRoot(book, part);
        agent = agentService.save(newAgent(book));
    }

    private AgentRun start() throws Exception {
        return agentRunService.start(agent);
    }

    @Test
    void aBookWithoutARunIsFree() throws Exception {
        assertThat(agentRunService.findLockingRun(book)).isNull();

        part.setResponse("edited");
        assertThat(chatMessageService.save(part).getResponse()).isEqualTo("edited");
        agentRunService.checkNotLocked(book);
    }

    @Test
    void aRunningRunLocksEveryChangeButNotReading() throws Exception {
        AgentRun run = start();

        assertThat(agentRunService.findLockingRun(book).getId()).isEqualTo(run.getId());
        Manuscript stored = manuscriptService.find(book);
        stored.setName("renamed");
        assertThatThrownBy(() -> manuscriptService.save(stored)).isInstanceOf(ManuscriptLockedException.class)
                .hasMessageContaining(agent.getName());
        part.setResponse("edited");
        assertThatThrownBy(() -> chatMessageService.save(part)).isInstanceOf(ManuscriptLockedException.class);
        assertThatThrownBy(() -> chatMessageService.addChild(part, new ChatMessage())).isInstanceOf(ManuscriptLockedException.class);
        assertThatThrownBy(() -> chatMessageService.delete(part, false)).isInstanceOf(ManuscriptLockedException.class);
        assertThatThrownBy(() -> manuscriptService.delete(book, false)).isInstanceOf(ManuscriptLockedException.class);
        assertThatThrownBy(() -> storyGenerationService.generateNextTurn(book, null, null, null, null))
                .isInstanceOf(ManuscriptLockedException.class);

        // reading goes on
        assertThat(manuscriptService.find(book).getName()).isNotEqualTo("renamed");
        assertThat(chatMessageService.getAllMessages(book)).hasSize(1);
        assertThat(chatMessageService.find(part).getResponse()).isEqualTo("Once upon a time");
    }

    @Test
    void theErrorNamesTheAgent() throws Exception {
        start();

        assertThatThrownBy(() -> agentRunService.checkNotLocked(book))
                .isInstanceOfSatisfying(ManuscriptLockedException.class, e -> assertThat(e.getAgentName()).isEqualTo(agent.getName()));
    }

    @Test
    void everyStatusThatIsGoingOnLocksAndTheOthersDoNot() throws Exception {
        for (AgentRunStatus status : AgentRunStatus.values()) {
            assertThat(status.holdsLock()).as(status.name()).isEqualTo(
                    status == AgentRunStatus.RUNNING || status == AgentRunStatus.IDLE
                            || status == AgentRunStatus.WAITING_APPROVAL || status == AgentRunStatus.PAUSING
                            || status == AgentRunStatus.STOPPING);
        }

        AgentRun run = start();
        run = agentRunService.transition(run, AgentRunStatus.IDLE, null);
        assertThat(agentRunService.findLockingRun(book)).as("waiting for the user locks too").isNotNull();
        run = agentRunService.transition(run, AgentRunStatus.PAUSED, null);
        assertThat(agentRunService.findLockingRun(book)).as("paused").isNull();
        run = agentRunService.transition(run, AgentRunStatus.RUNNING, null);
        assertThat(agentRunService.findLockingRun(book)).as("resumed").isNotNull();
        agentRunService.transition(run, AgentRunStatus.DONE, null);
        assertThat(agentRunService.findLockingRun(book)).as("done, waits for the review").isNull();
    }

    @Test
    void pausingStoppingAndTrashingTheRunFreeTheBook() throws Exception {
        AgentRun run = agentRunService.pause(start());
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        part.setResponse("while paused");
        part = chatMessageService.save(part);

        run = agentRunService.resume(run);
        assertThatThrownBy(() -> chatMessageService.save(part)).isInstanceOf(ManuscriptLockedException.class);

        agentRunService.stop(run, true);
        part.setResponse("while stopped");
        chatMessageService.save(part);

        run = agentRunService.resume(agentRunService.findUnfinished(agent));
        assertThat(agentRunService.findLockingRun(book)).isNotNull();
        agentRunService.delete(run, false);
        assertThat(agentRunService.findLockingRun(book)).as("a trashed run").isNull();
    }

    @Test
    void aDeletedAgentDoesNotLock() throws Exception {
        start();
        agentService.delete(agent, false);

        assertThat(agentRunService.findLockingRun(book)).isNull();
    }

    @Test
    void otherBooksAreNotLocked() throws Exception {
        start();
        Manuscript other = manuscript();

        other.setName("free");
        assertThat(manuscriptService.save(other).getName()).isEqualTo("free");
    }

    @Test
    void theLockDoesNotDependOnWhoAsks() throws Exception {
        start();
        AgentRun locking = agentRunService.findLockingRun(book);
        assertThat(locking).isNotNull();

        loginAdmin();
        assertThat(agentRunService.findLockingRun(book)).as("an administrator is locked out too").isNotNull();
    }
}
