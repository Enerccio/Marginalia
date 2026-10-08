package com.github.enerccio.marginalia.ui.workspace.parts;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.templates.MasterTemplateData;
import com.github.enerccio.marginalia.domain.templates.SummaryTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateData;
import com.github.enerccio.marginalia.domain.templates.UserPromptData;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.LorebookImportDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.TemplateHints;
import com.github.enerccio.marginalia.ui.widgets.TextAreaPopoverComponent;
import com.github.enerccio.marginalia.ui.widgets.TextFieldPopOverComponent;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

@Configurable
@Extendable
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

    @Autowired
    private BackupService backupService;

    private final Workspace workspace;

    private VerticalLayout mainLayout;

    // Tab 1 fields
    private ComboBox<AI> defaultModelCombo;
    private ComboBox<Protocol> defaultProtocolCombo;
    private ComboBox<BackupStrategy> backupStrategyCombo;
    private IntegerField backupStrategyValueField;

    // Tab 2 fields
    private TextAreaPopoverComponent masterTemplateField;
    private TextFieldPopOverComponent defaultPovField;
    private TextFieldPopOverComponent defaultTenseField;
    private TextAreaPopoverComponent defaultStyleField;
    private TextAreaPopoverComponent defaultUserPromptField;
    private TextAreaPopoverComponent defaultSummaryPromptField;

    // Tab 3 Extensions
    private Accordion extensionSettings;

    private UserSetting userSetting;

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

        Button importBackupAsNewButton = new Button(loc.getValue(L.LABEL_IMPORT_BACKUP_AS_NEW), event -> openImportBackupAsNewDialog());

        Button saveButton = new Button(loc.getValue(L.LABEL_SAVE), event -> save());
        saveButton.setThemeName("primary");

        headerLayout.add(titleSpan, importBackupAsNewButton, saveButton);
        headerLayout.setFlexGrow(1, titleSpan);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        createFields();

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.getStyle().set("min-height", "0");

        FormLayout defaultsFormLayout = new FormLayout();
        defaultsFormLayout.setWidthFull();
        defaultsFormLayout.add(defaultModelCombo, defaultProtocolCombo);
        defaultsFormLayout.add(new HorizontalLayout(backupStrategyCombo, backupStrategyValueField));

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

        templatesFormLayout.add(defaultSummaryPromptField);
        templatesFormLayout.setColspan(defaultSummaryPromptField, 2);

        tabSheet.add(loc.getValue(L.LABEL_TEMPLATES), templatesFormLayout);

        extensionSettings = new Accordion();
        extensionSettings.setSizeFull();
        tabSheet.add(loc.getValue(L.LABEL_EXTENSION_SETTINGS), extensionSettings);

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

        backupStrategyCombo = new ComboBox<>(loc.getValue(L.LABEL_BACKUP_STRATEGY));
        backupStrategyCombo.setWidthFull();
        backupStrategyCombo.setItems(BackupStrategy.values());
        backupStrategyCombo.setClearButtonVisible(true);
        backupStrategyCombo.setItemLabelGenerator(s -> {
            if (s == null) {
                return loc.getValue(L.ENUM_BACKUP_STRATEGY_NONE);
            }
            return loc.getValue(loc.getBackupStrategy(s));
        });

        backupStrategyValueField = new IntegerField(loc.getValue(L.LABEL_BACKUP_STRATEGY_VALUE_MESSAGES));
        backupStrategyValueField.setWidthFull();
        backupStrategyValueField.setMin(1);
        backupStrategyValueField.setStepButtonsVisible(true);

        backupStrategyCombo.addValueChangeListener(e -> updateBackupStrategyValueField(e.getValue()));

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

        defaultSummaryPromptField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_SUMMARY_PROMPT));
        defaultSummaryPromptField.setWidthFull();
        defaultSummaryPromptField.setMinHeight("120px");
        defaultSummaryPromptField.setPlaceholder(Defaults.DEFAULT_SUMMARY_PROMPT);
        defaultSummaryPromptField.setPopoverContent(createTemplateHintPopoverContent(
                defaultSummaryPromptField, defaultSummaryPromptField.getPopover(), SummaryTemplateData.class, Defaults.DEFAULT_SUMMARY_PROMPT));
    }

    private void updateBackupStrategyValueField(BackupStrategy strategy) {
        if (strategy == null || strategy == BackupStrategy.DISABLED) {
            backupStrategyValueField.setVisible(false);
        } else if (strategy == BackupStrategy.AFTER_N_MESSAGES) {
            backupStrategyValueField.setVisible(true);
            backupStrategyValueField.setLabel(loc.getValue(L.LABEL_BACKUP_STRATEGY_VALUE_MESSAGES));
        } else if (strategy == BackupStrategy.AFTER_N_MINUTES) {
            backupStrategyValueField.setVisible(true);
            backupStrategyValueField.setLabel(loc.getValue(L.LABEL_BACKUP_STRATEGY_VALUE_MINUTES));
        }
    }

    private Component createTemplateHintPopoverContent(HasValue<?, String> field, Popover popover, Class<? extends TemplateData> clazz, String defaultValue) {
        return TemplateHints.create(loc, field, popover, clazz, defaultValue);
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

            BackupStrategy strategy = userSetting.getBackupStrategy();
            backupStrategyCombo.setValue(strategy);
            String stratVal = userSetting.getBackupStrategyValue();
            if (StringUtils.isNotBlank(stratVal) && StringUtils.isNumeric(stratVal)) {
                backupStrategyValueField.setValue(Integer.parseInt(stratVal));
            } else {
                backupStrategyValueField.setValue(1);
            }
            updateBackupStrategyValueField(strategy);

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

        BackupStrategy strategy = backupStrategyCombo.getValue();
        if (strategy != null && (backupStrategyValueField.getValue() == null || backupStrategyValueField.getValue() < 1)) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
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

            userSetting.setBackupStrategy(strategy);
            userSetting.setBackupStrategyValue(strategy != null && backupStrategyValueField.getValue() != null ? String.valueOf(backupStrategyValueField.getValue()) : null);

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

    private void openImportBackupAsNewDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(loc.getValue(L.LABEL_IMPORT_BACKUP_AS_NEW));
        dialog.setWidth("450px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        TextField nameField = new TextField(loc.getValue(L.LABEL_NEW_MANUSCRIPT_NAME));
        nameField.setRequired(true);
        nameField.setWidthFull();

        InMemoryUploadHandler handler = new InMemoryUploadHandler((metadata, data) -> {
            String newName = nameField.getValue();
            if (StringUtils.isBlank(newName)) {
                Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
                return;
            }
            try {
                LorebookImportDialog.resolve(backupService.analyzeLorebooks(data), decisions -> {
                    try {
                        Manuscript newManuscript = backupService.restoreAsNewManuscript(data, newName, decisions);
                        dialog.close();
                        Notification.success(loc.getValue(L.LABEL_IMPORT_BACKUP) + ": " + newManuscript.getName());
                    } catch (Exception e) {
                        UIUtils.internalServerError(loc, e);
                    }
                });
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });

        Upload upload = new Upload(handler);
        upload.setAcceptedMimeTypes("application/json");

        layout.add(nameField, upload);
        dialog.add(layout);

        Button cancelBtn = new Button(loc.getValue(L.LABEL_CANCEL), event -> dialog.close());
        dialog.getFooter().add(cancelBtn);

        dialog.open();
    }

    @Override
    public void onTabSwitched() throws Exception {
        refresh();
    }

    @Override
    public void onTabClosed() throws Exception {

    }
}
