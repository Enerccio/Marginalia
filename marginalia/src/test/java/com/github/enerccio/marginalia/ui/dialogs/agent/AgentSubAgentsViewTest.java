package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.test.ComponentTree;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The conversations of the sub-agents of a run, as the user reads them.
 */
class AgentSubAgentsViewTest extends AgentTestBase {

    @Autowired
    private AgentMessageService messageService;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;
    private Agent agent;
    private AgentRun run;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        loginAgentUser();
        agent = agent();
        run = agentRunService.start(agent);
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private AgentSubAgentsView view() {
        AgentSubAgentsView view = new AgentSubAgentsView(() -> {
            try {
                return agentRunService.find(run.getId());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        view.create();
        return view;
    }

    private static String textOf(Component component) {
        return ComponentTree.all(component).filter(c -> c instanceof Span || c instanceof Div || c instanceof Markdown)
                .map(c -> c instanceof Markdown markdown ? markdown.getContent() : c.getElement().getText())
                .reduce("", (a, b) -> a + "\n" + b);
    }

    private void twoSubAgents() throws Exception {
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "go"),
                LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("c1", "spawn_agent", "{}"),
                        LLMToolCall.of("c2", "spawn_agent", "{}")))));
        messageService.startThread(run, "sub1", "check the names", "c1", LLMChatMessage.of(LLMRole.USER, "check the names"));
        messageService.startThread(run, "sub2", "check the dates", "c2", LLMChatMessage.of(LLMRole.USER, "check the dates"));
        messageService.appendAnswerToThread(run, "sub1", LLMChatMessage.ofToolCalls("Looking", List.of(
                LLMToolCall.of("t1", "list-parts", "{\"limit\": 3}"))), 120, "I should look first");
        messageService.appendToThread(run, "sub1", List.of(LLMChatMessage.ofToolResult("t1", "1: Once upon a time")));
        messageService.appendAnswerToThread(run, "sub1", LLMChatMessage.of(LLMRole.ASSISTANT, "The names are fine"), 150, null);
        messageService.endThread(run, "sub1", "DONE", "The names are fine");
    }

    @Test
    void aRunWithoutSubAgentsSaysSo() {
        AgentSubAgentsView view = view();

        view.refresh();

        assertThat(view.getEmptyLabel().isVisible()).isTrue();
        assertThat(view.getTranscriptBox().getParent().orElseThrow().isVisible()).isFalse();
    }

    @Test
    void theFirstSubAgentIsShownWithWhatItWasToldSaidCalledAndHowItEnded() throws Exception {
        twoSubAgents();
        AgentSubAgentsView view = view();

        view.refresh();

        assertThat(view.getEmptyLabel().isVisible()).isFalse();
        assertThat(view.getPicker().getListDataView().getItems()).extracting(thread -> thread.handle()).containsExactly("sub1", "sub2");
        assertThat(view.getPicker().getValue().handle()).isEqualTo("sub1");
        assertThat(view.getInfo().getText()).contains("check the names", "Rounds: 2");
        String text = textOf(view.getTranscriptBox());
        assertThat(text).contains("check the names", "Looking", "The names are fine", "1: Once upon a time", "I should look first");
        Details call = ComponentTree.all(view.getTranscriptBox(), Details.class).filter(d -> d.getSummaryText().startsWith("list-parts")).findFirst().orElseThrow();
        assertThat(textOf(call)).as("the result is in the folded call").contains("1: Once upon a time");
        // what it was told is from the agent, what it said is its own
        assertThat(text).contains("sub1");
        assertThat(view.getTranscriptBox().getChildren().toList().getLast()).as("the end of the conversation")
                .satisfies(last -> assertThat(textOf(last)).contains("Done", "The names are fine"));
    }

    @Test
    void anotherSubAgentIsShownWhenItIsPicked() throws Exception {
        twoSubAgents();
        AgentSubAgentsView view = view();
        view.refresh();

        view.getPicker().setValue(view.getPicker().getListDataView().getItems().toList().get(1));

        assertThat(textOf(view.getTranscriptBox())).contains("check the dates").doesNotContain("Looking");
        assertThat(view.getInfo().getText()).contains("check the dates");
    }

    @Test
    void aSubAgentThatDoesNotEndShowsItWorksWithTheRunAndOnlyTheNewRowsAreAdded() throws Exception {
        twoSubAgents();
        AgentSubAgentsView view = view();
        view.getPicker();
        view.refresh();
        view.getPicker().setValue(view.getPicker().getListDataView().getItems().toList().get(1));
        int before = view.getTranscriptBox().getComponentCount();
        Component first = view.getTranscriptBox().getComponentAt(0);

        messageService.appendAnswerToThread(run, "sub2", LLMChatMessage.of(LLMRole.ASSISTANT, "Dates checked so far"), 90, null);
        view.refresh();

        assertThat(view.getTranscriptBox().getComponentCount()).isEqualTo(before + 1);
        assertThat(view.getTranscriptBox().getComponentAt(0)).as("what was shown stays").isSameAs(first);
        assertThat(textOf(view.getTranscriptBox())).contains("Dates checked so far");
        assertThat(view.getInfo().getText()).contains("Running");
    }

    @Test
    void theSubAgentOfASpawnCallIsFoundByTheIdOfTheCall() throws Exception {
        twoSubAgents();
        AgentSubAgentsView view = view();

        assertThat(view.showCall("c2")).isTrue();
        assertThat(view.getPicker().getValue().handle()).isEqualTo("sub2");
        assertThat(textOf(view.getTranscriptBox())).contains("check the dates");

        assertThat(view.showCall("nobody")).isFalse();
    }

    @Test
    void aConversationTheAgentWentBackOverIsMarkedAsForgotten() throws Exception {
        twoSubAgents();
        // the agent goes back to after its first message: the calls that started the sub-agents are gone
        messageService.rewind(run, 1);
        AgentSubAgentsView view = view();

        view.refresh();

        assertThat(view.getPicker().getItemLabelGenerator().apply(view.getPicker().getValue())).contains("forgotten");
        assertThat(view.getTranscriptBox().getChildren()
                .allMatch(block -> block.getElement().getClassList().contains(CssStyles.AGENT_REWOUND))).isTrue();
    }
}
