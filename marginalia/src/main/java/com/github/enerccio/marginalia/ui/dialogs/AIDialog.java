package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.collections.ReasoningEffort;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.inference.OpenAICompatibleInferenceService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.extensions.ExtensionAIFactory;
import com.github.enerccio.marginalia.extensions.ExtensionAIForm;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

@Configurable
@Extendable
public class AIDialog extends Dialog implements LLMInteract {
    private static final Gson gson = new GsonBuilder().create();
    private static final int DEFAULT_MAX_CONTEXT = 16384;
    private static final int DEFAULT_MAX_RESPONSE = 2048;

    @Autowired
    protected Localization loc;

    @Autowired
    private AIService aiService;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private TokenizerService tokenizerService;

    @Autowired
    private User currentUser;

    private AI ai;
    private Runnable onSave;

    // Header fields (above tabs)
    private TextField nameField;
    private ComboBox<AIType> typeCombo;
    private Checkbox sharedCheckbox;

    // General fields (in tab 1)
    private IntegerField maxContextField;
    private IntegerField maxResponseField;
    private Checkbox needsJailbreakCheckbox;
    private TextArea jailbreakField;
    private Checkbox enabledReasoningCheckbox;
    private ComboBox<ReasoningEffort> reasoningEffortCombo;

    // Per type fields (in tab 2)
    private FormLayout dynamicFormLayout;
    private TextField urlField;
    private PasswordField apiKeyField;
    private Button resetApiKeyButton;
    // not apiKeyField.isEnabled(): the tab sheet disables the content of the tab that isn't shown
    private boolean apiKeyEditable = true;
    private ComboBox<String> modelCombo;
    private Button refreshModelsButton;
    private TextArea additionalParametersField;
    private IntegerField requestTimeoutField;
    private IntegerField maxRetriesField;
    private Button testConnectionButton;

    // Extension type: the provider the extension offers and the form of its settings (null until a provider is chosen)
    private ComboBox<ExtensionAIFactory> providerCombo;
    private ExtensionAIFactory extensionFactory;
    private ExtensionAIForm extensionForm;

    public AIDialog() {
        this(null);
    }

    public AIDialog(AI ai) {
        this.ai = ai;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "mainLayout", type = VerticalLayout.class),
                    @APILocals(name = "baseFormLayout", type = FormLayout.class),
                    @APILocals(name = "tabSheet", type = TabSheet.class),
                    @APILocals(name = "generalFormLayout", type = FormLayout.class),
                    @APILocals(name = "footerLayout", type = HorizontalLayout.class),
                    @APILocals(name = "cancelButton", type = Button.class),
                    @APILocals(name = "saveButton", type = Button.class)})
    public void create() {
        boolean isEdit = ai != null && ai.getId() != null;
        setHeaderTitle(isEdit ? loc.getValue(L.LABEL_EDIT_AI) : loc.getValue(L.LABEL_NEW_AI));
        setWidth("600px");
        setHeight("820px");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        createFields();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);

        FormLayout baseFormLayout = new FormLayout();
        baseFormLayout.setWidthFull();
        baseFormLayout.add(nameField, typeCombo);
        if (sharedCheckbox.isVisible()) {
            baseFormLayout.add(sharedCheckbox, 2);
        }

        mainLayout.add(baseFormLayout);

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addClassName(CssStyles.MIN_HEIGHT_0);

        FormLayout generalFormLayout = new FormLayout();
        generalFormLayout.setWidthFull();
        generalFormLayout.addClassName(CssStyles.FORM_PADDING_BOTTOM);
        generalFormLayout.add(maxContextField, maxResponseField);
        generalFormLayout.add(needsJailbreakCheckbox, jailbreakField);
        generalFormLayout.setColspan(jailbreakField, 2);
        generalFormLayout.add(enabledReasoningCheckbox, reasoningEffortCombo);

        tabSheet.add(loc.getValue(L.LABEL_GENERAL_SETTINGS), generalFormLayout);

        dynamicFormLayout = new FormLayout();
        dynamicFormLayout.setWidthFull();
        dynamicFormLayout.addClassName(CssStyles.FORM_PADDING_BOTTOM);

        tabSheet.add(loc.getValue(L.LABEL_TYPE_SETTINGS), dynamicFormLayout);

        mainLayout.add(tabSheet);
        mainLayout.setFlexGrow(1, tabSheet);

        typeCombo.addValueChangeListener(event -> updateDynamicFields(event.getValue()));
        providerCombo.addValueChangeListener(event -> {
            extensionFactory = null;
            extensionForm = null;
            updateDynamicFields(AIType.EXTENSION);
        });

        if (isEdit) {
            // the type decides the class of the AI, it can't be changed
            typeCombo.setReadOnly(true);
            providerCombo.setReadOnly(true);
            populateFields();
        } else {
            typeCombo.setValue(AIType.OPEN_AI_COMPATIBLE);
            maxContextField.setValue(DEFAULT_MAX_CONTEXT);
            maxResponseField.setValue(DEFAULT_MAX_RESPONSE);
            apiKeyEditable = true;
            apiKeyField.setEnabled(true);
            resetApiKeyButton.setVisible(false);
            needsJailbreakCheckbox.setValue(false);
            jailbreakField.setEnabled(false);
            enabledReasoningCheckbox.setValue(false);
            reasoningEffortCombo.setEnabled(false);
            reasoningEffortCombo.setValue(ReasoningEffort.NONE);
        }

        add(mainLayout);

        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Button cancelButton = new Button(loc.getValue(L.LABEL_CANCEL), event -> close());
        Button saveButton = new Button(loc.getValue(L.LABEL_OK), event -> save());
        saveButton.setThemeName("primary");

        footerLayout.add(cancelButton, saveButton);
        getFooter().add(footerLayout);
    }

    /**
     * The required fields that are empty, as any form, and what the dialog checks across fields when it saves: the
     * numbers have to be positive and the response smaller than the context, the additional parameters a JSON object.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = List.class),
            locals = {@APILocals(name = "problems", type = List.class),
                    @APILocals(name = "context", type = Integer.class),
                    @APILocals(name = "response", type = Integer.class),
                    @APILocals(name = "parameters", type = JsonElement.class),
                    @APILocals(name = "e", type = Exception.class)})
    @Override
    public List<String> getLLMProblems() {
        List<String> problems = new ArrayList<>(LLMInteract.super.getLLMProblems());
        if (maxContextField == null || maxResponseField == null) {
            return problems;
        }
        Integer context = maxContextField.getValue();
        Integer response = maxResponseField.getValue();
        if (context != null && context <= 0) {
            problems.add("\"" + maxContextField.getLabel() + "\": must be greater than zero");
        }
        if (response != null && response <= 0) {
            problems.add("\"" + maxResponseField.getLabel() + "\": must be greater than zero");
        }
        if (context != null && response != null && context > 0 && response > 0 && response >= context) {
            problems.add("\"" + maxResponseField.getLabel() + "\": must be smaller than \"" + maxContextField.getLabel() + "\"");
        }
        if (typeCombo.getValue() == AIType.OPEN_AI_COMPATIBLE && additionalParametersField != null
                && StringUtils.isNotBlank(additionalParametersField.getValue())) {
            try {
                JsonElement parameters = JsonParser.parseString(additionalParametersField.getValue());
                if (!parameters.isJsonObject()) {
                    problems.add("\"" + additionalParametersField.getLabel() + "\": must be a JSON object");
                }
            } catch (Exception e) {
                problems.add("\"" + additionalParametersField.getLabel() + "\": is not valid JSON");
            }
        }
        return problems;
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "types", type = List.class)})
    private void createFields() {
        nameField = new TextField(loc.getValue(L.LABEL_NAME));
        nameField.setRequired(true);
        nameField.setWidthFull();
        DialogFocus.onOpen(this, nameField);

        typeCombo = new ComboBox<>(loc.getValue(L.LABEL_TYPE));
        // the type Extension is offered when some extension provides an AI (or the AI being edited is one)
        List<AIType> types = new ArrayList<>(List.of(AIType.values()));
        if (inferenceServices.getExtensionFactories().isEmpty() && !(ai instanceof ExtensionAI)) {
            types.remove(AIType.EXTENSION);
        }
        typeCombo.setItems(types);
        typeCombo.setItemLabelGenerator(type -> loc.getValue(loc.getAIType(type)));
        typeCombo.setRequired(true);
        typeCombo.setWidthFull();

        providerCombo = new ComboBox<>(loc.getValue(L.LABEL_PROVIDER));
        providerCombo.setItems(inferenceServices.getExtensionFactories());
        providerCombo.setItemLabelGenerator(ExtensionAIFactory::getName);
        providerCombo.setRequired(true);
        providerCombo.setWidthFull();

        // only administrators share, the service refuses it for anybody else
        sharedCheckbox = new Checkbox(loc.getValue(L.LABEL_SHARED_WITH_ALL_USERS));
        sharedCheckbox.setVisible(aiService.isAdministrator());

        maxContextField = new IntegerField(loc.getValue(L.LABEL_MAX_CONTEXT));
        maxContextField.setRequired(true);
        maxContextField.setMin(1);
        maxContextField.setHelperText(loc.getValue(L.HELP_AI_MAX_CONTEXT));
        maxContextField.setWidthFull();

        maxResponseField = new IntegerField(loc.getValue(L.LABEL_MAX_RESPONSE));
        maxResponseField.setRequired(true);
        maxResponseField.setMin(1);
        maxResponseField.setHelperText(loc.getValue(L.HELP_AI_MAX_RESPONSE));
        maxResponseField.setWidthFull();

        needsJailbreakCheckbox = new Checkbox(loc.getValue(L.LABEL_NEEDS_JAILBREAK));
        jailbreakField = new TextArea(loc.getValue(L.LABEL_JAILBREAK));
        jailbreakField.setWidthFull();
        ResizableTextArea.install(loc, jailbreakField, "120px");
        jailbreakField.setEnabled(false);
        needsJailbreakCheckbox.addValueChangeListener(event -> jailbreakField.setEnabled(Boolean.TRUE.equals(event.getValue())));

        enabledReasoningCheckbox = new Checkbox(loc.getValue(L.LABEL_ENABLED_REASONING));
        reasoningEffortCombo = new ComboBox<>(loc.getValue(L.LABEL_REASONING_EFFORT));
        reasoningEffortCombo.setItems(ReasoningEffort.values());
        reasoningEffortCombo.setItemLabelGenerator(effort -> loc.getValue(loc.getReasoningEffort(effort)));
        reasoningEffortCombo.setWidthFull();
        reasoningEffortCombo.setEnabled(false);
        enabledReasoningCheckbox.addValueChangeListener(event -> reasoningEffortCombo.setEnabled(Boolean.TRUE.equals(event.getValue())));

        urlField = new TextField(loc.getValue(L.LABEL_URL));
        urlField.setRequired(true);
        urlField.setWidthFull();

        apiKeyField = new PasswordField(loc.getValue(L.LABEL_API_KEY));
        apiKeyField.setWidthFull();

        resetApiKeyButton = new Button(loc.getValue(L.LABEL_RESET), event -> {
            apiKeyEditable = true;
            apiKeyField.setEnabled(true);
            apiKeyField.focus();
            resetApiKeyButton.setEnabled(false);
        });

        modelCombo = new ComboBox<>(loc.getValue(L.LABEL_MODEL));
        modelCombo.setRequired(true);
        modelCombo.setWidthFull();
        modelCombo.setAllowCustomValue(true);
        modelCombo.addCustomValueSetListener(event -> {
            modelCombo.setItems(List.of(event.getDetail()));
            modelCombo.setValue(event.getDetail());
        });

        refreshModelsButton = new Button(loc.getValue(L.LABEL_REFRESH), event -> fetchModels());

        additionalParametersField = new TextArea(loc.getValue(L.LABEL_ADDITIONAL_PARAMETERS));
        additionalParametersField.setWidthFull();
        ResizableTextArea.install(loc, additionalParametersField, "120px");

        requestTimeoutField = new IntegerField(loc.getValue(L.LABEL_REQUEST_TIMEOUT));
        requestTimeoutField.setMin(1);
        requestTimeoutField.setHelperText(loc.getValue(L.HELP_AI_REQUEST_TIMEOUT));
        requestTimeoutField.setValue(OpenAICompatibleInferenceService.DEFAULT_TIMEOUT_SECONDS);
        requestTimeoutField.setWidthFull();

        maxRetriesField = new IntegerField(loc.getValue(L.LABEL_MAX_RETRIES));
        maxRetriesField.setMin(0);
        maxRetriesField.setMax(10);
        maxRetriesField.setStepButtonsVisible(true);
        maxRetriesField.setHelperText(loc.getValue(L.HELP_AI_MAX_RETRIES));
        maxRetriesField.setValue(OpenAICompatibleInferenceService.DEFAULT_MAX_RETRIES);
        maxRetriesField.setWidthFull();

        testConnectionButton = new Button(loc.getValue(L.LABEL_TEST_CONNECTION), event -> testConnection());
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "type", type = AIType.class)},
            locals = {@APILocals(name = "apiKeyLayout", type = HorizontalLayout.class),
                    @APILocals(name = "modelLayout", type = HorizontalLayout.class),
                    @APILocals(name = "factories", type = List.class)})
    private void updateDynamicFields(AIType type) {
        dynamicFormLayout.removeAll();
        if (type == AIType.OPEN_AI_COMPATIBLE) {
            HorizontalLayout apiKeyLayout = new HorizontalLayout();
            apiKeyLayout.setWidthFull();
            apiKeyLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
            apiKeyLayout.add(apiKeyField, resetApiKeyButton);
            apiKeyLayout.setFlexGrow(1, apiKeyField);

            HorizontalLayout modelLayout = new HorizontalLayout();
            modelLayout.setWidthFull();
            modelLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
            modelLayout.add(modelCombo, refreshModelsButton);
            modelLayout.setFlexGrow(1, modelCombo);

            dynamicFormLayout.add(urlField, apiKeyLayout, modelLayout, requestTimeoutField, maxRetriesField,
                    additionalParametersField, testConnectionButton);
            dynamicFormLayout.setColspan(urlField, 2);
            dynamicFormLayout.setColspan(apiKeyLayout, 2);
            dynamicFormLayout.setColspan(modelLayout, 2);
            dynamicFormLayout.setColspan(additionalParametersField, 2);
        } else if (type == AIType.EXTENSION) {
            List<ExtensionAIFactory> factories = inferenceServices.getExtensionFactories();
            if (providerCombo.isEmpty() && ai == null && factories.size() == 1) {
                // nothing to choose from; the listener of the combo builds the form
                providerCombo.setValue(factories.get(0));
                return;
            }
            dynamicFormLayout.add(providerCombo);
            dynamicFormLayout.setColspan(providerCombo, 2);
            addExtensionSettings();
        }
    }

    /**
     * The form of the chosen provider under the provider selector; when an AI being edited belongs to an extension that
     * is not loaded, a notice instead (its settings are not touched).
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "factory", type = ExtensionAIFactory.class),
                    @APILocals(name = "formComponent", type = Component.class),
                    @APILocals(name = "extensionAI", type = ExtensionAI.class),
                    @APILocals(name = "notice", type = Span.class)})
    private void addExtensionSettings() {
        ExtensionAIFactory factory = providerCombo.getValue();
        if (factory != null) {
            if (extensionForm == null || extensionFactory != factory) {
                extensionFactory = factory;
                extensionForm = factory.createForm(extensionAiFor(factory));
            }
            Component formComponent = extensionForm.getComponent();
            dynamicFormLayout.add(formComponent, testConnectionButton);
            dynamicFormLayout.setColspan(formComponent, 2);
        } else if (ai instanceof ExtensionAI extensionAI && isExtensionMissing()) {
            Span notice = new Span(String.format(loc.getValue(L.MSG_AI_EXTENSION_NOT_LOADED), extensionAI.getExtensionId()));
            notice.addClassName(CssStyles.NOTICE_WARNING);
            dynamicFormLayout.add(notice);
            dynamicFormLayout.setColspan(notice, 2);
        }
    }

    /**
     * Whether the AI being edited belongs to an extension that is not loaded now.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = boolean.class),
            locals = {@APILocals(name = "extensionAI", type = ExtensionAI.class)})
    private boolean isExtensionMissing() {
        return ai instanceof ExtensionAI extensionAI && inferenceServices.getExtensionFactory(extensionAI.getExtensionId()) == null;
    }

    /**
     * The AI the form of the provider reads its settings from: the one being edited, a new one for the provider
     * otherwise.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = ExtensionAI.class),
            parameters = {@APIParameter(name = "factory", type = ExtensionAIFactory.class)},
            locals = {@APILocals(name = "extensionAI", type = ExtensionAI.class),
                    @APILocals(name = "newAI", type = ExtensionAI.class)})
    private ExtensionAI extensionAiFor(ExtensionAIFactory factory) {
        if (ai instanceof ExtensionAI extensionAI && factory.getExtensionId().equals(extensionAI.getExtensionId())) {
            return extensionAI;
        }
        ExtensionAI newAI = new ExtensionAI();
        newAI.setAiType(AIType.EXTENSION);
        newAI.setExtensionId(factory.getExtensionId());
        return newAI;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "compatible", type = OpenAICompatible.class),
                    @APILocals(name = "modelName", type = String.class),
                    @APILocals(name = "extensionAI", type = ExtensionAI.class)})
    private void populateFields() {
        if (ai == null) {
            return;
        }

        nameField.setValue(StringUtils.defaultString(ai.getName()));
        typeCombo.setValue(ai.getAiType());
        sharedCheckbox.setValue(ai.isShared());
        // older providers may have no limits stored - offer the defaults, saving requires a value
        maxContextField.setValue(ai.getMaxContext() != null && ai.getMaxContext() > 0 ? ai.getMaxContext() : DEFAULT_MAX_CONTEXT);
        maxResponseField.setValue(ai.getMaxCompletionTokens() != null && ai.getMaxCompletionTokens() > 0 ? ai.getMaxCompletionTokens() : DEFAULT_MAX_RESPONSE);
        jailbreakField.setValue(StringUtils.defaultString(ai.getJailbreak()));
        needsJailbreakCheckbox.setValue(Boolean.TRUE.equals(ai.getNeedsJailbreak()));
        jailbreakField.setEnabled(Boolean.TRUE.equals(ai.getNeedsJailbreak()));

        enabledReasoningCheckbox.setValue(Boolean.TRUE.equals(ai.getEnabledReasoning()));
        reasoningEffortCombo.setValue(ai.getReasoningEffort() != null ? ai.getReasoningEffort() : ReasoningEffort.NONE);
        reasoningEffortCombo.setEnabled(Boolean.TRUE.equals(ai.getEnabledReasoning()));

        if (ai.getAiType() == AIType.OPEN_AI_COMPATIBLE && ai instanceof OpenAICompatible compatible) {
            urlField.setValue(StringUtils.defaultString(compatible.getUri()));

            apiKeyField.setValue("");
            apiKeyEditable = false;
            apiKeyField.setEnabled(false);
            resetApiKeyButton.setVisible(true);
            resetApiKeyButton.setEnabled(true);

            String modelName = compatible.getModelName() != null ? compatible.getModelName() : compatible.getModel();
            if (modelName != null) {
                modelCombo.setItems(List.of(modelName));
                modelCombo.setValue(modelName);
            }

            additionalParametersField.setValue(gson.toJson(compatible.getAdditionalParameters()));
            requestTimeoutField.setValue(compatible.getRequestTimeoutSeconds() != null && compatible.getRequestTimeoutSeconds() > 0
                    ? compatible.getRequestTimeoutSeconds() : OpenAICompatibleInferenceService.DEFAULT_TIMEOUT_SECONDS);
            maxRetriesField.setValue(compatible.getMaxRetries() != null && compatible.getMaxRetries() >= 0
                    ? compatible.getMaxRetries() : OpenAICompatibleInferenceService.DEFAULT_MAX_RETRIES);
        }

        if (ai.getAiType() == AIType.EXTENSION && ai instanceof ExtensionAI extensionAI) {
            // null when the extension is not loaded, the notice is shown instead of the form
            providerCombo.setValue(inferenceServices.getExtensionFactory(extensionAI.getExtensionId()));
        }
    }

    /**
     * Provider with the values currently in the dialog (not saved), null when they can't be used - the user was told
     * why.
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = OpenAICompatible.class),
            parameters = {@APIParameter(name = "needsModel", type = boolean.class)},
            locals = {@APILocals(name = "compatible", type = OpenAICompatible.class),
                    @APILocals(name = "c", type = OpenAICompatible.class),
                    @APILocals(name = "apiKeyToUse", type = String.class),
                    @APILocals(name = "copy", type = OpenAICompatible.class),
                    @APILocals(name = "parameters", type = JsonElement.class),
                    @APILocals(name = "e", type = Exception.class)})
    private OpenAICompatible currentProvider(boolean needsModel) {
        if (StringUtils.isBlank(urlField.getValue()) || (needsModel && StringUtils.isBlank(modelCombo.getValue()))) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
            return null;
        }

        OpenAICompatible compatible = (ai instanceof OpenAICompatible c) ? c : null;

        String apiKeyToUse;
        if (apiKeyEditable) {
            apiKeyToUse = apiKeyField.getValue();
        } else if (compatible != null) {
            apiKeyToUse = compatible.getApiKey();
        } else {
            return null;
        }

        OpenAICompatible copy = new OpenAICompatible();
        copy.setAiType(AIType.OPEN_AI_COMPATIBLE);
        copy.setUri(urlField.getValue().trim());
        copy.setApiKey(apiKeyToUse);
        copy.setModel(modelCombo.getValue());
        copy.setModelName(modelCombo.getValue());
        copy.setRequestTimeoutSeconds(requestTimeoutField.getValue());
        copy.setMaxRetries(maxRetriesField.getValue());
        if (StringUtils.isNotBlank(additionalParametersField.getValue())) {
            try {
                JsonElement parameters = JsonParser.parseString(additionalParametersField.getValue());
                if (!parameters.isJsonObject()) {
                    Notification.warning(loc.getValue(L.MSG_INVALID_JSON_OBJECT));
                    return null;
                }
                copy.setAdditionalParameters(parameters.getAsJsonObject());
            } catch (Exception e) {
                Notification.warning(loc.getValue(L.MSG_INVALID_JSON_OBJECT));
                return null;
            }
        }
        return copy;
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "copy", type = OpenAICompatible.class),
                    @APILocals(name = "models", type = List.class),
                    @APILocals(name = "selected", type = String.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void fetchModels() {
        if (typeCombo.getValue() != AIType.OPEN_AI_COMPATIBLE) {
            return;
        }
        OpenAICompatible copy = currentProvider(false);
        if (copy == null) {
            return;
        }

        try {
            List<String> models = new ArrayList<>(inferenceServices.forAI(copy).getModels());

            if (modelCombo.getValue() != null && !models.contains(modelCombo.getValue())) {
                models.add(0, modelCombo.getValue());
            }

            // setItems clears the selection
            String selected = modelCombo.getValue();
            modelCombo.setItems(models);
            if (selected != null) {
                modelCombo.setValue(selected);
            }
        } catch (Exception e) {
            if (InferenceErrors.find(e) != null) {
                UIUtils.inferenceError(loc, e);
            } else {
                UIUtils.showError(loc.getValue(L.MSG_FETCH_MODELS_FAILED), e);
            }
        }
    }

    /**
     * Lists the models and asks the model for one token, with the values in the dialog. Not retried, so a wrong
     * setting shows up at once.
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "copy", type = OpenAICompatible.class),
                    @APILocals(name = "models", type = List.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void testConnection() {
        if (typeCombo.getValue() == AIType.EXTENSION) {
            testExtensionConnection();
            return;
        }
        if (typeCombo.getValue() != AIType.OPEN_AI_COMPATIBLE) {
            return;
        }
        OpenAICompatible copy = currentProvider(true);
        if (copy == null) {
            return;
        }
        copy.setMaxRetries(0);

        try {
            List<String> models = inferenceServices.forAI(copy).testConnection();
            Notification.success(models.isEmpty()
                    ? loc.getValue(L.MSG_CONNECTION_OK_NO_MODELS)
                    : String.format(loc.getValue(L.MSG_CONNECTION_OK), models.size()));
        } catch (Exception e) {
            if (InferenceErrors.find(e) != null) {
                UIUtils.inferenceError(loc, e);
            } else {
                UIUtils.internalServerError(loc, e);
            }
        }
    }

    /**
     * Like {@link #testConnection()} for the provider of an extension: the inference service of an AI with the values
     * in the dialog (not saved) is asked to test the connection.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "copy", type = ExtensionAI.class),
                    @APILocals(name = "service", type = InferenceService.class),
                    @APILocals(name = "models", type = List.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void testExtensionConnection() {
        ExtensionAI copy = currentExtensionAI();
        if (copy == null) {
            return;
        }

        try {
            InferenceService service = inferenceServices.forAI(copy);
            if (service == null) {
                Notification.warning(String.format(loc.getValue(L.MSG_AI_EXTENSION_NOT_LOADED), copy.getExtensionId()));
                return;
            }
            List<String> models = service.testConnection();
            Notification.success(models.isEmpty()
                    ? loc.getValue(L.MSG_CONNECTION_OK_NO_MODELS)
                    : String.format(loc.getValue(L.MSG_CONNECTION_OK), models.size()));
        } catch (Exception e) {
            if (InferenceErrors.find(e) != null) {
                UIUtils.inferenceError(loc, e);
            } else {
                UIUtils.internalServerError(loc, e);
            }
        }
    }

    /**
     * AI of the extension with the values currently in the dialog (not saved), null when the form says they can't be
     * used - it told the user why.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = ExtensionAI.class),
            locals = {@APILocals(name = "copy", type = ExtensionAI.class),
                    @APILocals(name = "extensionAI", type = ExtensionAI.class)})
    private ExtensionAI currentExtensionAI() {
        if (extensionFactory == null || extensionForm == null || !extensionForm.validate()) {
            return null;
        }

        ExtensionAI copy = new ExtensionAI();
        copy.setAiType(AIType.EXTENSION);
        copy.setExtensionId(extensionFactory.getExtensionId());
        if (ai instanceof ExtensionAI extensionAI && extensionAI.getAttributes() != null) {
            copy.setAttributes(extensionAI.getAttributes().deepCopy());
        }
        copy.setName(nameField.getValue());
        copy.setMaxContext(maxContextField.getValue());
        copy.setMaxCompletionTokens(maxResponseField.getValue());
        copy.setEnabledReasoning(enabledReasoningCheckbox.getValue());
        copy.setReasoningEffort(reasoningEffortCombo.getValue());
        extensionForm.apply(copy);
        return copy;
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "name", type = String.class),
                    @APILocals(name = "type", type = AIType.class),
                    @APILocals(name = "maxContext", type = Integer.class),
                    @APILocals(name = "maxResponse", type = Integer.class),
                    @APILocals(name = "additionalParams", type = String.class),
                    @APILocals(name = "jsonElement", type = JsonElement.class),
                    @APILocals(name = "e", type = Exception.class),
                    @APILocals(name = "newExtensionAI", type = ExtensionAI.class),
                    @APILocals(name = "compatible", type = OpenAICompatible.class),
                    @APILocals(name = "selectedModel", type = String.class),
                    @APILocals(name = "extensionAI", type = ExtensionAI.class)})
    private void save() {
        String name = nameField.getValue();
        AIType type = typeCombo.getValue();

        Integer maxContext = maxContextField.getValue();
        Integer maxResponse = maxResponseField.getValue();

        if (StringUtils.isBlank(name) || type == null || maxContext == null || maxContext <= 0
                || maxResponse == null || maxResponse <= 0) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
            return;
        }

        if (maxResponse >= maxContext) {
            Notification.warning(loc.getValue(L.MSG_AI_RESPONSE_EXCEEDS_CONTEXT));
            return;
        }

        if (type == AIType.OPEN_AI_COMPATIBLE) {
            if (StringUtils.isBlank(urlField.getValue()) || StringUtils.isBlank(modelCombo.getValue())) {
                Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
                return;
            }

            if (requestTimeoutField.getValue() == null || requestTimeoutField.getValue() <= 0
                    || maxRetriesField.getValue() == null || maxRetriesField.getValue() < 0) {
                Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
                return;
            }

            String additionalParams = additionalParametersField.getValue();
            if (StringUtils.isNotBlank(additionalParams)) {
                try {
                    JsonElement jsonElement = JsonParser.parseString(additionalParams);
                    if (!jsonElement.isJsonObject()) {
                        Notification.warning(loc.getValue(L.MSG_INVALID_JSON_OBJECT));
                        return;
                    }
                } catch (Exception e) {
                    Notification.warning(loc.getValue(L.MSG_INVALID_JSON_OBJECT));
                    return;
                }
            }
        }

        // an AI of an extension that is not loaded keeps the settings of the extension as they are
        boolean editsExtensionSettings = false;
        if (type == AIType.EXTENSION) {
            if (!isExtensionMissing()) {
                if (providerCombo.getValue() == null || extensionForm == null) {
                    Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
                    return;
                }
                if (!extensionForm.validate()) {
                    return;
                }
                editsExtensionSettings = true;
            }
        }

        try {
            if (ai == null) {
                switch (type) {
                    case OPEN_AI_COMPATIBLE -> ai = new OpenAICompatible();
                    case EXTENSION -> {
                        ExtensionAI newExtensionAI = new ExtensionAI();
                        newExtensionAI.setExtensionId(providerCombo.getValue().getExtensionId());
                        ai = newExtensionAI;
                    }
                    default -> throw new RuntimeException();
                }
            }

            ai.setName(name.trim());
            ai.setAiType(type);
            ai.setShared(sharedCheckbox.isVisible() && sharedCheckbox.getValue());
            ai.setMaxContext(maxContext);
            ai.setMaxCompletionTokens(maxResponse);
            ai.setJailbreak(jailbreakField.getValue());
            ai.setNeedsJailbreak(needsJailbreakCheckbox.getValue());
            ai.setEnabledReasoning(enabledReasoningCheckbox.getValue());
            ai.setReasoningEffort(reasoningEffortCombo.getValue());

            if (type == AIType.OPEN_AI_COMPATIBLE && ai instanceof OpenAICompatible compatible) {
                compatible.setUri(urlField.getValue().trim());

                if (apiKeyEditable) {
                    compatible.setApiKey(apiKeyField.getValue());
                }

                String selectedModel = modelCombo.getValue().trim();
                compatible.setModelName(selectedModel);
                compatible.setModel(selectedModel);
                compatible.setAdditionalParameters(gson.fromJson(additionalParametersField.getValue(), JsonObject.class));
                compatible.setRequestTimeoutSeconds(requestTimeoutField.getValue());
                compatible.setMaxRetries(maxRetriesField.getValue());
            }

            if (editsExtensionSettings && ai instanceof ExtensionAI extensionAI) {
                extensionForm.apply(extensionAI);
            }

            ai = aiService.save(ai);
            tokenizerService.invalidateCache(ai.getId());
            close();

            if (onSave != null) {
                onSave.run();
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Runnable.class))
    public Runnable getOnSave() {
        return onSave;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "onSave", type = Runnable.class)})
    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }
}