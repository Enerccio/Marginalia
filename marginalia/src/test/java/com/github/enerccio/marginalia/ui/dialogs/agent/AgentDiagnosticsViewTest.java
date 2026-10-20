package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.test.ComponentTree;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.markdown.Markdown;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The diagnostics of a run as the console shows them.
 */
class AgentDiagnosticsViewTest extends AgentTestBase {

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

    private AgentDiagnosticsView view(AgentRun run) {
        AgentDiagnosticsView view = new AgentDiagnosticsView(() -> run);
        view.create();
        return view;
    }

    @Test
    void theReportOfTheRunIsShownAndCanBeCopiedAndDownloaded() throws Exception {
        loginAgentUser();
        Agent agent = agentService.save(newAgent(manuscript()));
        AgentRun run = agentRunService.start(agent);
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, "Count the parts of the book please")));
        messageService.appendAnswer(run, LLMChatMessage.of(LLMRole.ASSISTANT, "The first answer"), 100, null);
        AgentDiagnosticsView view = view(run);
        assertThat(view.getCopyButton().isEnabled()).as("nothing before the first refresh").isFalse();

        view.refresh();

        assertThat(view.getDiagnostics().overview().rounds()).isEqualTo(1);
        assertThat(ComponentTree.all(view, Markdown.class).findFirst().orElseThrow().getContent()).contains("# Diagnostics of the agent");
        assertThat(ComponentTree.all(view, Button.class).filter(Button::isEnabled).count()).isEqualTo(4);
    }

    @Test
    void withoutARunThereIsNothingToShow() throws Exception {
        loginAgentUser();
        AgentDiagnosticsView view = view(null);

        view.refresh();

        assertThat(view.getDiagnostics()).isNull();
        assertThat(view.getCopyButton().isEnabled()).isFalse();
    }
}
