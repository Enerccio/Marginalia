package com.github.enerccio.marginalia.ui.workspace.parts;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.AgentAttentionService.Notice;
import com.github.enerccio.marginalia.domain.service.AgentRunner;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.test.ComponentTree;
import com.github.enerccio.marginalia.ui.dialogs.agent.AgentConsoleDialog;
import com.github.enerccio.marginalia.ui.dialogs.agent.AgentMergeDialog;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The tab with the agents: what the table shows and what the buttons of a row do to the run.
 */
class AgentPartTest extends AgentTestBase {

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;
    private User user;
    private AgentPart part;
    // the runs the buttons handed to the runner; a real runner would work on them and change their status meanwhile
    private final List<AgentRun> takenUp = new ArrayList<>();
    // the first messages the runner was given with a run, null for a run started without one
    private final List<String> firstMessages = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        user = loginAgentUser();
        part = new AgentPart(null);
        ReflectionTestUtils.setField(part, "agentRunner", new AgentRunner() {
            @Override
            public void run(AgentRun run) {
                takenUp.add(run);
            }

            @Override
            public void run(AgentRun run, String firstMessage) {
                takenUp.add(run);
                firstMessages.add(firstMessage);
            }

            @Override
            public AgentRun send(AgentRun run, String text, java.util.List<String> images) {
                throw new UnsupportedOperationException();
            }

            @Override
            public java.util.List<String> consoleCommands(com.github.enerccio.marginalia.domain.model.impl.Agent agent) {
                return java.util.List.of(COMPACT);
            }

            @Override
            public AgentRun command(AgentRun run, String line) {
                throw new UnsupportedOperationException();
            }

            @Override
            public java.util.List<QueuedMessage> queued(AgentRun run) {
                return java.util.List.of();
            }

            @Override
            public void force(AgentRun run, String id) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean cancelQueued(AgentRun run, String id) {
                throw new UnsupportedOperationException();
            }

            @Override
            public AgentRun pauseNow(AgentRun run) throws Exception {
                return agentRunService.pause(run);
            }

            @Override
            public com.github.enerccio.marginalia.domain.service.agent.AgentContext context(AgentRun run) {
                throw new UnsupportedOperationException();
            }

            @Override
            public com.github.enerccio.marginalia.domain.service.agent.AgentDiagnostics.Report diagnostics(AgentRun run) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isWorking(AgentRun run) {
                return false;
            }
        });
        part.create();
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private List<Agent> rows() {
        return part.getGrid().getDataProvider().fetch(new Query<>()).toList();
    }

    @SuppressWarnings("unchecked")
    private Component actionsOf(Agent agent) {
        Grid.Column<Agent> column = part.getGrid().getColumns().getLast();
        return ((ComponentRenderer<Component, Agent>) column.getRenderer()).createComponent(agent);
    }

    /**
     * The buttons of a row by the tooltip they carry (they have icons, no text).
     */
    private static Button button(Component actions, String tooltip) {
        return ComponentTree.all(actions, Button.class)
                .filter(b -> b.getTooltip() != null && tooltip.equals(b.getTooltip().getText()))
                .findFirst().orElseThrow(() -> new AssertionError("No button " + tooltip));
    }

    private static boolean has(Component actions, String tooltip) {
        // an agent without a run starts from a menu bar, not from a button
        if (tooltip.equals("Start") && ComponentTree.all(actions, MenuBar.class).anyMatch(Component::isVisible)) {
            return true;
        }
        return ComponentTree.all(actions, Button.class)
                .anyMatch(b -> b.isVisible() && b.getTooltip() != null && tooltip.equals(b.getTooltip().getText()));
    }

    /**
     * Picks an item of the menu the Start button of the last row built opens.
     */
    private void choose(String text) {
        MenuItem item = part.getLastStartItem().getSubMenu().getItems().stream().filter(i -> text.equals(i.getText())).findFirst()
                .orElseThrow(() -> new AssertionError("No menu item " + text));
        ComponentUtil.fireEvent(item, new ClickEvent<>(item));
    }

    private String statusOf(Agent agent) {
        return part.statusText(agent);
    }

    @Test
    void theTableListsTheAgentsOfTheUserWithIdNameAndStatus() throws Exception {
        Agent mine = agent();
        loginAgentUser();
        Agent theirs = agent();
        loginAs(user);
        part.refresh();

        assertThat(rows()).extracting(Agent::getId).containsExactly(mine.getId()).doesNotContain(theirs.getId());
        List<String> headers = part.getGrid().getColumns().stream().map(Grid.Column::getHeaderText).toList();
        assertThat(headers).startsWith("Agent ID", "Name", "Book", "Status", "Last run");
        assertThat(statusOf(mine)).isEqualTo("Not running");
    }

    @Test
    void theFieldsAboveTheTableKeepTheAgentsWithAllTheWordsOfTheNameAndTheBook() throws Exception {
        Agent alpha = newAgent(manuscript());
        alpha.setName("Night Editor");
        alpha = agentService.save(alpha);
        Agent beta = newAgent(manuscript());
        beta.setName("Day Editor");
        beta = agentService.save(beta);
        part.refresh();
        assertThat(rows()).hasSize(2);

        part.getNameFilter().setValue("editor night");
        assertThat(rows()).extracting(Agent::getId).containsExactly(alpha.getId());

        part.getNameFilter().setValue("");
        part.getBookFilter().setValue(beta.getManuscript().getName().toUpperCase());
        assertThat(rows()).extracting(Agent::getId).containsExactly(beta.getId());

        part.getBookFilter().setValue("no such book");
        assertThat(rows()).isEmpty();

        part.getBookFilter().setValue("  ");
        assertThat(rows()).hasSize(2);
    }

    @Test
    void theColumnsOfNameStatusAndLastRunSortAndTheLastRunIsShown() throws Exception {
        Agent never = newAgent(manuscript());
        never.setName("A never");
        never = agentService.save(never);
        Agent ran = newAgent(manuscript());
        ran.setName("B ran");
        ran = agentService.save(ran);
        AgentRun run = agentRunService.start(ran);
        agentRunService.stop(run, false);
        part.refresh();

        assertThat(part.lastRunText(never)).isEqualTo("never");
        assertThat(part.lastRunText(ran)).isNotEqualTo("never").isNotBlank();
        List<String> headers = part.getGrid().getColumns().stream().map(Grid.Column::getHeaderText).toList();
        assertThat(headers).startsWith("Agent ID", "Name", "Book", "Status", "Last run");
        assertThat(part.getGrid().getColumns().stream().filter(c -> !c.getHeaderText().isEmpty()).filter(c -> !c.getHeaderText().equals("Agent ID")))
                .allMatch(Grid.Column::isSortable);

        Grid.Column<Agent> lastRun = part.getGrid().getColumns().get(4);
        List<Agent> asc = rows().stream().sorted(lastRun.getComparator(com.vaadin.flow.data.provider.SortDirection.ASCENDING)).toList();
        assertThat(asc).extracting(Agent::getId).containsExactly(never.getId(), ran.getId());
        List<Agent> desc = rows().stream().sorted(lastRun.getComparator(com.vaadin.flow.data.provider.SortDirection.DESCENDING)).toList();
        assertThat(desc).extracting(Agent::getId).containsExactly(ran.getId(), never.getId());
    }

    @Test
    void thePhoneSortsByNameStatusOrTheNewestRun() throws Exception {
        Agent never = newAgent(manuscript());
        never.setName("A never");
        never = agentService.save(never);
        Agent ran = newAgent(manuscript());
        ran.setName("B ran");
        ran = agentService.save(ran);
        agentRunService.start(ran);
        AgentPart phonePart = new AgentPart(null, true);
        phonePart.create();

        List<Agent> byName = phonePart.getGrid().getDataProvider().fetch(new Query<>()).toList();
        assertThat(byName).extracting(Agent::getId).containsExactly(never.getId(), ran.getId());

        phonePart.getSortBox().setValue(AgentPart.Sort.LAST_RUN);
        List<Agent> newest = phonePart.getGrid().getDataProvider().fetch(new Query<>()).toList();
        assertThat(newest).extracting(Agent::getId).containsExactly(ran.getId(), never.getId());

        phonePart.getSortBox().setValue(AgentPart.Sort.STATUS);
        List<Agent> byStatus = phonePart.getGrid().getDataProvider().fetch(new Query<>()).toList();
        // "Not running" sorts before "Running"
        assertThat(byStatus).extracting(Agent::getId).containsExactly(never.getId(), ran.getId());
    }

    @Test
    void aDisabledAgentSaysSo() throws Exception {
        Agent agent = newAgent(manuscript());
        agent.setEnabled(false);
        agent = agentService.save(agent);
        part.refresh();

        assertThat(statusOf(agent)).isEqualTo("Disabled");
    }

    @Test
    @SuppressWarnings("unchecked")
    void onThePhoneEachAgentIsACardWithItsNameStateAndButtons() throws Exception {
        Agent agent = agent();
        AgentPart phonePart = new AgentPart(null, true);
        phonePart.create();

        assertThat(phonePart.getGrid().getColumns()).as("one column, not a table").hasSize(1);
        Component card = ((ComponentRenderer<Component, Agent>) phonePart.getGrid().getColumns().getFirst().getRenderer()).createComponent(agent);

        assertThat(ComponentTree.all(card, com.vaadin.flow.component.html.Span.class).map(com.vaadin.flow.component.html.Span::getText))
                .contains(agent.getName(), "Not running");
        assertThat(has(card, "Edit agent")).isTrue();
        assertThat(has(card, "Start")).isTrue();
    }

    @Test
    void startPauseResumeAndStopFollowTheRun() throws Exception {
        Agent agent = agent();
        part.refresh();
        Component actions = actionsOf(agent);
        assertThat(has(actions, "Start")).isTrue();
        assertThat(has(actions, "Pause")).isFalse();
        assertThat(has(actions, "Stop")).isFalse();

        choose("Start immediately");

        AgentRun run = agentRunService.findUnfinished(agent);
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.RUNNING);
        assertThat(takenUp).extracting(AgentRun::getUuid).containsExactly(run.getUuid());
        actions = actionsOf(agent);
        assertThat(statusOf(agent)).isEqualTo("Running");
        assertThat(has(actions, "Start")).isFalse();
        assertThat(has(actions, "Pause")).isTrue();
        assertThat(has(actions, "Pause now")).isTrue();
        assertThat(has(actions, "Stop")).isTrue();

        button(actions, "Pause").click();

        assertThat(agentRunService.findUnfinished(agent).getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        actions = actionsOf(agent);
        assertThat(statusOf(agent)).isEqualTo("Paused");
        assertThat(has(actions, "Resume")).isTrue();
        assertThat(has(actions, "Pause")).isFalse();
        assertThat(has(actions, "Pause now")).isFalse();

        button(actions, "Resume").click();
        // the note is optional: the dialog resumes without one
        assertThat(agentRunService.findUnfinished(agent).getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        ComponentTree.button(part.getLastDialog(), "Resume").click();
        assertThat(agentRunService.findUnfinished(agent).getStatus()).isEqualTo(AgentRunStatus.RUNNING);
        assertThat(takenUp).hasSize(2).allSatisfy(taken -> assertThat(taken.getUuid()).isEqualTo(run.getUuid()));

        // Stop asks whether the result is kept
        button(actionsOf(agent), "Stop").click();
        Dialog question = part.getLastDialog();
        ComponentTree.button(question, "Stop and discard the result").click();

        assertThat(agentRunService.findUnfinished(agent)).isNull();
        assertThat(agentRunService.findForAgent(agent).getFirst().getStatus()).isEqualTo(AgentRunStatus.STOPPED_DISCARDED);
        assertThat(statusOf(agent)).isEqualTo("Not running");
        assertThat(has(actionsOf(agent), "Start")).isTrue();
    }

    @Test
    void aRefusedStartLeavesTheAgentAsItWas() throws Exception {
        Agent agent = newAgent(manuscript());
        agent.setEnabled(false);
        agent = agentService.save(agent);
        part.refresh();

        actionsOf(agent);
        choose("Start immediately");

        assertThat(agentRunService.findUnfinished(agent)).isNull();
        assertThat(statusOf(agent)).isEqualTo("Disabled");
    }

    @Test
    void theLimitOfAgentsDisablesNewAndClone() throws Exception {
        Agent agent = agent();
        part.refresh();
        assertThat(part.getAddAgentButton().isEnabled()).isTrue();
        assertThat(button(actionsOf(agent), "Clone agent").isEnabled()).isTrue();

        updatePermission(user, PermissionType.AGENTS, permission -> permission.setAgentsMaxAgents(1));
        part.refresh();

        assertThat(part.getAddAgentButton().isEnabled()).isFalse();
        assertThat(button(actionsOf(agent), "You have all the agents you are allowed to have.").isEnabled()).isFalse();
    }

    @Test
    void newOpensTheDialogOfANewAgent() throws Exception {
        manuscript();

        part.getAddAgentButton().click();

        Dialog dialog = part.getLastDialog();
        assertThat(dialog.getHeaderTitle()).isEqualTo("New agent");
    }

    @Test
    void deleteMovesTheAgentToTheTrash() throws Exception {
        Agent agent = agent();
        part.refresh();

        button(actionsOf(agent), "Delete").click();
        // asks first
        Dialog question = part.getLastDialog();
        assertThat(agentService.find(agent.getId()).isDeleted()).isFalse();
        ComponentTree.all(question, Button.class).filter(b -> "Yes".equals(b.getText())).findFirst().orElseThrow().click();

        assertThat(agentService.find(agent.getId()).isDeleted()).isTrue();
        assertThat(rows()).isEmpty();
    }

    @Test
    void theTabCountsTheAgentsThatNeedTheUser() throws Exception {
        Agent waiting = agent();
        Agent working = agent();
        part.refresh();
        assertThat(part.getAttentionBadge().isVisible()).isFalse();

        agentRunService.transition(agentRunService.start(waiting), AgentRunStatus.IDLE, "Which chapter?");
        agentRunService.start(working);
        part.refresh();

        assertThat(part.getAttentionBadge().isVisible()).isTrue();
        assertThat(part.getAttentionBadge().getText()).isEqualTo("1");
        assertThat(button(actionsOf(waiting), "Console").getThemeName()).contains("primary");
        assertThat(button(actionsOf(working), "Console").getThemeName()).doesNotContain("primary");

        // looking at it takes it away
        agentRunService.acknowledge(agentRunService.findUnfinished(waiting));
        part.refresh();

        assertThat(part.getAttentionBadge().isVisible()).isFalse();
    }

    @Test
    void aListenerIsToldTheCountOfTheAgentsThatNeedTheUser() throws Exception {
        Agent waiting = agent();
        part.refresh();
        List<Integer> counts = new ArrayList<>();

        part.addAttentionListener(counts::add);
        agentRunService.transition(agentRunService.start(waiting), AgentRunStatus.IDLE, "Which chapter?");
        part.refresh();
        agentRunService.acknowledge(agentRunService.findUnfinished(waiting));
        part.refresh();

        assertThat(counts).as("now, then at every look").containsExactly(0, 1, 0);
    }

    @Test
    void whatHappenedWhileTheUserWasAwayIsThereWhenTheTabOpens() throws Exception {
        Agent agent = agent();
        AgentRun run = agentRunService.start(agent);
        agentRunService.transition(run, AgentRunStatus.DONE, "All done");

        AgentPart later = new AgentPart(null);
        later.create();

        assertThat(later.getAttentionBadge().isVisible()).isTrue();
        assertThat(later.getAttentionBadge().getText()).isEqualTo("1");
    }

    @Test
    void aNoticeRefreshesTheTableAndSaysWhatTheAgentWants() throws Exception {
        Agent agent = agent();
        AgentRun run = agentRunService.transition(agentRunService.start(agent), AgentRunStatus.IDLE, null);
        String name = agent.getName();
        String uuid = run.getUuid();

        assertThat(part.noticeText(new Notice(user.getId(), uuid, agent.getUuid(), name, AgentRunStatus.IDLE, null, true)))
                .isEqualTo("The agent \"" + name + "\" waits for you.");
        assertThat(part.noticeText(new Notice(user.getId(), uuid, agent.getUuid(), name, AgentRunStatus.WAITING_APPROVAL, "search-replace-parts", true)))
                .contains(name).contains("approval").contains("search-replace-parts");
        assertThat(part.noticeText(new Notice(user.getId(), uuid, agent.getUuid(), name, AgentRunStatus.DONE, "r", true)))
                .isEqualTo("The agent \"" + name + "\" has finished.");
        assertThat(part.noticeText(new Notice(user.getId(), uuid, agent.getUuid(), name, AgentRunStatus.FAILED, "x", true)))
                .contains("failed");
        assertThat(part.noticeText(new Notice(user.getId(), uuid, agent.getUuid(), name, AgentRunStatus.PAUSED, "Token budget used up", true)))
                .contains("paused").contains("Token budget used up");

        part.onAttention(new Notice(user.getId(), uuid, agent.getUuid(), name, AgentRunStatus.IDLE, null, true));

        assertThat(part.getAttentionBadge().getText()).isEqualTo("1");
        assertThat(statusOf(agent)).isEqualTo("Waiting for you");
    }

    @Test
    void startOpensAMenuWithTwoWaysToStart() throws Exception {
        Agent agent = agent();
        part.refresh();
        actionsOf(agent);

        assertThat(part.getLastStartItem().getSubMenu().getItems()).extracting(MenuItem::getText)
                .containsExactly("Start immediately", "Start in console");
        assertThat(agentRunService.findUnfinished(agent)).as("opening the menu starts nothing").isNull();
    }

    @Test
    void startImmediatelyLetsTheAgentWorkOnItsTask() throws Exception {
        Agent agent = agent();
        part.refresh();
        actionsOf(agent);

        choose("Start immediately");

        assertThat(agentRunService.findUnfinished(agent).getStatus()).isEqualTo(AgentRunStatus.RUNNING);
        assertThat(takenUp).hasSize(1);
        assertThat(firstMessages).isEmpty();
        assertThat(part.getLastDialog()).isNull();
    }

    @Test
    void startInConsoleOpensTheConsoleAndTheAgentAsksWhatTheUserWants() throws Exception {
        Agent agent = agent();
        part.refresh();
        actionsOf(agent);

        choose("Start in console");

        AgentRun run = agentRunService.findUnfinished(agent);
        assertThat(run.getStatus()).isEqualTo(AgentRunStatus.RUNNING);
        assertThat(takenUp).extracting(AgentRun::getUuid).containsExactly(run.getUuid());
        assertThat(firstMessages).containsExactly(Defaults.AGENT_CONSOLE_START_MESSAGE);
        assertThat(Defaults.AGENT_CONSOLE_START_MESSAGE).contains("ask_user");
        assertThat(part.getLastDialog()).isInstanceOf(AgentConsoleDialog.class);
        assertThat(part.getLastDialog().isOpened()).isTrue();
        // a conversation: the agent's finish does not end it
        assertThat(run.isInteractive()).isTrue();
    }

    @Test
    void startImmediatelyIsAnAutonomousRun() throws Exception {
        Agent agent = agent();
        part.refresh();
        actionsOf(agent);

        choose("Start immediately");

        assertThat(agentRunService.findUnfinished(agent).isInteractive()).isFalse();
    }

    @Test
    void aRefusedStartInConsoleOpensNoConsole() throws Exception {
        Agent agent = newAgent(manuscript());
        agent.setEnabled(false);
        agent = agentService.save(agent);
        part.refresh();
        actionsOf(agent);

        choose("Start in console");

        assertThat(agentRunService.findUnfinished(agent)).isNull();
        assertThat(takenUp).isEmpty();
        assertThat(part.getLastDialog()).isNull();
    }

    @Test
    void aStopThatEndsLaterIsShownWhenTheScreenIsToldOfIt() throws Exception {
        Agent agent = agent();
        part.refresh();
        actionsOf(agent);
        choose("Start immediately");
        AgentRun run = agentRunService.findUnfinished(agent);
        assertThat(statusOf(agent)).isEqualTo("Running");

        // the worker finishes the stop after the buttons were pressed
        agentRunService.transition(agentRunService.transition(run, AgentRunStatus.STOPPING, null), AgentRunStatus.STOPPED_DISCARDED, null);
        assertThat(statusOf(agent)).as("not told yet").isEqualTo("Running");
        part.onAttention(new Notice(user.getId(), run.getUuid(), agent.getUuid(), agent.getName(), AgentRunStatus.STOPPED_DISCARDED, null, false));

        assertThat(statusOf(agent)).isEqualTo("Not running");
        assertThat(has(actionsOf(agent), "Start")).isTrue();
        assertThat(part.getLastStartItem().getSubMenu().getItems()).hasSize(2);
        assertThat(part.getAttentionBadge().isVisible()).isFalse();
    }

    @Test
    void aRunThatIsOverBringsTheStartMenuBack() throws Exception {
        Agent agent = agent();
        part.refresh();
        Component running = actionsOf(agent);
        choose("Start immediately");
        assertThat(ComponentTree.all(actionsOf(agent), MenuBar.class).count()).as("a run: no start menu").isZero();

        button(actionsOf(agent), "Stop").click();
        ComponentTree.button(part.getLastDialog(), "Stop and discard the result").click();

        assertThat(ComponentTree.all(actionsOf(agent), MenuBar.class).count()).isEqualTo(1);
    }

    @Test
    void theResultOfAFinishedRunIsReviewedFromTheRow() throws Exception {
        Agent agent = agent();
        part.refresh();
        actionsOf(agent);
        choose("Start immediately");
        assertThat(has(actionsOf(agent), "Review changes")).as("a run that works has no result yet").isFalse();

        agentRunService.transition(agentRunService.findUnfinished(agent), AgentRunStatus.DONE, "report");
        part.refresh();
        button(actionsOf(agent), "Review changes").click();

        assertThat(part.getLastDialog()).isInstanceOf(AgentMergeDialog.class);
        assertThat(part.getLastDialog().isOpened()).isTrue();
    }
}
