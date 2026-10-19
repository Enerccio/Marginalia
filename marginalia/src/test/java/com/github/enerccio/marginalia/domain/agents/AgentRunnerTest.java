package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.AgentRunner;
import com.github.enerccio.marginalia.domain.service.AgentVirtualFileSystemService;
import com.github.enerccio.marginalia.domain.service.agent.ImageFiles;
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
    private AgentMessageService messageService;

    @Autowired
    private AgentVirtualFileSystemService fileSystemService;

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
}
