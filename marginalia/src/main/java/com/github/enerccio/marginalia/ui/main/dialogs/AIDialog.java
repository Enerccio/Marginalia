package com.github.enerccio.marginalia.ui.main.dialogs;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.openai.models.models.Model;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
public class AIDialog extends Dialog {

    @Autowired
    protected Localization loc;

    @Autowired
    private AIService aiService;

    @Autowired
    private InferenceServices inferenceServices;

    private AI ai;
    private Runnable onSave;

    private TextField nameField;
    private ComboBox<AIType> typeCombo;

    private FormLayout dynamicFormLayout;

    // Remaining fields for ConnectionType.GENERAL_REASONING
    private TextField urlField;
    private PasswordField apiKeyField;
    private Button resetApiKeyButton;
    private ComboBox<String> modelCombo;
    private Button refreshModelsButton;
    private IntegerField maxContextField;
    private IntegerField maxResponseField;
    private TextArea additionalParametersField;

    public AIDialog() {
        this(null);
    }

    public AIDialog(AI ai) {
        this.ai = ai;
    }

    public void create() {
        boolean isEdit = ai != null && ai.getId() != null;
        setHeaderTitle(isEdit ? loc.getValue(L.LABEL_EDIT_AI) : loc.getValue(L.LABEL_NEW_AI));
        setWidth("550px");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        FormLayout baseFormLayout = new FormLayout();
        baseFormLayout.setWidthFull();

        nameField = new TextField(loc.getValue(L.LABEL_NAME));
        nameField.setRequired(true);
        nameField.setWidthFull();

        typeCombo = new ComboBox<>(loc.getValue(L.LABEL_TYPE));
        typeCombo.setItems(AIType.values());
        typeCombo.setItemLabelGenerator(type -> loc.getValue(loc.getAIType(type)));
        typeCombo.setRequired(true);
        typeCombo.setWidthFull();

        baseFormLayout.add(nameField, typeCombo);
        mainLayout.add(baseFormLayout);

        dynamicFormLayout = new FormLayout();
        dynamicFormLayout.setWidthFull();
        mainLayout.add(dynamicFormLayout);

        createDynamicFields();

        typeCombo.addValueChangeListener(event -> updateDynamicFields(event.getValue()));

        if (isEdit) {
            nameField.setValue(StringUtils.defaultString(ai.getName()));
            typeCombo.setValue(ai.getAiType());
            populateConnectionFields();
        } else {
            typeCombo.setValue(AIType.OPEN_AI_COMPATIBLE);
            apiKeyField.setEnabled(true);
            resetApiKeyButton.setVisible(false);
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

    private void createDynamicFields() {
        urlField = new TextField(loc.getValue(L.LABEL_URL));
        urlField.setRequired(true);
        urlField.setWidthFull();

        apiKeyField = new PasswordField(loc.getValue(L.LABEL_API_KEY));
        apiKeyField.setWidthFull();

        resetApiKeyButton = new Button(loc.getValue(L.LABEL_RESET), event -> {
            apiKeyField.setEnabled(true);
            apiKeyField.focus();
            resetApiKeyButton.setEnabled(false);
        });

        modelCombo = new ComboBox<>(loc.getValue(L.LABEL_MODEL));
        modelCombo.setRequired(true);
        modelCombo.setWidthFull();
        modelCombo.setAllowCustomValue(true);
        modelCombo.addCustomValueSetListener(event -> modelCombo.setValue(event.getDetail()));

        refreshModelsButton = new Button(loc.getValue(L.LABEL_REFRESH), event -> fetchModels());

        maxContextField = new IntegerField(loc.getValue(L.LABEL_MAX_CONTEXT));
        maxContextField.setWidthFull();

        maxResponseField = new IntegerField(loc.getValue(L.LABEL_MAX_RESPONSE));
        maxResponseField.setWidthFull();

        additionalParametersField = new TextArea(loc.getValue(L.LABEL_ADDITIONAL_PARAMETERS));
        additionalParametersField.setWidthFull();
    }

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

            dynamicFormLayout.add(urlField, apiKeyLayout, modelLayout, maxContextField, maxResponseField, additionalParametersField);
            dynamicFormLayout.setColspan(urlField, 2);
            dynamicFormLayout.setColspan(apiKeyLayout, 2);
            dynamicFormLayout.setColspan(modelLayout, 2);
            dynamicFormLayout.setColspan(additionalParametersField, 2);
        }
    }

    private void populateConnectionFields() {
        if (ai == null) {
            return;
        }

        if (ai.getAiType() == AIType.OPEN_AI_COMPATIBLE && ai instanceof OpenAICompatible compatible) {
            urlField.setValue(StringUtils.defaultString(compatible.getUri()));

            apiKeyField.setValue("");
            apiKeyField.setEnabled(false);
            resetApiKeyButton.setVisible(true);
            resetApiKeyButton.setEnabled(true);

            if (compatible.getModelName() != null) {
                modelCombo.setItems(List.of(compatible.getModelName()));
                modelCombo.setValue(compatible.getModelName());
            }

            maxContextField.setValue(ai.getMaxContext());
            maxResponseField.setValue(compatible.getMaxCompletionTokens());
            additionalParametersField.setValue(StringUtils.defaultString(compatible.getAdditionalParameters()));
        }
    }

    private void fetchModels() {
        String url = urlField.getValue();
        if (StringUtils.isBlank(url)) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
            return;
        }

        OpenAICompatible compatible = (ai instanceof OpenAICompatible c) ? c : null;

        if (compatible != null || typeCombo.getValue() == AIType.OPEN_AI_COMPATIBLE) {
            String apiKeyToUse;
            if (apiKeyField.isEnabled()) {
                apiKeyToUse = apiKeyField.getValue();
            } else if (compatible != null) {
                apiKeyToUse = compatible.getApiKey();
            } else {
                return;
            }

            try {
                OpenAICompatible copy = new OpenAICompatible();
                copy.setAiType(AIType.OPEN_AI_COMPATIBLE);
                copy.setUri(url);
                copy.setApiKey(apiKeyToUse);
                List<String> models = inferenceServices.forAI(copy).getModels();

                if (modelCombo.getValue() != null && !models.contains(modelCombo.getValue())) {
                    models.add(0, modelCombo.getValue());
                }

                modelCombo.setItems(models);
            } catch (Exception e) {
                UIUtils.showError(loc.getValue(L.MSG_FETCH_MODELS_FAILED), e);
            }   
        }
    }

    private void save() {
        String name = nameField.getValue();
        AIType type = typeCombo.getValue();

        if (StringUtils.isBlank(name) || type == null) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
            return;
        }

        if (type == AIType.OPEN_AI_COMPATIBLE) {
            if (StringUtils.isBlank(urlField.getValue()) || StringUtils.isBlank(modelCombo.getValue())) {
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

        try {
            if (ai == null) {
                switch (type) {
                    case OPEN_AI_COMPATIBLE -> ai = new OpenAICompatible();
                    default -> throw new RuntimeException();
                }
            }

            ai.setName(name.trim());
            ai.setAiType(type);

            if (type == AIType.OPEN_AI_COMPATIBLE && ai instanceof OpenAICompatible compatible) {
                compatible.setUri(urlField.getValue().trim());

                if (apiKeyField.isEnabled()) {
                    compatible.setApiKey(apiKeyField.getValue());
                }

                compatible.setModelName(modelCombo.getValue().trim());
                compatible.setAdditionalParameters(additionalParametersField.getValue());
            }

            ai.setMaxContext(maxContextField.getValue());
            ai.setMaxCompletionTokens(maxResponseField.getValue());

            aiService.save(ai);
            close();

            if (onSave != null) {
                onSave.run();
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    public Runnable getOnSave() {
        return onSave;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }
}