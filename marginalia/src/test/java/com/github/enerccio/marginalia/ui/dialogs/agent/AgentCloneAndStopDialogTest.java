package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.github.enerccio.marginalia.test.ComponentTree.button;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cloning an agent to another book, and the question how a run ends.
 */
class AgentCloneAndStopDialogTest extends AgentTestBase {

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        user = loginAgentUser();
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private AgentCloneDialog dialog(Agent agent) {
        AgentCloneDialog dialog = new AgentCloneDialog(agent);
        dialog.create();
        dialog.open();
        return dialog;
    }

    @Test
    void cloningCopiesTheDefinitionToAnotherBook() throws Exception {
        Agent original = newAgent(manuscript());
        original.setInstructions("be careful");
        original = agentService.save(original);
        Manuscript target = manuscript();
        AgentCloneDialog dialog = dialog(original);
        boolean[] saved = {false};
        dialog.setOnSave(() -> saved[0] = true);

        assertThat(dialog.getNameField().getValue()).isEqualTo(original.getName());
        dialog.getNameField().setValue("the copy");
        dialog.getManuscriptCombo().setValue(dialog.getManuscriptCombo().getListDataView().getItems()
                .filter(m -> m.getId().equals(target.getId())).findFirst().orElseThrow());
        button(dialog, "Clone agent").click();

        assertThat(saved[0]).isTrue();
        Agent copy = agentService.findForManuscript(target);
        assertThat(copy.getName()).isEqualTo("the copy");
        assertThat(copy.getInstructions()).isEqualTo("be careful");
        assertThat(copy.getMainProfile()).isEqualTo(original.getMainProfile());
        assertThat(agentService.findAllForUser()).hasSize(2);
    }

    @Test
    void theBookOfTheOriginalIsNotOffered() throws Exception {
        Agent original = agent();

        AgentCloneDialog dialog = dialog(original);

        assertThat(dialog.getManuscriptCombo().getListDataView().getItems().toList())
                .extracting(Manuscript::getId).doesNotContain(original.getManuscript().getId());
        assertThat(dialog.getCloneButton().isEnabled()).as("no book to clone to").isFalse();
    }

    @Test
    void theLimitOfTheAdministratorRefusesTheClone() throws Exception {
        Agent original = agent();
        Manuscript target = manuscript();
        updatePermission(user, PermissionType.AGENTS, permission -> permission.setAgentsMaxAgents(1));
        AgentCloneDialog dialog = dialog(original);
        dialog.getManuscriptCombo().setValue(dialog.getManuscriptCombo().getListDataView().getItems().findFirst().orElseThrow());

        button(dialog, "Clone agent").click();

        assertThat(dialog.isOpened()).as("refused, the dialog stays").isTrue();
        assertThat(agentService.findForManuscript(target)).isNull();
        assertThat(agentService.findAllForUser()).hasSize(1);
    }

    @Test
    void stoppingAsksWhetherTheResultIsKept() throws Exception {
        List<Boolean> answers = new ArrayList<>();
        AgentStopDialog dialog = new AgentStopDialog("My agent", true, answers::add);
        dialog.create();
        dialog.open();

        button(dialog, "Stop and keep the result").click();
        AgentStopDialog second = new AgentStopDialog("My agent", true, answers::add);
        second.create();
        second.open();
        button(second, "Stop and discard the result").click();

        assertThat(answers).containsExactly(true, false);
        assertThat(dialog.isOpened()).isFalse();
    }

    @Test
    void aFinishedRunCanOnlyBeDiscarded() {
        AgentStopDialog dialog = new AgentStopDialog("My agent", false, keep -> {
        });
        dialog.create();
        dialog.open();

        assertThat(dialog.getKeepButton().isVisible()).isFalse();
        assertThat(dialog.getDiscardButton().isVisible()).isTrue();
    }

    @Test
    void theResumeDialogPassesTheNoteStrippedAndEmptyWhenThereIsNone() {
        List<String> notes = new ArrayList<>();
        AgentResumeDialog dialog = new AgentResumeDialog(notes::add);
        dialog.create();
        dialog.open();
        dialog.getNoteField().setValue("  Start with chapter 3.  ");
        dialog.getResumeButton().click();
        assertThat(dialog.isOpened()).isFalse();

        AgentResumeDialog second = new AgentResumeDialog(notes::add);
        second.create();
        second.open();
        second.getResumeButton().click();

        assertThat(notes).containsExactly("Start with chapter 3.", "");
    }

    @Test
    void aLockedBookIsRefusedAndAFreeOneIsNot() throws Exception {
        Agent agent = agent();
        AgentLockCheck check = new AgentLockCheck();
        assertThat(check.refuses(agent.getManuscript())).isFalse();

        AgentRun run = agentRunService.start(agent);
        assertThat(check.refuses(agent.getManuscript())).isTrue();

        agentRunService.pause(run);
        assertThat(check.refuses(agent.getManuscript())).isFalse();
    }
}
