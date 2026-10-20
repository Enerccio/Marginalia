package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentRunner;
import com.github.enerccio.marginalia.domain.service.agent.AgentContext;
import com.github.enerccio.marginalia.domain.service.agent.AgentContext.Kind;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMRequest;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The context of a run taken apart: what {@code AgentRunner#context} lists is what the model was sent, part by part.
 */
class AgentContextTest extends AgentTestBase {

    @Autowired
    private AgentRunner runner;

    private AgentRun finishedTurn() throws Exception {
        loginAgentUser();
        Agent agent = newAgent(manuscript());
        agent.setSystemPrompt("You are a test agent.");
        agent.setInstructions("Count the parts.");
        agent.setAllowedCommands(List.of("get-book-info"));
        agent.setDestructiveMode(DestructiveCommandMode.DENY);
        agent = agentService.save(agent);
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}"), MockLLMResponse.text("The book is counted."));
        AgentRun run = agentRunService.start(agent);
        runner.run(run);
        long end = System.currentTimeMillis() + 20_000;
        while (System.currentTimeMillis() < end) {
            AgentRun current = agentRunService.find(run.getId());
            if (current.getStatus() == AgentRunStatus.IDLE && !runner.isWorking(current)) {
                return current;
            }
            Thread.sleep(25);
        }
        throw new AssertionError("The run did not end its turn");
    }

    @Test
    void theContextIsTheSystemMessageTheToolsAndTheConversationAsTheModelWasSentThem() throws Exception {
        AgentRun run = finishedTurn();
        MockLLMRequest sent = llm.lastCompletionRequest();

        AgentContext context = runner.context(run);

        List<AgentContext.Part> system = context.parts().stream().filter(part -> part.kind().isSystem()).toList();
        assertThat(system.getFirst().kind()).isEqualTo(Kind.SYSTEM_PROMPT);
        assertThat(system.getFirst().text()).isEqualTo("You are a test agent.");
        assertThat(system).extracting(AgentContext.Part::kind).contains(Kind.INSTRUCTIONS, Kind.NOTE);
        assertThat(String.join("\n\n", system.stream().map(AgentContext.Part::text).toList()))
                .as("the sections joined are the system message that was sent").isEqualTo(sent.getMessages("system").getFirst());

        List<String> toolNames = context.parts().stream().filter(part -> part.kind() == Kind.TOOL).map(AgentContext.Part::title).toList();
        assertThat(toolNames).contains("Tool: get-book-info", "Tool: finish", "Tool: read_file");
        Set<String> sentTools = new java.util.HashSet<>();
        for (JsonElement tool : sent.getBody().getAsJsonArray("tools")) {
            sentTools.add("Tool: " + tool.getAsJsonObject().getAsJsonObject("function").get("name").getAsString());
        }
        assertThat(toolNames).as("the tools of the next request").containsExactlyInAnyOrderElementsOf(sentTools);

        List<AgentContext.Part> conversation = context.parts().stream().filter(part -> !part.kind().isSystem() && part.kind() != Kind.TOOL).toList();
        assertThat(conversation).extracting(AgentContext.Part::kind)
                .containsExactly(Kind.USER, Kind.ASSISTANT, Kind.RESULT, Kind.ASSISTANT);
        assertThat(conversation.get(1).title()).contains("get-book-info");
        assertThat(conversation.get(1).text()).contains("[call ").contains("get-book-info");
        assertThat(conversation.get(2).title()).endsWith("result of get-book-info");
        assertThat(conversation.get(3).text()).isEqualTo("The book is counted.");

        assertThat(context.parts()).allSatisfy(part -> assertThat(part.tokens()).isPositive());
        assertThat(context.estimatedTokens()).isEqualTo(context.parts().stream().mapToLong(AgentContext.Part::tokens).sum());
        assertThat(context.status()).isEqualTo(AgentRunStatus.IDLE);
        assertThat(context.rounds()).isEqualTo(2);
    }

    @Test
    void theMarkdownHoldsEveryPartInACodeFence() throws Exception {
        AgentRun run = finishedTurn();

        String markdown = runner.context(run).toMarkdown();

        assertThat(markdown).startsWith("# Context of the agent")
                .contains("## System message", "### Prompt of the agent", "## Tools (", "### Tool: get-book-info", "## Conversation (4 message(s)")
                .contains("You are a test agent.", "The book is counted.");
        assertThat(markdown).contains("\n```\nYou are a test agent.\n```\n");
    }

    @Test
    void aRunThatDoesNotExistHasNoContext() throws Exception {
        AgentRun run = finishedTurn();
        AgentRun other = new AgentRun();
        other.setUuid("no-such-run");
        assertThat(run).isNotNull();

        assertThatThrownBy(() -> runner.context(other)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> runner.context(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
