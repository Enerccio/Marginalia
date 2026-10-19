package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore.Info;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore.Kind;
import com.github.enerccio.marginalia.domain.service.agent.ImageFiles;
import com.github.enerccio.marginalia.domain.service.agent.VirtualFileSystem;
import com.github.enerccio.marginalia.domain.service.agent.VirtualPath;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.test.TestImages;
import com.github.enerccio.marginalia.test.llm.MockLLMRequest;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The runner owns the loop of a run: it asks the model one round at a time, runs the calls, writes every round to the
 * transcript, keeps the budgets and makes the status changes. The model is the mock server, the commands are real.
 */
class AgentRunnerTest extends AgentTestBase {

    private static final Set<AgentRunStatus> ENDED_TURN = Set.of(AgentRunStatus.IDLE, AgentRunStatus.DONE,
            AgentRunStatus.PAUSED, AgentRunStatus.STOPPED_KEPT, AgentRunStatus.STOPPED_DISCARDED, AgentRunStatus.FAILED);

    @Autowired
    private AgentRunner runner;

    @Autowired
    private AIGate aiGate;

    @Autowired
    private com.github.enerccio.marginalia.domain.service.SettingService settingService;

    @Autowired
    private AgentMessageService messageService;

    @Autowired
    private AgentVirtualFileSystemService fileSystemService;

    @Autowired
    private CheckpointService checkpointService;

    /**
     * An agent that may read the book information and does not mind the rest.
     */
    private Agent readingAgent() throws Exception {
        loginAgentUser();
        Agent agent = newAgent(manuscript());
        agent.setSystemPrompt("You are a test agent.");
        agent.setInstructions("Count the parts.");
        agent.setAllowedCommands(List.of("get-book-info"));
        agent.setDestructiveMode(DestructiveCommandMode.DENY);
        return agentService.save(agent);
    }

    private AgentRun startRun(Agent agent) throws Exception {
        AgentRun run = agentRunService.start(agent);
        runner.run(run);
        return run;
    }

    private AgentRun await(AgentRun run, Set<AgentRunStatus> statuses) throws Exception {
        long end = System.currentTimeMillis() + 20_000;
        while (System.currentTimeMillis() < end) {
            AgentRun current = agentRunService.find(run.getId());
            if (statuses.contains(current.getStatus()) && !runner.isWorking(current)) {
                return current;
            }
            Thread.sleep(25);
        }
        throw new AssertionError("The run is " + agentRunService.find(run.getId()).getStatus() + " after 20 s, wanted " + statuses);
    }

    private AgentRun await(AgentRun run) throws Exception {
        return await(run, ENDED_TURN);
    }

    private List<LLMChatMessage> transcript(AgentRun run) throws Exception {
        return messageService.transcript(run);
    }

    @Test
    void aRunWaitsForAPlaceAtAnAIWithACapAndGoesOnWhenItIsFree() throws Exception {
        Agent agent = readingAgent();
        var main = agentService.resolveProfile(agent.getMainProfile());
        main.ai().setMaxConcurrentRequests(1);
        aiService.save(main.ai());
        // another run (here: the test) is using the only place
        AIGate.Permit taken = aiGate.acquire(main.ai()).get();
        llm.reply("Nothing to do.");

        AgentRun run = startRun(agent);
        Thread.sleep(600);

        assertThat(llm.getCompletionRequests()).as("nothing was sent to the model").isEmpty();
        assertThat(agentRunService.find(run.getId()).getStatus()).isEqualTo(AgentRunStatus.RUNNING);
        assertThat(aiGate.waiting(main.ai())).isEqualTo(1);

        taken.close();
        run = await(run);

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        assertThat(llm.getCompletionRequests()).hasSize(1);
        assertThat(aiGate.active(main.ai())).as("the place was given back").isZero();
    }

    @Test
    void aPauseEndsTheWaitForAPlaceAtOnce() throws Exception {
        Agent agent = readingAgent();
        var main = agentService.resolveProfile(agent.getMainProfile());
        main.ai().setMaxConcurrentRequests(1);
        aiService.save(main.ai());
        AIGate.Permit taken = aiGate.acquire(main.ai()).get();
        llm.reply("Nothing to do.");

        AgentRun run = startRun(agent);
        Thread.sleep(300);
        agentRunService.pause(agentRunService.find(run.getId()));

        run = await(run, Set.of(AgentRunStatus.PAUSED));
        assertThat(llm.getCompletionRequests()).isEmpty();
        assertThat(aiGate.waiting(main.ai())).as("the run left the queue").isZero();
        taken.close();
        assertThat(aiGate.active(main.ai())).isZero();
    }

    @Test
    void pausingAtOnceCancelsTheRequestThatIsGoingOn() throws Exception {
        Agent agent = readingAgent();
        CountDownLatch never = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("A very slow answer").waitFor(never, Duration.ofSeconds(30)));

        AgentRun run = startRun(agent);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));
        long began = System.currentTimeMillis();
        runner.pauseNow(agentRunService.find(run.getId()));

        run = await(run, Set.of(AgentRunStatus.PAUSED));
        never.countDown();

        assertThat(System.currentTimeMillis() - began).as("did not wait for the model").isLessThan(10_000);
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        assertThat(transcript(run)).as("the answer that was not waited for is not in the transcript")
                .extracting(LLMChatMessage::getRole).containsExactly(LLMRole.USER);
    }

    @Test
    void aPlainPauseWaitsForTheRequestAndKeepsItsAnswer() throws Exception {
        Agent agent = readingAgent();
        CountDownLatch release = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("Slow but kept").waitFor(release, Duration.ofSeconds(30)));

        AgentRun run = startRun(agent);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));
        agentRunService.pause(agentRunService.find(run.getId()));
        Thread.sleep(500);
        assertThat(agentRunService.find(run.getId()).getStatus()).isEqualTo(AgentRunStatus.PAUSING);

        release.countDown();
        run = await(run, Set.of(AgentRunStatus.PAUSED, AgentRunStatus.IDLE));

        assertThat(transcript(run).getLast().getContent()).isEqualTo("Slow but kept");
    }

    @Test
    void anAgentWithoutInstructionsGetsTheDefaultOfItsUserInThePrompt() throws Exception {
        Agent agent = readingAgent();
        agent.setInstructions(null);
        agent = agentService.save(agent);
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        setting.setDefaultAgentInstructions("Never rename a character.");
        settingService.save(setting);
        llm.reply("Nothing to do.");

        await(startRun(agent));

        String system = llm.lastCompletionRequest().getMessages("system").getFirst();
        assertThat(system).contains("# Instructions of the author").contains("Never rename a character.");
    }

    @Test
    void aReplyWithoutACallEndsTheTurn() throws Exception {
        Agent agent = readingAgent();
        llm.reply("Nothing to do.");

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        assertThat(run.getRounds()).isEqualTo(1);
        List<LLMChatMessage> transcript = transcript(run);
        assertThat(transcript).extracting(LLMChatMessage::getRole).containsExactly(LLMRole.USER, LLMRole.ASSISTANT);
        assertThat(transcript.getLast().getContent()).isEqualTo("Nothing to do.");

        // the system message is made from the agent: its prompt, the instructions of the author, how the run works
        MockLLMRequest request = llm.lastCompletionRequest();
        String system = request.getMessages("system").getFirst();
        assertThat(system).startsWith("You are a test agent.").contains("Count the parts.").contains("finish");
        assertThat(request.getBody().getAsJsonArray("tools").toString())
                .contains("get-book-info", "read_file", "finish", "ask_user", "update_plan", "get_run_status");
    }

    @Test
    void theModelCallsToolsUntilItFinishes() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}"),
                MockLLMResponse.toolCall("finish", "{\"summary\":\"Counted: all done\"}"));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.DONE);
        assertThat(run.getStatusReason()).isEqualTo("Counted: all done");
        assertThat(run.getRounds()).isEqualTo(2);
        assertThat(run.getFinishedAt()).isNotNull();

        List<LLMChatMessage> transcript = transcript(run);
        assertThat(transcript).extracting(LLMChatMessage::getRole)
                .containsExactly(LLMRole.USER, LLMRole.ASSISTANT, LLMRole.TOOL, LLMRole.ASSISTANT, LLMRole.TOOL);
        assertThat(transcript.get(1).getToolCalls()).extracting(LLMToolCall::getName).containsExactly("get-book-info");
        assertThat(transcript.get(2).getContent()).contains(agent.getManuscript().getName());
        // the second request had the first round in it
        assertThat(llm.lastCompletionRequest().getMessages("tool").getFirst()).contains(agent.getManuscript().getName());
    }

    @Test
    void aFinishInAConversationGivesTheReportAndWaitsForTheNextMessage() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("finish", "{\"summary\":\"Counted: all done\"}"),
                MockLLMResponse.toolCall("finish", "{\"summary\":\"Counted again\"}"));
        AgentRun started = agentRunService.start(agent, true);
        assertThat(started.isInteractive()).isTrue();
        runner.run(started);

        AgentRun run = await(started);

        // the report is for the user as always, the run is not over
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        assertThat(run.getStatusReason()).isEqualTo("Counted: all done");
        assertThat(run.isInteractive()).isTrue();
        assertThat(run.isAttention()).isTrue();
        assertThat(llm.lastCompletionRequest().getMessages("system").getFirst()).contains("conversation with the author");

        runner.send(run, "Count them once more.");
        run = await(run);

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        assertThat(run.getStatusReason()).isEqualTo("Counted again");
        assertThat(transcript(run)).extracting(LLMChatMessage::getRole).containsExactly(LLMRole.USER, LLMRole.ASSISTANT,
                LLMRole.TOOL, LLMRole.USER, LLMRole.ASSISTANT, LLMRole.TOOL);
    }

    @Test
    void anAutonomousRunDoesNotKnowOfTheConversation() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}"));

        AgentRun run = await(startRun(agent));

        assertThat(run.isInteractive()).isFalse();
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.DONE);
        assertThat(llm.lastCompletionRequest().getMessages("system").getFirst()).doesNotContain("conversation with the author");
    }

    @Test
    void aFailingCallIsAnErrorTheModelGets() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("no_such_tool", "{}"), MockLLMResponse.text("I will do without."));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        LLMChatMessage result = transcript(run).get(2);
        assertThat(result.getRole()).isEqualTo(LLMRole.TOOL);
        assertThat(result.getContent()).startsWith("Error: ").contains("no_such_tool");
    }

    @Test
    void aCommandTheAgentMayNotCallIsNotOffered() throws Exception {
        Agent agent = readingAgent();
        llm.reply("ok");

        await(startRun(agent));

        assertThat(llm.lastCompletionRequest().getBody().getAsJsonArray("tools").toString()).doesNotContain("list-lorebooks");
    }

    @Test
    void theFilesOfTheAgentAreTools() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("write_file", "{\"path\":\"/notes.md\",\"content\":\"seen\"}"),
                MockLLMResponse.toolCall("read_file", "{\"path\":\"/notes.md\"}"),
                MockLLMResponse.toolCall("finish", "{\"summary\":\"noted\"}"));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.DONE);
        assertThat(transcript(run).stream().filter(m -> m.getRole() == LLMRole.TOOL).map(LLMChatMessage::getContent).toList())
                .anyMatch(text -> text.contains("seen"));
    }

    @Test
    void askingTheUserEndsTheTurnAndAMessageGoesOn() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("ask_user", "{\"question\":\"Which part?\",\"options\":[\"first\",\"last\"]}"),
                MockLLMResponse.text("Taking the last one."));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        assertThat(run.getStatusReason()).contains("Which part?").contains("- last");
        assertThat(runner.isWorking(run)).isFalse();

        AgentRun running = runner.send(run, "The last one.");
        assertThat(running.getStatus()).isEqualTo(AgentRunStatus.RUNNING);
        run = await(running);

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        List<LLMChatMessage> transcript = transcript(run);
        assertThat(transcript.stream().filter(m -> m.getRole() == LLMRole.USER).map(LLMChatMessage::getContent).toList())
                .contains("The last one.");
        assertThat(transcript.getLast().getContent()).isEqualTo("Taking the last one.");
        assertThat(llm.lastCompletionRequest().getMessages("user")).contains("The last one.");
    }

    @Test
    void aMessageIsOnlyForARunThatWaitsForIt() throws Exception {
        Agent agent = readingAgent();
        AgentRun run = agentRunService.start(agent);

        assertThatThrownBy(() -> runner.send(run, "hello")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> runner.send(run, " ")).isInstanceOf(IllegalArgumentException.class);
    }

    private AgentRun startConversation(Agent agent) throws Exception {
        AgentRun run = agentRunService.start(agent, true);
        runner.run(run);
        return run;
    }

    @Test
    void aMessageToAConversationThatWorksWaitsForTheEndOfTheTurn() throws Exception {
        Agent agent = readingAgent();
        CountDownLatch gate = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}").waitFor(gate, Duration.ofSeconds(20)),
                MockLLMResponse.text("Counted."), MockLLMResponse.text("Got your message."));
        AgentRun run = startConversation(agent);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));

        AgentRun queued = runner.send(agentRunService.find(run.getId()), "Also count the chapters.");
        runner.send(agentRunService.find(run.getId()), "And the lorebooks.");

        assertThat(queued.getStatus()).isEqualTo(AgentRunStatus.RUNNING);
        assertThat(runner.queued(run)).extracting(AgentRunner.QueuedMessage::text)
                .containsExactly("Also count the chapters.", "And the lorebooks.");
        assertThat(transcript(run)).as("nothing is written to the transcript yet").hasSize(1);

        gate.countDown();
        run = await(run);

        // the turn ended with the text, the messages were read instead of waiting for them, as one message
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        assertThat(runner.queued(run)).isEmpty();
        List<LLMChatMessage> transcript = transcript(run);
        assertThat(transcript).extracting(LLMChatMessage::getRole).containsExactly(LLMRole.USER, LLMRole.ASSISTANT,
                LLMRole.TOOL, LLMRole.ASSISTANT, LLMRole.USER, LLMRole.ASSISTANT);
        assertThat(transcript.get(4).getContent()).isEqualTo("Also count the chapters.\n\nAnd the lorebooks.");
        assertThat(llm.getCompletionRequests()).hasSize(3);
    }

    @Test
    void anAutonomousRunDoesNotQueueMessages() throws Exception {
        Agent agent = readingAgent();
        CountDownLatch gate = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("Done.").waitFor(gate, Duration.ofSeconds(20)));
        AgentRun run = startRun(agent);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));

        AgentRun working = agentRunService.find(run.getId());
        assertThatThrownBy(() -> runner.send(working, "hello")).isInstanceOf(IllegalStateException.class);
        assertThat(runner.queued(run)).isEmpty();
        gate.countDown();
        await(run);
    }

    @Test
    void aForcedMessageThrowsAwayTheRequestInFlight() throws Exception {
        Agent agent = readingAgent();
        CountDownLatch gate = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("The answer nobody reads.").waitFor(gate, Duration.ofSeconds(20)),
                MockLLMResponse.text("Switching to that."));
        AgentRun run = startConversation(agent);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));
        runner.send(agentRunService.find(run.getId()), "Stop, do this instead.");
        AgentRunner.QueuedMessage message = runner.queued(run).getFirst();

        runner.force(agentRunService.find(run.getId()), message.id());
        run = await(run);
        gate.countDown();

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        assertThat(runner.queued(run)).isEmpty();
        List<LLMChatMessage> transcript = transcript(run);
        assertThat(transcript).extracting(LLMChatMessage::getRole).containsExactly(LLMRole.USER, LLMRole.USER, LLMRole.ASSISTANT);
        assertThat(transcript.get(1).getContent()).isEqualTo("Stop, do this instead.");
        assertThat(transcript.getLast().getContent()).isEqualTo("Switching to that.");
        assertThat(run.getRounds()).as("the thrown away round is not counted").isEqualTo(1);
    }

    @Test
    void aForcedMessageRefusesTheCommandThatWaitsForAnApproval() throws Exception {
        loginAgentUser();
        Agent asking = newAgent(manuscript());
        asking.setSystemPrompt("You are a test agent.");
        asking.setAllowedCommands(List.of("search-replace-parts"));
        asking.setDestructiveMode(DestructiveCommandMode.ASK);
        Agent agent = agentService.save(asking);
        llm.enqueue(MockLLMResponse.toolCall("search-replace-parts", "{\"search\":\"zzz\",\"replace\":\"y\"}")
                        .andToolCall("get-book-info", "{}"),
                MockLLMResponse.text("Understood."));
        AgentRun run = startConversation(agent);
        long end = System.currentTimeMillis() + 20_000;
        while (agentRunService.find(run.getId()).getStatus() != AgentRunStatus.WAITING_APPROVAL && System.currentTimeMillis() < end) {
            Thread.sleep(25);
        }
        runner.send(agentRunService.find(run.getId()), "Never mind that.");

        runner.force(agentRunService.find(run.getId()), runner.queued(run).getFirst().id());
        run = await(run, Set.of(AgentRunStatus.IDLE));

        List<LLMChatMessage> transcript = transcript(run);
        assertThat(transcript).extracting(LLMChatMessage::getRole).containsExactly(LLMRole.USER, LLMRole.ASSISTANT,
                LLMRole.TOOL, LLMRole.TOOL, LLMRole.USER, LLMRole.ASSISTANT);
        assertThat(transcript.get(2).getContent()).startsWith("Error:");
        assertThat(transcript.get(3).getContent()).isEqualTo(Defaults.AGENT_SKIPPED_CALL);
        assertThat(transcript.get(4).getContent()).isEqualTo("Never mind that.");
    }

    @Test
    void aForcedMessageThatWasDeliveredMeanwhileIsNothing() throws Exception {
        Agent agent = readingAgent();
        llm.reply("Hello.");
        AgentRun run = await(startConversation(agent));

        // nothing is queued, forcing is a no-op
        runner.force(run, "no-such-message");

        assertThat(agentRunService.find(run.getId()).getStatus()).isEqualTo(AgentRunStatus.IDLE);
    }

    @Test
    void aPlanSurvivesInTheStatus() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("update_plan",
                        "{\"items\":[{\"text\":\"Read the book\",\"status\":\"in progress\"},{\"text\":\"Report\",\"status\":\"pending\"}]}"),
                MockLLMResponse.toolCall("get_run_status", "{}"),
                MockLLMResponse.text("done for now"));

        AgentRun run = await(startRun(agent));

        List<String> results = transcript(run).stream().filter(m -> m.getRole() == LLMRole.TOOL).map(LLMChatMessage::getContent).toList();
        assertThat(results.get(0)).startsWith("Plan saved.").contains("1. [~] Read the book").contains("2. [ ] Report");
        assertThat(results.get(1)).contains("Rounds: 2").contains("Plan:").contains("[~] Read the book");
    }

    @Test
    void theStepBudgetPausesTheRunAndAResumeGoesOn() throws Exception {
        Agent agent = readingAgent();
        agent.setMaxRounds(1);
        agent = agentService.save(agent);
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}"), MockLLMResponse.text("Finished reading."));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        assertThat(run.getStatusReason()).contains("1 rounds");
        assertThat(transcript(run)).extracting(LLMChatMessage::getRole)
                .containsExactly(LLMRole.USER, LLMRole.ASSISTANT, LLMRole.TOOL);

        agent.setMaxRounds(null);
        agentService.save(agent);
        runner.run(agentRunService.resume(run));
        run = await(run);

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        assertThat(run.getRounds()).isEqualTo(2);
        // the model saw the first round again
        assertThat(llm.lastCompletionRequest().getMessages("tool")).hasSize(1);
    }

    @Test
    void theModelIsToldWhatTheUserChangedWhileTheRunWasPausedAndCanAskAgain() throws Exception {
        Agent agent = readingAgent();
        agent.setMaxRounds(1);
        agent = agentService.save(agent);
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}"), MockLLMResponse.toolCall("get_sync_report", "{}"),
                MockLLMResponse.text("Thanks."));

        AgentRun run = await(startRun(agent));
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        // the book is the user's while the run is paused
        Manuscript book = manuscriptService.find(agent.getManuscript().getId());
        book.setDescription("A new description");
        manuscriptService.save(book);
        agent.setMaxRounds(null);
        agentService.save(agent);

        runner.run(agentRunService.resume(run));
        run = await(run);

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        List<LLMChatMessage> transcript = transcript(run);
        LLMChatMessage told = transcript.stream().filter(m -> m.getRole() == LLMRole.USER && m.getContent().contains("While the run was paused"))
                .findFirst().orElseThrow();
        assertThat(told.getContent()).contains("book setting \"description\" changed by the user").endsWith("with your task. If you have finished it, call finish with your report.");
        assertThat(transcript.stream().filter(m -> m.getRole() == LLMRole.TOOL).map(LLMChatMessage::getContent).toList().getLast())
                .contains("Taken over into your working copy");
        assertThat(llm.lastCompletionRequest().getBody().getAsJsonArray("tools").toString()).contains("get_sync_report");
    }

    @Test
    void aResumeWithoutChangesToTheBookAddsNoNoteForTheModel() throws Exception {
        Agent agent = readingAgent();
        agent.setMaxRounds(1);
        agent = agentService.save(agent);
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}"), MockLLMResponse.text("Done reading."));

        AgentRun run = await(startRun(agent));
        agent.setMaxRounds(null);
        agentService.save(agent);
        runner.run(agentRunService.resume(run));
        run = await(run);

        assertThat(transcript(run).stream().map(LLMChatMessage::getContent)).noneMatch(c -> c != null && c.contains("While the run was paused"));
        // nothing to ask about, so the model is not offered the tool
        assertThat(llm.lastCompletionRequest().getBody().getAsJsonArray("tools").toString()).doesNotContain("get_sync_report");
    }

    @Test
    void aLoopOfTheSameCallInOneAnswerIsMadeOnce() throws Exception {
        Agent agent = readingAgent();
        agent = agentService.save(agent);
        MockLLMResponse loop = MockLLMResponse.toolCall("get_run_status", "{}");
        for (int i = 0; i < 99; i++) {
            loop = loop.andToolCall("get_run_status", "{}");
        }
        llm.enqueue(loop, MockLLMResponse.text("Sorry."));

        AgentRun run = await(startRun(agent));

        List<LLMChatMessage> transcript = transcript(run);
        assertThat(transcript.stream().filter(m -> m.getRole() == LLMRole.TOOL)).hasSize(1);
        assertThat(transcript.stream().filter(m -> m.getRole() == LLMRole.ASSISTANT && m.hasToolCalls()).findFirst().orElseThrow()
                .getToolCalls()).hasSize(1);
        assertThat(transcript.stream().filter(m -> m.getRole() == LLMRole.USER).map(LLMChatMessage::getContent).toList().getLast())
                .contains("had 100 calls, 99 of them repeats").contains("Only 1 were made");
    }

    @Test
    void anAnswerWithMoreCallsThanTheLimitIsCutAtTheLimit() throws Exception {
        Agent agent = readingAgent();
        agent = agentService.save(agent);
        MockLLMResponse many = MockLLMResponse.toolCall("read_file", "{\"path\":\"/f0\"}");
        for (int i = 1; i < 40; i++) {
            many = many.andToolCall("read_file", "{\"path\":\"/f" + i + "\"}");
        }
        llm.enqueue(many, MockLLMResponse.text("Done."));

        AgentRun run = await(startRun(agent));

        assertThat(transcript(run).stream().filter(m -> m.getRole() == LLMRole.TOOL)).hasSize(Defaults.AGENT_MAX_CALLS_PER_ROUND);
    }

    @Test
    void theTokenBudgetPausesTheRun() throws Exception {
        Agent agent = readingAgent();
        agent.setTokenBudget(100L);
        agent = agentService.save(agent);
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}").usage(80, 40), MockLLMResponse.text("never asked"));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        assertThat(run.getStatusReason()).contains("budget of 100 tokens");
        assertThat(run.getPrefillTokens()).isEqualTo(80);
        assertThat(run.getGeneratedTokens()).isEqualTo(40);
        assertThat(llm.getCompletionRequests()).hasSize(1);
    }

    @Test
    void theContextCapPausesTheRun() throws Exception {
        Agent agent = readingAgent();
        agent.setContextCap(500);
        agent = agentService.save(agent);
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}").usage(480, 40));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        assertThat(run.getStatusReason()).contains("context of the model is full").contains("500");
    }

    @Test
    void aPauseWaitsForTheRoundInFlight() throws Exception {
        Agent agent = readingAgent();
        CountDownLatch gate = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}").waitFor(gate, Duration.ofSeconds(20)));
        AgentRun run = startRun(agent);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));

        AgentRun pausing = agentRunService.pause(run);
        assertThat(pausing.getStatus()).isEqualTo(AgentRunStatus.PAUSING);
        gate.countDown();
        run = await(run);

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        // the round was finished and written first
        assertThat(transcript(run)).extracting(LLMChatMessage::getRole)
                .containsExactly(LLMRole.USER, LLMRole.ASSISTANT, LLMRole.TOOL);
    }

    @Test
    void aStopWaitsForTheRoundInFlightAndDiscards() throws Exception {
        Agent agent = readingAgent();
        CountDownLatch gate = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}").waitFor(gate, Duration.ofSeconds(20)));
        AgentRun run = startRun(agent);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));

        assertThat(agentRunService.stop(run, false).getStatus()).isEqualTo(AgentRunStatus.STOPPING);
        gate.countDown();
        run = await(run);

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.STOPPED_DISCARDED);
        assertThat(llm.getCompletionRequests()).hasSize(1);
    }

    @Test
    void aStopThatKeepsTheResultEndsKept() throws Exception {
        Agent agent = readingAgent();
        CountDownLatch gate = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("late").waitFor(gate, Duration.ofSeconds(20)));
        AgentRun run = startRun(agent);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));

        agentRunService.stop(run, true);
        gate.countDown();
        run = await(run);

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.STOPPED_KEPT);
    }

    @Test
    void aModelThatFailsPausesTheRun() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.error(400, "bad request"));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        assertThat(run.getStatusReason()).startsWith("The model could not be asked");
    }

    @Test
    void aCallInterruptedBeforeItsResultGetsAnAnswerOnTheNextWork() throws Exception {
        Agent agent = readingAgent();
        AgentRun run = agentRunService.start(agent);
        // what the transcript of a server that died between the calls and their results looks like
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "Start"),
                LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("call_x", "get-book-info", "{}")))));
        llm.reply("I will check.");

        runner.run(run);
        run = await(run);

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        MockLLMRequest request = llm.lastCompletionRequest();
        assertThat(request.getMessages("tool").getFirst()).startsWith("Error: this call was interrupted");
    }

    @Test
    void aRunThatWasIdleIsToldToGoOnAfterAPause() throws Exception {
        Agent agent = readingAgent();
        llm.reply("Waiting.");
        AgentRun run = await(startRun(agent));
        run = agentRunService.pause(run);
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        llm.reply("Back at it.");

        runner.run(agentRunService.resume(run));
        run = await(run);

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        // an assistant reply is not the last message of a request
        assertThat(llm.lastCompletionRequest().getLastMessage().role()).isEqualTo("user");
        assertThat(llm.lastCompletionRequest().getLastMessage().content()).contains("Go on");
    }

    @Test
    void aRunIsTakenUpOnlyWhenItIsRunning() throws Exception {
        Agent agent = readingAgent();
        llm.reply("ok");
        AgentRun run = await(startRun(agent));

        assertThatThrownBy(() -> runner.run(run)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aTrashedRunEndsItsWorker() throws Exception {
        Agent agent = readingAgent();
        CountDownLatch gate = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("never read").waitFor(gate, Duration.ofSeconds(20)));
        AgentRun run = startRun(agent);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));

        agentRunService.delete(run, false);

        long end = System.currentTimeMillis() + 10_000;
        while (runner.isWorking(run) && System.currentTimeMillis() < end) {
            Thread.sleep(25);
        }
        gate.countDown();
        assertThat(runner.isWorking(run)).isFalse();
    }

    @Test
    void aRunStartedInTheConsoleBeginsWithTheOtherFirstMessageAndAsksTheAuthor() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("ask_user", "{\"question\":\"What shall I do?\"}"));
        AgentRun run = agentRunService.start(agent);

        runner.run(run, Defaults.AGENT_CONSOLE_START_MESSAGE);
        run = await(run);

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        assertThat(run.getStatusReason()).contains("What shall I do?");
        List<LLMChatMessage> messages = transcript(run);
        assertThat(messages.getFirst().getRole()).isEqualTo(LLMRole.USER);
        assertThat(messages.getFirst().getContent()).isEqualTo(Defaults.AGENT_CONSOLE_START_MESSAGE.strip());
        assertThat(messages.stream().filter(m -> m.getRole() == LLMRole.USER)).as("no second start message").hasSize(1);
    }

    @Test
    void theFirstMessageIsOnlyForARunThatHasNotWorked() throws Exception {
        Agent agent = readingAgent();
        llm.reply("ok");
        AgentRun run = await(startRun(agent));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> runner.run(agentRunService.find(run.getId()), "again"))
                .isInstanceOf(IllegalStateException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> runner.run(agentRunService.start(agent()), " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theContextOfTheRunIsKeptForTheConsole() throws Exception {
        Agent agent = readingAgent();
        agent.setContextCap(5_000);
        agent = agentService.save(agent);
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}").usage(80, 40),
                MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}").usage(300, 20));

        AgentRun run = await(startRun(agent));

        assertThat(messageService.getContextTokens(run)).as("of the last request").isEqualTo(320);
        assertThat(agentRunService.getContextCap(run)).isEqualTo(5_000);
    }

    // --- checkpoints ---

    private Agent writingAgent() throws Exception {
        Agent agent = readingAgent();
        agent.setAllowedCommands(List.of("get-book-info", "set-book-info"));
        return agentService.save(agent);
    }

    private String nameOfCopy(AgentRun run) throws Exception {
        return agentRunService.find(run.getId()).getWorkingCopy().getName();
    }

    @Test
    void theRunStartsWithACheckpointOfItsOwn() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}"), MockLLMResponse.text("Done."));

        AgentRun run = await(startRun(agent));

        assertThat(checkpointService.list(run)).as("a round of reads makes no other").hasSize(1);
        assertThat(checkpointService.list(run).getFirst().kind()).isEqualTo(Kind.START);
    }

    @Test
    void aCheckpointIsMadeBeforeARoundThatChangesWhatChangedSinceTheLastOne() throws Exception {
        Agent agent = writingAgent();
        llm.enqueue(MockLLMResponse.toolCall("set-book-info", "{\"name\":\"One\"}"),
                MockLLMResponse.toolCall("set-book-info", "{\"name\":\"Two\"}"),
                MockLLMResponse.toolCall("get-book-info", "{}"),
                MockLLMResponse.toolCall("get-book-info", "{}"),
                MockLLMResponse.toolCall("set-book-info", "{\"name\":\"Three\"}"),
                MockLLMResponse.text("Done."));

        AgentRun run = await(startRun(agent));

        // the first change starts from the state of the start checkpoint; before the second and the third the copy had changed
        List<Info> checkpoints = checkpointService.list(run);
        assertThat(checkpoints).extracting(Info::kind).containsExactly(Kind.START, Kind.AUTOMATIC, Kind.AUTOMATIC);
        assertThat(checkpoints.get(1).label()).startsWith("before round");
        assertThat(nameOfCopy(run)).isEqualTo("Three");
    }

    @Test
    void aRewoundRunGoesOnFromTheCheckpointWithoutRememberingWhatCameAfterIt() throws Exception {
        Agent agent = writingAgent();
        llm.enqueue(MockLLMResponse.toolCall("set-book-info", "{\"name\":\"One\"}"),
                MockLLMResponse.toolCall("set-book-info", "{\"name\":\"Two\"}"),
                MockLLMResponse.text("Done."));
        AgentRun run = await(startRun(agent));
        assertThat(nameOfCopy(run)).isEqualTo("Two");
        // the automatic checkpoint before the second change (the first one starts from the checkpoint of the start): the book is named One then
        Info before = checkpointService.list(run).get(1);
        assertThat(before.kind()).isEqualTo(Kind.AUTOMATIC);
        run = agentRunService.pause(agentRunService.find(run.getId()));

        checkpointService.rewind(run, before.id());

        assertThat(nameOfCopy(run)).isEqualTo("One");
        llm.enqueue(MockLLMResponse.text("Back at One."));
        runner.run(agentRunService.resume(agentRunService.find(run.getId())));
        run = await(run);

        List<LLMChatMessage> seen = transcript(run);
        assertThat(seen.stream().filter(m -> m.getRole() == LLMRole.ASSISTANT && m.hasToolCalls())
                .flatMap(m -> m.getToolCalls().stream()).map(LLMToolCall::getArguments)).containsExactly("{\"name\":\"One\"}");
        assertThat(seen.stream().map(LLMChatMessage::getContent).filter(java.util.Objects::nonNull))
                .anyMatch(text -> text.startsWith("Notice from the run: the author rewound the run to checkpoint " + before.id()));
        assertThat(llm.lastCompletionRequest().getBody().toString()).doesNotContain("Two");
        assertThat(nameOfCopy(run)).isEqualTo("One");
    }

    @Test
    void theAgentSavesAndRestoresACheckpointAndIsToldWhatCameBack() throws Exception {
        Agent agent = writingAgent();
        String original = agentService.findForUser(agent.getUuid()).getManuscript().getName();
        llm.enqueue(MockLLMResponse.toolCall("write_file", "{\"path\":\"/notes.txt\",\"content\":\"keep\"}"),
                MockLLMResponse.toolCall("checkpoint", "{\"label\":\"before renaming\"}"),
                MockLLMResponse.toolCall("set-book-info", "{\"name\":\"Ruined\"}"),
                MockLLMResponse.toolCall("write_file", "{\"path\":\"/later.txt\",\"content\":\"x\"}"),
                MockLLMResponse.toolCall("checkpoint_restore", "{\"id\":\"2\"}"),
                MockLLMResponse.toolCall("checkpoint_list", "{}"),
                MockLLMResponse.text("Done."));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        assertThat(nameOfCopy(run)).as("the copy is the one that was saved").isEqualTo(original);
        VirtualFileSystem fs = fileSystemService.open(run);
        assertThat(fs.readText(fs.parse("/notes.txt"))).isEqualTo("keep");
        assertThat(fs.stat(fs.parse("/later.txt"))).isNull();
        // the agent reads what happened
        List<String> users = transcript(run).stream().filter(m -> m.getRole() == LLMRole.USER).map(LLMChatMessage::getContent).toList();
        assertThat(users).anyMatch(text -> text.contains("went back to checkpoint 0002") && text.contains("/later.txt")
                && text.contains("The state before the restore is checkpoint"));
        List<String> results = transcript(run).stream().filter(m -> m.getRole() == LLMRole.TOOL).map(LLMChatMessage::getContent).toList();
        assertThat(results).anyMatch(text -> text.contains("Checkpoint 0002 saved"));
        assertThat(results).anyMatch(text -> text.contains("goes back to checkpoint 0002 when this round ends"));
        assertThat(results.getLast()).contains("0001 start").contains("0002 manual").contains("\"before renaming\"")
                .contains("before restoring 0002");
    }

    @Test
    void aRestoreOfACheckpointThatIsNotThereIsAnErrorForTheAgent() throws Exception {
        Agent agent = writingAgent();
        llm.enqueue(MockLLMResponse.toolCall("checkpoint_restore", "{\"id\":\"0077\"}"),
                MockLLMResponse.toolCall("checkpoint_delete", "{\"id\":\"1\"}"),
                MockLLMResponse.text("Done."));

        AgentRun run = await(startRun(agent));

        List<String> results = transcript(run).stream().filter(m -> m.getRole() == LLMRole.TOOL).map(LLMChatMessage::getContent).toList();
        assertThat(results.get(0)).startsWith("Error:").contains("There is no checkpoint 0077").contains("0001 start");
        assertThat(results.get(1)).startsWith("Error:").contains("start of the run can't be deleted");
        assertThat(checkpointService.list(run)).hasSize(1);
    }

    // --- the context fills up ---

    @Test
    void theAgentIsToldAfterEveryRoundOnceItsContextPassesThreeQuartersOfTheCap() throws Exception {
        Agent agent = readingAgent();
        agent.setContextCap(10_000);
        agent = agentService.save(agent);
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}").usage(5_000, 500),
                MockLLMResponse.toolCall("get-book-info", "{}").usage(7_000, 600),
                MockLLMResponse.toolCall("get-book-info", "{}").usage(7_200, 400),
                MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}").usage(100, 10));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).as(run.getStatusReason()).isEqualTo(AgentRunStatus.DONE);
        List<MockLLMRequest> requests = llm.getCompletionRequests();
        assertThat(requests.get(1).getMessages("user")).as("55% is no reason yet").noneMatch(text -> text.contains("Notice from the run"));
        assertThat(requests.get(2).getMessages("user")).as("76%").anyMatch(text -> text.contains("context is 76% full")
                && text.contains("7600 of 10000") && text.contains("request_compaction") && !text.contains("stops when it is full"));
        assertThat(requests.get(3).getMessages("user")).as("76% and then 76% again, once for every round")
                .filteredOn(text -> text.contains("Notice from the run")).hasSize(2);
    }

    @Test
    void theWarningSaysThatTheRunStopsWhenTheContextIsNearlyFull() throws Exception {
        Agent agent = readingAgent();
        agent.setContextCap(10_000);
        // the runner would compact the run at 90%
        agent.setAutoCompactPercent(0);
        agent = agentService.save(agent);
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}").usage(9_000, 300),
                MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}").usage(100, 10));

        await(startRun(agent));

        assertThat(llm.getCompletionRequests().get(1).getMessages("user")).anyMatch(text -> text.contains("context is 93% full")
                && text.contains("stops when it is full"));
    }

    @Test
    void theRunnerCompactsTheRunWhenTheContextPassesNinetyPercentAndTheAgentDidNot() throws Exception {
        Agent agent = readingAgent();
        agent.setContextCap(10_000);
        agent = agentService.save(agent);
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}").usage(9_000, 300),
                MockLLMResponse.text("Task: count parts. Done so far: the book info."),
                MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}").usage(100, 10));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).as(run.getStatusReason()).isEqualTo(AgentRunStatus.DONE);
        List<MockLLMRequest> requests = llm.getCompletionRequests();
        assertThat(requests).hasSize(3);
        assertThat(requests.get(1).getMessages("user").getLast()).as("the request for the summary").startsWith(Defaults.DEFAULT_AGENT_COMPACTION_PROMPT.strip());
        assertThat(requests.get(2).getMessages("tool")).as("the earlier rounds are gone").isEmpty();
        assertThat(requests.get(2).getMessages("user")).anyMatch(text -> text.startsWith(Defaults.AGENT_AUTO_COMPACTED_NOTE)
                && text.contains("the book info"));
        assertThat(requests.get(2).getMessages("user")).as("no warning on top of it").noneMatch(text -> text.contains("Notice from the run"));
    }

    @Test
    void theShareAtWhichTheRunnerCompactsIsTheOneOfTheAgent() throws Exception {
        Agent agent = readingAgent();
        agent.setContextCap(10_000);
        agent.setAutoCompactPercent(80);
        agent = agentService.save(agent);
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}").usage(8_200, 300),
                MockLLMResponse.text("A summary."),
                MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}").usage(100, 10));

        await(startRun(agent));

        assertThat(llm.getCompletionRequests().get(2).getMessages("user")).anyMatch(text -> text.startsWith(Defaults.AGENT_AUTO_COMPACTED_NOTE));
    }

    @Test
    void anAutomaticCompactionThatFailsIsReportedAndNotTriedAgainAtOnce() throws Exception {
        Agent agent = readingAgent();
        agent.setContextCap(10_000);
        agent = agentService.save(agent);
        // the model calls a tool in place of writing the summary
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}").usage(9_000, 300),
                MockLLMResponse.toolCall("get-book-info", "{}"),
                MockLLMResponse.toolCall("get-book-info", "{}").usage(9_100, 300),
                MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}").usage(100, 10));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).as(run.getStatusReason()).isEqualTo(AgentRunStatus.DONE);
        assertThat(llm.getCompletionRequests()).as("no second try in the very next round").hasSize(4);
        assertThat(llm.lastCompletionRequest().getMessages("user")).anyMatch(text -> text.contains("The run tried to compact your work")
                && text.contains("Nothing was changed"));
    }

    @Test
    void noWarningComesWhenTheContextHasNoCap() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}").usage(9_000, 300),
                MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}").usage(100, 10));

        await(startRun(agent));

        assertThat(llm.getCompletionRequests().get(1).getMessages("user")).noneMatch(text -> text.contains("Notice from the run"));
    }

    @Test
    void theAgentCompactsItselfWhenItAsksForIt() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}"),
                MockLLMResponse.toolCall("request_compaction", "{\"focus\":\"the chapters\"}"),
                MockLLMResponse.text("Task: count parts. Notes in notes.md."),
                MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}"));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.DONE);
        // the request for the summary: everything so far, then the prompt with the agent's own focus as the last message
        MockLLMRequest ask = llm.getCompletionRequests().get(2);
        assertThat(ask.getMessages("user").getLast()).startsWith(Defaults.DEFAULT_AGENT_COMPACTION_PROMPT.strip())
                .endsWith("You asked the summary to stress: the chapters");
        assertThat(ask.getMessages("tool")).as("the call has its result before the compaction").hasSize(2);
        // and the next round reads the summary only
        MockLLMRequest next = llm.getCompletionRequests().get(3);
        assertThat(next.getMessages("tool")).isEmpty();
        assertThat(next.getMessages("user").getLast()).startsWith(Defaults.AGENT_SELF_COMPACTED_NOTE).contains("Notes in notes.md.");
        assertThat(transcript(run).getFirst().getContent()).startsWith(Defaults.AGENT_SELF_COMPACTED_NOTE);
    }

    @Test
    void aCompactionTheAgentAskedForThatFailsIsReportedToTheAgent() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("request_compaction", "{}"),
                MockLLMResponse.toolCall("get-book-info", "{}"),
                MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}"));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.DONE);
        assertThat(llm.lastCompletionRequest().getMessages("user")).anyMatch(text -> text.contains("You asked to compact your work, but it failed")
                && text.contains("Nothing was changed."));
        assertThat(llm.lastCompletionRequest().getMessages("tool")).as("nothing was dropped").hasSize(1);
    }

    @Test
    void theReasoningOfTheModelIsKeptBesideItsAnswerAndNotSentBack() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}").withReasoning("I should look at the book first."),
                MockLLMResponse.text("Done.").withReasoning("Now I can answer.").reasoningField("reasoning"));

        AgentRun run = await(startRun(agent));

        List<AgentMessage> rows = messageService.messages(run);
        assertThat(rows).extracting(AgentMessage::getReasoning).containsExactly(null, "I should look at the book first.", null, "Now I can answer.");
        // the transcript the model gets has no reasoning in it
        assertThat(llm.lastCompletionRequest().getBody().toString()).doesNotContain("I should look at the book first.");
        assertThat(transcript(run)).extracting(LLMChatMessage::getContent).doesNotContain("I should look at the book first.");
    }

    @Test
    void theModelIsToldThatAFileCanBePassedAsAnArgument() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.text("Nothing to do."));

        await(startRun(agent));

        assertThat(llm.lastCompletionRequest().getMessages("system").getFirst()).contains("@file:/path");
    }

    @Test
    void theModelKnowsHowMuchItCanReadAndWrite() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("get_run_status", "{}"), MockLLMResponse.text("noted"));

        AgentRun run = await(startRun(agent));

        // the protocol of the tests allows 300 tokens of a reply, the AI 1000 and a context of 32000: the smaller reply counts
        String system = llm.lastCompletionRequest().getMessages("system").getFirst();
        assertThat(system).contains("Limits of the model you run on:").contains("Context: 32000 tokens")
                .contains("at most 300 tokens").contains("append_file");
        String status = transcript(run).stream().filter(m -> m.getRole() == LLMRole.TOOL).map(LLMChatMessage::getContent).findFirst().orElseThrow();
        assertThat(status).contains("Longest answer you can write: 300 tokens").contains("of 32000 tokens");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // images
    // ---------------------------------------------------------------------------------------------------------------

    /** An agent whose model sees images, with the images in its workspace. */
    private Agent seeingAgent(String... images) throws Exception {
        Agent agent = readingAgent();
        agent.getMainProfile().setImages(true);
        agent = agentService.save(agent);
        for (String image : images) {
            fileSystemService.openWorkspace(agent).writeImage(VirtualPath.parse(image), TestImages.image(64, 48, "png"));
        }
        return agent;
    }

    /** The data URLs of the images in the messages of a request, in order. */
    private static List<String> imagesOf(MockLLMRequest request) {
        List<String> urls = new java.util.ArrayList<>();
        for (var message : request.getBody().getAsJsonArray("messages")) {
            var content = message.getAsJsonObject().get("content");
            if (content != null && content.isJsonArray()) {
                for (var part : content.getAsJsonArray()) {
                    if (part.getAsJsonObject().has("image_url")) {
                        urls.add(part.getAsJsonObject().getAsJsonObject("image_url").get("url").getAsString());
                    }
                }
            }
        }
        return urls;
    }

    private static String view(String path) {
        return "{\"path\":\"" + path + "\"}";
    }

    @Test
    void aModelThatSeesImagesGetsTheToolAndTheImageAfterTheResult() throws Exception {
        Agent agent = seeingAgent("/refs/map.png");
        llm.enqueue(MockLLMResponse.toolCall("view_image", view("/refs/map.png")), MockLLMResponse.text("A map of a coast."));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        List<MockLLMRequest> requests = llm.getCompletionRequests();
        assertThat(requests).hasSize(2);
        assertThat(requests.getFirst().getBody().getAsJsonArray("tools").toString()).contains("view_image");
        assertThat(imagesOf(requests.getFirst())).isEmpty();

        // the result is text, the picture follows it as a message of the user
        MockLLMRequest second = requests.get(1);
        assertThat(imagesOf(second)).hasSize(1).allMatch(url -> url.startsWith("data:image/png;base64,"));
        List<MockLLMRequest.Message> messages = second.getMessages();
        assertThat(messages.get(messages.size() - 2).role()).isEqualTo("tool");
        assertThat(messages.get(messages.size() - 2).content()).startsWith("Showing /refs/map.png (png, 64 x 48 pixels)");
        assertThat(messages.getLast().role()).isEqualTo("user");
        assertThat(messages.getLast().content()).contains("/refs/map.png");

        // the transcript keeps the path, never the bytes
        LLMChatMessage shown = transcript(run).stream().filter(LLMChatMessage::hasAttachments).findFirst().orElseThrow();
        assertThat(shown.getRole()).isEqualTo(LLMRole.USER);
        assertThat(shown.getAttachments()).hasSize(1);
        LLMAttachment reference = shown.getAttachments().getFirst();
        assertThat(ImageFiles.isReference(reference)).isTrue();
        assertThat(reference.getName()).isEqualTo("/refs/map.png");
        assertThat(messageService.messages(run).stream().map(AgentMessage::getMessage).toList()).noneMatch(json -> json.contains("base64"));
    }

    @Test
    void aModelThatDoesNotSeeImagesDoesNotGetTheTool() throws Exception {
        Agent agent = readingAgent();
        llm.reply("Nothing to look at.");

        await(startRun(agent));

        assertThat(llm.lastCompletionRequest().getBody().getAsJsonArray("tools").toString()).doesNotContain("view_image");
    }

    @Test
    void onlyTheNewestImagesStayInFrontOfTheModel() throws Exception {
        String[] names = {"/a.png", "/b.png", "/c.png", "/d.png", "/e.png"};
        Agent agent = seeingAgent(names);
        MockLLMResponse[] answers = new MockLLMResponse[names.length + 1];
        for (int i = 0; i < names.length; i++) {
            answers[i] = MockLLMResponse.toolCall("view_image", view(names[i]));
        }
        answers[names.length] = MockLLMResponse.text("Seen them all.");
        llm.enqueue(answers);

        await(startRun(agent));

        MockLLMRequest last = llm.lastCompletionRequest();
        assertThat(imagesOf(last)).hasSize(4);
        // the oldest one is a note that says how to see it again
        String prompt = last.getPromptText();
        assertThat(prompt).contains("/a.png is not shown any more, view_image shows it again");
        assertThat(prompt).doesNotContain("/e.png is not shown any more");
    }

    @Test
    void anImageThatIsGoneIsANoteNotAnError() throws Exception {
        Agent agent = seeingAgent("/gone.png");
        llm.enqueue(MockLLMResponse.toolCall("view_image", view("/gone.png")), MockLLMResponse.text("Done."));
        AgentRun run = await(startRun(agent));
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);

        // later the file is deleted and the run goes on
        fileSystemService.openWorkspace(agent).delete(VirtualPath.parse("/gone.png"), false);
        llm.reply("Still here.");
        runner.send(run, "Anything else?");
        await(run);

        MockLLMRequest last = llm.lastCompletionRequest();
        assertThat(imagesOf(last)).isEmpty();
        assertThat(last.getPromptText()).contains("/gone.png can't be shown");
    }

    @Test
    void anImageAttachedToAMessageIsSentToTheModelAndKeptAsAPath() throws Exception {
        Agent agent = seeingAgent("/uploads/shot.png");
        llm.reply("Which one?");
        AgentRun run = await(startRun(agent));
        llm.reply("A coast.");

        runner.send(run, "Look at this.", List.of("/uploads/shot.png"));
        await(run);

        MockLLMRequest request = llm.lastCompletionRequest();
        assertThat(imagesOf(request)).hasSize(1).allMatch(url -> url.startsWith("data:image/png;base64,"));
        assertThat(request.getMessages().getLast().content()).startsWith("Look at this.").contains("Attached image: /uploads/shot.png");
        LLMChatMessage stored = transcript(run).stream().filter(LLMChatMessage::hasAttachments).findFirst().orElseThrow();
        assertThat(ImageFiles.isReference(stored.getAttachments().getFirst())).isTrue();
    }

    @Test
    void aMessageNeedsTextOrAnImageAndAnImageThatTheModelCanSee() throws Exception {
        Agent blind = readingAgent();
        llm.reply("Which one?");
        AgentRun blindRun = await(startRun(blind));
        fileSystemService.openWorkspace(blind).writeImage(VirtualPath.parse("/a.png"), TestImages.image(20, 20, "png"));

        assertThatThrownBy(() -> runner.send(blindRun, "", List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> runner.send(blindRun, "x", List.of("/a.png"))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not see images");

        Agent seeing = seeingAgent("/a.png");
        llm.reply("Which one?");
        AgentRun run = await(startRun(seeing));
        assertThatThrownBy(() -> runner.send(run, "x", List.of("/missing.png"))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no file /missing.png");
        assertThatThrownBy(() -> runner.send(run, "x", java.util.Collections.nCopies(9, "/a.png")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("at most 8");
        // nothing was sent: the run still waits
        assertThat(agentRunService.find(run.getId()).getStatus()).isEqualTo(AgentRunStatus.IDLE);
    }

    // --- commands of the author ---

    @Test
    void theConsoleOffersCompactAndTheReadingCommandsTheAgentMay() throws Exception {
        loginAgentUser();
        Agent asking = newAgent(manuscript());
        asking.setAllowedCommands(List.of("get-book-info", "search-replace-parts", "no-such-command"));
        Agent agent = agentService.save(asking);

        assertThat(runner.consoleCommands(agent)).as("compact and clear-workspace, then what only reads, search-replace changes data")
                .containsExactly("compact", "clear-workspace", "get-book-info");
    }

    @Test
    void aCommandThatIsNotOfferedIsRefused() throws Exception {
        Agent agent = readingAgent();
        llm.reply("Hello.");
        AgentRun run = await(startConversation(agent));

        assertThatThrownBy(() -> runner.command(run, "/list-lorebooks")).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("list-lorebooks");
        assertThatThrownBy(() -> runner.command(run, "get-book-info")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> runner.command(run, "/")).isInstanceOf(IllegalArgumentException.class);
        assertThat(llm.getCompletionRequests()).hasSize(1);
    }

    @Test
    void aCommandOfTheAuthorIsAMessageOfTheAuthorNotACallOfTheAgent() throws Exception {
        Agent agent = readingAgent();
        llm.reply("Hello.");
        AgentRun run = await(startConversation(agent));

        runner.command(agentRunService.find(run.getId()), "/get-book-info");
        run = await(run);

        assertThat(run.getStatus()).as("the run stays as it was").isEqualTo(AgentRunStatus.IDLE);
        assertThat(llm.getCompletionRequests()).as("the agent is not asked to answer").hasSize(1);
        List<LLMChatMessage> transcript = transcript(run);
        assertThat(transcript).extracting(LLMChatMessage::getRole).containsExactly(LLMRole.USER, LLMRole.ASSISTANT, LLMRole.USER);
        assertThat(transcript.getLast().getContent()).startsWith(Defaults.AGENT_AUTHOR_COMMAND_NOTE)
                .contains("Command: /get-book-info").contains(agent.getManuscript().getName());
        assertThat(transcript).flatExtracting(LLMChatMessage::getToolCalls).as("no call of the agent").isEmpty();
    }

    @Test
    void theAgentReadsTheCommandAndTheNextMessageAsOneMessageOfTheAuthor() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.text("Hello."), MockLLMResponse.text("Seen."));
        AgentRun run = await(startConversation(agent));
        runner.command(agentRunService.find(run.getId()), "/get-book-info");
        run = await(run);

        runner.send(agentRunService.find(run.getId()), "What did it say?");
        await(run);

        List<String> users = llm.lastCompletionRequest().getMessages("user");
        assertThat(users).hasSize(2);
        assertThat(users.getLast()).contains("Command: /get-book-info").endsWith("What did it say?");
    }

    @Test
    void aFailingCommandIsTheErrorInTheMessage() throws Exception {
        Agent agent = readingAgent();
        llm.reply("Hello.");
        AgentRun run = await(startConversation(agent));

        runner.command(agentRunService.find(run.getId()), "/get-book-info too many arguments");
        run = await(run);

        assertThat(transcript(run).getLast().getContent()).contains("Command: /get-book-info too many arguments").contains("Result:\nError:");
    }

    @Test
    void aCommandIsQueuedWhileAConversationWorksAndRunsWhereTheAgentWouldHaveWaited() throws Exception {
        Agent agent = readingAgent();
        CountDownLatch gate = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("Counted.").waitFor(gate, Duration.ofSeconds(20)));
        AgentRun run = startConversation(agent);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));

        runner.command(agentRunService.find(run.getId()), "/get-book-info");

        assertThat(runner.queued(run)).containsExactly(new AgentRunner.QueuedMessage(runner.queued(run).getFirst().id(),
                "/get-book-info", true));
        assertThat(transcript(run)).as("nothing is written yet").hasSize(1);
        gate.countDown();
        run = await(run);

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        assertThat(runner.queued(run)).isEmpty();
        assertThat(llm.getCompletionRequests()).as("a command does not make the agent answer").hasSize(1);
        assertThat(transcript(run)).extracting(LLMChatMessage::getRole).containsExactly(LLMRole.USER, LLMRole.ASSISTANT, LLMRole.USER);
        assertThat(transcript(run).getLast().getContent()).contains("Command: /get-book-info");
    }

    @Test
    void aMessageAndACommandQueuedTogetherAreDeliveredInOrderAndTheAgentAnswers() throws Exception {
        Agent agent = readingAgent();
        CountDownLatch gate = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("Counted.").waitFor(gate, Duration.ofSeconds(20)), MockLLMResponse.text("Understood."));
        AgentRun run = startConversation(agent);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));
        runner.send(agentRunService.find(run.getId()), "Look at the book.");
        runner.command(agentRunService.find(run.getId()), "/get-book-info");

        gate.countDown();
        run = await(run);

        List<LLMChatMessage> transcript = transcript(run);
        assertThat(transcript).extracting(LLMChatMessage::getRole).containsExactly(LLMRole.USER, LLMRole.ASSISTANT, LLMRole.USER,
                LLMRole.USER, LLMRole.ASSISTANT);
        assertThat(transcript.get(2).getContent()).isEqualTo("Look at the book.");
        assertThat(transcript.get(3).getContent()).contains("Command: /get-book-info");
        assertThat(llm.getCompletionRequests()).hasSize(2);
    }

    @Test
    void aForcedCommandThrowsAwayTheRoundAndTheAgentGoesOn() throws Exception {
        Agent agent = readingAgent();
        CountDownLatch gate = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("The answer nobody reads.").waitFor(gate, Duration.ofSeconds(20)),
                MockLLMResponse.text("Seen the book."));
        AgentRun run = startConversation(agent);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));
        runner.command(agentRunService.find(run.getId()), "/get-book-info");

        runner.force(agentRunService.find(run.getId()), runner.queued(run).getFirst().id());
        run = await(run);
        gate.countDown();

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        List<LLMChatMessage> transcript = transcript(run);
        assertThat(transcript).extracting(LLMChatMessage::getRole).containsExactly(LLMRole.USER, LLMRole.USER, LLMRole.ASSISTANT);
        assertThat(transcript.get(1).getContent()).contains("Command: /get-book-info");
        assertThat(transcript.getLast().getContent()).isEqualTo("Seen the book.");
    }

    @Test
    void aCommandIsNotQueuedForAnAutonomousRunAndNotGivenToAnEndedOne() throws Exception {
        Agent agent = readingAgent();
        CountDownLatch gate = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("Done.").waitFor(gate, Duration.ofSeconds(20)));
        AgentRun run = startRun(agent);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));

        AgentRun working = agentRunService.find(run.getId());
        assertThatThrownBy(() -> runner.command(working, "/get-book-info")).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("conversation");
        gate.countDown();
        run = await(run);

        runner.command(run, "/get-book-info");
        run = await(run);
        assertThat(run.getStatus()).as("an autonomous run that waits takes a command").isEqualTo(AgentRunStatus.IDLE);

        AgentRun stopped = agentRunService.stop(run, true);
        assertThatThrownBy(() -> runner.command(await(stopped, Set.of(AgentRunStatus.STOPPED_KEPT)), "/get-book-info"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aPausedRunTakesACommandAndStaysPaused() throws Exception {
        Agent agent = readingAgent();
        llm.reply("Hello.");
        AgentRun run = await(startConversation(agent));
        run = agentRunService.pause(run);
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.PAUSED);

        runner.command(run, "/get-book-info");
        run = await(run, Set.of(AgentRunStatus.PAUSED));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        assertThat(transcript(run).getLast().getContent()).contains("Command: /get-book-info");
        assertThat(llm.getCompletionRequests()).hasSize(1);
    }

    @Test
    void aMessageWaitsWhileACommandIsRun() throws Exception {
        Agent agent = readingAgent();
        CountDownLatch gate = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("Hello."), MockLLMResponse.text("The summary.").waitFor(gate, Duration.ofSeconds(20)));
        AgentRun run = await(startConversation(agent));
        runner.command(agentRunService.find(run.getId()), "/compact");
        llm.awaitCompletionRequests(2, Duration.ofSeconds(10));

        AgentRun idle = agentRunService.find(run.getId());
        assertThatThrownBy(() -> runner.send(idle, "Too early.")).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("command is running");
        assertThatThrownBy(() -> runner.command(idle, "/get-book-info")).isInstanceOf(IllegalStateException.class);
        gate.countDown();
        await(run);
    }

    // --- /compact ---

    @Test
    void compactReplacesTheTranscriptTheModelReadsByASummaryItWrote() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}"), MockLLMResponse.text("Counted."),
                MockLLMResponse.text("Task: count parts. Done: counted."), MockLLMResponse.text("Carrying on."));
        AgentRun run = await(startConversation(agent));
        assertThat(transcript(run)).hasSize(4);

        runner.command(agentRunService.find(run.getId()), "/compact the counting");
        run = await(run);

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        // the request: everything so far, and the compaction prompt with the focus as the last message, no tools called
        MockLLMRequest request = llm.lastCompletionRequest();
        String ask = request.getMessages("user").getLast();
        assertThat(ask).startsWith(Defaults.DEFAULT_AGENT_COMPACTION_PROMPT.strip()).endsWith("The author wants the summary to stress: the counting");
        assertThat(request.getMessages("tool")).hasSize(1);
        // what the model reads now is the summary alone
        List<LLMChatMessage> transcript = transcript(run);
        assertThat(transcript).hasSize(1);
        assertThat(transcript.getFirst().getRole()).isEqualTo(LLMRole.USER);
        assertThat(transcript.getFirst().getContent()).startsWith(Defaults.AGENT_COMPACTED_NOTE)
                .contains("Task: count parts. Done: counted.");
        // the user still has all of it, marked
        List<AgentMessage> rows = messageService.messages(run);
        assertThat(rows).hasSize(5);
        assertThat(rows.subList(0, 4)).allMatch(AgentMessage::isCompacted).noneMatch(AgentMessage::isSummary);
        assertThat(rows.getLast().isSummary()).isTrue();
        assertThat(rows.getLast().isCompacted()).isFalse();
        assertThat(messageService.count(run)).isEqualTo(5);
        assertThat(messageService.getContextTokens(run)).as("the summary with the system prompt and the tools, not the summary alone")
                .isGreaterThan(transcript.getFirst().getContent().length() / 4 + Defaults.AGENT_RUNNER_NOTE.length() / 4);

        // the agent goes on from the summary, with the next message of the author after it as one message
        runner.send(agentRunService.find(run.getId()), "Now the chapters.");
        run = await(run);
        List<String> users = llm.lastCompletionRequest().getMessages("user");
        assertThat(users).hasSize(1);
        assertThat(users.getFirst()).startsWith(Defaults.AGENT_COMPACTED_NOTE).endsWith("Now the chapters.");
        assertThat(llm.lastCompletionRequest().getMessages("tool")).isEmpty();
    }

    @Test
    void compactWithoutAFocusAsksJustThePrompt() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.text("Hello."), MockLLMResponse.text("The summary."));
        AgentRun run = await(startConversation(agent));

        runner.command(agentRunService.find(run.getId()), "/compact");
        await(run);

        assertThat(llm.lastCompletionRequest().getMessages("user").getLast()).isEqualTo(Defaults.DEFAULT_AGENT_COMPACTION_PROMPT.strip());
    }

    @Test
    void theCompactionPromptFollowsTheAgentThenTheDefaults() throws Exception {
        Agent agent = readingAgent();
        agent.setCompactionPrompt("Write three lines.");
        agentService.save(agent);
        llm.enqueue(MockLLMResponse.text("Hello."), MockLLMResponse.text("One. Two. Three."));
        AgentRun run = await(startConversation(agent));

        runner.command(agentRunService.find(run.getId()), "/compact");
        await(run);

        assertThat(llm.lastCompletionRequest().getMessages("user").getLast()).isEqualTo("Write three lines.");
    }

    @Test
    void thePlanSurvivesACompactionInTheSummary() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("update_plan", "{\"items\":[{\"text\":\"Count parts\",\"status\":\"done\"},"
                        + "{\"text\":\"Count lore\",\"status\":\"pending\"}]}"),
                MockLLMResponse.text("Planned."), MockLLMResponse.text("The summary."));
        AgentRun run = await(startConversation(agent));

        runner.command(agentRunService.find(run.getId()), "/compact");
        run = await(run);

        assertThat(transcript(run).getFirst().getContent()).contains("The summary.").contains("Your plan when this summary was written")
                .contains("Count parts").contains("Count lore");
    }

    @Test
    void whenTheOldestPartDoesNotFitTheContextItIsLeftOutOfTheSummaryRequest() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}"), MockLLMResponse.text("Counted."),
                MockLLMResponse.text("The summary."));
        AgentRun run = await(startConversation(agent));
        assertThat(transcript(run)).hasSize(4);
        agent = agentService.findForUser(agent.getUuid());
        agent.setContextCap(50);
        agentService.save(agent);

        runner.command(agentRunService.find(run.getId()), "/compact");
        run = await(run);

        // the newest message always stays, so the model sees at least what it answered last
        assertThat(llm.lastCompletionRequest().getMessages("user")).as("only the compaction prompt was left").hasSize(1);
        assertThat(transcript(run).getFirst().getContent()).contains(String.format(Defaults.AGENT_COMPACTED_TRIMMED, 3));
    }

    @Test
    void aCompactionThatFailsLeavesTheTranscriptAndSaysSo() throws Exception {
        Agent agent = readingAgent();
        llm.enqueue(MockLLMResponse.text("Hello."), MockLLMResponse.toolCall("get-book-info", "{}"));
        AgentRun run = await(startConversation(agent));

        runner.command(agentRunService.find(run.getId()), "/compact");
        run = await(run);

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        List<LLMChatMessage> transcript = transcript(run);
        assertThat(transcript).extracting(LLMChatMessage::getRole).containsExactly(LLMRole.USER, LLMRole.ASSISTANT, LLMRole.USER);
        assertThat(transcript.getLast().getContent()).contains("compact your work, but it failed").contains("called a tool")
                .contains("Nothing was changed");
        assertThat(messageService.messages(run)).noneMatch(AgentMessage::isCompacted);
    }

    @Test
    void compactIsQueuedLikeAnyCommandAndTheAgentWaitsAfterIt() throws Exception {
        Agent agent = readingAgent();
        CountDownLatch gate = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("Counted.").waitFor(gate, Duration.ofSeconds(20)), MockLLMResponse.text("The summary."));
        AgentRun run = startConversation(agent);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));
        runner.command(agentRunService.find(run.getId()), "/compact");

        gate.countDown();
        run = await(run);

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        assertThat(llm.getCompletionRequests()).hasSize(2);
        assertThat(transcript(run)).hasSize(1);
        assertThat(transcript(run).getFirst().getContent()).contains("The summary.");
    }

    @Test
    void clearWorkspaceDeletesTheRestAndTellsTheAgentWhatWasKept() throws Exception {
        Agent agent = readingAgent();
        llm.reply("Hello.");
        AgentRun run = await(startConversation(agent));
        VirtualFileSystem files = fileSystemService.open(run);
        files.writeText(VirtualPath.parse("/notes.md"), "n", false);
        files.writeText(VirtualPath.parse("/keep.md"), "k", false);
        files.writeText(VirtualPath.parse("/drafts/one.md"), "1", false);
        files.writeText(VirtualPath.parse("/drafts/two.txt"), "2", false);
        files.writeText(VirtualPath.parse("/my notes/a.md"), "a", false);
        files.createFolder(VirtualPath.parse("/empty"));

        runner.command(agentRunService.find(run.getId()), "/clear-workspace /keep.md /drafts/two.txt \"/my notes\"");
        run = await(run);

        assertThat(run.getStatus()).as("the run stays as it was").isEqualTo(AgentRunStatus.IDLE);
        assertThat(llm.getCompletionRequests()).as("the agent is not asked to answer").hasSize(1);
        assertThat(files.walk(VirtualPath.ROOT, 100)).extracting(e -> e.path().toString())
                .containsExactlyInAnyOrder("/keep.md", "/drafts", "/drafts/two.txt", "/my notes", "/my notes/a.md");
        assertThat(transcript(run).getLast().getContent()).startsWith(Defaults.AGENT_AUTHOR_COMMAND_NOTE)
                .contains("Command: /clear-workspace").contains("Deleted 2 file(s) and 1 folder(s)").contains("/keep.md, /drafts/two.txt, /my notes");
    }

    @Test
    void clearWorkspaceWithoutArgumentsEmptiesItAndAMissingKeepDeletesNothing() throws Exception {
        Agent agent = readingAgent();
        llm.reply("Hello.");
        AgentRun run = await(startConversation(agent));
        VirtualFileSystem files = fileSystemService.open(run);
        files.writeText(VirtualPath.parse("/a.md"), "a", false);
        files.writeText(VirtualPath.parse("/dir/b.md"), "b", false);

        runner.command(agentRunService.find(run.getId()), "/clear-workspace /missing.md");
        run = await(run);
        assertThat(files.walk(VirtualPath.ROOT, 100)).hasSize(3);
        assertThat(transcript(run).getLast().getContent()).contains("Error:").contains("/missing.md").contains("Nothing was deleted");

        runner.command(agentRunService.find(run.getId()), "/clear-workspace");
        run = await(run);
        assertThat(files.walk(VirtualPath.ROOT, 100)).isEmpty();
        assertThat(transcript(run).getLast().getContent()).contains("Deleted 2 file(s) and 1 folder(s) from the workspace.").doesNotContain("Kept");
    }
}
