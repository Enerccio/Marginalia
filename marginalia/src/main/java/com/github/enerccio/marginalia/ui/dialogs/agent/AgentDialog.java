package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.AgentBatchMode;
import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Language;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.widgets.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Creates or edits an agent. The system prompt of a new agent is blank, which means it follows the default (the prompt
 * of the user's settings, the one of the application settings, then {@code Defaults.DEFAULT_AGENT_PROMPT}): the default
 * is the placeholder of the field and the info popover fills it in to edit a copy. A new agent picks the book it works on (a book has at most one agent, and the book of an agent is not changed). The main
 * model is the AI and the protocol of the first model profile; the other profiles, the models of its sub-agents, are edited in the
 * table of {@link AgentModelsEditor}.
 */
@Configurable
@Extendable
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

    @Autowired
    private TokenizerService tokenizerService;

    private final Agent source;
    private Runnable onSave;

    private TextField nameField;
    private Checkbox enabledCheckbox;
    private ComboBox<Manuscript> manuscriptCombo;
    private ModelProtocolPicker modelPicker;
    private Checkbox imagesCheckbox;
    private Checkbox useOwnModelCheckbox;
    private ComboBox<AgentBatchMode> batchModeCombo;
    private ComboBox<Language> languageCombo;
    private ComboBox<DestructiveCommandMode> destructiveCombo;
    private IntegerField maxRoundsField;
    private IntegerField maxSubagentsField;
    private IntegerField tokenBudgetField;
    private IntegerField contextLimitField;
    private IntegerField autoCompactField;
    private TextAreaPopoverComponent systemPromptField;
    private TextAreaPopoverComponent compactionPromptField;
    private TextAreaPopoverComponent instructionsField;
    private AgentCommandsEditor commandsEditor;
    private AgentModelsEditor modelsEditor;
    // what the prompt, the instructions and the commands weigh in every request
    private Span sizeLabel;
    private String defaultPrompt = "";
    private String defaultInstructions = "";
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

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "onSave", type = Runnable.class)})
    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = boolean.class))
    private boolean isEdit() {
        return source != null && source.getId() != null;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "form", type = FormLayout.class),
                    @APILocals(name = "cancelButton", type = Button.class),
                    @APILocals(name = "footer", type = HorizontalLayout.class)})
    public void create() {
        setHeaderTitle(loc.getValue(isEdit() ? L.LABEL_EDIT_AGENT : L.LABEL_NEW_AGENT));
        setWidth("1100px");
        setMaxWidth("96vw");
        setHeight("90%");
        UIUtils.fitPhone(this, true);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        createFields();
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        form.setWidthFull();
        form.add(nameField, enabledCheckbox, manuscriptCombo, modelPicker, imagesCheckbox, useOwnModelCheckbox);
        form.add(destructiveCombo, batchModeCombo, languageCombo, maxRoundsField, maxSubagentsField, tokenBudgetField, contextLimitField, autoCompactField);
        form.add(modelsEditor);
        form.add(systemPromptField, compactionPromptField, instructionsField, sizeLabel, commandsEditor);
        add(form);

        Button cancelButton = new Button(loc.getValue(L.LABEL_CANCEL), event -> close());
        saveButton = new Button(loc.getValue(L.LABEL_OK), event -> save());
        saveButton.setThemeName("primary");
        HorizontalLayout footer = new HorizontalLayout(cancelButton, saveButton);
        footer.setWidthFull();
        footer.setWrap(true);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);

        load();
        showSize();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
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

        useOwnModelCheckbox = new Checkbox(loc.getValue(L.LABEL_AGENT_USE_OWN_MODEL), true);
        useOwnModelCheckbox.setHelperText(loc.getValue(L.HELP_AGENT_USE_OWN_MODEL));

        batchModeCombo = new ComboBox<>(loc.getValue(L.LABEL_AGENT_BATCH_MODE));
        batchModeCombo.setItems(AgentBatchMode.values());
        batchModeCombo.setItemLabelGenerator(mode -> loc.getValue(switch (mode) {
            case BATCHED_IGNORE_POLICIES -> L.ENUM_AGENT_BATCH_IGNORE_POLICIES;
            case BATCHED -> L.ENUM_AGENT_BATCH_BATCHED;
            case NOT_BATCHED -> L.ENUM_AGENT_BATCH_NOT_BATCHED;
        }));
        batchModeCombo.setAllowCustomValue(false);
        batchModeCombo.setClearButtonVisible(false);
        batchModeCombo.setValue(AgentBatchMode.BATCHED);
        batchModeCombo.setHelperText(loc.getValue(L.HELP_AGENT_BATCH_MODE));
        batchModeCombo.setWidthFull();

        languageCombo = new ComboBox<>(loc.getValue(L.LABEL_AGENT_LANGUAGE));
        languageCombo.setItems(loc.getAvailableLanguages());
        languageCombo.setItemLabelGenerator(this::languageLabel);
        languageCombo.setAllowCustomValue(false);
        languageCombo.setClearButtonVisible(false);
        languageCombo.setValue(Language.EN);
        languageCombo.setHelperText(loc.getValue(L.HELP_AGENT_LANGUAGE));
        languageCombo.setWidthFull();

        destructiveCombo = new ComboBox<>(loc.getValue(L.LABEL_DESTRUCTIVE_COMMANDS));
        destructiveCombo.setItems(DestructiveCommandMode.values());
        destructiveCombo.setItemLabelGenerator(mode -> loc.getValue(loc.getDestructiveCommandMode(mode)));
        destructiveCombo.setAllowCustomValue(false);
        destructiveCombo.setClearButtonVisible(false);
        destructiveCombo.setHelperText(loc.getValue(L.HELP_AGENT_DESTRUCTIVE));
        destructiveCombo.setWidthFull();

        maxRoundsField = positiveField(L.LABEL_MAX_ROUNDS, L.HELP_AGENT_MAX_ROUNDS);
        // empty or zero is none
        maxSubagentsField = new IntegerField(loc.getValue(L.LABEL_MAX_SUBAGENTS));
        maxSubagentsField.setMin(0);
        maxSubagentsField.setMax(AgentService.MAX_SUBAGENTS);
        maxSubagentsField.setStepButtonsVisible(true);
        maxSubagentsField.setHelperText(loc.getValue(L.HELP_AGENT_MAX_SUBAGENTS));
        maxSubagentsField.setWidthFull();
        tokenBudgetField = positiveField(L.LABEL_RUN_TOKEN_BUDGET, L.HELP_AGENT_TOKEN_BUDGET);
        contextLimitField = positiveField(L.LABEL_CONTEXT_LIMIT, L.HELP_AGENT_CONTEXT_LIMIT);

        // empty follows the default, zero is never
        autoCompactField = new IntegerField(loc.getValue(L.LABEL_AGENT_AUTO_COMPACT));
        autoCompactField.setMin(0);
        autoCompactField.setMax(AgentService.MAX_AUTO_COMPACT_PERCENT);
        autoCompactField.setStepButtonsVisible(false);
        autoCompactField.setPlaceholder(String.valueOf(Defaults.AGENT_AUTO_COMPACT_PERCENT));
        autoCompactField.setHelperText(String.format(loc.getValue(L.HELP_AGENT_AUTO_COMPACT), Defaults.AGENT_AUTO_COMPACT_PERCENT,
                AgentService.MIN_AUTO_COMPACT_PERCENT));
        autoCompactField.setWidthFull();

        systemPromptField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_AGENT_SYSTEM_PROMPT));
        systemPromptField.setHelperText(loc.getValue(L.HELP_AGENT_SYSTEM_PROMPT));
        systemPromptField.setWidthFull();
        ResizableTextArea.install(loc, systemPromptField, "220px");

        compactionPromptField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_AGENT_COMPACTION_PROMPT));
        compactionPromptField.setHelperText(loc.getValue(L.HELP_AGENT_COMPACTION_PROMPT));
        compactionPromptField.setWidthFull();
        ResizableTextArea.install(loc, compactionPromptField, "160px");

        instructionsField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_AGENT_INSTRUCTIONS));
        instructionsField.setHelperText(loc.getValue(L.HELP_AGENT_INSTRUCTIONS));
        instructionsField.setMaxLength(AgentService.MAX_INSTRUCTIONS_LENGTH);
        instructionsField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
        instructionsField.setWidthFull();
        ResizableTextArea.install(loc, instructionsField, "180px");

        commandsEditor = new AgentCommandsEditor();
        commandsEditor.setWidthFull();
        modelsEditor = new AgentModelsEditor();

        // a blank prompt or blank instructions follow the default, which is what counts; the field of the prompt is not
        // lazy by default
        sizeLabel = new Span();
        sizeLabel.addClassName(CssStyles.HINT_TEXT);
        systemPromptField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
        systemPromptField.addValueChangeListener(event -> showSize());
        instructionsField.addValueChangeListener(event -> showSize());
        commandsEditor.onChange(this::showSize);
    }

    /**
     * What the prompt, the instructions and the ticked commands add to every request, in tokens: an estimate, the notes of the
     * runner and the tools for files and for the run come on top.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void showSize() {
        try {
            long prompt = tokenizerService.countTokensApprox(StringUtils.defaultIfBlank(systemPromptField.getValue(), defaultPrompt));
            long instructions = tokenizerService.countTokensApprox(StringUtils.defaultIfBlank(instructionsField.getValue(), defaultInstructions));
            long commands = commandsEditor.getSelectedTokens();
            sizeLabel.setText(String.format(loc.getValue(L.MSG_AGENT_PROMPT_SIZE), prompt + instructions + commands, prompt, instructions, commands));
        } catch (Exception e) {
            sizeLabel.setText("");
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = IntegerField.class),
            parameters = {@APIParameter(name = "label", type = L.class),
                    @APIParameter(name = "help", type = L.class)},
            locals = {@APILocals(name = "field", type = IntegerField.class)})
    private IntegerField positiveField(L label, L help) {
        IntegerField field = new IntegerField(loc.getValue(label));
        field.setMin(1);
        field.setStepButtonsVisible(false);
        field.setHelperText(loc.getValue(help));
        field.setWidthFull();
        return field;
    }

    /**
     * The default a blank prompt follows is the placeholder, and the popover fills it into the field.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "defaultCompaction", type = String.class)})
    private void showDefaultPrompt() throws Exception {
        defaultPrompt = StringUtils.defaultString(agentService.getDefaultSystemPrompt());
        systemPromptField.setPlaceholder(defaultPrompt);
        systemPromptField.setPopoverContent(TemplateHints.create(loc, systemPromptField, systemPromptField.getPopover(),
                null, defaultPrompt));
        defaultInstructions = StringUtils.defaultString(agentService.getDefaultInstructions());
        instructionsField.setPlaceholder(defaultInstructions);
        instructionsField.setPopoverContent(TemplateHints.create(loc, instructionsField, instructionsField.getPopover(),
                null, defaultInstructions));
        String defaultCompaction = agentService.getDefaultCompactionPrompt();
        compactionPromptField.setPlaceholder(defaultCompaction);
        compactionPromptField.setPopoverContent(TemplateHints.create(loc, compactionPromptField,
                compactionPromptField.getPopover(), null, defaultCompaction));
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "agent", type = Agent.class),
                    @APILocals(name = "books", type = List.class),
                    @APILocals(name = "manuscript", type = Manuscript.class),
                    @APILocals(name = "main", type = AgentModelProfile.class),
                    @APILocals(name = "ai", type = AI.class),
                    @APILocals(name = "protocol", type = Protocol.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void load() {
        try {
            showDefaultPrompt();
            Agent agent = isEdit() ? agentService.findForUser(source.getUuid()) : null;
            List<Manuscript> books = new ArrayList<>();
            if (agent != null) {
                books.add(agentService.findManuscript(agent));
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
                // the defaults of a new agent; the prompt stays blank and follows the default
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
            manuscriptCombo.setValue(books.getFirst());
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
            maxSubagentsField.setValue(agent.getMaxSubagents());
            tokenBudgetField.setValue(agent.getTokenBudget() == null ? null : (int) Math.min(Integer.MAX_VALUE, agent.getTokenBudget()));
            contextLimitField.setValue(agent.getContextCap());
            autoCompactField.setValue(agent.getAutoCompactPercent());
            useOwnModelCheckbox.setValue(agent.isUsingAgentModel());
            batchModeCombo.setValue(agent.batchModeOrDefault());
            showLanguage(agent.getLanguage());
            systemPromptField.setValue(StringUtils.defaultString(agent.getSystemPrompt()));
            compactionPromptField.setValue(StringUtils.defaultString(agent.getCompactionPrompt()));
            instructionsField.setValue(StringUtils.defaultString(agent.getInstructions()));
            commandsEditor.setAllowed(agent.getAllowedCommands());
            modelsEditor.setProfiles(agent.getModelProfiles(), agent.isSelfSubagentAllowed());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Selects the language of the agent. One whose bundle is gone (an extension that is not loaded) stays in the list, marked, so
     * saving the agent does not change it silently.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "code", type = String.class)},
            locals = {@APILocals(name = "languages", type = List.class),
                    @APILocals(name = "stored", type = Language.class)})
    private void showLanguage(String code) {
        List<Language> languages = new ArrayList<>(loc.getAvailableLanguages());
        Language stored = code == null ? Language.EN : Language.of(code);
        if (!languages.contains(stored)) {
            languages.add(stored);
        }
        languageCombo.setItems(languages);
        languageCombo.setValue(stored);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "language", type = Language.class)})
    private String languageLabel(Language language) {
        return loc.getAvailableLanguages().contains(language) ? loc.getDisplayName(language)
                : String.format(loc.getValue(L.MSG_AGENT_LANGUAGE_UNAVAILABLE), language.code());
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "agent", type = Agent.class),
                    @APILocals(name = "e", type = RuntimeException.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void save() {
        if (StringUtils.isBlank(nameField.getValue()) || manuscriptCombo.getValue() == null
                || (modelPicker.getAi() == null) != (modelPicker.getProtocol() == null)
                || fieldInvalid(maxRoundsField) || fieldInvalid(maxSubagentsField) || fieldInvalid(tokenBudgetField) || fieldInvalid(contextLimitField) || fieldInvalid(autoCompactField)
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
            modelsEditor.applyTo(agent);
            agent.setDestructiveMode(destructiveCombo.getValue());
            agent.setMaxRounds(maxRoundsField.getValue());
            agent.setMaxSubagents(maxSubagentsField.getValue() == null || maxSubagentsField.getValue() == 0 ? null : maxSubagentsField.getValue());
            agent.setTokenBudget(tokenBudgetField.getValue() == null ? null : tokenBudgetField.getValue().longValue());
            agent.setContextCap(contextLimitField.getValue());
            agent.setAutoCompactPercent(autoCompactField.getValue());
            agent.setUseAgentModel(useOwnModelCheckbox.getValue());
            agent.setBatchMode(batchModeCombo.getValue() == AgentBatchMode.BATCHED ? null : batchModeCombo.getValue());
            agent.setLanguage(languageCombo.getValue() == null || languageCombo.getValue().equals(Language.EN) ? null : languageCombo.getValue().code());
            agent.setSystemPrompt(systemPromptField.getValue());
            agent.setCompactionPrompt(compactionPromptField.getValue());
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
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "agent", type = Agent.class)},
            locals = {@APILocals(name = "profiles", type = List.class),
                    @APILocals(name = "ai", type = AI.class),
                    @APILocals(name = "protocol", type = Protocol.class)})
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

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = TextField.class))
    TextField getNameField() {
        return nameField;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Checkbox.class))
    Checkbox getEnabledCheckbox() {
        return enabledCheckbox;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = ComboBox.class))
    ComboBox<Manuscript> getManuscriptCombo() {
        return manuscriptCombo;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = ModelProtocolPicker.class))
    ModelProtocolPicker getModelPicker() {
        return modelPicker;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Checkbox.class))
    Checkbox getImagesCheckbox() {
        return imagesCheckbox;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = ComboBox.class))
    ComboBox<DestructiveCommandMode> getDestructiveCombo() {
        return destructiveCombo;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = IntegerField.class))
    IntegerField getMaxSubagentsField() {
        return maxSubagentsField;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = IntegerField.class))
    IntegerField getMaxRoundsField() {
        return maxRoundsField;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = IntegerField.class))
    IntegerField getTokenBudgetField() {
        return tokenBudgetField;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = IntegerField.class))
    IntegerField getContextLimitField() {
        return contextLimitField;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = TextAreaPopoverComponent.class))
    TextAreaPopoverComponent getSystemPromptField() {
        return systemPromptField;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = TextAreaPopoverComponent.class))
    TextAreaPopoverComponent getCompactionPromptField() {
        return compactionPromptField;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Checkbox.class))
    Checkbox getUseOwnModelCheckbox() {
        return useOwnModelCheckbox;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = ComboBox.class))
    ComboBox<Language> getLanguageCombo() {
        return languageCombo;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = ComboBox.class))
    ComboBox<AgentBatchMode> getBatchModeCombo() {
        return batchModeCombo;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = IntegerField.class))
    IntegerField getAutoCompactField() {
        return autoCompactField;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = TextAreaPopoverComponent.class))
    TextAreaPopoverComponent getInstructionsField() {
        return instructionsField;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = AgentModelsEditor.class))
    AgentModelsEditor getModelsEditor() {
        return modelsEditor;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = AgentCommandsEditor.class))
    AgentCommandsEditor getCommandsEditor() {
        return commandsEditor;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Span.class))
    Span getSizeLabel() {
        return sizeLabel;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Button.class))
    Button getSaveButton() {
        return saveButton;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    void clickSave() {
        saveButton.click();
    }
}
