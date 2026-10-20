package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.AgentRunner;
import com.github.enerccio.marginalia.domain.service.AgentService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
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
                "update_plan");
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
