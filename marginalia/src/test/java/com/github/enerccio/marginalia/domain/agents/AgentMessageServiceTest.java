package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
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

    @Test
    void whatTheProviderWantsBackWithACallIsKeptInTheTranscript() throws Exception {
        AgentRun run = run();
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "task")));
        messageService.appendAnswer(run, LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("c1", "ask_user", "{}", "c2lnbmVk"),
                LLMToolCall.of("c2", "list_files", "{}"))), 100, null);

        List<LLMToolCall> calls = messageService.transcript(run).get(1).getToolCalls();

        assertThat(calls.get(0).getSignature()).isEqualTo("c2lnbmVk");
        assertThat(calls.get(1).getSignature()).isNull();
    }

    @Test
    void compactingMarksEverythingAndTheSummaryIsAllTheModelReads() throws Exception {
        AgentRun run = run();
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "first"), LLMChatMessage.of(LLMRole.ASSISTANT, "second")));

        AgentMessage summary = messageService.compact(run, LLMChatMessage.of(LLMRole.USER, "the summary"), 7);

        assertThat(summary.isSummary()).isTrue();
        assertThat(summary.getOrdinal()).isEqualTo(2);
        assertThat(messageService.transcript(run)).extracting(LLMChatMessage::getContent).containsExactly("the summary");
        List<AgentMessage> rows = messageService.messages(run);
        assertThat(rows).extracting(AgentMessage::isCompacted).containsExactly(true, true, false);
        assertThat(rows).extracting(AgentMessage::isSummary).containsExactly(false, false, true);
        assertThat(messageService.count(run)).as("the user still has all of it").isEqualTo(3);
        assertThat(messageService.getContextTokens(run)).isEqualTo(7);

        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "after")));
        assertThat(messageService.transcript(run)).extracting(LLMChatMessage::getContent).containsExactly("the summary", "after");
        assertThat(messageService.messages(run).getLast().getOrdinal()).isEqualTo(3);

        messageService.compact(run, LLMChatMessage.of(LLMRole.USER, "the second summary"), 5);
        assertThat(messageService.transcript(run)).extracting(LLMChatMessage::getContent).containsExactly("the second summary");
        assertThat(messageService.messages(run)).extracting(AgentMessage::isCompacted).containsExactly(true, true, true, true, false);
    }

    @Test
    void theConversationOfASubagentIsStoredApartFromTheOneOfTheAgent() throws Exception {
        AgentRun run = run();
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "go"),
                LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("c1", "spawn_agent", "{}")))));
        messageService.startThread(run, "sub1", "check names", "c1", LLMChatMessage.of(LLMRole.USER, "check names"));
        messageService.startThread(run, "sub2", "check dates", "c1b", LLMChatMessage.of(LLMRole.USER, "check dates"));
        messageService.appendAnswerToThread(run, "sub1", LLMChatMessage.of(LLMRole.ASSISTANT, "names are fine"), 120, null);
        messageService.appendToThread(run, "sub2", List.of(LLMChatMessage.of(LLMRole.ASSISTANT, "thinking")));

        assertThat(messageService.count(run)).as("only the agent's own").isEqualTo(2);
        assertThat(messageService.transcript(run)).hasSize(2);
        assertThat(messageService.messages(run)).extracting(AgentMessage::getOrdinal).containsExactly(0, 1);
        List<AgentMessageService.SubThread> threads = messageService.threads(run);
        assertThat(threads).extracting(AgentMessageService.SubThread::handle).containsExactly("sub1", "sub2");
        assertThat(threads.getFirst().task()).isEqualTo("check names");
        assertThat(threads.getFirst().callId()).isEqualTo("c1");
        assertThat(threads.getFirst().rounds()).isEqualTo(1);
        assertThat(threads.getFirst().tokens()).isEqualTo(120);
        assertThat(threads.getFirst().rows()).extracting(AgentMessage::getOrdinal).containsExactly(0, 1);
        assertThat(threads.get(1).rows()).extracting(AgentMessage::getOrdinal).as("counted per conversation").containsExactly(0, 1);
        assertThat(threads.getFirst().conversation()).extracting(LLMChatMessage::getContent).containsExactly("check names", "names are fine");
    }

    @Test
    void aConversationThatWasNotEndedGoesOnAndOneThatWasEndedDoesNot() throws Exception {
        AgentRun run = run();
        messageService.startThread(run, "sub1", "a", "c1", LLMChatMessage.of(LLMRole.USER, "a"));
        messageService.startThread(run, "sub2", "b", "c2", LLMChatMessage.of(LLMRole.USER, "b"));
        messageService.endThread(run, "sub1", "DONE", "the answer");
        messageService.endThread(run, "sub1", "FAILED", "the first end wins");

        assertThat(messageService.endOpenThreads(run, "CANCELLED", "the book is another one")).isEqualTo(1);
        assertThat(messageService.endOpenThreads(run, "CANCELLED", "again")).isZero();

        List<AgentMessageService.SubThread> threads = messageService.threads(run);
        assertThat(threads.getFirst().end()).isEqualTo("DONE");
        assertThat(threads.getFirst().result()).isEqualTo("the answer");
        assertThat(threads.getFirst().conversation()).as("without the end").extracting(LLMChatMessage::getContent).containsExactly("a");
        assertThat(threads.get(1).end()).isEqualTo("CANCELLED");
        assertThat(threads.get(1).result()).isEqualTo("the book is another one");
    }

    @Test
    void goingBackOverTheCallThatStartedASubagentTakesTheSubagentAlong() throws Exception {
        AgentRun run = run();
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "go"),
                LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("c1", "spawn_agent", "{}"))),
                LLMChatMessage.ofToolResult("c1", "started sub1"),
                LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("c2", "spawn_agent", "{}"))),
                LLMChatMessage.ofToolResult("c2", "started sub2")));
        messageService.startThread(run, "sub1", "a", "c1", LLMChatMessage.of(LLMRole.USER, "a"));
        messageService.startThread(run, "sub2", "b", "c2", LLMChatMessage.of(LLMRole.USER, "b"));

        messageService.rewind(run, 3);

        List<AgentMessageService.SubThread> threads = messageService.threads(run);
        assertThat(threads).extracting(AgentMessageService.SubThread::rewound).containsExactly(false, true);
    }

    @Test
    void deletingTheRunForGoodTakesTheConversationsOfItsSubagentsToo() throws Exception {
        AgentRun run = run();
        messageService.startThread(run, "sub1", "a", "c1", LLMChatMessage.of(LLMRole.USER, "a"));

        messageService.deleteForRun(run, true);

        assertThat(messageService.threads(run)).isEmpty();
    }
}
