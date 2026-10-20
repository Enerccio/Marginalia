package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.AgentBatchMode;
import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.AgentService;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.enerccio.marginalia.test.ComponentTree.button;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The dialog an agent is created and edited in: a new agent has a blank prompt that follows the default, picks a book that has no
 * agent, and the allowed commands are chosen with the check boxes or a preset.
 */
class AgentDialogTest extends AgentTestBase {

    @Autowired
    private Localization loc;

    @Autowired
    private CommandService commandService;

    @Autowired
    private SettingService settingService;

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

    private AgentDialog dialog(Agent agent) {
        AgentDialog dialog = new AgentDialog(agent);
        dialog.create();
        dialog.open();
        return dialog;
    }

    private List<String> commandNames() {
        return commandService.getCommands().stream().filter(CommandDefinition::availableForAgent).map(CommandDefinition::commandName)
                .distinct().toList();
    }

    private List<String> readOnlyCommandNames() {
        return commandService.getCommands().stream().filter(CommandDefinition::availableForAgent).filter(c -> !c.dataSeverity().isDestructive())
                .map(CommandDefinition::commandName).distinct().toList();
    }

    @Test
    void aNewAgentHasABlankPromptThatFollowsTheDefaultAndTheCommandsThatOnlyRead() throws Exception {
        manuscript();

        AgentDialog dialog = dialog(null);

        assertThat(dialog.getSystemPromptField().getValue()).isEmpty();
        assertThat(dialog.getSystemPromptField().getPlaceholder()).isEqualTo(Defaults.DEFAULT_AGENT_PROMPT);
        assertThat(dialog.getDestructiveCombo().getValue()).isEqualTo(DestructiveCommandMode.ASK);
        assertThat(dialog.getEnabledCheckbox().getValue()).isTrue();
        assertThat(dialog.getCommandsEditor().getPreset()).isEqualTo(AgentCommandPreset.NON_DESTRUCTIVE);
        assertThat(dialog.getCommandsEditor().getAllowed()).containsExactlyInAnyOrderElementsOf(readOnlyCommandNames());
    }

    @Test
    void theNewAgentOffersOnlyTheBooksWithoutAnAgent() throws Exception {
        Manuscript free = manuscript();
        Manuscript taken = manuscript();
        agentService.save(newAgent(taken));

        AgentDialog dialog = dialog(null);

        assertThat(dialog.getManuscriptCombo().getListDataView().getItems().toList())
                .extracting(Manuscript::getId).contains(free.getId()).doesNotContain(taken.getId());
    }

    @Test
    void savingCreatesTheAgentWithWhatTheDialogHolds() throws Exception {
        Manuscript book = manuscript();
        OpenAICompatible ai = createAI();
        Protocol protocol = protocol();
        AgentDialog dialog = dialog(null);
        boolean[] saved = {false};
        dialog.setOnSave(() -> saved[0] = true);

        dialog.getNameField().setValue("Continuity checker");
        dialog.getManuscriptCombo().setValue(dialog.getManuscriptCombo().getListDataView().getItems()
                .filter(m -> m.getId().equals(book.getId())).findFirst().orElseThrow());
        dialog.getModelPicker().setSelection(ai, protocol);
        dialog.getDestructiveCombo().setValue(DestructiveCommandMode.DENY);
        dialog.getMaxRoundsField().setValue(40);
        dialog.getTokenBudgetField().setValue(200_000);
        dialog.getContextLimitField().setValue(8000);
        dialog.getAutoCompactField().setValue(80);
        assertThat(dialog.getUseOwnModelCheckbox().getValue()).as("on for a new agent").isTrue();
        dialog.getUseOwnModelCheckbox().setValue(false);
        assertThat(dialog.getBatchModeCombo().getValue()).as("batched for a new agent").isEqualTo(AgentBatchMode.BATCHED);
        dialog.getBatchModeCombo().setValue(AgentBatchMode.NOT_BATCHED);
        dialog.getInstructionsField().setValue("Check the names.");
        dialog.getCommandsEditor().select(AgentCommandPreset.NONE);
        button(dialog, "OK").click();

        assertThat(saved[0]).isTrue();
        assertThat(dialog.isOpened()).isFalse();
        Agent agent = agentService.findForManuscript(book);
        assertThat(agent.getName()).isEqualTo("Continuity checker");
        assertThat(agent.getMainProfile().getAiUuid()).isEqualTo(ai.getUuid());
        assertThat(agent.getMainProfile().getProtocolUuid()).isEqualTo(protocol.getUuid());
        assertThat(agent.getDestructiveMode()).isEqualTo(DestructiveCommandMode.DENY);
        assertThat(agent.getMaxRounds()).isEqualTo(40);
        assertThat(agent.getTokenBudget()).isEqualTo(200_000L);
        assertThat(agent.getContextCap()).isEqualTo(8000);
        assertThat(agent.getAutoCompactPercent()).isEqualTo(80);
        assertThat(agent.isUsingAgentModel()).isFalse();
        assertThat(agent.getBatchMode()).isEqualTo(AgentBatchMode.NOT_BATCHED);
        assertThat(agent.getInstructions()).isEqualTo("Check the names.");
        assertThat(agent.getSystemPrompt()).as("blank: it follows the default").isBlank();
        assertThat(agent.getAllowedCommands()).isEmpty();
    }

    @Test
    void theAuthorSaysWhetherTheModelSeesImages() throws Exception {
        Manuscript book = manuscript();
        AgentDialog dialog = dialog(null);
        dialog.getNameField().setValue("Looker");
        dialog.getManuscriptCombo().setValue(dialog.getManuscriptCombo().getListDataView().getItems().findFirst().orElseThrow());
        dialog.getModelPicker().setSelection(createAI(), protocol());
        assertThat(dialog.getImagesCheckbox().getValue()).as("a model is not taken to see images").isFalse();

        dialog.getImagesCheckbox().setValue(true);
        button(dialog, "OK").click();

        Agent saved = agentService.findForManuscript(book);
        assertThat(saved.getMainProfile().isImages()).isTrue();
        AgentDialog edit = dialog(saved);
        assertThat(edit.getImagesCheckbox().getValue()).isTrue();
        edit.getImagesCheckbox().setValue(false);
        button(edit, "OK").click();
        assertThat(agentService.findForManuscript(book).getMainProfile().isImages()).isFalse();
    }

    @Test
    void savingNeedsANameAndABook() throws Exception {
        manuscript();
        AgentDialog dialog = dialog(null);

        button(dialog, "OK").click();

        assertThat(dialog.isOpened()).as("not saved, still open").isTrue();
        assertThat(agentService.findAllForUser()).isEmpty();
    }

    @Test
    void aModelNeedsItsProtocolToo() throws Exception {
        Manuscript book = manuscript();
        AgentDialog dialog = dialog(null);
        dialog.getNameField().setValue("half");
        dialog.getManuscriptCombo().setValue(dialog.getManuscriptCombo().getListDataView().getItems().findFirst().orElseThrow());
        dialog.getModelPicker().setSelection(createAI(), null);

        button(dialog, "OK").click();

        assertThat(dialog.isOpened()).isTrue();
        assertThat(agentService.findForManuscript(book)).isNull();
    }

    @Test
    void theLimitOfTheAdministratorStopsASave() throws Exception {
        manuscript();
        updatePermission(user, PermissionType.AGENTS, permission -> permission.setAgentsMaxAgents(0));
        AgentDialog dialog = dialog(null);
        dialog.getNameField().setValue("one too many");
        dialog.getManuscriptCombo().setValue(dialog.getManuscriptCombo().getListDataView().getItems().findFirst().orElseThrow());

        button(dialog, "OK").click();

        assertThat(dialog.isOpened()).as("refused by the service").isTrue();
        assertThat(agentService.findAllForUser()).isEmpty();
    }

    @Test
    void editingShowsTheAgentAndKeepsItsBookAndTheOtherModels() throws Exception {
        Agent agent = newAgent(manuscript());
        agent.setSystemPrompt("my prompt");
        agent.setInstructions("my instructions");
        agent.setDestructiveMode(DestructiveCommandMode.AUTO);
        agent.setMaxRounds(12);
        agent.setAllowedCommands(List.of(commandNames().getFirst(), "gone-command"));
        AgentModelProfile helper = profile("helper");
        agent.getModelProfiles().add(helper);
        Agent saved = agentService.save(agent);

        AgentDialog dialog = dialog(saved);

        assertThat(dialog.getNameField().getValue()).isEqualTo(saved.getName());
        assertThat(dialog.getManuscriptCombo().getValue().getId()).isEqualTo(saved.getManuscript().getId());
        assertThat(dialog.getManuscriptCombo().isReadOnly()).as("the book of an agent is not changed").isTrue();
        assertThat(dialog.getSystemPromptField().getValue()).isEqualTo("my prompt");
        assertThat(dialog.getInstructionsField().getValue()).isEqualTo("my instructions");
        assertThat(dialog.getDestructiveCombo().getValue()).isEqualTo(DestructiveCommandMode.AUTO);
        assertThat(dialog.getMaxRoundsField().getValue()).isEqualTo(12);
        assertThat(dialog.getModelPicker().getAi().getUuid()).isEqualTo(saved.getMainProfile().getAiUuid());
        assertThat(dialog.getCommandsEditor().isSelected(commandNames().getFirst())).isTrue();

        dialog.getNameField().setValue("renamed");
        dialog.getEnabledCheckbox().setValue(false);
        dialog.getMaxRoundsField().clear();
        button(dialog, "OK").click();

        Agent after = agentService.find(saved.getId());
        assertThat(after.getName()).isEqualTo("renamed");
        assertThat(after.isEnabled()).isFalse();
        assertThat(after.getMaxRounds()).isNull();
        assertThat(after.getModelProfiles()).as("the other model stays").hasSize(2).contains(helper);
        assertThat(after.getAllowedCommands()).as("an allowed command of an extension that is not loaded stays")
                .contains("gone-command", commandNames().getFirst());
    }

    @Test
    void theInfoPopoverFillsTheDefaultPromptIn() throws Exception {
        manuscript();
        AgentDialog dialog = dialog(null);
        dialog.getSystemPromptField().setValue("something else");

        button(dialog.getSystemPromptField().getPopover(), "Insert Default Template").click();

        assertThat(dialog.getSystemPromptField().getValue()).isEqualTo(Defaults.DEFAULT_AGENT_PROMPT);
    }

    @Test
    void theCompactionPromptIsBlankFollowsTheDefaultAndIsSaved() throws Exception {
        Manuscript book = manuscript();
        AgentDialog dialog = dialog(null);
        dialog.getManuscriptCombo().setValue(dialog.getManuscriptCombo().getListDataView().getItems()
                .filter(m -> m.getId().equals(book.getId())).findFirst().orElseThrow());
        assertThat(dialog.getCompactionPromptField().getValue()).isEmpty();
        assertThat(dialog.getCompactionPromptField().getPlaceholder()).isEqualTo(Defaults.DEFAULT_AGENT_COMPACTION_PROMPT);
        dialog.getNameField().setValue("Compacting");
        dialog.getModelPicker().setSelection(createAI(), protocol());
        button(dialog.getCompactionPromptField().getPopover(), "Insert Default Template").click();
        assertThat(dialog.getCompactionPromptField().getValue()).isEqualTo(Defaults.DEFAULT_AGENT_COMPACTION_PROMPT);
        dialog.getCompactionPromptField().setValue("Short summary please.");

        button(dialog, "OK").click();

        assertThat(agentService.findForManuscript(book).getCompactionPrompt()).isEqualTo("Short summary please.");
    }

    @Test
    void theInstructionsFollowTheDefaultOfTheUserAsTheirPlaceholder() throws Exception {
        manuscript();
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        setting.setDefaultAgentInstructions("Keep the names.");
        settingService.save(setting);

        AgentDialog dialog = dialog(null);

        assertThat(dialog.getInstructionsField().getValue()).isEmpty();
        assertThat(dialog.getInstructionsField().getPlaceholder()).isEqualTo("Keep the names.");
    }

    @Test
    void theDefaultPromptIsTheUsersOwnWhenTheSettingsHaveOne() throws Exception {
        manuscript();
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        setting.setDefaultAgentPrompt("My own prompt for agents");
        settingService.save(setting);

        AgentDialog dialog = dialog(null);

        assertThat(dialog.getSystemPromptField().getValue()).isEmpty();
        assertThat(dialog.getSystemPromptField().getPlaceholder()).isEqualTo("My own prompt for agents");
        button(dialog.getSystemPromptField().getPopover(), "Insert Default Template").click();
        assertThat(dialog.getSystemPromptField().getValue()).isEqualTo("My own prompt for agents");
    }

    @Test
    void thePresetsTickTheCommandsAndATickOfYourOwnIsCustom() throws Exception {
        manuscript();
        AgentCommandsEditor editor = dialog(null).getCommandsEditor();
        String first = editor.getCommandNames().getFirst();

        editor.select(AgentCommandPreset.ALL);
        assertThat(editor.getAllowed()).containsExactlyInAnyOrderElementsOf(commandNames());
        assertThat(editor.getPreset()).isEqualTo(AgentCommandPreset.ALL);

        editor.select(AgentCommandPreset.NONE);
        assertThat(editor.getAllowed()).isEmpty();
        assertThat(editor.getPreset()).isEqualTo(AgentCommandPreset.NONE);

        editor.select(AgentCommandPreset.NON_DESTRUCTIVE);
        assertThat(editor.getAllowed()).containsExactlyInAnyOrderElementsOf(readOnlyCommandNames());

        editor.select(AgentCommandPreset.NONE);
        editor.setSelected(first, true);
        assertThat(editor.getAllowed()).containsExactly(first);
        assertThat(editor.getPreset()).as("a selection of your own").isIn(null, AgentCommandPreset.NON_DESTRUCTIVE, AgentCommandPreset.ALL);

        List<String> destructive = commandService.getCommands().stream().filter(c -> c.dataSeverity().isDestructive())
                .map(CommandDefinition::commandName).collect(Collectors.toList());
        if (!destructive.isEmpty()) {
            editor.select(AgentCommandPreset.NON_DESTRUCTIVE);
            editor.setSelected(destructive.getFirst(), true);
            assertThat(editor.getPreset()).as("one destructive command more than the preset").isNotEqualTo(AgentCommandPreset.NON_DESTRUCTIVE);
        }
    }

    @Test
    void theRolesReadEverythingAndWriteOnlyWhatTheirJobNeeds() throws Exception {
        manuscript();
        AgentCommandsEditor editor = dialog(null).getCommandsEditor();
        List<String> destructive = commandService.getCommands().stream().filter(c -> c.dataSeverity().isDestructive())
                .map(CommandDefinition::commandName).toList();

        editor.select(AgentCommandPreset.AUDITOR);
        assertThat(editor.getAllowed()).containsExactlyInAnyOrderElementsOf(
                commandService.getCommands().stream().filter(c -> c.readOnly() && c.availableForAgent()).map(CommandDefinition::commandName).distinct().toList());
        assertThat(editor.getPreset()).isEqualTo(AgentCommandPreset.AUDITOR);

        editor.select(AgentCommandPreset.LORE_KEEPER);
        assertThat(editor.getAllowed()).contains("get-lore-entry", "create-lore-entry", "update-lore-entry", "add-subbook")
                .doesNotContain("edit-part", "next", "delete-lore-entry");
        assertThat(editor.getPreset()).isEqualTo(AgentCommandPreset.LORE_KEEPER);

        editor.select(AgentCommandPreset.EDITOR);
        assertThat(editor.getAllowed()).contains("get-part", "edit-part", "set-summary", "set-book-info")
                .doesNotContain("create-part", "next", "update-lore-entry", "replace-part-text");
        assertThat(editor.getPreset()).isEqualTo(AgentCommandPreset.EDITOR);

        editor.select(AgentCommandPreset.DRAFTER);
        assertThat(editor.getAllowed()).contains("get-part", "create-part", "next", "swipe", "create-lore-entry")
                .doesNotContain("regenerate", "delete-part", "set-book-prompts");
        assertThat(editor.getPreset()).isEqualTo(AgentCommandPreset.DRAFTER);

        for (AgentCommandPreset role : List.of(AgentCommandPreset.AUDITOR, AgentCommandPreset.LORE_KEEPER, AgentCommandPreset.EDITOR,
                AgentCommandPreset.DRAFTER)) {
            editor.select(role);
            assertThat(editor.getAllowed()).as(role + " removes no data").doesNotContainAnyElementsOf(destructive);
        }
    }

    @Test
    void changesOfTheSelectionAreReported() throws Exception {
        manuscript();
        AgentCommandsEditor editor = dialog(null).getCommandsEditor();
        int[] changes = {0};
        editor.onChange(() -> changes[0]++);

        editor.select(AgentCommandPreset.ALL);
        editor.setSelected(editor.getCommandNames().getFirst(), false);
        editor.setAllowed(List.of());

        assertThat(changes[0]).as("setting the model into the editor is no edit").isEqualTo(2);
    }

    @Test
    void anUnknownCommandCanNotBeTicked() throws Exception {
        manuscript();
        AgentCommandsEditor editor = dialog(null).getCommandsEditor();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> editor.setSelected("no-such-command", true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theInstructionsAreCappedAtTheLimitOfTheService() throws Exception {
        manuscript();
        AgentDialog dialog = dialog(null);

        assertThat(dialog.getInstructionsField().getMaxLength()).isEqualTo(AgentService.MAX_INSTRUCTIONS_LENGTH);
    }

    @Test
    void theDialogShowsWhatThePromptTheInstructionsAndTheCommandsWeighInEveryRequest() throws Exception {
        manuscript();
        AgentDialog dialog = dialog(null);
        String first = dialog.getSizeLabel().getText();
        assertThat(first).startsWith("About ").contains("tokens with every request");

        dialog.getInstructionsField().setValue("Never rename a character. ".repeat(200));
        String longer = dialog.getSizeLabel().getText();
        dialog.getCommandsEditor().select(AgentCommandPreset.NONE);

        assertThat(tokensOf(longer)).as("long instructions weigh").isGreaterThan(tokensOf(first));
        assertThat(tokensOf(dialog.getSizeLabel().getText())).as("no commands weigh less").isLessThan(tokensOf(longer));
        assertThat(dialog.getCommandsEditor().getSelectedTokens()).isZero();
    }

    @Test
    void everyCommandHasTheTokensItsDefinitionCosts() throws Exception {
        manuscript();
        AgentDialog dialog = dialog(null);

        dialog.getCommandsEditor().select(AgentCommandPreset.ALL);

        assertThat(dialog.getCommandsEditor().getGrid().getListDataView().getItems()).allSatisfy(row -> assertThat(row.tokens()).isPositive());
        assertThat(dialog.getCommandsEditor().getSelectedTokens()).isPositive();
    }

    @Test
    void theGridShowsTheBatchPolicyOfEachCommandAndItsColumnsCanBeResized() throws Exception {
        manuscript();
        AgentDialog dialog = dialog(null);

        var rows = dialog.getCommandsEditor().getGrid().getListDataView().getItems().toList();

        assertThat(rows.stream().filter(row -> row.name().equals("regenerate-summary")).findFirst().orElseThrow().batch())
                .isEqualTo(loc.getValue(L.ENUM_BATCH_POLICY_SINGLE));
        assertThat(rows.stream().filter(row -> row.name().equals("get-book-info")).findFirst().orElseThrow().batch())
                .isEqualTo(loc.getValue(L.ENUM_BATCH_POLICY_BATCHABLE));
        assertThat(dialog.getCommandsEditor().getGrid().getColumns()).allMatch(com.vaadin.flow.component.grid.Grid.Column::isResizable);
    }

    private static long tokensOf(String sizeText) {
        return Long.parseLong(sizeText.replaceFirst("^About (\\d+) tokens.*$", "$1"));
    }
}
