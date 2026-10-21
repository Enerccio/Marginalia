package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.AgentApprovalService;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.AgentRunner;
import com.github.enerccio.marginalia.domain.service.AgentService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMRequest;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Sub-agents: threads of a run with a conversation of their own. The agent starts them with {@code spawn_agent}; with a limit
 * of one the call waits for the answer, with more it returns a handle until the limit is reached. A limit of zero offers no
 * tools, and a sub-agent never gets the tools to start another.
 */
class AgentSubagentsTest extends AgentTestBase {

    private static final String SUB_MARK = "You are a sub-agent";
    private static final Set<AgentRunStatus> ENDED_TURN = Set.of(AgentRunStatus.IDLE, AgentRunStatus.DONE,
            AgentRunStatus.PAUSED, AgentRunStatus.STOPPED_KEPT, AgentRunStatus.STOPPED_DISCARDED, AgentRunStatus.FAILED);

    @Autowired
    private AgentRunner runner;

    @Autowired
    private AgentMessageService messageService;

    @Autowired
    private AgentApprovalService approvalService;

    @Autowired
    private com.github.enerccio.marginalia.domain.service.AgentVirtualFileSystemService fileSystemService;

    // the user of the test: one per test, the agents are theirs
    private User user;

    private User agentUser() throws Exception {
        if (user == null) {
            user = loginAgentUser();
        }
        return user;
    }

    private Agent agentWith(Integer maxSubagents) throws Exception {
        agentUser();
        Agent agent = newAgent(manuscript());
        agent.setSystemPrompt("You are a test agent.");
        agent.setInstructions("Check the book.");
        agent.setAllowedCommands(List.of("get-book-info"));
        agent.setDestructiveMode(DestructiveCommandMode.DENY);
        agent.setMaxSubagents(maxSubagents);
        return agentService.save(agent);
    }

    private AgentRun startRun(Agent agent) throws Exception {
        AgentRun run = agentRunService.start(agent);
        runner.run(run);
        return run;
    }

    private AgentRun await(AgentRun run, Set<AgentRunStatus> statuses) throws Exception {
        long end = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < end) {
            AgentRun current = agentRunService.find(run.getId());
            if (statuses.contains(current.getStatus()) && !runner.isWorking(current)) {
                return current;
            }
            Thread.sleep(25);
        }
        throw new AssertionError("The run is " + agentRunService.find(run.getId()).getStatus() + " after 30 s, wanted " + statuses);
    }

    private AgentRun await(AgentRun run) throws Exception {
        return await(run, ENDED_TURN);
    }

    private static boolean isSub(MockLLMRequest request) {
        return request.getMessages("system").getFirst().contains(SUB_MARK);
    }

    private static List<String> tools(MockLLMRequest request) {
        List<String> names = new java.util.ArrayList<>();
        request.getBody().getAsJsonArray("tools").forEach(tool -> names.add(tool.getAsJsonObject().getAsJsonObject("function").get("name").getAsString()));
        return names;
    }

    /**
     * What the agent's model says, by how far it got: the n-th time it is asked it makes the n-th call; the sub-agents answer with the text.
     */
    private void script(String subAnswer, MockLLMResponse... agentRounds) {
        llm.respondWith(request -> {
            if (isSub(request)) {
                return MockLLMResponse.text(subAnswer);
            }
            int done = request.getMessages("tool").size();
            return done < agentRounds.length ? agentRounds[done] : MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}");
        });
    }

    @Test
    void anAgentWithoutSubagentsIsOfferedNoToolsForThem() throws Exception {
        Agent agent = agentWith(null);
        llm.reply("Nothing to do.");

        await(startRun(agent));

        MockLLMRequest request = llm.lastCompletionRequest();
        assertThat(tools(request)).contains("finish").doesNotContain("spawn_agent", "await_agent", "agent_status", "cancel_agent");
        assertThat(request.getMessages("system").getFirst()).doesNotContain("Sub-agents:");
    }

    @Test
    void withALimitOfOneSpawningWaitsForTheAnswer() throws Exception {
        Agent agent = agentWith(1);
        script("The book has no parts yet.", MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"Count the parts.\"}"));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.DONE);
        List<MockLLMRequest> requests = llm.getCompletionRequests();
        MockLLMRequest first = requests.getFirst();
        assertThat(tools(first)).contains("spawn_agent").doesNotContain("await_agent", "agent_status", "cancel_agent");
        assertThat(first.getMessages("system").getFirst()).contains("Sub-agents: you can start up to 1").contains("waits until the sub-agent is done");

        // the answer is the result of the call
        List<LLMChatMessage> transcript = messageService.transcript(run);
        String result = transcript.stream().filter(message -> message.getRole() == LLMRole.TOOL).findFirst().orElseThrow().getContent();
        assertThat(result).contains("sub1").contains("is done").contains("The book has no parts yet.");

        // the sub-agent was told its task, with its own prompt, and has no way to start another or to end the run
        MockLLMRequest sub = requests.stream().filter(AgentSubagentsTest::isSub).findFirst().orElseThrow();
        assertThat(sub.getMessages("user")).contains("Count the parts.");
        assertThat(sub.getMessages("system").getFirst()).startsWith("You are a test agent.").contains("Check the book.");
        assertThat(tools(sub)).contains("get-book-info", "read_file").doesNotContain("spawn_agent", "await_agent", "finish", "ask_user", "checkpoint",
                "update_plan", "report");
        // the request of the sub-agent counts in the budgets of the run
        assertThat(run.getRounds()).isEqualTo(3);
    }

    @Test
    void withMoreTheCallReturnsAtOnceAndTheAnswerIsAwaited() throws Exception {
        Agent agent = agentWith(3);
        script("Found three parts.",
                MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"Count the parts.\",\"context\":\"Look at the whole book.\"}"),
                MockLLMResponse.toolCall("await_agent", "{\"handle\":\"sub1\"}"));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.DONE);
        MockLLMRequest first = llm.getCompletionRequests().getFirst();
        assertThat(tools(first)).contains("spawn_agent", "await_agent", "agent_status", "cancel_agent");
        List<String> results = messageService.transcript(run).stream().filter(message -> message.getRole() == LLMRole.TOOL)
                .map(LLMChatMessage::getContent).toList();
        assertThat(results.get(0)).contains("sub1 was started").contains("1 of 3 working");
        assertThat(results.get(1)).contains("sub1").contains("is done").contains("Found three parts.");
        MockLLMRequest sub = llm.getCompletionRequests().stream().filter(AgentSubagentsTest::isSub).findFirst().orElseThrow();
        assertThat(sub.getMessages("user").getFirst()).contains("Count the parts.").contains("Material:").contains("Look at the whole book.");
    }

    @Test
    void theCallThatReachesTheLimitWaitsUntilASubagentEnds() throws Exception {
        Agent agent = agentWith(2);
        CountDownLatch gate = new CountDownLatch(1);
        llm.respondWith(request -> {
            if (isSub(request)) {
                return MockLLMResponse.text("answer of " + request.getMessages("user").getFirst()).waitFor(gate, Duration.ofSeconds(30));
            }
            return switch (request.getMessages("tool").size()) {
                case 0 -> MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"first\"}").andToolCall("spawn_agent", "{\"task\":\"second\"}");
                case 2 -> MockLLMResponse.toolCall("await_agent", "{\"handle\":\"sub2\"}");
                default -> MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}");
            };
        });

        AgentRun run = startRun(agent);
        // the model was asked by the agent and the first sub-agent, the second spawn waits for a place
        llm.awaitCompletionRequests(2, Duration.ofSeconds(10));
        Thread.sleep(700);
        assertThat(agentRunService.find(run.getId()).getStatus()).isEqualTo(AgentRunStatus.RUNNING);
        assertThat(messageService.transcript(run).stream().filter(message -> message.getRole() == LLMRole.TOOL))
                .as("the first spawn is answered, the second waits").hasSize(1);

        gate.countDown();
        run = await(run);

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.DONE);
        List<String> results = messageService.transcript(run).stream().filter(message -> message.getRole() == LLMRole.TOOL)
                .map(LLMChatMessage::getContent).toList();
        assertThat(results.get(0)).contains("sub1 was started").contains("1 of 2 working");
        assertThat(results.get(1)).contains("sub2 was started").contains("limit of 2").contains("waited")
                .as("whichever of the two asked the model first").contains("is done").contains("answer of ");
    }

    @Test
    void anAgentCanCancelASubagent() throws Exception {
        // two requests in the air, so that the agent can ask while the sub-agent's request is waiting
        updatePermission(agentUser(), PermissionType.AGENTS, permission -> permission.setAgentsMaxConcurrency(2));
        Agent agent = agentWith(2);
        CountDownLatch never = new CountDownLatch(1);
        llm.respondWith(request -> {
            if (isSub(request)) {
                return MockLLMResponse.text("never read").waitFor(never, Duration.ofSeconds(30));
            }
            return switch (request.getMessages("tool").size()) {
                case 0 -> MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"slow job\"}");
                case 1 -> MockLLMResponse.toolCall("cancel_agent", "{\"handle\":\"sub1\"}");
                case 2 -> MockLLMResponse.toolCall("await_agent", "{\"handle\":\"sub1\"}");
                default -> MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}");
            };
        });

        AgentRun run = await(startRun(agent));
        never.countDown();

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.DONE);
        List<String> results = messageService.transcript(run).stream().filter(message -> message.getRole() == LLMRole.TOOL)
                .map(LLMChatMessage::getContent).toList();
        assertThat(results.get(1)).contains("Cancel asked for sub1");
        assertThat(results.get(2)).contains("sub1").contains("was stopped").contains("canceled");
        assertThat(messageService.threads(run)).singleElement().satisfies(thread -> {
            assertThat(thread.end()).as("the end is stored").isEqualTo("CANCELLED");
            assertThat(thread.result()).isEqualTo("It was canceled.");
        });
    }

    @Test
    void theStatusOfTheRunListsTheSubagentsThatWork() throws Exception {
        updatePermission(agentUser(), PermissionType.AGENTS, permission -> permission.setAgentsMaxConcurrency(2));
        Agent agent = agentWith(2);
        CountDownLatch never = new CountDownLatch(1);
        llm.respondWith(request -> {
            if (isSub(request)) {
                return MockLLMResponse.text("never read").waitFor(never, Duration.ofSeconds(30));
            }
            return switch (request.getMessages("tool").size()) {
                case 0 -> MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"Count\\nthe   dragons in the lorebook\"}");
                case 1 -> MockLLMResponse.toolCall("get_run_status", "{}");
                case 2 -> MockLLMResponse.toolCall("cancel_agent", "{\"handle\":\"sub1\"}");
                case 3 -> MockLLMResponse.toolCall("get_run_status", "{}");
                default -> MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}");
            };
        });

        AgentRun run = await(startRun(agent));
        never.countDown();

        List<String> results = messageService.transcript(run).stream().filter(message -> message.getRole() == LLMRole.TOOL)
                .map(LLMChatMessage::getContent).toList();
        assertThat(results.get(1)).contains("Sub-agents: 1 working of at most 2")
                .containsPattern("- sub1: running, round \\d+, \\d+ tokens.* - Count the dragons in the lorebook");
        assertThat(results.get(3)).as("the sub-agent waits for its model, it is told to stop").contains("- sub1: running")
                .contains(", asked to stop - Count the dragons");
    }

    @Test
    void pausingTheRunSuspendsTheSubagentAndLeavesTheCallOfTheAgentOpen() throws Exception {
        Agent agent = agentWith(1);
        CountDownLatch never = new CountDownLatch(1);
        llm.respondWith(request -> isSub(request) ? MockLLMResponse.text("never read").waitFor(never, Duration.ofSeconds(30))
                : MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"slow job\"}"));

        AgentRun run = startRun(agent);
        llm.awaitCompletionRequests(2, Duration.ofSeconds(10));
        agentRunService.pause(agentRunService.find(run.getId()));

        run = await(run, Set.of(AgentRunStatus.PAUSED));
        never.countDown();

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        assertThat(messageService.transcript(run)).as("the call waits for its answer").noneMatch(message -> message.getRole() == LLMRole.TOOL);
        assertThat(messageService.threads(run)).singleElement().satisfies(thread -> {
            assertThat(thread.handle()).isEqualTo("sub1");
            assertThat(thread.isOver()).as("only paused").isFalse();
            assertThat(thread.task()).isEqualTo("slow job");
            assertThat(thread.conversation()).extracting(LLMChatMessage::getRole).containsExactly(LLMRole.USER);
        });
    }

    @Test
    void aResumedRunGoesOnWithTheSubagentAndTheCallThatWaitedForItWaitsAgain() throws Exception {
        Agent agent = agentWith(1);
        CountDownLatch never = new CountDownLatch(1);
        AtomicInteger subRequests = new AtomicInteger();
        llm.respondWith(request -> {
            if (isSub(request)) {
                return subRequests.incrementAndGet() == 1 ? MockLLMResponse.text("never read").waitFor(never, Duration.ofSeconds(30))
                        : MockLLMResponse.text("The names are fine.");
            }
            return request.getMessages("tool").isEmpty() ? MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"check the names\"}")
                    : MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}");
        });
        AgentRun run = startRun(agent);
        llm.awaitCompletionRequests(2, Duration.ofSeconds(10));
        agentRunService.pause(agentRunService.find(run.getId()));
        run = await(run, Set.of(AgentRunStatus.PAUSED));
        never.countDown();

        runner.run(agentRunService.resume(run));
        run = await(run, Set.of(AgentRunStatus.DONE));

        List<LLMChatMessage> transcript = messageService.transcript(run);
        int call = IntStream.range(0, transcript.size()).filter(i -> transcript.get(i).hasToolCalls()).findFirst().orElseThrow();
        assertThat(transcript.get(call + 1).getRole()).as("answered where the call is").isEqualTo(LLMRole.TOOL);
        assertThat(transcript.get(call + 1).getContent()).contains("sub1").contains("is done").contains("The names are fine.");
        assertThat(transcript).as("started once").filteredOn(LLMChatMessage::hasToolCalls)
                .filteredOn(message -> message.getToolCalls().stream().anyMatch(c -> c.getName().equals("spawn_agent"))).hasSize(1);
        assertThat(messageService.threads(run)).singleElement().satisfies(thread -> {
            assertThat(thread.end()).isEqualTo("DONE");
            assertThat(thread.result()).isEqualTo("The names are fine.");
            assertThat(thread.conversation()).extracting(LLMChatMessage::getRole).containsExactly(LLMRole.USER);
        });
        assertThat(subRequests.get()).isEqualTo(2);
    }

    /**
     * A sub-agent whose server died in the middle of a round: its conversation ends with two calls, neither has a result.
     */
    private AgentRun runWithCutOffSubagent(Agent agent, String marker) throws Exception {
        CountDownLatch never = new CountDownLatch(1);
        AtomicInteger subRequests = new AtomicInteger();
        llm.respondWith(request -> {
            if (isSub(request)) {
                return subRequests.incrementAndGet() == 1 ? MockLLMResponse.text("never read").waitFor(never, Duration.ofSeconds(30))
                        : MockLLMResponse.text("Written.");
            }
            return request.getMessages("tool").isEmpty() ? MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"write two notes\"}")
                    : MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}");
        });
        AgentRun run = startRun(agent);
        llm.awaitCompletionRequests(2, Duration.ofSeconds(10));
        agentRunService.pause(agentRunService.find(run.getId()));
        run = await(run, Set.of(AgentRunStatus.PAUSED));
        never.countDown();
        messageService.appendAnswerToThread(run, "sub1", LLMChatMessage.ofToolCalls(null, List.of(
                LLMToolCall.of("k1", "write_file", "{\"path\":\"/tmp/sub1/a.md\",\"content\":\"one\"}"),
                LLMToolCall.of("k2", "write_file", "{\"path\":\"/tmp/sub1/b.md\",\"content\":\"two\"}"))), 10, null);
        if (marker != null) {
            fileSystemService.writeCallMarker(run, "sub1", marker);
        }
        runner.run(agentRunService.resume(run));
        return await(run, Set.of(AgentRunStatus.DONE));
    }

    @Test
    void theCallsOfASubagentThatDidNotBeginWhenTheServerStoppedAreMadeAfterTheRestart() throws Exception {
        Agent agent = agentWith(1);

        AgentRun run = runWithCutOffSubagent(agent, null);

        List<LLMChatMessage> conversation = messageService.threads(run).getFirst().conversation();
        assertThat(conversation.stream().filter(message -> message.getRole() == LLMRole.TOOL).map(LLMChatMessage::getContent).toList())
                .hasSize(2).noneMatch(result -> result.startsWith("Error"));
        var files = fileSystemService.open(run);
        assertThat(files.readText(com.github.enerccio.marginalia.domain.service.agent.VirtualPath.parse("/tmp/sub1/a.md"))).isEqualTo("one");
        assertThat(files.readText(com.github.enerccio.marginalia.domain.service.agent.VirtualPath.parse("/tmp/sub1/b.md"))).isEqualTo("two");
        assertThat(fileSystemService.readCallMarker(run, "sub1")).isNull();
    }

    @Test
    void aCallOfASubagentThatHadBegunIsAnsweredAsInterruptedAndTheOnesAfterItAreNotMade() throws Exception {
        Agent agent = agentWith(1);

        AgentRun run = runWithCutOffSubagent(agent, "k1");

        List<String> results = messageService.threads(run).getFirst().conversation().stream().filter(message -> message.getRole() == LLMRole.TOOL)
                .map(LLMChatMessage::getContent).toList();
        assertThat(results).hasSize(2);
        assertThat(results.get(0)).startsWith("Error: this call was interrupted");
        assertThat(results.get(1)).startsWith("Error: this call was not made because an earlier call");
        assertThat(fileSystemService.open(run).stat(com.github.enerccio.marginalia.domain.service.agent.VirtualPath.parse("/tmp/sub1/b.md"))).isNull();
    }

    @Test
    void aSubagentThatWorkedBeforeThePauseKeepsItsConversationAndAnswerCallsAfterIt() throws Exception {
        updatePermission(agentUser(), PermissionType.AGENTS, permission -> permission.setAgentsMaxConcurrency(2));
        Agent agent = agentWith(2);
        CountDownLatch never = new CountDownLatch(1);
        AtomicInteger subRequests = new AtomicInteger();
        llm.respondWith(request -> {
            if (isSub(request)) {
                return switch (subRequests.incrementAndGet()) {
                    case 1 -> MockLLMResponse.toolCall("get-book-info", "{}");
                    case 2 -> MockLLMResponse.text("never read").waitFor(never, Duration.ofSeconds(30));
                    default -> MockLLMResponse.text("It is a short book.");
                };
            }
            return switch (request.getMessages("tool").size()) {
                case 0 -> MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"describe the book\"}");
                case 1 -> MockLLMResponse.toolCall("await_agent", "{\"handle\":\"sub1\"}");
                default -> MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}");
            };
        });
        AgentRun run = startRun(agent);
        llm.awaitCompletionRequests(4, Duration.ofSeconds(10));
        agentRunService.pause(agentRunService.find(run.getId()));
        run = await(run, Set.of(AgentRunStatus.PAUSED));
        never.countDown();

        assertThat(messageService.transcript(run)).as("spawn_agent is answered, await_agent waits").filteredOn(message -> message.getRole() == LLMRole.TOOL)
                .hasSize(1);
        assertThat(messageService.threads(run).getFirst().conversation()).extracting(LLMChatMessage::getRole)
                .containsExactly(LLMRole.USER, LLMRole.ASSISTANT, LLMRole.TOOL);

        runner.run(agentRunService.resume(run));
        run = await(run, Set.of(AgentRunStatus.DONE));

        List<String> results = messageService.transcript(run).stream().filter(message -> message.getRole() == LLMRole.TOOL)
                .map(LLMChatMessage::getContent).toList();
        assertThat(results).hasSize(3);
        assertThat(results.get(1)).contains("sub1").contains("is done").contains("It is a short book.");
        assertThat(messageService.threads(run).getFirst().conversation()).as("the conversation went on").hasSize(3);
    }

    @Test
    void anAgentThatEndsItsTurnWhileSubagentsWorkIsToldOnce() throws Exception {
        updatePermission(agentUser(), PermissionType.AGENTS, permission -> permission.setAgentsMaxConcurrency(2));
        Agent agent = agentWith(2);
        CountDownLatch never = new CountDownLatch(1);
        llm.respondWith(request -> {
            if (isSub(request)) {
                return MockLLMResponse.text("never read").waitFor(never, Duration.ofSeconds(30));
            }
            return switch (request.getMessages("tool").size()) {
                case 0 -> request.getMessages("user").size() > 1 ? MockLLMResponse.text("I am done, really.")
                        : MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"slow job\"}");
                default -> MockLLMResponse.text("I am done.");
            };
        });

        AgentRun run = await(startRun(agent));
        never.countDown();

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.IDLE);
        List<String> users = messageService.transcript(run).stream().filter(message -> message.getRole() == LLMRole.USER)
                .map(LLMChatMessage::getContent).toList();
        assertThat(users).anyMatch(text -> text.contains("1 sub-agent(s) still work") && text.contains("sub1"));
        assertThat(users.stream().filter(text -> text.contains("still work"))).hasSize(1);
    }

    /**
     * An agent with a second model: the protocol answers with at most {@code reply} tokens, which the sub-agent on it is told.
     */
    private Agent agentWithAuditor(int reply, java.util.function.Consumer<AgentModelProfile> auditorSetup) throws Exception {
        Agent agent = agentWith(2);
        agent.setAllowedCommands(List.of("get-book-info", "list-parts"));
        AgentModelProfile auditor = profile("auditor");
        var protocol = protocolService.findForUser(auditor.getProtocolUuid());
        protocol.setReplyTokens(reply);
        protocolService.save(protocol);
        auditor.setDescription("a cheap model for reading");
        auditorSetup.accept(auditor);
        agent.getModelProfiles().add(auditor);
        return agentService.save(agent);
    }

    @Test
    void theModelsForSubagentsAreListedForTheAgent() throws Exception {
        Agent agent = agentWithAuditor(111, auditor -> auditor.setCommands(List.of("get-book-info", "write-everything")));
        llm.reply("Nothing to do.");

        await(startRun(agent));

        String system = llm.lastCompletionRequest().getMessages("system").getFirst();
        assertThat(system).contains("Models for sub-agents").contains("overseer (your own model, the default when profile is left out)")
                .contains("auditor: a cheap model for reading [commands: get-book-info]");
    }

    @Test
    void aSubagentRunsOnTheModelItWasAskedForWithItsCommandsOnly() throws Exception {
        Agent agent = agentWithAuditor(111, auditor -> auditor.setCommands(List.of("get-book-info")));
        script("Looked.", MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"read\",\"profile\":\"Auditor\"}"),
                MockLLMResponse.toolCall("await_agent", "{\"handle\":\"sub1\"}"));

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.DONE);
        MockLLMRequest sub = llm.getCompletionRequests().stream().filter(AgentSubagentsTest::isSub).findFirst().orElseThrow();
        assertThat(sub.getMessages("system").getFirst()).contains("at most 111 tokens");
        assertThat(tools(sub)).contains("get-book-info", "read_file").doesNotContain("list-parts");
        MockLLMRequest first = llm.getCompletionRequests().getFirst();
        assertThat(tools(first)).as("the agent keeps its own").contains("get-book-info", "list-parts");
        assertThat(first.getMessages("system").getFirst()).doesNotContain("at most 111 tokens");

        assertThat(messageService.threads(run)).singleElement().satisfies(thread -> {
            assertThat(thread.profile()).isEqualTo("auditor");
            assertThat(thread.rows().stream().map(row -> row.getTelemetry()).filter(java.util.Objects::nonNull))
                    .allMatch(telemetry -> telemetry.contains("\"profile\":\"auditor\""));
        });
        List<String> results = messageService.transcript(run).stream().filter(message -> message.getRole() == LLMRole.TOOL)
                .map(LLMChatMessage::getContent).toList();
        assertThat(results.get(1)).contains("sub1 on auditor");
    }

    @Test
    void aModelThatIsNotThereIsAnErrorWithTheModels() throws Exception {
        Agent agent = agentWithAuditor(111, auditor -> {
        });
        script("never", MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"read\",\"profile\":\"poet\"}"));

        AgentRun run = await(startRun(agent));

        String result = messageService.transcript(run).stream().filter(message -> message.getRole() == LLMRole.TOOL).findFirst().orElseThrow().getContent();
        assertThat(result).contains("There is no model poet").contains("auditor").contains("leave it out for your own");
        assertThat(messageService.threads(run)).isEmpty();
    }

    @Test
    void withoutTheOwnModelASubagentNeedsAnotherOne() throws Exception {
        Agent agent = agentWithAuditor(111, auditor -> {
        });
        agent.setAllowSelfSubagent(false);
        agentService.save(agent);
        script("never", MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"read\"}"),
                MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"read\",\"profile\":\"overseer\"}"));

        AgentRun run = await(startRun(agent));

        List<String> results = messageService.transcript(run).stream().filter(message -> message.getRole() == LLMRole.TOOL)
                .map(LLMChatMessage::getContent).toList();
        assertThat(results.get(0)).contains("You may not start a sub-agent on your own model").contains("auditor");
        assertThat(results.get(1)).contains("You may not start a sub-agent on your own model");
        assertThat(llm.getCompletionRequests().getFirst().getMessages("system").getFirst())
                .contains("overseer (your own model): you may not start a sub-agent on it.");
    }

    @Test
    void anAgentWithNothingToChooseAndNoOwnModelIsOfferedNoSpawn() throws Exception {
        Agent agent = agentWith(2);
        agent.setAllowSelfSubagent(false);
        agentService.save(agent);
        llm.reply("Nothing to do.");

        await(startRun(agent));

        assertThat(tools(llm.lastCompletionRequest())).doesNotContain("spawn_agent");
    }

    @Test
    void theOwnModelOfTheAgentHasItsCommandsToo() throws Exception {
        Agent agent = agentWith(1);
        agent.setAllowedCommands(List.of("get-book-info", "list-parts"));
        agent.getMainProfile().setCommands(List.of("list-parts"));
        agentService.save(agent);
        script("Looked.", MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"read\"}"));

        await(startRun(agent));

        MockLLMRequest sub = llm.getCompletionRequests().stream().filter(AgentSubagentsTest::isSub).findFirst().orElseThrow();
        assertThat(tools(sub)).contains("list-parts").doesNotContain("get-book-info");
        assertThat(llm.getCompletionRequests().getFirst().getMessages("system").getFirst()).contains("Models for sub-agents")
                .contains("[commands: list-parts]");
    }

    @Test
    void aModelWithoutToolsAnswersWithTextOnly() throws Exception {
        Agent agent = agentWithAuditor(111, auditor -> auditor.setTools(false));
        script("Words.", MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"say\",\"profile\":\"auditor\"}"));

        await(startRun(agent));

        MockLLMRequest sub = llm.getCompletionRequests().stream().filter(AgentSubagentsTest::isSub).findFirst().orElseThrow();
        assertThat(sub.getBody().has("tools") ? tools(sub) : List.of()).isEmpty();
    }

    @Test
    void cloningAnAgentKeepsWhatItsModelsAndSubagentsNeed() throws Exception {
        Agent agent = agentWithAuditor(111, auditor -> auditor.setCommands(List.of("get-book-info")));
        agent.setAllowSelfSubagent(false);
        agentService.save(agent);

        Agent copy = agentService.cloneAgent(agent, "copy", manuscript());

        assertThat(copy.getMaxSubagents()).isEqualTo(2);
        assertThat(copy.isSelfSubagentAllowed()).isFalse();
        assertThat(copy.getModelProfiles().get(1).getCommands()).containsExactly("get-book-info");
    }

    private Agent askingAgent() throws Exception {
        Agent agent = agentWith(1);
        agent.setAllowedCommands(List.of("search-replace-parts"));
        agent.setDestructiveMode(DestructiveCommandMode.ASK);
        return agentService.save(agent);
    }

    private void awaitApproval(AgentRun run) throws Exception {
        long end = System.currentTimeMillis() + 20_000;
        while (agentRunService.find(run.getId()).getStatus() != AgentRunStatus.WAITING_APPROVAL && System.currentTimeMillis() < end) {
            Thread.sleep(25);
        }
        assertThat(agentRunService.find(run.getId()).getStatus()).isEqualTo(AgentRunStatus.WAITING_APPROVAL);
    }

    private void scriptDestructiveSub() {
        AtomicInteger subRequests = new AtomicInteger();
        llm.respondWith(request -> {
            if (isSub(request)) {
                return subRequests.incrementAndGet() == 1 ? MockLLMResponse.toolCall("search-replace-parts", "{\"search\":\"zzz\",\"replace\":\"y\"}")
                        : MockLLMResponse.text("Handled.");
            }
            return request.getMessages("tool").isEmpty() ? MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"replace\"}")
                    : MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}");
        });
    }

    @Test
    void aDestructiveCallOfASubagentWaitsForTheUserInTheQueueOfTheRun() throws Exception {
        Agent agent = askingAgent();
        scriptDestructiveSub();

        AgentRun run = startRun(agent);
        awaitApproval(run);

        assertThat(approvalService.pending(run)).singleElement().satisfies(approval -> assertThat(approval.command()).isEqualTo("search-replace-parts"));
        assertThat(messageService.threads(run).getFirst().conversation()).as("the sub-agent waits at its call").hasSize(2);
        approvalService.decide(run, approvalService.pending(run).getFirst().id(), true);
        run = await(run, Set.of(AgentRunStatus.DONE));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.DONE);
        assertThat(messageService.threads(run).getFirst().result()).isEqualTo("Handled.");
    }

    @Test
    void aRefusedCallOfASubagentIsAnErrorForIt() throws Exception {
        Agent agent = askingAgent();
        scriptDestructiveSub();

        AgentRun run = startRun(agent);
        awaitApproval(run);
        approvalService.decide(run, approvalService.pending(run).getFirst().id(), false);
        run = await(run, Set.of(AgentRunStatus.DONE));

        assertThat(messageService.threads(run).getFirst().conversation().stream().filter(message -> message.getRole() == LLMRole.TOOL).findFirst()
                .orElseThrow().getContent()).startsWith("Error:");
    }

    @Test
    void pausingWhileASubagentWaitsForAnApprovalRefusesItAndPausesTheRun() throws Exception {
        Agent agent = askingAgent();
        scriptDestructiveSub();

        AgentRun run = startRun(agent);
        awaitApproval(run);
        agentRunService.pause(agentRunService.find(run.getId()));
        run = await(run, Set.of(AgentRunStatus.PAUSED));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        assertThat(approvalService.pending(run)).isEmpty();
    }

    private String workspaceFile(Agent agent, String path) throws Exception {
        return fileSystemService.openWorkspace(agent).readText(com.github.enerccio.marginalia.domain.service.agent.VirtualPath.parse(path));
    }

    @Test
    void aSubagentWritesInItsFolderAndItsAnswerIsSavedThere() throws Exception {
        Agent agent = agentWith(1);
        AtomicInteger subRequests = new AtomicInteger();
        llm.respondWith(request -> {
            if (isSub(request)) {
                return switch (subRequests.incrementAndGet()) {
                    case 1 -> MockLLMResponse.toolCall("write_file", "{\"path\":\"/tmp/sub1/notes.md\",\"content\":\"found\"}");
                    case 2 -> MockLLMResponse.toolCall("write_file", "{\"path\":\"/elsewhere.md\",\"content\":\"no\"}");
                    default -> MockLLMResponse.text("The long answer. ".repeat(600));
                };
            }
            return request.getMessages("tool").isEmpty() ? MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"write\"}")
                    : MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}");
        });

        AgentRun run = await(startRun(agent));

        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.DONE);
        assertThat(workspaceFile(agent, "/tmp/sub1/notes.md")).isEqualTo("found");
        assertThat(workspaceFile(agent, "/tmp/sub1/result.md")).startsWith("The long answer.").hasSize("The long answer. ".repeat(600).strip().length());
        List<LLMChatMessage> conversation = messageService.threads(run).getFirst().conversation();
        assertThat(conversation.stream().filter(message -> message.getRole() == LLMRole.TOOL).map(LLMChatMessage::getContent).toList())
                .satisfies(results -> {
                    assertThat(results.get(0)).doesNotStartWith("Error");
                    assertThat(results.get(1)).startsWith("Error").contains("read only for you").contains("/tmp/sub1");
                });
        assertThat(llm.getCompletionRequests().stream().filter(AgentSubagentsTest::isSub).findFirst().orElseThrow().getMessages("system").getFirst())
                .contains("Your folder: /tmp/sub1");
        // the agent is told where all of it is
        String result = messageService.transcript(run).stream().filter(message -> message.getRole() == LLMRole.TOOL).findFirst().orElseThrow().getContent();
        assertThat(result).contains("cut off").contains("/tmp/sub1/result.md");
    }

    @Test
    void whatAnEarlierRunLeftInTheFolderOfASubagentIsGone() throws Exception {
        Agent agent = agentWith(1);
        script("Done.", MockLLMResponse.toolCall("spawn_agent", "{\"task\":\"first\"}"));
        com.github.enerccio.marginalia.domain.service.agent.VirtualFileSystem files = fileSystemService.openWorkspace(agent);
        files.writeText(com.github.enerccio.marginalia.domain.service.agent.VirtualPath.parse("/tmp/sub1/stale.md"), "old", false);

        await(startRun(agent));

        assertThat(files.stat(com.github.enerccio.marginalia.domain.service.agent.VirtualPath.parse("/tmp/sub1/stale.md"))).isNull();
        assertThat(files.stat(com.github.enerccio.marginalia.domain.service.agent.VirtualPath.parse("/tmp/sub1/result.md"))).isNotNull();
    }

    @Test
    void theMostSubagentsIsCheckedWhenTheAgentIsSaved() throws Exception {
        Agent agent = agentWith(1);

        agent.setMaxSubagents(AgentService.MAX_SUBAGENTS + 1);
        assertThatThrownBy(() -> agentService.save(agent)).isInstanceOf(IllegalArgumentException.class);
        agent.setMaxSubagents(-1);
        assertThatThrownBy(() -> agentService.save(agent)).isInstanceOf(IllegalArgumentException.class);
        agent.setMaxSubagents(AgentService.MAX_SUBAGENTS);
        assertThat(agentService.save(agent).getMaxSubagents()).isEqualTo(AgentService.MAX_SUBAGENTS);
    }

    @Test
    void theConcurrencyOfARunIsWhatTheAdministratorGaveTheUser() throws Exception {
        Agent agent = agentWith(2);
        AgentRun run = agentRunService.start(agent);
        assertThat(agentRunService.getMaxConcurrency(run)).as("default").isEqualTo(1);
        assertThat(agentRunService.getMaxSubagents(run)).isEqualTo(2);
        assertThat(agentService.getGrant(agentUser()).maxConcurrency()).isEqualTo(1);

        updatePermission(agentUser(), PermissionType.AGENTS, permission -> permission.setAgentsMaxConcurrency(4));

        assertThat(agentService.getGrant(agentUser()).maxConcurrency()).isEqualTo(4);
        assertThat(agentRunService.getMaxConcurrency(run)).isEqualTo(4);
    }
}
