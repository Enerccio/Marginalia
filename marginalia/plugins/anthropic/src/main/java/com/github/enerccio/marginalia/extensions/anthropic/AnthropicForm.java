package com.github.enerccio.marginalia.extensions.anthropic;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.service.InferenceErrors;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.extensions.ExtensionAIForm;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

/**
 * The settings of a Claude AI in the tab of the AI dialog: the API key, the model (listed from Anthropic), an optional
 * proxy, the timeout and the retries.
 */
@Configurable(preConstruction = true)
public class AnthropicForm implements ExtensionAIForm {

    @Autowired
    private Localization loc;

    @Autowired
    private InferenceServices inferenceServices;

    private final AnthropicFactory factory;
    private final ExtensionAI ai;

    private final FormLayout layout = new FormLayout();
    private PasswordField apiKeyField;
    private Button resetApiKeyButton;
    // not apiKeyField.isEnabled(): the tab sheet disables the content of the tab that isn't shown
    private boolean apiKeyEditable = true;
    private ComboBox<String> modelCombo;
    private TextField baseUrlField;
    private IntegerField timeoutField;
    private IntegerField retriesField;

    AnthropicForm(AnthropicFactory factory, ExtensionAI ai) {
        this.factory = factory;
        this.ai = ai;
        createFields();
        populate();
    }

    @Override
    public Component getComponent() {
        return layout;
    }

    private void createFields() {
        layout.setWidthFull();

        apiKeyField = new PasswordField(loc.getValue(L.LABEL_API_KEY));
        apiKeyField.setWidthFull();
        apiKeyField.setHelperText("The key of the Claude Console (platform.claude.com)");
        resetApiKeyButton = new Button(loc.getValue(L.LABEL_RESET), event -> {
            apiKeyEditable = true;
            apiKeyField.setEnabled(true);
            apiKeyField.focus();
            resetApiKeyButton.setEnabled(false);
        });
        HorizontalLayout apiKeyLayout = new HorizontalLayout(apiKeyField, resetApiKeyButton);
        apiKeyLayout.setWidthFull();
        apiKeyLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        apiKeyLayout.setFlexGrow(1, apiKeyField);

        modelCombo = new ComboBox<>(loc.getValue(L.LABEL_MODEL));
        modelCombo.setRequired(true);
        modelCombo.setWidthFull();
        modelCombo.setAllowCustomValue(true);
        modelCombo.addCustomValueSetListener(event -> {
            modelCombo.setItems(List.of(event.getDetail()));
            modelCombo.setValue(event.getDetail());
        });
        Button refreshModelsButton = new Button(loc.getValue(L.LABEL_REFRESH), event -> fetchModels());
        HorizontalLayout modelLayout = new HorizontalLayout(modelCombo, refreshModelsButton);
        modelLayout.setWidthFull();
        modelLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        modelLayout.setFlexGrow(1, modelCombo);

        baseUrlField = new TextField("URL");
        baseUrlField.setWidthFull();
        baseUrlField.setPlaceholder("https://api.anthropic.com");
        baseUrlField.setHelperText("Optional. Empty uses Anthropic's API, set it to go through a proxy.");

        timeoutField = new IntegerField(loc.getValue(L.LABEL_REQUEST_TIMEOUT));
        timeoutField.setMin(1);
        timeoutField.setHelperText(loc.getValue(L.HELP_AI_REQUEST_TIMEOUT));
        timeoutField.setWidthFull();

        retriesField = new IntegerField(loc.getValue(L.LABEL_MAX_RETRIES));
        retriesField.setMin(0);
        retriesField.setMax(10);
        retriesField.setStepButtonsVisible(true);
        retriesField.setHelperText(loc.getValue(L.HELP_AI_MAX_RETRIES));
        retriesField.setWidthFull();

        layout.add(apiKeyLayout, modelLayout, baseUrlField, timeoutField, retriesField);
        layout.setColspan(apiKeyLayout, 2);
        layout.setColspan(modelLayout, 2);
        layout.setColspan(baseUrlField, 2);
    }

    private void populate() {
        AnthropicSettings settings = AnthropicSettings.of(ai, inferenceServices::revealSecret);

        if (StringUtils.isNotEmpty(AnthropicSettings.storedKey(ai))) {
            // the key is not shown, and not sent back to the browser
            apiKeyEditable = false;
            apiKeyField.setEnabled(false);
            resetApiKeyButton.setVisible(true);
        } else {
            apiKeyEditable = true;
            resetApiKeyButton.setVisible(false);
        }
        if (!settings.model().isBlank()) {
            modelCombo.setItems(List.of(settings.model()));
            modelCombo.setValue(settings.model());
        }
        baseUrlField.setValue(settings.baseUrl());
        timeoutField.setValue(settings.timeoutSeconds());
        retriesField.setValue(settings.maxRetries());
    }

    @Override
    public boolean validate() {
        if (!connectionValuesValid() || StringUtils.isBlank(modelCombo.getValue())) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
            return false;
        }
        return true;
    }

    /** What is needed to talk to Anthropic: the key, the address, the timeout and the retries. */
    private boolean connectionValuesValid() {
        if (apiKeyEditable && StringUtils.isBlank(apiKeyField.getValue())) {
            return false;
        }
        String baseUrl = StringUtils.trimToEmpty(baseUrlField.getValue());
        if (!baseUrl.isEmpty() && !baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            return false;
        }
        return timeoutField.getValue() != null && timeoutField.getValue() > 0
                && retriesField.getValue() != null && retriesField.getValue() >= 0;
    }

    @Override
    public void apply(ExtensionAI target) {
        JsonObject settings = target.getSettings();
        if (apiKeyEditable) {
            String key = StringUtils.trimToEmpty(apiKeyField.getValue());
            if (key.isEmpty()) {
                settings.remove(AnthropicSettings.API_KEY);
            } else {
                settings.addProperty(AnthropicSettings.API_KEY, inferenceServices.protectSecret(key));
            }
        }
        if (StringUtils.isNotBlank(modelCombo.getValue())) {
            settings.addProperty(AnthropicSettings.MODEL, modelCombo.getValue().trim());
        }
        String baseUrl = StringUtils.trimToEmpty(baseUrlField.getValue());
        if (baseUrl.isEmpty()) {
            settings.remove(AnthropicSettings.BASE_URL);
        } else {
            settings.addProperty(AnthropicSettings.BASE_URL, baseUrl);
        }
        settings.addProperty(AnthropicSettings.TIMEOUT_SECONDS, timeoutField.getValue());
        settings.addProperty(AnthropicSettings.MAX_RETRIES, retriesField.getValue());
    }

    /** Asks Anthropic for the models the key can use and offers them in the model selector. */
    private void fetchModels() {
        if (!connectionValuesValid()) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
            return;
        }
        ExtensionAI copy = new ExtensionAI();
        copy.setAiType(AIType.EXTENSION);
        copy.setExtensionId(ai.getExtensionId());
        if (ai.getAttributes() != null) {
            copy.setAttributes(ai.getAttributes().deepCopy());
        }
        apply(copy);

        try (AnthropicInferenceService service = factory.createService(copy)) {
            List<String> models = new ArrayList<>(service.getModels());
            String selected = modelCombo.getValue();
            if (selected != null && !models.contains(selected)) {
                models.add(0, selected);
            }
            // setItems clears the selection
            modelCombo.setItems(models);
            if (selected != null) {
                modelCombo.setValue(selected);
            }
        } catch (Exception e) {
            if (InferenceErrors.find(e) != null) {
                Notification.error(InferenceErrors.describe(loc, e), Notification.DURATION_LONGER);
            } else {
                UIUtils.showError(loc.getValue(L.MSG_FETCH_MODELS_FAILED), e);
            }
        }
    }
}
