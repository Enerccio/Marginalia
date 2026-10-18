package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The transcript of a run: appended in order, read back as the messages the model is sent.
 */
class AgentMessageServiceTest extends AgentTestBase {

    @Autowired
    private AgentMessageService messageService;

    private AgentRun run() throws Exception {
        loginAgentUser();
        return agentRunService.start(agent());
    }

    @Test
    void messagesComeBackInTheOrderTheyWereAppended() throws Exception {
        AgentRun run = run();
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "first"),
                LLMChatMessage.ofToolCalls("thinking", List.of(LLMToolCall.of("c1", "read_file", "{\"path\":\"/a\"}")))));
        messageService.append(run, List.of(LLMChatMessage.ofToolResult("c1", "text of a")));

        List<LLMChatMessage> transcript = messageService.transcript(run);

        assertThat(messageService.count(run)).isEqualTo(3);
        assertThat(transcript).extracting(LLMChatMessage::getRole).containsExactly(LLMRole.USER, LLMRole.ASSISTANT, LLMRole.TOOL);
        assertThat(transcript.get(1).getContent()).isEqualTo("thinking");
        assertThat(transcript.get(1).getToolCalls()).singleElement().satisfies(call -> {
            assertThat(call.getId()).isEqualTo("c1");
            assertThat(call.getName()).isEqualTo("read_file");
            assertThat(call.getArguments()).isEqualTo("{\"path\":\"/a\"}");
        });
        assertThat(transcript.get(2).getToolCallId()).isEqualTo("c1");
    }

    @Test
    void theTranscriptsOfRunsAreSeparate() throws Exception {
        // a paused run is not working, the user may have another
        AgentRun one = agentRunService.pause(run());
        AgentRun two = agentRunService.start(agent());
        messageService.append(one, List.of(LLMChatMessage.of(LLMRole.USER, "one")));
        messageService.append(two, List.of(LLMChatMessage.of(LLMRole.USER, "two"), LLMChatMessage.of(LLMRole.ASSISTANT, "x")));

        assertThat(messageService.transcript(one)).extracting(LLMChatMessage::getContent).containsExactly("one");
        assertThat(messageService.count(two)).isEqualTo(2);
    }

    @Test
    void aRunOfAnotherUserHasNoTranscriptForYou() throws Exception {
        AgentRun run = run();
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "secret")));

        loginAgentUser();

        assertThatThrownBy(() -> messageService.transcript(run)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "x"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deletingTheRunForGoodTakesTheTranscriptWithIt() throws Exception {
        AgentRun run = run();
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "gone")));

        messageService.deleteForRun(run, true);

        assertThat(messageService.count(run)).isZero();
    }

    @Test
    void theContextOfARunIsOnTheLastAnswerOfTheModel() throws Exception {
        AgentRun run = run();
        assertThat(messageService.getContextTokens(run)).as("no answer yet").isZero();

        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "task")));
        messageService.appendAnswer(run, LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("c1", "read_file", "{}"))), 1200, null);
        messageService.append(run, List.of(LLMChatMessage.ofToolResult("c1", "text")));
        assertThat(messageService.getContextTokens(run)).isEqualTo(1200);

        messageService.appendAnswer(run, LLMChatMessage.of(LLMRole.ASSISTANT, "done"), 4800, null);
        // the results of the calls and the messages of the user after it don't hide it
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "and more")));
        assertThat(messageService.getContextTokens(run)).isEqualTo(4800);
        assertThat(messageService.transcript(run)).extracting(LLMChatMessage::getRole)
                .containsExactly(LLMRole.USER, LLMRole.ASSISTANT, LLMRole.TOOL, LLMRole.ASSISTANT, LLMRole.USER);
    }
}
