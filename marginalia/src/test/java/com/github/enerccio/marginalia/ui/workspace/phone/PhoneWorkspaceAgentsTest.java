package com.github.enerccio.marginalia.ui.workspace.phone;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.test.ComponentTree;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the phone workspace does for agents beyond their tab: the cue in the top bar while the drawer is closed, and the swipe
 * between the parts.
 */
class PhoneWorkspaceAgentsTest extends AgentTestBase {

    private UI ui;
    private Workspace workspace;
    private AppLayout layout;

    @BeforeEach
    void open() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        loginAgentUser();
        workspace = new PhoneWorkspace();
        layout = (AppLayout) workspace.create();
        workspace.refresh();
    }

    @AfterEach
    void close() {
        UI.setCurrent(null);
        ui = null;
    }

    private Button cue() {
        return ComponentTree.all(layout, Button.class).filter(button -> button.hasClassName(CssStyles.AGENT_ATTENTION_CUE)).findFirst()
                .orElseThrow();
    }

    private String shownTab() {
        return workspace.storeState().get("tab").getAsString();
    }

    private void swipe(String direction) {
        ObjectNode data = JsonNodeFactory.instance.objectNode();
        data.put("event.detail", direction);
        layout.getElement().getNode().getFeature(ElementListenerMap.class).fireEvent(new DomEvent(layout.getElement(), "swipe", data));
    }

    private void waits() throws Exception {
        Agent agent = agent();
        agentRunService.transition(agentRunService.start(agent), AgentRunStatus.IDLE, "Which chapter?");
        workspace.refresh();
    }

    @Test
    void theTopBarShowsHowManyAgentsWaitWhileTheDrawerIsClosed() throws Exception {
        assertThat(cue().isVisible()).as("nobody waits").isFalse();

        waits();

        assertThat(cue().isVisible()).isTrue();
        assertThat(ComponentTree.all(cue(), Span.class).findFirst().orElseThrow().getText()).isEqualTo("1");
        assertThat(cue().getTooltip().getText()).isEqualTo("Agents waiting for you");

        // the drawer has the same count in the tab of the agents
        layout.setDrawerOpened(true);
        assertThat(cue().isVisible()).isFalse();
        layout.setDrawerOpened(false);
        assertThat(cue().isVisible()).isTrue();
    }

    @Test
    void theCueTakesTheUserToTheAgentsAndIsNotNeededThere() throws Exception {
        waits();
        assertThat(shownTab()).isEqualTo("books");

        cue().click();

        assertThat(shownTab()).isEqualTo("agents");
        assertThat(cue().isVisible()).as("the agents are in front of the user").isFalse();

        workspace.getInteraction().get("open-section:Books").action().run();
        assertThat(cue().isVisible()).isTrue();
    }

    @Test
    void aSwipeGoesToTheNextOrThePreviousPart() {
        assertThat(shownTab()).isEqualTo("books");

        swipe("left");
        assertThat(shownTab()).isEqualTo("lorebooks");
        swipe("left");
        assertThat(shownTab()).isEqualTo("settings");
        swipe("right");
        assertThat(shownTab()).isEqualTo("lorebooks");
        swipe("right");
        swipe("right");
        assertThat(shownTab()).as("nothing before the first part").isEqualTo("books");
        swipe("up");
        assertThat(shownTab()).as("only sideways").isEqualTo("books");
    }

    @Test
    void aSwipeDoesNothingWithTheDrawerOpenOrWhileNavigationIsLocked() {
        layout.setDrawerOpened(true);
        swipe("left");
        assertThat(shownTab()).isEqualTo("books");

        layout.setDrawerOpened(false);
        workspace.setNavigationLocked(true);
        swipe("left");
        assertThat(shownTab()).isEqualTo("books");

        workspace.setNavigationLocked(false);
        swipe("left");
        assertThat(shownTab()).isEqualTo("lorebooks");
    }
}
