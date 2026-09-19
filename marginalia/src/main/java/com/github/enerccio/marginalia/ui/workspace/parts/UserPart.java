package com.github.enerccio.marginalia.ui.workspace.parts;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.templates.MasterTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateData;
import com.github.enerccio.marginalia.domain.templates.UserPromptData;
import com.github.enerccio.marginalia.domain.traits.LocalizedTemplateDescription;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.TextAreaPopoverComponent;
import com.github.enerccio.marginalia.ui.widgets.TextFieldPopOverComponent;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.tabs.TabSheet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@Configurable
public class UserPart implements WorkspaceComponent {

    @Autowired
    private Localization loc;

    @Autowired
    private SettingService settingService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private TemplateService templateService;

    private final Workspace workspace;

    private VerticalLayout mainLayout;

    // Tab 1 fields
    private ComboBox<AI> defaultModelCombo;
    private ComboBox<Protocol> defaultProtocolCombo;

    // Tab 2 fields
    private TextAreaPopoverComponent masterTemplateField;
    private TextFieldPopOverComponent defaultPovField;
    private TextFieldPopOverComponent defaultTenseField;
    private TextAreaPopoverComponent defaultStyleField;
    private TextAreaPopoverComponent defaultUserPromptField;

    private UserSetting userSetting;

    public UserPart() {
        this(null);
    }

    public UserPart(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getName() {
        return loc.getValue(L.LABEL_SETTINGS);
    }

    @Override
    public Component create() throws Exception {
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Span titleSpan = new Span(loc.getValue(L.LABEL_SETTINGS));
        titleSpan.getStyle().set("font-size", "var(--lumo-font-size-l)");
        titleSpan.getStyle().set("font-weight", "bold");

        Button saveButton = new Button(loc.getValue(L.LABEL_SAVE), event -> save());
        saveButton.setThemeName("primary");

        headerLayout.add(titleSpan, saveButton);
        headerLayout.setFlexGrow(1, titleSpan);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        createFields();

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.getStyle().set("min-height", "0");

        FormLayout defaultsFormLayout = new FormLayout();
        defaultsFormLayout.setWidthFull();
        defaultsFormLayout.add(defaultModelCombo, defaultProtocolCombo);

        tabSheet.add(loc.getValue(L.LABEL_GENERAL_SETTINGS), defaultsFormLayout);

        FormLayout templatesFormLayout = new FormLayout();
        templatesFormLayout.setWidthFull();
        templatesFormLayout.getStyle().set("padding-bottom", "16px");

        templatesFormLayout.add(masterTemplateField);
        templatesFormLayout.setColspan(masterTemplateField, 2);

        templatesFormLayout.add(defaultPovField, defaultTenseField);

        templatesFormLayout.add(defaultStyleField);
        templatesFormLayout.setColspan(defaultStyleField, 2);

        templatesFormLayout.add(defaultUserPromptField);
        templatesFormLayout.setColspan(defaultUserPromptField, 2);

        tabSheet.add(loc.getValue(L.LABEL_TEMPLATES), templatesFormLayout);

        mainLayout.add(headerLayout, tabSheet);
        mainLayout.setFlexGrow(1, tabSheet);

        refresh();

        return mainLayout;
    }

    private void createFields() {
        defaultModelCombo = new ComboBox<>(loc.getValue(L.LABEL_DEFAULT_MODEL));
        defaultModelCombo.setWidthFull();
        defaultModelCombo.setItemLabelGenerator(AI::getName);
        defaultModelCombo.setClearButtonVisible(true);

        defaultProtocolCombo = new ComboBox<>(loc.getValue(L.LABEL_DEFAULT_PROTOCOL));
        defaultProtocolCombo.setWidthFull();
        defaultProtocolCombo.setItemLabelGenerator(Protocol::getName);
        defaultProtocolCombo.setClearButtonVisible(true);

        masterTemplateField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_MASTER_TEMPLATE));
        masterTemplateField.setWidthFull();
        masterTemplateField.setMinHeight("180px");
        masterTemplateField.setPlaceholder(Defaults.DEFAULT_MASTER_TEMPLATE);
        masterTemplateField.setPopoverContent(createTemplateHintPopoverContent(
                masterTemplateField, masterTemplateField.getPopover(), MasterTemplateData.class, Defaults.DEFAULT_MASTER_TEMPLATE));

        defaultUserPromptField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_USER_PROMPT));
        defaultUserPromptField.setWidthFull();
        defaultUserPromptField.setMinHeight("180px");
        defaultUserPromptField.setPlaceholder(Defaults.DEFAULT_USER_PROMPT);
        defaultUserPromptField.setPopoverContent(createTemplateHintPopoverContent(
                defaultUserPromptField, defaultUserPromptField.getPopover(), UserPromptData.class, Defaults.DEFAULT_USER_PROMPT));

        defaultStyleField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_STYLE));
        defaultStyleField.setWidthFull();
        defaultStyleField.setMinHeight("120px");
        defaultStyleField.setPlaceholder(Defaults.DEFAULT_STYLE);
        defaultStyleField.setPopoverContent(createTemplateHintPopoverContent(
                defaultStyleField, defaultStyleField.getPopover(), null, Defaults.DEFAULT_STYLE));

        defaultPovField = new TextFieldPopOverComponent(loc.getValue(L.LABEL_POV));
        defaultPovField.setWidthFull();
        defaultPovField.setPlaceholder(Defaults.DEFAULT_POV);
        defaultPovField.setPopoverContent(createTemplateHintPopoverContent(
                defaultPovField, defaultPovField.getPopover(), null, Defaults.DEFAULT_POV));

        defaultTenseField = new TextFieldPopOverComponent(loc.getValue(L.LABEL_TENSE));
        defaultTenseField.setWidthFull();
        defaultTenseField.setPlaceholder(Defaults.DEFAULT_TENSE);
        defaultTenseField.setPopoverContent(createTemplateHintPopoverContent(
                defaultTenseField, defaultTenseField.getPopover(), null, Defaults.DEFAULT_TENSE));
    }

    private Component createTemplateHintPopoverContent(HasValue<?, String> field, Popover popover, Class<? extends TemplateData> clazz, String defaultValue) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setWidth("380px");

        if (StringUtils.isNotBlank(defaultValue)) {
            Button fillDefaultButton = new Button(loc.getValue(L.LABEL_FILL_DEFAULT), event -> {
                field.setValue(defaultValue);
                if (popover != null) {
                    popover.close();
                }
            });
            fillDefaultButton.setThemeName("primary small");
            fillDefaultButton.setWidthFull();
            layout.add(fillDefaultButton);
        }

        if (clazz != null) {
            Span header = new Span(loc.getValue(L.LABEL_AVAILABLE_VARIABLES));
            header.getStyle().set("font-weight", "bold");
            header.getStyle().set("font-size", "var(--lumo-font-size-m)");
            layout.add(header);

            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                LocalizedTemplateDescription descAnnot = f.getAnnotation(LocalizedTemplateDescription.class);
                if (descAnnot == null) {
                    String getterName = "get" + StringUtils.capitalize(f.getName());
                    try {
                        Method method = clazz.getMethod(getterName);
                        descAnnot = method.getAnnotation(LocalizedTemplateDescription.class);
                    } catch (Exception ignored) {
                    }
                }

                String varName = f.getName();
                String descriptionText = "";
                if (descAnnot != null) {
                    descriptionText = loc.getValue(descAnnot.loc());
                }

                VerticalLayout itemLayout = new VerticalLayout();
                itemLayout.setPadding(false);
                itemLayout.setSpacing(false);

                Span varSpan = new Span("{{" + varName + "}}");
                varSpan.getStyle().set("font-family", "monospace");
                varSpan.getStyle().set("font-weight", "bold");
                varSpan.getStyle().set("color", "var(--lumo-primary-color)");

                Span descSpan = new Span(descriptionText);
                descSpan.getStyle().set("font-size", "var(--lumo-font-size-s)");
                descSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");

                itemLayout.add(varSpan, descSpan);
                layout.add(itemLayout);
            }
        }

        return layout;
    }

    @Override
    public void refresh() throws Exception {
        try {
            userSetting = settingService.getOrCreate(UserSetting.class);

            List<AI> allModels = aiService.findAllForUser();
            defaultModelCombo.setItems(allModels);

            Long defaultModelId = userSetting.getDefaultModel();
            if (defaultModelId != null) {
                AI selectedModel = aiService.find(defaultModelId);
                if (selectedModel != null && !selectedModel.isDeleted()) {
                    defaultModelCombo.setValue(selectedModel);
                } else {
                    defaultModelCombo.setValue(null);
                }
            } else {
                defaultModelCombo.setValue(null);
            }

            List<Protocol> allProtocols = protocolService.findAllForUser();
            defaultProtocolCombo.setItems(allProtocols);

            Long defaultProtocolId = userSetting.getDefaultProtocol();
            if (defaultProtocolId != null) {
                Protocol selectedProtocol = protocolService.find(defaultProtocolId);
                if (selectedProtocol != null && !selectedProtocol.isDeleted()) {
                    defaultProtocolCombo.setValue(selectedProtocol);
                } else {
                    defaultProtocolCombo.setValue(null);
                }
            } else {
                defaultProtocolCombo.setValue(null);
            }

            masterTemplateField.setValue(StringUtils.defaultString(userSetting.getMasterTemplate()));
            defaultPovField.setValue(StringUtils.defaultString(userSetting.getDefaultPov()));
            defaultTenseField.setValue(StringUtils.defaultString(userSetting.getDefaultTense()));
            defaultStyleField.setValue(StringUtils.defaultString(userSetting.getDefaultStyle()));
            defaultUserPromptField.setValue(StringUtils.defaultString(userSetting.getDefaultUserPrompt()));

        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void save() {
        if (userSetting == null) {
            return;
        }

        String masterTemplate = masterTemplateField.getValue();
        if (StringUtils.isNotBlank(masterTemplate)) {
            try {
                TemplateService.ValidationResult result = templateService.isValidTemplate(masterTemplate, "masterTemplate");
                if (!result.isValid()) {
                    Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + result.errorMessage());
                    return;
                }
            } catch (Exception e) {
                Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + e.getMessage());
                return;
            }
        }

        String userPrompt = defaultUserPromptField.getValue();
        if (StringUtils.isNotBlank(userPrompt)) {
            try {
                TemplateService.ValidationResult result = templateService.isValidTemplate(userPrompt, "defaultUserPrompt");
                if (!result.isValid()) {
                    Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + result.errorMessage());
                    return;
                }
            } catch (Exception e) {
                Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + e.getMessage());
                return;
            }
        }

        String defaultPov = defaultPovField.getValue();
        if (StringUtils.isNotBlank(defaultPov)) {
            try {
                TemplateService.ValidationResult result = templateService.isValidTemplate(defaultPov, "defaultPov");
                if (!result.isValid()) {
                    Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + result.errorMessage());
                    return;
                }
            } catch (Exception e) {
                Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + e.getMessage());
                return;
            }
        }

        String defaultTense = defaultTenseField.getValue();
        if (StringUtils.isNotBlank(defaultTense)) {
            try {
                TemplateService.ValidationResult result = templateService.isValidTemplate(defaultTense, "defaultTense");
                if (!result.isValid()) {
                    Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + result.errorMessage());
                    return;
                }
            } catch (Exception e) {
                Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + e.getMessage());
                return;
            }
        }

        String defaultStyle = defaultStyleField.getValue();
        if (StringUtils.isNotBlank(defaultStyle)) {
            try {
                TemplateService.ValidationResult result = templateService.isValidTemplate(defaultStyle, "defaultStyle");
                if (!result.isValid()) {
                    Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + result.errorMessage());
                    return;
                }
            } catch (Exception e) {
                Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + e.getMessage());
                return;
            }
        }

        try {
            AI selectedModel = defaultModelCombo.getValue();
            userSetting.setDefaultModel(selectedModel != null ? selectedModel.getId() : null);

            Protocol selectedProtocol = defaultProtocolCombo.getValue();
            userSetting.setDefaultProtocol(selectedProtocol != null ? selectedProtocol.getId() : null);

            userSetting.setMasterTemplate(masterTemplate);
            userSetting.setDefaultPov(defaultPov);
            userSetting.setDefaultTense(defaultTense);
            userSetting.setDefaultStyle(defaultStyle);
            userSetting.setDefaultUserPrompt(userPrompt);

            settingService.save(userSetting);
            Notification.success(loc.getValue(L.MSG_SETTINGS_SAVED));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @Override
    public void onTabSwitched() throws Exception {
        refresh();
    }

    @Override
    public void onTabClosed() throws Exception {

    }
}