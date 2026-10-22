package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.enerccio.marginalia.test.ComponentTree.button;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The models of the sub-agents in the dialog of an agent: the table of the profiles, the check box for the main model, and the
 * dialog of one profile with its commands.
 */
class AgentModelsEditorTest extends AgentTestBase {

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        loginAgentUser();
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private AgentDialog dialog(Agent agent) {
        AgentDialog dialog = new AgentDialog(agent);
        dialog.create();
        dialog.open();
        return dialog;
    }

    @Test
    void aNewAgentOffersTheMainModelToItsSubagents() throws Exception {
        manuscript();

        AgentModelsEditor editor = dialog(null).getModelsEditor();

        assertThat(editor.getSelfCheckbox().getValue()).isTrue();
        assertThat(editor.getOthers()).isEmpty();
        assertThat(editor.getGrid().getListDataView().getItems().toList()).hasSize(1);
    }

    @Test
    void theTableShowsTheModelsOfTheAgentAndSavingKeepsWhatWasEdited() throws Exception {
        Agent agent = newAgent(manuscript());
        AgentModelProfile auditor = profile("auditor");
        auditor.setDescription("reads");
        auditor.setCommands(List.of("get-book-info"));
        agent.getModelProfiles().add(auditor);
        agent.setAllowSelfSubagent(false);
        agent = agentService.save(agent);

        AgentDialog dialog = dialog(agent);
        AgentModelsEditor editor = dialog.getModelsEditor();

        assertThat(editor.getSelfCheckbox().getValue()).isFalse();
        assertThat(editor.getOthers()).singleElement().satisfies(model -> {
            assertThat(model.getRole()).isEqualTo("auditor");
            assertThat(model.getCommands()).containsExactly("get-book-info");
        });
        editor.getSelfCheckbox().setValue(true);
        AgentModelProfile writer = profile("writer");
        writer.setTools(false);
        editor.addModel(writer);
        button(dialog, "OK").click();

        Agent saved = agentService.findForUser(agent.getUuid());
        assertThat(saved.isSelfSubagentAllowed()).isTrue();
        assertThat(saved.getModelProfiles()).extracting(AgentModelProfile::getRole).containsExactly("overseer", "auditor", "writer");
        assertThat(saved.getModelProfiles().get(1).getCommands()).containsExactly("get-book-info");
        assertThat(saved.getModelProfiles().get(2).isTools()).isFalse();
    }

    @Test
    void aDeletedModelIsGoneWhenSaved() throws Exception {
        Agent agent = newAgent(manuscript());
        agent.getModelProfiles().add(profile("auditor"));
        agent = agentService.save(agent);
        AgentDialog dialog = dialog(agent);

        dialog.getModelsEditor().getOthers().clear();
        button(dialog, "OK").click();

        assertThat(agentService.findForUser(agent.getUuid()).getModelProfiles()).extracting(AgentModelProfile::getRole).containsExactly("overseer");
    }

    @Test
    void theDialogOfAModelEditsACopyUntilItIsConfirmed() throws Exception {
        OpenAICompatible ai = createAI();
        Protocol protocol = protocol();
        AgentModelProfile model = new AgentModelProfile("auditor", ai.getUuid(), protocol.getUuid());
        AgentModelProfile[] edited = new AgentModelProfile[1];
        AgentModelDialog dialog = new AgentModelDialog(model, false, result -> edited[0] = result);
        dialog.create();
        dialog.open();

        dialog.getRoleField().setValue("reader");
        dialog.getDescriptionField().setValue("a cheap model");
        dialog.getLimitCommandsCheckbox().setValue(true);
        dialog.getCommandsEditor().setAllowed(List.of("get-book-info"));
        assertThat(model.getRole()).as("not changed before the OK").isEqualTo("auditor");
        button(dialog, "OK").click();

        assertThat(dialog.isOpened()).isFalse();
        assertThat(edited[0].getRole()).isEqualTo("reader");
        assertThat(edited[0].getDescription()).isEqualTo("a cheap model");
        assertThat(edited[0].getCommands()).containsExactly("get-book-info");
        assertThat(edited[0].getAiUuid()).isEqualTo(ai.getUuid());
        assertThat(model.getCommands()).isNull();
    }

    @Test
    void aModelNeedsARoleAndItsAI() throws Exception {
        AgentModelProfile[] edited = new AgentModelProfile[1];
        AgentModelDialog dialog = new AgentModelDialog(new AgentModelProfile(), false, result -> edited[0] = result);
        dialog.create();
        dialog.open();

        button(dialog, "OK").click();

        assertThat(dialog.isOpened()).isTrue();
        assertThat(edited[0]).isNull();
    }

    @Test
    void theMainModelIsEditedForItsCommandsOnly() throws Exception {
        Manuscript book = manuscript();
        Agent agent = agentService.save(newAgent(book));
        AgentDialog dialog = dialog(agent);
        AgentModelsEditor editor = dialog.getModelsEditor();
        AgentModelProfile[] edited = new AgentModelProfile[1];
        AgentModelDialog main = new AgentModelDialog(editor.getMain(), true, result -> edited[0] = result);
        main.create();
        main.open();

        main.getLimitCommandsCheckbox().setValue(true);
        main.getCommandsEditor().setAllowed(List.of("list-parts"));
        button(main, "OK").click();

        assertThat(edited[0].getCommands()).containsExactly("list-parts");
        assertThat(edited[0].getRole()).isEqualTo("overseer");
        assertThat(edited[0].getAiUuid()).as("the model of the agent itself is picked in its own dialog").isEqualTo(agent.getMainProfile().getAiUuid());
    }
}
