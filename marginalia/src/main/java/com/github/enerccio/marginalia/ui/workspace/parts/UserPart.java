package com.github.enerccio.marginalia.ui.workspace.parts;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.templates.*;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.LorebookImportDialog;
import com.github.enerccio.marginalia.ui.widgets.*;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
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
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.streams.UploadHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Configurable
@Extendable
public class UserPart implements WorkspaceComponent {

    private static final String CHANGE_TRACKED_KEY = UserPart.class.getName() + ".changeTracked";

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
    private Span unsavedChangesSpan;
    private Button discardButton;

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
    private TextAreaPopoverComponent defaultMetaSummaryPromptField;

    // Tab 3 Extensions
    private Accordion extensionSettings;

    private UserSetting userSetting;

    // true while refresh() (and the extension decorators around it) fills the fields
    private boolean loading;
    private boolean changed;

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

        unsavedChangesSpan = new Span(loc.getValue(L.MSG_UNSAVED_SETTINGS));
        unsavedChangesSpan.getStyle().set("color", "var(--lumo-error-text-color)");
        unsavedChangesSpan.setVisible(false);

        Button importBackupAsNewButton = new Button(loc.getValue(L.LABEL_IMPORT_BACKUP_AS_NEW), event -> openImportBackupAsNewDialog());

        discardButton = new Button(loc.getValue(L.LABEL_DISCARD_CHANGES), event -> discard());
        discardButton.setEnabled(false);

        Button saveButton = new Button(loc.getValue(L.LABEL_SAVE), event -> save());
        saveButton.setThemeName("primary");

        headerLayout.add(titleSpan, unsavedChangesSpan, importBackupAsNewButton, discardButton, saveButton);
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

        templatesFormLayout.add(defaultMetaSummaryPromptField);
        templatesFormLayout.setColspan(defaultMetaSummaryPromptField, 2);

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
        ResizableTextArea.install(loc, masterTemplateField, "240px");
        masterTemplateField.setPlaceholder(Defaults.DEFAULT_MASTER_TEMPLATE);
        masterTemplateField.setPopoverContent(createTemplateHintPopoverContent(
                masterTemplateField, masterTemplateField.getPopover(), MasterTemplateData.class, Defaults.DEFAULT_MASTER_TEMPLATE));

        defaultUserPromptField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_USER_PROMPT));
        defaultUserPromptField.setWidthFull();
        ResizableTextArea.install(loc, defaultUserPromptField, "240px");
        defaultUserPromptField.setPlaceholder(Defaults.DEFAULT_USER_PROMPT);
        defaultUserPromptField.setPopoverContent(createTemplateHintPopoverContent(
                defaultUserPromptField, defaultUserPromptField.getPopover(), UserPromptData.class, Defaults.DEFAULT_USER_PROMPT));

        defaultStyleField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_STYLE));
        defaultStyleField.setWidthFull();
        ResizableTextArea.install(loc, defaultStyleField, "160px");
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
        ResizableTextArea.install(loc, defaultSummaryPromptField, "160px");
        defaultSummaryPromptField.setPlaceholder(Defaults.DEFAULT_SUMMARY_PROMPT);
        defaultSummaryPromptField.setPopoverContent(createTemplateHintPopoverContent(
                defaultSummaryPromptField, defaultSummaryPromptField.getPopover(), SummaryTemplateData.class, Defaults.DEFAULT_SUMMARY_PROMPT));

        defaultMetaSummaryPromptField = new TextAreaPopoverComponent(loc.getValue(L.LABEL_META_SUMMARY_PROMPT));
        defaultMetaSummaryPromptField.setWidthFull();
        ResizableTextArea.install(loc, defaultMetaSummaryPromptField, "160px");
        defaultMetaSummaryPromptField.setPlaceholder(Defaults.DEFAULT_META_SUMMARY);
        defaultMetaSummaryPromptField.setPopoverContent(createTemplateHintPopoverContent(
                defaultMetaSummaryPromptField, defaultMetaSummaryPromptField.getPopover(), MetaSummaryTemplateData.class, Defaults.DEFAULT_META_SUMMARY));
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
        loading = true;
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
            defaultSummaryPromptField.setValue(StringUtils.defaultString(userSetting.getDefaultSummaryPrompt()));
            defaultMetaSummaryPromptField.setValue(StringUtils.defaultString(userSetting.getDefaultMetaSummaryPrompt()));
            validateTemplate(masterTemplateField, "masterTemplate", MasterTemplateData.class);
            validateTemplate(defaultUserPromptField, "defaultUserPrompt", UserPromptData.class);
            validateTemplate(defaultSummaryPromptField, "defaultSummaryPrompt", SummaryTemplateData.class);
            validateTemplate(defaultMetaSummaryPromptField, "defaultMetaSummaryPrompt", MetaSummaryTemplateData.class);

        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        } finally {
            finishLoading();
        }
    }

    /**
     * Extension decorators fill their settings forms after refresh() returns, so the fields are tracked (and the
     * loaded values accepted as unchanged) only before the response is sent.
     */
    private void finishLoading() {
        mainLayout.getElement().getNode().runWhenAttached(ui -> ui.beforeClientResponse(mainLayout, ctx -> {
            trackChanges(mainLayout);
            loading = false;
            setChanged(false);
        }));
    }

    /**
     * Listens to every field of the part, including the extension settings forms. Any change outside of refresh()
     * locks the workspace navigation until the settings are saved or discarded.
     */
    private void trackChanges(Component component) {
        if (component instanceof HasValue<?, ?> field && ComponentUtil.getData(component, CHANGE_TRACKED_KEY) == null) {
            ComponentUtil.setData(component, CHANGE_TRACKED_KEY, Boolean.TRUE);
            field.addValueChangeListener(e -> {
                if (!loading) {
                    setChanged(true);
                }
            });
        }
        component.getChildren().forEach(this::trackChanges);
    }

    private void setChanged(boolean changed) {
        if (this.changed == changed) {
            return;
        }
        this.changed = changed;
        unsavedChangesSpan.setVisible(changed);
        discardButton.setEnabled(changed);
        workspace.setNavigationLocked(changed);
    }

    private void discard() {
        try {
            refresh();
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

        if (!validateTemplate(masterTemplateField, "masterTemplate", MasterTemplateData.class)
                || !validateTemplate(defaultUserPromptField, "defaultUserPrompt", UserPromptData.class)
                || !validateTemplate(defaultSummaryPromptField, "defaultSummaryPrompt", SummaryTemplateData.class)
                || !validateTemplate(defaultMetaSummaryPromptField, "defaultMetaSummaryPrompt", MetaSummaryTemplateData.class)) {
            return;
        }

        String masterTemplate = masterTemplateField.getValue();
        String userPrompt = defaultUserPromptField.getValue();
        String summaryPrompt = defaultSummaryPromptField.getValue();
        String metaSummaryPrompt = defaultMetaSummaryPromptField.getValue();

        String defaultPov = defaultPovField.getValue();
        String defaultTense = defaultTenseField.getValue();
        String defaultStyle = defaultStyleField.getValue();

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
            userSetting.setDefaultSummaryPrompt(summaryPrompt);
            userSetting.setDefaultMetaSummaryPrompt(metaSummaryPrompt);

            settingService.save(userSetting);
            setChanged(false);
            Notification.success(loc.getValue(L.MSG_SETTINGS_SAVED));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * @return false if the template is invalid and must not be saved; unknown names are only shown as a warning
     */
    private boolean validateTemplate(TextAreaPopoverComponent field, String templateName, Class<? extends TemplateData> dataClass) {
        String template = field.getValue();
        if (StringUtils.isBlank(template)) {
            TemplateHints.showWarnings(loc, field, null);
            return true;
        }
        try {
            TemplateService.ValidationResult result = templateService.isValidTemplate(template, templateName, dataClass);
            if (!result.isValid()) {
                Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + result.errorMessage());
                return false;
            }
            TemplateHints.showWarnings(loc, field, result);
            return true;
        } catch (Exception e) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE_EXT) + e.getMessage());
            return false;
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
        nameField.setValueChangeMode(ValueChangeMode.EAGER);

        // the backup is read from a file, it can be big and have images; the file is needed until the lorebooks are
        // resolved (a dialog), so it's deleted when the restore is done or the dialog is closed
        AtomicReference<File> uploaded = new AtomicReference<>();
        dialog.addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                FileUtils.deleteQuietly(uploaded.getAndSet(null));
            }
        });

        Upload upload = new Upload(UploadHandler.toFile((metadata, file) -> {
            FileUtils.deleteQuietly(uploaded.getAndSet(file));
            String newName = nameField.getValue();
            if (StringUtils.isBlank(newName)) {
                Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
                return;
            }
            try {
                LorebookImportDialog.resolve(backupService.analyzeLorebooks(file), decisions -> {
                    try {
                        Manuscript newManuscript = backupService.restoreAsNewManuscript(file, newName, decisions);
                        dialog.close();
                        Notification.success(loc.getValue(L.LABEL_IMPORT_BACKUP) + ": " + newManuscript.getName());
                    } catch (IllegalArgumentException e) {
                        Notification.warning(e.getMessage());
                    } catch (Exception e) {
                        UIUtils.internalServerError(loc, e);
                    }
                });
            } catch (IllegalArgumentException e) {
                Notification.warning(e.getMessage());
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }, metadata -> {
            File file = Files.createTempFile("marginalia-backup-", ".upload").toFile();
            file.deleteOnExit();
            return file;
        }));
        upload.setAcceptedFileExtensions(".json", ".zip");
        // the name is needed when the upload arrives, so the file can be picked only after it's entered
        upload.setEnabled(false);
        nameField.addValueChangeListener(e -> upload.setEnabled(StringUtils.isNotBlank(e.getValue())));

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
