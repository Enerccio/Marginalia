package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.test.ComponentTree;
import com.github.enerccio.marginalia.ui.widgets.ChartJs;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The context of a run as the console charts it.
 */
class AgentContextViewTest extends AgentTestBase {

    @Autowired
    private AgentMessageService messageService;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;

    @BeforeEach
    void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private AgentContextView view(AgentRun run) {
        AgentContextView view = new AgentContextView(() -> run);
        view.create();
        return view;
    }

    @Test
    void theChartsAreTheKindsThePartsAndTheSizeAtEachAnswer() throws Exception {
        loginAgentUser();
        Agent agent = agentService.save(newAgent(manuscript()));
        AgentRun run = agentRunService.start(agent);
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "Count the parts of the book please")));
        messageService.appendAnswer(run, LLMChatMessage.of(LLMRole.ASSISTANT, "The first answer"), 100, null);
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "And once more")));
        messageService.appendAnswer(run, LLMChatMessage.of(LLMRole.ASSISTANT, "The second answer"), 220, null);
        AgentContextView view = view(run);

        view.refresh();

        assertThat(view.getContext().history()).hasSize(2).allSatisfy(point -> assertThat(point.estimated()).isNotEmpty());
        assertThat(ComponentTree.all(view, ChartJs.class).toList()).hasSize(3);
    }

    @Test
    void aRunWithoutAnswersHasNoHistoryChart() throws Exception {
        loginAgentUser();
        Agent agent = agentService.save(newAgent(manuscript()));
        AgentRun run = agentRunService.start(agent);
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "Count the parts of the book please")));
        AgentContextView view = view(run);

        view.refresh();

        assertThat(ComponentTree.all(view, ChartJs.class).toList()).hasSize(2);
    }
}
