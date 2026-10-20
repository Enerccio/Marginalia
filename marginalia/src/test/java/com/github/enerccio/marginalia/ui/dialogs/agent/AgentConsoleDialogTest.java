package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.agent.RunToolsImpl;
import com.github.enerccio.marginalia.domain.service.agent.VirtualPath;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.test.ComponentTree;
import com.github.enerccio.marginalia.test.TestImages;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.github.enerccio.marginalia.test.ComponentTree.button;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The console of an agent: the transcript of its latest run, what the run waits for, writing to it, and the buttons
 * that pause, resume and stop it.
 */
class AgentConsoleDialogTest extends AgentTestBase {

    @Autowired
    private AgentMessageService agentMessageService;

    @Autowired
    private CheckpointService checkpointService;

    @Autowired
    private AgentApprovalService approvals;

    @Autowired
    private AgentVirtualFileSystemService fileSystemService;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;
    private User user;
    private Agent agent;
    private final List<String> sent = new ArrayList<>();
    private final List<List<String>> sentImages = new ArrayList<>();
    private final List<AgentRun> takenUp = new ArrayList<>();
    // what the fake runner reports as queued, and the ids it was told to force
    private List<AgentRunner.QueuedMessage> queued = List.of();
    private final List<String> forced = new ArrayList<>();
    private final List<String> cancelled = new ArrayList<>();
    private final List<AgentRun> pausedNow = new ArrayList<>();
    private final List<String> commandLines = new ArrayList<>();
    private boolean changed;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        user = loginAgentUser();
        agent = agent();
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private AgentConsoleDialog console() {
        AgentConsoleDialog dialog = new AgentConsoleDialog(agent);
        ReflectionTestUtils.setField(dialog, "agentRunner", new AgentRunner() {
            @Override
            public void run(AgentRun run) {
                takenUp.add(run);
            }

            @Override
            public void run(AgentRun run, String firstMessage) {
                takenUp.add(run);
            }

            @Override
            public AgentRun send(AgentRun run, String text, List<String> images) throws Exception {
                sent.add(text);
                sentImages.add(images);
                return agentRunService.transition(run, AgentRunStatus.RUNNING, null);
            }

            @Override
            public List<String> consoleCommands(Agent agent) {
                return List.of(COMPACT, "get-book-info");
            }

            @Override
            public AgentRun command(AgentRun run, String line) {
                commandLines.add(line);
                return run;
            }

            @Override
            public List<AgentRunner.QueuedMessage> queued(AgentRun run) {
                return queued;
            }

            @Override
            public void force(AgentRun run, String id) {
                forced.add(id);
            }

            @Override
            public boolean cancelQueued(AgentRun run, String id) {
                cancelled.add(id);
                return true;
            }

            @Override
            public AgentRun pauseNow(AgentRun run) throws Exception {
                pausedNow.add(run);
                return agentRunService.pause(run);
            }

            @Override
            public com.github.enerccio.marginalia.domain.service.agent.AgentContext context(AgentRun run) {
                return new com.github.enerccio.marginalia.domain.service.agent.AgentContext("Agent", run.getStatus(), run.getRounds(),
                        List.of(new com.github.enerccio.marginalia.domain.service.agent.AgentContext.Part(
                                        com.github.enerccio.marginalia.domain.service.agent.AgentContext.Kind.SYSTEM_PROMPT, "Prompt of the agent", "Be careful.", 3),
                                new com.github.enerccio.marginalia.domain.service.agent.AgentContext.Part(
                                        com.github.enerccio.marginalia.domain.service.agent.AgentContext.Kind.USER, "#1 user", "Start.", 2)),
                        List.of(), 100, 0);
            }

            @Override
            public boolean isWorking(AgentRun run) {
                return false;
            }
        });
        dialog.setOnChange(() -> changed = true);
        dialog.create();
        // the context tab asks the runner too
        ReflectionTestUtils.setField(dialog.getContextView(), "agentRunner", ReflectionTestUtils.getField(dialog, "agentRunner"));
        dialog.open();
        return dialog;
    }

    private AgentRun startedRun() throws Exception {
        return agentRunService.start(agent);
    }

    private void say(AgentRun run, LLMChatMessage... messages) throws Exception {
        agentMessageService.append(run, List.of(messages));
    }

    private static String textOf(Component component) {
        return ComponentTree.all(component).filter(c -> c instanceof Span || c instanceof com.vaadin.flow.component.html.Div
                        || c instanceof Markdown)
                .map(c -> c instanceof Markdown markdown ? markdown.getContent() : c.getElement().getText())
                .reduce("", (a, b) -> a + "\n" + b);
    }

    @Test
    void aConversationThatWorksTakesMessagesAndShowsThemQueuedWithAButtonToForceThem() throws Exception {
        AgentRun run = agentRunService.start(agent, true);
        say(run, LLMChatMessage.of(LLMRole.USER, "Start."));
        queued = List.of(new AgentRunner.QueuedMessage("q1", "Do this next."));

        AgentConsoleDialog dialog = console();

        assertThat(dialog.getSendButton().isEnabled()).isTrue();
        assertThat(dialog.getMessageField().isEnabled()).isTrue();
        assertThat(dialog.getQueuedBubbles()).hasSize(1);
        com.vaadin.flow.component.html.Div bubble = dialog.getQueuedBubbles().getFirst();
        assertThat(textOf(bubble)).contains("Do this next.").contains("Queued");
        // the last bubble of the transcript
        assertThat(dialog.getTranscriptBox().getComponentAt(dialog.getTranscriptBox().getComponentCount() - 1)).isSameAs(bubble);

        ComponentTree.all(bubble).filter(c -> c instanceof Button).map(c -> (Button) c).findFirst().orElseThrow().click();

        assertThat(forced).containsExactly("q1");

        // the second small button takes the message back
        ComponentTree.all(bubble).filter(c -> c instanceof Button).map(c -> (Button) c).skip(1).findFirst().orElseThrow().click();

        assertThat(cancelled).containsExactly("q1");

        // delivered: the bubble is gone and the message is in the transcript, once
        say(run, LLMChatMessage.of(LLMRole.USER, "Do this next."));
        queued = List.of();
        dialog.refreshRun();

        assertThat(dialog.getQueuedBubbles()).isEmpty();
        assertThat(textOf(dialog.getTranscriptBox())).doesNotContain("Queued");
    }

    @Test
    void anAutonomousRunThatWorksDoesNotTakeMessages() throws Exception {
        agentRunService.start(agent);

        AgentConsoleDialog dialog = console();

        assertThat(dialog.getSendButton().isEnabled()).isFalse();
        assertThat(dialog.getMessageField().isEnabled()).isFalse();
    }

    @Test
    void anAgentThatNeverRanSaysSo() {
        AgentConsoleDialog dialog = console();

        assertThat(dialog.getStatusLabel().getText()).startsWith("The agent has not run yet");
        assertThat(dialog.getTranscriptBox().getChildren().count()).isZero();
        assertThat(dialog.getSendButton().isEnabled()).isFalse();
        assertThat(dialog.getPauseButton().isEnabled()).isFalse();
        assertThat(dialog.getResumeButton().isEnabled()).isFalse();
        assertThat(dialog.getStopButton().isEnabled()).isFalse();
    }

    @Test
    void theTranscriptShowsWhatWasSaidAndTheCallsFolded() throws Exception {
        AgentRun run = startedRun();
        say(run, LLMChatMessage.of(LLMRole.USER, "Check the dragon"),
                LLMChatMessage.ofToolCalls("Let me look", List.of(LLMToolCall.of("c1", "list-parts", "{\"limit\": 5}"))),
                LLMChatMessage.ofToolResult("c1", "1: Once upon a time"),
                LLMChatMessage.of(LLMRole.ASSISTANT, "All good"));

        AgentConsoleDialog dialog = console();

        // the user, the assistant with its call, the answer; the result sits in the folded call
        assertThat(dialog.getTranscriptBox().getChildren().toList()).hasSize(4);
        List<String> shown = dialog.getTranscriptBox().getChildren().map(AgentConsoleDialogTest::textOf).toList();
        assertThat(shown.get(0)).contains("Check the dragon");
        assertThat(shown.get(1)).contains("Let me look");
        assertThat(shown.get(2)).contains("list-parts");
        assertThat(shown.get(3)).contains("All good");
        String text = textOf(dialog.getTranscriptBox());
        assertThat(text).contains("Check the dragon", "Let me look", "All good", "1: Once upon a time");
        Details call = ComponentTree.all(dialog.getTranscriptBox(), Details.class).findFirst().orElseThrow();
        assertThat(call.getSummaryText()).startsWith("list-parts").contains("limit");
        assertThat(call.isOpened()).isFalse();
        assertThat(textOf(call)).contains("{\"limit\": 5}", "1: Once upon a time");
    }

    @Test
    void whatARewindTookOutOfTheMemoryOfTheAgentIsGreyedInTheTranscript() throws Exception {
        AgentRun run = startedRun();
        say(run, LLMChatMessage.of(LLMRole.USER, "task"), LLMChatMessage.of(LLMRole.ASSISTANT, "first"));
        checkpointService.ensureStart(run);
        say(run, LLMChatMessage.of(LLMRole.ASSISTANT, "second"));
        AgentConsoleDialog dialog = console();
        assertThat(dialog.getTranscriptBox().getChildren().filter(c -> c.hasClassName(CssStyles.AGENT_REWOUND))).isEmpty();
        run = agentRunService.pause(agentRunService.find(run.getId()));

        checkpointService.rewind(run, checkpointService.list(run).getFirst().id());
        dialog.rewound();

        List<Component> shown = dialog.getTranscriptBox().getChildren().toList();
        // task, first, second (greyed), the notice of the run
        assertThat(shown).hasSize(4);
        assertThat(shown.stream().map(c -> c.hasClassName(CssStyles.AGENT_REWOUND)).toList()).containsExactly(false, false, true, false);
        assertThat(textOf(shown.getLast())).contains("rewound the run to checkpoint");
    }

    @Test
    void aMessageOfTheAuthorHasAButtonThatGoesBackToBeforeItWhileNothingWorks() throws Exception {
        AgentRun run = startedRun();
        say(run, LLMChatMessage.of(LLMRole.USER, "task"), LLMChatMessage.of(LLMRole.ASSISTANT, "first"));
        checkpointService.makeManual(run, "after the first answer");
        say(run, LLMChatMessage.of(LLMRole.USER, "do it differently"), LLMChatMessage.of(LLMRole.ASSISTANT, "second"));
        String second = agentMessageService.messages(run).get(2).getUuid();
        AgentConsoleDialog dialog = console();

        // a button under each message of the author, none while the agent works
        assertThat(dialog.getRollbackButtons()).hasSize(2);
        assertThat(dialog.getRollbackButtons().stream().map(Button::isEnabled)).containsOnly(false);

        run = agentRunService.pause(agentRunService.find(run.getId()));
        dialog.refreshRun();

        assertThat(dialog.getRollbackButtons().stream().map(Button::isEnabled)).containsOnly(true);

        dialog.rollBackNow(second);

        List<Component> shown = dialog.getTranscriptBox().getChildren().toList();
        // task, first, do it differently (greyed), second (greyed), the notice of the run
        assertThat(shown).hasSize(5);
        assertThat(shown.stream().map(c -> c.hasClassName(CssStyles.AGENT_REWOUND)).toList())
                .containsExactly(false, false, true, true, false);
        assertThat(textOf(shown.getLast())).contains("rewound the run to checkpoint");
        assertThat(dialog.getRollbackButtons()).as("the forgotten messages have none: the first one and the notice have").hasSize(2);
    }

    @Test
    void everyMessageShowsWhenItWasWritten() throws Exception {
        AgentRun run = startedRun();
        say(run, LLMChatMessage.of(LLMRole.USER, "first"), LLMChatMessage.of(LLMRole.ASSISTANT, "second"));
        AgentConsoleDialog dialog = console();

        List<Component> shown = dialog.getTranscriptBox().getChildren().toList();

        assertThat(shown).hasSize(2);
        for (Component block : shown) {
            Component time = block.getChildren().findFirst().orElseThrow();
            assertThat(time.hasClassName(CssStyles.AGENT_TIME)).as("the time comes first in the block").isTrue();
            assertThat(textOf(time).strip()).as("today: the time of day only").matches("\\d{1,2}:\\d{2}:\\d{2}.*");
            assertThat(time.getElement().getAttribute("title")).isNotBlank();
        }
    }

    @Test
    void newMessagesAreAddedWhenTheConsoleLooksAgain() throws Exception {
        AgentRun run = startedRun();
        say(run, LLMChatMessage.of(LLMRole.USER, "first"));
        AgentConsoleDialog dialog = console();
        assertThat(dialog.getTranscriptBox().getChildren().count()).isEqualTo(1);

        say(run, LLMChatMessage.of(LLMRole.ASSISTANT, "second"));
        dialog.refreshRun();

        assertThat(dialog.getTranscriptBox().getChildren().count()).isEqualTo(2);
        assertThat(textOf(dialog.getTranscriptBox())).contains("first", "second");
        dialog.refreshRun();
        assertThat(dialog.getTranscriptBox().getChildren().count()).as("nothing twice").isEqualTo(2);
    }

    @Test
    void theStateAndTheTokensOfTheRunAreShown() throws Exception {
        AgentRun run = startedRun();
        agentRunService.addUsage(run, 100, 20);

        AgentConsoleDialog dialog = console();

        assertThat(dialog.getStatusLabel().getText()).isEqualTo("Running");
        assertThat(dialog.getUsageLabel().getText()).contains("Rounds: 1").contains("Tokens in total: 120");
    }

    @Test
    void anIdleRunShowsItsQuestionAndTakesAnAnswer() throws Exception {
        AgentRun run = startedRun();
        say(run, LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("q1", "ask_user",
                        "{\"question\": \"Which chapter is first?\", \"options\": [\"One\", \"Two\"]}"))),
                LLMChatMessage.ofToolResult("q1", RunToolsImpl.QUESTION_ACK));
        agentRunService.transition(run, AgentRunStatus.IDLE, "Which chapter is first?");

        AgentConsoleDialog dialog = console();

        List<Component> bubbles = dialog.getTranscriptBox().getChildren().toList();
        assertThat(bubbles).as("the question, its acknowledgement says nothing more").hasSize(1);
        assertThat(textOf(bubbles.getFirst())).contains("Question of the agent", "Which chapter is first?", "- One", "- Two");
        assertThat(dialog.getSendButton().isEnabled()).isTrue();
        assertThat(dialog.getMessageField().isEnabled()).isTrue();

        dialog.getMessageField().setValue("The second one");
        dialog.getSendButton().click();

        assertThat(sent).containsExactly("The second one");
        assertThat(dialog.getMessageField().getValue()).isEmpty();
        assertThat(dialog.getStatusLabel().getText()).isEqualTo("Running");
        assertThat(dialog.getSendButton().isEnabled()).as("the agent is busy again").isFalse();
        assertThat(changed).isTrue();
    }

    @Test
    void anEmptyMessageIsNotSent() throws Exception {
        AgentRun run = startedRun();
        agentRunService.transition(run, AgentRunStatus.IDLE, "done for now");
        AgentConsoleDialog dialog = console();

        dialog.getMessageField().setValue("   ");
        dialog.getSendButton().click();

        assertThat(sent).isEmpty();
    }

    @Test
    void finishEndsARunThatWaitsForTheUser() throws Exception {
        AgentRun run = startedRun();
        AgentConsoleDialog dialog = console();
        assertThat(dialog.getFinishButton().isEnabled()).as("a run that works").isFalse();

        agentRunService.transition(agentRunService.find(run.getId()), AgentRunStatus.IDLE, "Anything else?");
        dialog.refreshRun();
        assertThat(dialog.getFinishButton().isEnabled()).isTrue();

        dialog.getFinishButton().click();

        assertThat(agentRunService.find(run.getId()).getStatus()).isEqualTo(AgentRunStatus.DONE);
        assertThat(dialog.getFinishButton().isEnabled()).isFalse();
        assertThat(dialog.getSendButton().isEnabled()).isFalse();
    }

    @Test
    void theResultOfAFinishedRunCanBeReviewed() throws Exception {
        AgentRun run = startedRun();
        AgentConsoleDialog dialog = console();
        assertThat(dialog.getReviewButton().isVisible()).as("a run that works").isFalse();

        agentRunService.transition(agentRunService.find(run.getId()), AgentRunStatus.DONE, "report");
        dialog.refreshRun();

        assertThat(dialog.getReviewButton().isVisible()).isTrue();
    }

    @Test
    void aFinishedRunShowsItsReport() throws Exception {
        AgentRun run = startedRun();
        say(run, LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("f1", "finish", "{\"summary\": \"No contradictions found.\"}"))),
                LLMChatMessage.ofToolResult("f1", RunToolsImpl.REPORT_ACK));
        agentRunService.transition(run, AgentRunStatus.DONE, "No contradictions found.");

        AgentConsoleDialog dialog = console();

        List<Component> bubbles = dialog.getTranscriptBox().getChildren().toList();
        assertThat(bubbles).hasSize(1);
        assertThat(textOf(bubbles.getFirst())).contains("Report of the agent", "No contradictions found.");
        assertThat(dialog.getSendButton().isEnabled()).isFalse();
        assertThat(dialog.getStopButton().isEnabled()).as("a finished run can be discarded").isTrue();
    }

    @Test
    void pauseNowIsOfferedToARunThatWorksAndAsksTheRunner() throws Exception {
        AgentRun run = startedRun();
        AgentConsoleDialog dialog = console();
        assertThat(dialog.getPauseNowButton().isEnabled()).isTrue();

        dialog.getPauseNowButton().click();

        assertThat(pausedNow).extracting(AgentRun::getId).containsExactly(run.getId());
        assertThat(agentRunService.find(run.getId()).getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        assertThat(dialog.getPauseNowButton().isEnabled()).isFalse();
    }

    @Test
    void pauseNowIsNotOfferedToARunThatPausesAtOnceAnyway() throws Exception {
        AgentRun run = startedRun();
        agentRunService.pause(run);

        assertThat(console().getPauseNowButton().isEnabled()).isFalse();
    }

    @Test
    void pauseAndResumeFollowTheRun() throws Exception {
        AgentRun run = startedRun();
        AgentConsoleDialog dialog = console();
        assertThat(dialog.getPauseButton().isEnabled()).isTrue();
        assertThat(dialog.getResumeButton().isEnabled()).isFalse();

        dialog.getPauseButton().click();

        assertThat(agentRunService.find(run.getId()).getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        assertThat(dialog.getStatusLabel().getText()).isEqualTo("Paused");
        assertThat(dialog.getPauseButton().isEnabled()).isFalse();
        assertThat(dialog.getResumeButton().isEnabled()).isTrue();
        assertThat(changed).isTrue();

        dialog.getResumeButton().click();

        assertThat(agentRunService.find(run.getId()).getStatus()).isEqualTo(AgentRunStatus.RUNNING);
        assertThat(takenUp).extracting(AgentRun::getId).containsExactly(run.getId());
        assertThat(dialog.getStatusLabel().getText()).isEqualTo("Running");
    }

    @Test
    void stopAsksHowTheRunEnds() throws Exception {
        AgentRun run = startedRun();
        agentRunService.pause(run);
        AgentConsoleDialog dialog = console();

        dialog.getStopButton().click();
        assertThat(dialog.getLastDialog()).isInstanceOf(AgentStopDialog.class);
        ((AgentStopDialog) dialog.getLastDialog()).getDiscardButton().click();

        assertThat(agentRunService.find(run.getId()).getStatus()).isEqualTo(AgentRunStatus.STOPPED_DISCARDED);
        // a discarded run is gone from the console
        assertThat(dialog.getStatusLabel().getText()).startsWith("The agent has not run yet");
        assertThat(dialog.getStopButton().isEnabled()).isFalse();
    }

    @Test
    void aNewRunShowsItsOwnTranscript() throws Exception {
        AgentRun first = startedRun();
        say(first, LLMChatMessage.of(LLMRole.USER, "old run"));
        AgentConsoleDialog dialog = console();
        assertThat(textOf(dialog.getTranscriptBox())).contains("old run");

        agentRunService.transition(first, AgentRunStatus.FAILED, "broke");
        AgentRun second = startedRun();
        say(second, LLMChatMessage.of(LLMRole.USER, "new run"));
        dialog.refreshRun();

        assertThat(textOf(dialog.getTranscriptBox())).contains("new run").doesNotContain("old run");
    }

    @Test
    void aDiscardedRunIsGoneFromTheConsole() throws Exception {
        AgentRun run = startedRun();
        say(run, LLMChatMessage.of(LLMRole.USER, "thrown away"));
        agentRunService.stop(agentRunService.pause(run), false);

        AgentConsoleDialog dialog = console();

        assertThat(dialog.getTranscriptBox().getChildren().count()).isZero();
        assertThat(dialog.getStatusLabel().getText()).startsWith("The agent has not run yet");
        assertThat(dialog.getStopButton().isEnabled()).isFalse();
    }

    @Test
    void theConsoleLetsGoOfARunThatIsDiscardedMeanwhile() throws Exception {
        AgentRun run = startedRun();
        say(run, LLMChatMessage.of(LLMRole.USER, "seen"));
        AgentConsoleDialog dialog = console();
        assertThat(textOf(dialog.getTranscriptBox())).contains("seen");

        agentRunService.stop(agentRunService.pause(run), false);
        dialog.refreshRun();

        assertThat(dialog.getTranscriptBox().getChildren().count()).isZero();
        assertThat(dialog.getPauseButton().isEnabled()).isFalse();
    }

    @Test
    void theLatestRunIsShownAfterItEnded() throws Exception {
        AgentRun run = startedRun();
        say(run, LLMChatMessage.of(LLMRole.ASSISTANT, "my last words"));
        agentRunService.transition(run, AgentRunStatus.FAILED, "the model broke");

        AgentConsoleDialog dialog = console();

        assertThat(textOf(dialog.getTranscriptBox())).contains("my last words");
        assertThat(dialog.getStatusLabel().getText()).startsWith("Failed").contains("the model broke");
        assertThat(button(dialog, "Close")).isNotNull();
    }

    private CompletableFuture<Boolean> waitingCommand(AgentRun run) throws Exception {
        CompletableFuture<Boolean> decision = approvals.request(run, "search-replace-parts", Map.of("search", "dragon", "replace", "wyrm"));
        agentRunService.transition(run, AgentRunStatus.WAITING_APPROVAL, "search-replace-parts");
        return decision;
    }

    @Test
    void aCommandThatWaitsForApprovalPopsUpAndTheUserApproves() throws Exception {
        CompletableFuture<Boolean> decision = waitingCommand(startedRun());

        AgentConsoleDialog dialog = console();

        AgentApprovalDialog popup = dialog.getApprovalDialog();
        assertThat(popup).isNotNull();
        assertThat(popup.isOpened()).isTrue();
        assertThat(textOf(popup)).contains("search-replace-parts");
        assertThat(ComponentTree.all(popup, com.vaadin.flow.component.html.Div.class).map(com.vaadin.flow.component.html.Div::getText)
                .reduce("", (a, b) -> a + b)).contains("search: dragon").contains("replace: wyrm");
        assertThat(decision).isNotDone();

        button(popup, "Approve").click();

        assertThat(decision.get(5, TimeUnit.SECONDS)).isTrue();
        assertThat(dialog.getApprovalDialog()).isNull();
        assertThat(popup.isOpened()).isFalse();
    }

    @Test
    void theUserCanDeny() throws Exception {
        CompletableFuture<Boolean> decision = waitingCommand(startedRun());
        AgentConsoleDialog dialog = console();

        button(dialog.getApprovalDialog(), "Deny").click();

        assertThat(decision.get(5, TimeUnit.SECONDS)).isFalse();
        assertThat(dialog.getApprovalDialog()).isNull();
    }

    @Test
    void thePopupGoesWhenTheRunIsPausedMeanwhile() throws Exception {
        AgentRun run = startedRun();
        CompletableFuture<Boolean> decision = waitingCommand(run);
        AgentConsoleDialog dialog = console();
        assertThat(dialog.getApprovalDialog()).isNotNull();

        agentRunService.pause(agentRunService.find(run.getId()));
        dialog.refreshRun();

        assertThat(decision.get(5, TimeUnit.SECONDS)).as("refused by the pause").isFalse();
        assertThat(dialog.getApprovalDialog()).isNull();
    }

    @Test
    void noPopupWhenNothingWaits() throws Exception {
        startedRun();

        assertThat(console().getApprovalDialog()).isNull();
    }

    @Test
    void theConsoleReadsTopToBottom() throws Exception {
        AgentRun run = startedRun();
        say(run, LLMChatMessage.of(LLMRole.USER, "first"), LLMChatMessage.of(LLMRole.ASSISTANT, "second"));
        agentRunService.transition(run, AgentRunStatus.IDLE, "Which one?");

        AgentConsoleDialog dialog = console();

        // the state, the transcript that scrolls, the command box, the images waiting to be sent, the box that answers
        List<Component> tab = dialog.getTranscriptBox().getParent().orElseThrow().getChildren().toList();
        assertThat(tab.indexOf(dialog.getTranscriptBox())).isEqualTo(1);
        assertThat(tab.indexOf(dialog.getCommandBar().getParent().orElseThrow())).isEqualTo(2);
        assertThat(tab.indexOf(dialog.getMessageField().getParent().orElseThrow())).isEqualTo(4);
        List<Component> bubbles = dialog.getTranscriptBox().getChildren().toList();
        assertThat(textOf(bubbles.getFirst())).contains("first");
    }

    @Test
    void theReportAndTheQuestionStayInTheTranscriptWhereTheyWereWritten() throws Exception {
        AgentRun run = startedRun();
        say(run, LLMChatMessage.of(LLMRole.USER, "first"),
                LLMChatMessage.ofToolCalls("looking", List.of(LLMToolCall.of("q1", "ask_user", "{\"question\": \"Which one?\"}"))),
                LLMChatMessage.ofToolResult("q1", RunToolsImpl.QUESTION_ACK));
        agentRunService.transition(run, AgentRunStatus.IDLE, "Which one?");
        AgentConsoleDialog dialog = console();
        assertThat(dialog.getTranscriptBox().getChildren().count()).isEqualTo(3);

        AgentRun working = agentRunService.transition(agentRunService.find(run.getId()), AgentRunStatus.RUNNING, null);
        say(working, LLMChatMessage.of(LLMRole.USER, "the second"),
                LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("f1", "finish", "{\"summary\": \"the long report\"}"))),
                LLMChatMessage.ofToolResult("f1", RunToolsImpl.REPORT_ACK_INTERACTIVE), LLMChatMessage.of(LLMRole.USER, "and then"));
        agentRunService.transition(agentRunService.find(run.getId()), AgentRunStatus.IDLE, "the long report");
        dialog.refreshRun();

        List<Component> bubbles = dialog.getTranscriptBox().getChildren().toList();
        assertThat(bubbles).hasSize(6);
        assertThat(textOf(bubbles.get(2))).as("the question is not gone when the run went on").contains("Question of the agent", "Which one?");
        assertThat(textOf(bubbles.get(3))).contains("the second");
        assertThat(textOf(bubbles.get(4))).contains("Report of the agent", "the long report");
        assertThat(textOf(bubbles.get(5))).contains("and then");
        // the input box is not in the part that scrolls
        assertThat(dialog.getMessageField().getParent().orElseThrow().getParent().orElseThrow())
                .isNotSameAs(dialog.getTranscriptBox());
    }

    @Test
    void aReportThatFailedIsAnOrdinaryCallWithItsResult() throws Exception {
        AgentRun run = startedRun();
        say(run, LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("f1", "finish", "{}"))),
                LLMChatMessage.ofToolResult("f1", "Error: write your report"));

        AgentConsoleDialog dialog = console();

        Details call = ComponentTree.all(dialog.getTranscriptBox(), Details.class).findFirst().orElseThrow();
        assertThat(call.getSummaryText()).startsWith("finish");
        assertThat(textOf(call)).contains("write your report");
    }

    @Test
    void whatIsSaidIsMarkdownAndTheArgumentsOfACallStayPlain() throws Exception {
        AgentRun run = startedRun();
        say(run, LLMChatMessage.of(LLMRole.USER, "Is **this** bold?"),
                LLMChatMessage.ofToolCalls("# Plan\n- one", List.of(LLMToolCall.of("c1", "list-parts", "{\"a\": \"**x**\"}"))),
                LLMChatMessage.ofToolResult("c1", "**result**"));

        AgentConsoleDialog dialog = console();

        List<String> markdown = ComponentTree.all(dialog.getTranscriptBox(), Markdown.class).map(Markdown::getContent).toList();
        assertThat(markdown).containsExactly("Is **this** bold?", "# Plan\n- one");
        Details call = ComponentTree.all(dialog.getTranscriptBox(), Details.class).findFirst().orElseThrow();
        assertThat(ComponentTree.all(call, Markdown.class)).isEmpty();
        assertThat(textOf(call)).contains("**x**", "**result**");
    }

    private static List<AgentRun> runsOf(AgentConsoleDialog dialog) {
        return dialog.getHistory().getGrid().getDataProvider().fetch(new com.vaadin.flow.data.provider.Query<>()).toList();
    }

    @Test
    void theHistoryListsAllTheRunsTheLatestFirstDiscardedOnesToo() throws Exception {
        AgentRun first = startedRun();
        agentRunService.transition(first, AgentRunStatus.FAILED, "broke");
        AgentRun second = startedRun();
        agentRunService.stop(agentRunService.pause(second), false);
        AgentRun third = startedRun();

        AgentConsoleDialog dialog = console();

        assertThat(runsOf(dialog)).extracting(AgentRun::getUuid).containsExactly(third.getUuid(), second.getUuid(), first.getUuid());
        // the console itself reads the latest run that was not thrown away
        assertThat(dialog.getStatusLabel().getText()).isEqualTo("Running");
        assertThat(dialog.getTabs().getTabCount()).isEqualTo(6);
    }

    @Test
    void viewingARunFromTheHistoryShowsItsStepsEvenWhenItWasDiscarded() throws Exception {
        AgentRun old = startedRun();
        say(old, LLMChatMessage.of(LLMRole.USER, "the old task"),
                LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("c1", "list-parts", "{}"))),
                LLMChatMessage.ofToolResult("c1", "old result"));
        agentRunService.stop(agentRunService.pause(old), false);
        AgentRun current = startedRun();
        say(current, LLMChatMessage.of(LLMRole.USER, "the new task"));
        AgentConsoleDialog dialog = console();
        assertThat(textOf(dialog.getTranscriptBox())).contains("the new task").doesNotContain("the old task");
        dialog.getTabs().setSelectedIndex(1);

        dialog.getHistory().view(old);

        assertThat(dialog.getTabs().getSelectedIndex()).as("back to the run").isEqualTo(0);
        assertThat(textOf(dialog.getTranscriptBox())).contains("the old task", "old result").doesNotContain("the new task");
        assertThat(dialog.getStatusLabel().getText()).startsWith("Stopped");
        // it is still the one picked while the console looks again
        dialog.refreshRun();
        assertThat(textOf(dialog.getTranscriptBox())).contains("the old task");
    }

    @Test
    void deletingARunFromTheHistoryRemovesItAndTheConsoleLetsGoOfIt() throws Exception {
        AgentRun old = startedRun();
        say(old, LLMChatMessage.of(LLMRole.USER, "the old task"));
        agentRunService.transition(old, AgentRunStatus.FAILED, "broke");
        AgentConsoleDialog dialog = console();
        dialog.getHistory().view(old);
        changed = false;

        dialog.getHistory().delete(old);

        assertThat(runsOf(dialog)).isEmpty();
        assertThat(agentRunService.find(old.getId()).isDeleted()).isTrue();
        assertThat(dialog.getStatusLabel().getText()).startsWith("The agent has not run yet");
        assertThat(textOf(dialog.getTranscriptBox())).doesNotContain("the old task");
        assertThat(changed).as("the table of agents hears of it").isTrue();
    }

    @Test
    void aRunThatIsGoingOnCanNotBeDeletedFromTheHistory() throws Exception {
        AgentRun working = startedRun();
        AgentConsoleDialog dialog = console();

        Button delete = ComponentTree.all(dialog.getHistory().actions(working), Button.class)
                .filter(b -> "The run is going on, pause or stop it first.".equals(b.getTooltip().getText())).findFirst().orElseThrow();

        assertThat(delete.isEnabled()).isFalse();
        assertThat(agentRunService.find(working.getId()).isDeleted()).isFalse();
    }

    @Test
    void theDownloadIsTheTranscriptAsMarkdown() throws Exception {
        AgentRun run = startedRun();
        say(run, LLMChatMessage.of(LLMRole.USER, "Check the dragon"), LLMChatMessage.of(LLMRole.ASSISTANT, "All good"));
        AgentConsoleDialog dialog = console();

        String md = dialog.getHistory().export(run);

        assertThat(md).contains("# " + agent.getName()).containsPattern("## You · \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\n\nCheck the dragon")
                .containsPattern("## Agent · \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\n\nAll good");
        assertThat(AgentRunHistory.fileName(run)).startsWith("agent-run-").endsWith(".md");
    }

    @Test
    void theConsoleShowsTheCurrentContextAgainstItsCap() throws Exception {
        agent.setContextCap(20_000);
        agent = agentService.save(agent);
        AgentRun run = startedRun();
        agentMessageService.appendAnswer(run, LLMChatMessage.of(LLMRole.ASSISTANT, "an answer"), 14_210, null);

        AgentConsoleDialog dialog = console();

        assertThat(dialog.getUsageLabel().getText()).contains("Context size: 14210 / 20000");
    }

    @Test
    void theMessageBoxSendsWithCtrlEnter() throws Exception {
        AgentRun run = startedRun();
        agentRunService.transition(run, AgentRunStatus.IDLE, "Which one?");

        AgentConsoleDialog dialog = console();

        // the shortcut clicks the button, so what the button does is tested above; the text has to be on the server by then
        assertThat(dialog.getMessageField().getValueChangeMode()).isEqualTo(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
        assertThat(dialog.getSendButton().getTooltip().getText()).isEqualTo("Ctrl+Enter");
    }

    @Test
    void theReasoningOfAnAnswerIsAFoldedBlockBeforeIt() throws Exception {
        AgentRun run = startedRun();
        agentMessageService.appendAnswer(run, LLMChatMessage.of(LLMRole.ASSISTANT, "The answer"), 100, "my private thoughts");
        agentMessageService.appendAnswer(run, LLMChatMessage.of(LLMRole.ASSISTANT, "No thoughts here"), 200, null);

        AgentConsoleDialog dialog = console();

        List<Component> bubbles = dialog.getTranscriptBox().getChildren().toList();
        assertThat(bubbles).hasSize(3);
        Details reasoning = ComponentTree.all(bubbles.get(0), Details.class).findFirst().orElseThrow();
        assertThat(reasoning.getSummaryText()).isEqualTo("Reasoning");
        assertThat(reasoning.isOpened()).isFalse();
        assertThat(textOf(reasoning)).contains("my private thoughts");
        assertThat(textOf(bubbles.get(1))).contains("The answer");
        assertThat(ComponentTree.all(bubbles.get(2), Details.class)).isEmpty();
    }

    // --- images with the message ---

    private void agentSeesImages() throws Exception {
        agent.getMainProfile().setImages(true);
        agent = agentService.save(agent);
    }

    @Test
    void imagesCanBeAttachedOnlyWhenTheModelSeesThem() throws Exception {
        assertThat(console().getImageUpload().isVisible()).isFalse();

        agentSeesImages();

        assertThat(console().getImageUpload().isVisible()).isTrue();
    }

    @Test
    void anAttachedImageIsStoredInTheWorkspaceAndSentWithTheMessage() throws Exception {
        agentSeesImages();
        AgentRun run = startedRun();
        agentRunService.transition(run, AgentRunStatus.IDLE, "Which one?");
        AgentConsoleDialog dialog = console();
        byte[] picture = TestImages.image(30, 20, "png");

        dialog.attachImage("map.png", picture);
        dialog.attachImage("notes.txt", "not an image at all".getBytes());
        assertThat(dialog.getPendingImages()).isEqualTo(1);
        dialog.getMessageField().setValue("This one.");
        dialog.getSendButton().click();

        assertThat(sent).containsExactly("This one.");
        assertThat(sentImages).containsExactly(List.of("/uploads/map.png"));
        assertThat(fileSystemService.openWorkspace(agent).read(VirtualPath.parse("/uploads/map.png"))).isEqualTo(picture);
        assertThat(dialog.getPendingImages()).isZero();
    }

    @Test
    void anImageAloneIsAMessageAndATakenNameGetsANumber() throws Exception {
        agentSeesImages();
        AgentRun run = startedRun();
        agentRunService.transition(run, AgentRunStatus.IDLE, null);
        AgentConsoleDialog dialog = console();
        byte[] picture = TestImages.image(30, 20, "png");

        dialog.attachImage("map.png", picture);
        dialog.getSendButton().click();
        agentRunService.transition(agentRunService.find(run.getId()), AgentRunStatus.IDLE, null);
        dialog.refreshRun();
        dialog.attachImage("map.png", picture);
        dialog.getSendButton().click();

        assertThat(sentImages).containsExactly(List.of("/uploads/map.png"), List.of("/uploads/map-2.png"));
    }

    // --- commands of the author ---

    @Test
    void theAuthorRunsACommandFromTheCommandBar() throws Exception {
        AgentRun run = agentRunService.start(agent, true);
        say(run, LLMChatMessage.of(LLMRole.USER, "Start."));
        agentRunService.transition(run, AgentRunStatus.IDLE, null);
        AgentConsoleDialog dialog = console();

        assertThat(dialog.getCommandBar().isEnabled()).isTrue();
        // the bar offers what the runner says, with /compact among them as a command of its own
        List<com.github.enerccio.marginalia.domain.service.command.CommandDefinition> offered = ReflectionTestUtils
                .invokeMethod(dialog.getCommandBar(), "definitions");
        assertThat(offered).extracting(d -> d.commandName()).contains("compact", "get-book-info").doesNotContain("hello-world");

        ReflectionTestUtils.invokeMethod(dialog.getCommandBar(), "run", "/compact the names");

        assertThat(commandLines).containsExactly("/compact the names");
    }

    @Test
    void aCommandIsTakenWhereTheRunWaitsIsPausedOrWorksAsAConversation() throws Exception {
        AgentRun run = agentRunService.start(agent, true);
        say(run, LLMChatMessage.of(LLMRole.USER, "Start."));
        AgentConsoleDialog dialog = console();

        assertThat(dialog.getCommandBar().isEnabled()).as("a conversation that works queues it").isTrue();

        agentRunService.transition(agentRunService.find(run.getId()), AgentRunStatus.PAUSING, null);
        agentRunService.transition(agentRunService.find(run.getId()), AgentRunStatus.PAUSED, null);
        dialog.refreshRun();
        assertThat(dialog.getCommandBar().isEnabled()).as("a paused run takes one too").isTrue();

        agentRunService.stop(agentRunService.find(run.getId()), true);
        dialog.refreshRun();
        assertThat(dialog.getCommandBar().isEnabled()).isFalse();
    }

    @Test
    void aCommandTheAgentDoesNotOfferIsNotRun() throws Exception {
        AgentRun run = agentRunService.start(agent, true);
        say(run, LLMChatMessage.of(LLMRole.USER, "Start."));
        AgentConsoleDialog dialog = console();

        ReflectionTestUtils.invokeMethod(dialog.getCommandBar(), "run", "/hello-world");

        assertThat(commandLines).isEmpty();
    }

    @Test
    void aQueuedCommandShowsAsACommandWithAButtonToRunItNow() throws Exception {
        AgentRun run = agentRunService.start(agent, true);
        say(run, LLMChatMessage.of(LLMRole.USER, "Start."));
        queued = List.of(new AgentRunner.QueuedMessage("c1", "/get-book-info", true));
        AgentConsoleDialog dialog = console();

        assertThat(dialog.getQueuedBubbles()).hasSize(1);
        com.vaadin.flow.component.html.Div bubble = dialog.getQueuedBubbles().getFirst();
        assertThat(textOf(bubble)).contains("/get-book-info").contains("Queued").contains("Command");
        Button now = ComponentTree.all(bubble, Button.class).findFirst().orElseThrow();
        assertThat(now.getTooltip().getText()).startsWith("Run now");
        now.click();

        assertThat(forced).containsExactly("c1");
    }

    @Test
    void aCommandTheAuthorRanShowsAsTheCommandAndItsResultNotAsTheNoteTheAgentReads() throws Exception {
        AgentRun run = agentRunService.start(agent, true);
        say(run, LLMChatMessage.of(LLMRole.USER, "Start."),
                LLMChatMessage.of(LLMRole.USER, Defaults.AGENT_AUTHOR_COMMAND_NOTE + "\n\nCommand: /get-book-info\n\nResult:\nThe Book"));
        AgentConsoleDialog dialog = console();

        List<Component> bubbles = dialog.getTranscriptBox().getChildren().toList();
        assertThat(bubbles).hasSize(2);
        assertThat(textOf(bubbles.get(1))).contains("Command: /get-book-info", "The Book").doesNotContain("You did not call it");
    }

    @Test
    void theSummaryOfACompactionIsFoldedInTheTranscript() throws Exception {
        AgentRun run = agentRunService.start(agent);
        say(run, LLMChatMessage.of(LLMRole.USER, "Start."), LLMChatMessage.of(LLMRole.ASSISTANT, "Working on it."));
        agentMessageService.compact(run, LLMChatMessage.of(LLMRole.USER, "Earlier: counted parts."), 12);
        AgentConsoleDialog dialog = console();

        Details summary = ComponentTree.all(dialog.getTranscriptBox(), Details.class)
                .filter(details -> "Summary of the earlier work".equals(details.getSummaryText())).findFirst().orElseThrow();
        assertThat(textOf(summary)).contains("Earlier: counted parts.");
        assertThat(textOf(dialog.getTranscriptBox())).as("what was compacted is still there for the user").contains("Working on it.");
    }

    @Test
    void theContextTabShowsThePartsOfTheContextWhenItIsPicked() throws Exception {
        AgentRun run = agentRunService.start(agent);
        say(run, LLMChatMessage.of(LLMRole.USER, "Start."));
        AgentConsoleDialog dialog = console();
        assertThat(dialog.getContextView().getContext()).as("not made before the tab is picked").isNull();

        dialog.getTabs().setSelectedIndex(AgentConsoleDialog.CONTEXT_TAB);

        assertThat(dialog.getContextView().getContext()).isNotNull();
        assertThat(dialog.getContextView().getGrid().getListDataView().getItems()).extracting(part -> part.title())
                .containsExactly("Prompt of the agent", "#1 user");
        assertThat(textOf(dialog.getContextView())).contains("Next request: about 5 of 100 tokens");
    }

    @Test
    void theBookCopyTabShowsTheCopyOfTheRunWhenItIsPicked() throws Exception {
        startedRun();
        AgentConsoleDialog dialog = console();
        assertThat(dialog.getCopyView().getBranchBox().getListDataView().getItemCount()).as("not read before the tab is picked").isZero();

        dialog.getTabs().setSelectedIndex(AgentConsoleDialog.COPY_TAB);

        assertThat(dialog.getCopyView().getNoCopyLabel().isVisible()).isFalse();
        assertThat(dialog.getCopyView().getLoreGrid()).isNotNull();
    }
}
