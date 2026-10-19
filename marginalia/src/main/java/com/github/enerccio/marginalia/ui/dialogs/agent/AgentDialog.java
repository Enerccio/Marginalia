package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.AgentService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.widgets.ModelProtocolPicker;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Creates or edits an agent. A new agent starts with the default system prompt ({@link Defaults#DEFAULT_AGENT_PROMPT})
 * and picks the book it works on (a book has at most one agent, and the book of an agent is not changed). The main
 * model is the AI and the protocol of the first model profile; other profiles an agent may have are left as they are.
 */
@Configurable
public class AgentDialog extends Dialog implements LLMInteract {

    /**
     * Role of the main model profile of a new agent.
     */
    static final String MAIN_ROLE = "overseer";

    @Autowired
    private Localization loc;

    @Autowired
    private AgentService agentService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    private final Agent source;
    private Runnable onSave;

    private TextField nameField;
    private Checkbox enabledCheckbox;
    private ComboBox<Manuscript> manuscriptCombo;
    private ModelProtocolPicker modelPicker;
    private Checkbox imagesCheckbox;
    private ComboBox<DestructiveCommandMode> destructiveCombo;
    private IntegerField maxRoundsField;
    private IntegerField tokenBudgetField;
    private IntegerField contextLimitField;
    private TextArea systemPromptField;
    private TextArea instructionsField;
    private AgentCommandsEditor commandsEditor;
    private Button saveButton;

    /**
     * @param agent the agent to edit, null for a new one
     */
    public AgentDialog(Agent agent) {
        this.source = agent;
    }

    public AgentDialog() {
        this(null);
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    private boolean isEdit() {
        return source != null && source.getId() != null;
    }

    public void create() {
        setHeaderTitle(loc.getValue(isEdit() ? L.LABEL_EDIT_AGENT : L.LABEL_NEW_AGENT));
        setWidth("860px");
        setHeight("90%");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        createFields();
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        form.setWidthFull();
        form.add(nameField, enabledCheckbox, manuscriptCombo, modelPicker, imagesCheckbox);
        form.add(destructiveCombo, maxRoundsField, tokenBudgetField, contextLimitField);
        form.add(systemPromptField, createRestoreButton(), instructionsField, commandsEditor);
        add(form);

        Button cancelButton = new Button(loc.getValue(L.LABEL_CANCEL), event -> close());
        saveButton = new Button(loc.getValue(L.LABEL_OK), event -> save());
        saveButton.setThemeName("primary");
        HorizontalLayout footer = new HorizontalLayout(cancelButton, saveButton);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);

        load();
    }

    private void createFields() {
        nameField = new TextField(loc.getValue(L.LABEL_NAME));
        nameField.setRequiredIndicatorVisible(true);
        nameField.setWidthFull();

        enabledCheckbox = new Checkbox(loc.getValue(L.LABEL_ENABLED), true);

        manuscriptCombo = new ComboBox<>(loc.getValue(L.LABEL_BOOK));
        manuscriptCombo.setRequiredIndicatorVisible(true);
        manuscriptCombo.setItemLabelGenerator(Manuscript::getName);
        manuscriptCombo.setWidthFull();

        modelPicker = new ModelProtocolPicker();

        imagesCheckbox = new Checkbox(loc.getValue(L.LABEL_AGENT_SEES_IMAGES));
        imagesCheckbox.setHelperText(loc.getValue(L.HELP_AGENT_SEES_IMAGES));

        destructiveCombo = new ComboBox<>(loc.getValue(L.LABEL_DESTRUCTIVE_COMMANDS));
        destructiveCombo.setItems(DestructiveCommandMode.values());
        destructiveCombo.setItemLabelGenerator(mode -> loc.getValue(loc.getDestructiveCommandMode(mode)));
        destructiveCombo.setAllowCustomValue(false);
        destructiveCombo.setClearButtonVisible(false);
        destructiveCombo.setHelperText(loc.getValue(L.HELP_AGENT_DESTRUCTIVE));
        destructiveCombo.setWidthFull();

        maxRoundsField = positiveField(L.LABEL_MAX_ROUNDS, L.HELP_AGENT_MAX_ROUNDS);
        tokenBudgetField = positiveField(L.LABEL_RUN_TOKEN_BUDGET, L.HELP_AGENT_TOKEN_BUDGET);
        contextLimitField = positiveField(L.LABEL_CONTEXT_LIMIT, L.HELP_AGENT_CONTEXT_LIMIT);

        systemPromptField = new TextArea(loc.getValue(L.LABEL_AGENT_SYSTEM_PROMPT));
        systemPromptField.setHelperText(loc.getValue(L.HELP_AGENT_SYSTEM_PROMPT));
        systemPromptField.setWidthFull();
        ResizableTextArea.install(loc, systemPromptField, "220px");

        instructionsField = new TextArea(loc.getValue(L.LABEL_AGENT_INSTRUCTIONS));
        instructionsField.setHelperText(loc.getValue(L.HELP_AGENT_INSTRUCTIONS));
        instructionsField.setMaxLength(AgentService.MAX_INSTRUCTIONS_LENGTH);
        instructionsField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
        instructionsField.setWidthFull();
        ResizableTextArea.install(loc, instructionsField, "180px");

        commandsEditor = new AgentCommandsEditor();
        commandsEditor.setWidthFull();
    }

    private IntegerField positiveField(L label, L help) {
        IntegerField field = new IntegerField(loc.getValue(label));
        field.setMin(1);
        field.setStepButtonsVisible(false);
        field.setHelperText(loc.getValue(help));
        field.setWidthFull();
        return field;
    }

    private Button createRestoreButton() {
        Button restore = new Button(loc.getValue(L.LABEL_RESTORE_DEFAULT), Solid.UNDO.create(),
                event -> systemPromptField.setValue(Defaults.DEFAULT_AGENT_PROMPT));
        restore.setThemeName("tertiary");
        return restore;
    }

    private void load() {
        try {
            Agent agent = isEdit() ? agentService.findForUser(source.getUuid()) : null;
            List<Manuscript> books = new ArrayList<>();
            if (agent != null) {
                books.add(agent.getManuscript());
            } else {
                for (Manuscript manuscript : manuscriptService.findAllRegularForUser()) {
                    if (agentService.findForManuscript(manuscript) == null) {
                        books.add(manuscript);
                    }
                }
                books.sort(Comparator.comparing(Manuscript::getName, loc.createLocaleComparator()));
            }
            manuscriptCombo.setItems(books);

            if (agent == null) {
                // the defaults of a new agent
                systemPromptField.setValue(Defaults.DEFAULT_AGENT_PROMPT);
                destructiveCombo.setValue(DestructiveCommandMode.ASK);
                commandsEditor.select(AgentCommandPreset.NON_DESTRUCTIVE);
                if (books.size() == 1) {
                    manuscriptCombo.setValue(books.getFirst());
                } else if (books.isEmpty()) {
                    Notification.warning(loc.getValue(L.MSG_AGENT_NO_FREE_BOOK));
                }
                return;
            }

            nameField.setValue(agent.getName());
            enabledCheckbox.setValue(agent.isEnabled());
            manuscriptCombo.setValue(agent.getManuscript());
            manuscriptCombo.setReadOnly(true);
            AgentModelProfile main = agent.getMainProfile();
            if (main != null) {
                AI ai = aiService.findAllForUser().stream()
                        .filter(candidate -> Objects.equals(candidate.getUuid(), main.getAiUuid())).findFirst().orElse(null);
                Protocol protocol = main.getProtocolUuid() == null ? null : protocolService.findForUser(main.getProtocolUuid());
                modelPicker.setSelection(ai, protocol);
                imagesCheckbox.setValue(main.isImages());
            }
            destructiveCombo.setValue(agent.getDestructiveMode());
            maxRoundsField.setValue(agent.getMaxRounds());
            tokenBudgetField.setValue(agent.getTokenBudget() == null ? null : (int) Math.min(Integer.MAX_VALUE, agent.getTokenBudget()));
            contextLimitField.setValue(agent.getContextCap());
            systemPromptField.setValue(StringUtils.defaultString(agent.getSystemPrompt()));
            instructionsField.setValue(StringUtils.defaultString(agent.getInstructions()));
            commandsEditor.setAllowed(agent.getAllowedCommands());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void save() {
        if (StringUtils.isBlank(nameField.getValue()) || manuscriptCombo.getValue() == null
                || (modelPicker.getAi() == null) != (modelPicker.getProtocol() == null)
                || fieldInvalid(maxRoundsField) || fieldInvalid(tokenBudgetField) || fieldInvalid(contextLimitField)
                || instructionsField.getValue().length() > AgentService.MAX_INSTRUCTIONS_LENGTH) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
            return;
        }
        try {
            Agent agent = isEdit() ? agentService.findForUser(source.getUuid()) : new Agent();
            if (agent == null) {
                throw new IllegalStateException("The agent no longer exists");
            }
            agent.setName(nameField.getValue());
            agent.setEnabled(enabledCheckbox.getValue());
            if (!isEdit()) {
                agent.setManuscript(manuscriptCombo.getValue());
            }
            applyMainProfile(agent);
            agent.setDestructiveMode(destructiveCombo.getValue());
            agent.setMaxRounds(maxRoundsField.getValue());
            agent.setTokenBudget(tokenBudgetField.getValue() == null ? null : tokenBudgetField.getValue().longValue());
            agent.setContextCap(contextLimitField.getValue());
            agent.setSystemPrompt(systemPromptField.getValue());
            agent.setInstructions(StringUtils.isBlank(instructionsField.getValue()) ? null : instructionsField.getValue());
            agent.setAllowedCommands(commandsEditor.getAllowed());
            agentService.save(agent);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            // refused by the rules of the service (limits, one agent per book), its text says why
            Notification.error(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
            return;
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        close();
        if (onSave != null) {
            onSave.run();
        }
    }

    /**
     * Puts the chosen model into the main profile, the other profiles stay. Neither an AI nor a protocol removes the
     * main profile.
     */
    private void applyMainProfile(Agent agent) {
        List<AgentModelProfile> profiles = new ArrayList<>(agent.getModelProfiles());
        AI ai = modelPicker.getAi();
        Protocol protocol = modelPicker.getProtocol();
        if (ai == null || protocol == null) {
            if (!profiles.isEmpty()) {
                profiles.removeFirst();
            }
        } else if (profiles.isEmpty()) {
            profiles.add(new AgentModelProfile(MAIN_ROLE, ai.getUuid(), protocol.getUuid()));
        } else {
            profiles.getFirst().setAiUuid(ai.getUuid());
            profiles.getFirst().setProtocolUuid(protocol.getUuid());
        }
        if (!profiles.isEmpty()) {
            profiles.getFirst().setImages(imagesCheckbox.getValue());
        }
        agent.setModelProfiles(profiles);
    }

    private static boolean fieldInvalid(IntegerField field) {
        return field.getValue() != null && field.getValue() < 1;
    }

    // for the tests of the dialog

    TextField getNameField() {
        return nameField;
    }

    Checkbox getEnabledCheckbox() {
        return enabledCheckbox;
    }

    ComboBox<Manuscript> getManuscriptCombo() {
        return manuscriptCombo;
    }

    ModelProtocolPicker getModelPicker() {
        return modelPicker;
    }

    Checkbox getImagesCheckbox() {
        return imagesCheckbox;
    }

    ComboBox<DestructiveCommandMode> getDestructiveCombo() {
        return destructiveCombo;
    }

    IntegerField getMaxRoundsField() {
        return maxRoundsField;
    }

    IntegerField getTokenBudgetField() {
        return tokenBudgetField;
    }

    IntegerField getContextLimitField() {
        return contextLimitField;
    }

    TextArea getSystemPromptField() {
        return systemPromptField;
    }

    TextArea getInstructionsField() {
        return instructionsField;
    }

    AgentCommandsEditor getCommandsEditor() {
        return commandsEditor;
    }

    Button getSaveButton() {
        return saveButton;
    }

    void clickSave() {
        saveButton.click();
    }
}
