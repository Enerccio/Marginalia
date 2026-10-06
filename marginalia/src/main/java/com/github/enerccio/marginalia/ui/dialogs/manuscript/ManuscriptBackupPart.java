package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.BackupService;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.BackupService.ManuscriptBackup;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.TextInputDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.InputStreamDownloadCallback;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configurable
@Extendable
public class ManuscriptBackupPart implements ManuscriptDialogPart {

    @Autowired
    private Localization loc;

    @Autowired
    private BackupService backupService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SettingService settingService;

    private final ManuscriptDialog parent;
    private Grid<ManuscriptBackup> grid;
    private Button takeBackupBtn;
    private Button importBackupBtn;
    private Button refreshBtn;

    private Checkbox perManuscriptCheckbox;
    private ComboBox<BackupStrategy> backupStrategyCombo;
    private IntegerField backupStrategyValueField;
    private boolean loadingStrategy = false;

    public ManuscriptBackupPart(ManuscriptDialog parent) {
        this.parent = parent;
    }

    @Override
    public Component create(VTabSheet container) throws Exception {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        FormLayout strategyForm = new FormLayout();
        strategyForm.setWidthFull();

        perManuscriptCheckbox = new Checkbox(loc.getValue(L.LABEL_PER_MANUSCRIPT_BACKUP));

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

        perManuscriptCheckbox.addValueChangeListener(e -> {
            if (loadingStrategy) return;
            if (Boolean.TRUE.equals(e.getValue())) {
                backupStrategyCombo.setReadOnly(false);
                saveManuscriptStrategy();
            } else {
                try {
                    Manuscript manuscript = parent.refreshManuscript();
                    manuscript.setBackupStrategy(null);
                    manuscript.setBackupStrategyValue(null);
                    parent.save();
                    loadBackupStrategyUI();
                } catch (Exception ex) {
                    UIUtils.internalServerError(loc, ex);
                }
            }
        });

        backupStrategyCombo.addValueChangeListener(e -> {
            if (loadingStrategy) return;
            updateStrategyValueFieldState(e.getValue());
            if (perManuscriptCheckbox.getValue()) {
                saveManuscriptStrategy();
            }
        });

        backupStrategyValueField.addValueChangeListener(e -> {
            if (loadingStrategy) return;
            if (e.isFromClient() && perManuscriptCheckbox.getValue()) {
                saveManuscriptStrategy();
            }
        });

        strategyForm.add(perManuscriptCheckbox, new HorizontalLayout(backupStrategyCombo, backupStrategyValueField));

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);

        takeBackupBtn = new Button(loc.getValue(L.LABEL_TAKE_BACKUP), Solid.PLUS.create(), event -> takeBackup());
        takeBackupBtn.setThemeName("primary");

        importBackupBtn = new Button(loc.getValue(L.LABEL_IMPORT_BACKUP), Solid.FILE_IMPORT.create(), event -> openImportDialog());

        refreshBtn = new Button(loc.getValue(L.LABEL_REFRESH), Solid.REFRESH.create(), event -> refreshBackups());

        toolbar.add(takeBackupBtn, importBackupBtn, refreshBtn);

        grid = new Grid<>(ManuscriptBackup.class, false);
        grid.setSizeFull();

        grid.addColumn(backup -> backup.getBackupCreationDate() != null
                        ? loc.getDateHourFormat().format(backup.getBackupCreationDate())
                        : "")
                .setHeader(loc.getValue(L.LABEL_BACKUP_DATE))
                .setFlexGrow(1);

        grid.addColumn(ManuscriptBackup::getTotalMessagesCount)
                .setHeader(loc.getValue(L.LABEL_MESSAGES_COUNT))
                .setFlexGrow(0)
                .setWidth("120px");

        grid.addComponentColumn(backup -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button restoreBtn = new Button(loc.getValue(L.LABEL_RESTORE_BACKUP), Solid.UNDO.create(), event -> openRestoreOptionsDialog(backup));
            restoreBtn.setThemeName("small primary");

            Button cloneBtn = new Button(loc.getValue(L.LABEL_CLONE_BACKUP), Solid.COPY.create(), event -> cloneBackup(backup));
            cloneBtn.setThemeName("small");

            Anchor exportAnchor = new Anchor(new InputStreamDownloadHandler((InputStreamDownloadCallback) downloadEvent -> {
                try {
                    ManuscriptBackup b = backup;
                    if (!b.isLoaded()) {
                        b = backupService.loadBackup(b);
                    }
                    byte[] data = b.getBackup() != null ? backupService.serializeBackup(b).getBytes(StandardCharsets.UTF_8) : new byte[0];
                    return new DownloadResponse(new ByteArrayInputStream(data),
                            getBackupFileName(backup), "application/json", data.length);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }), "");
            exportAnchor.getElement().setAttribute("download", true);
            Button exportBtn = new Button(loc.getValue(L.LABEL_EXPORT_BACKUP), Solid.DOWNLOAD.create());
            exportBtn.setThemeName("small");
            exportAnchor.add(exportBtn);

            Button deleteBtn = new Button(Solid.TRASH.create(), event -> deleteBackup(backup));
            deleteBtn.setThemeName("small error");

            actions.add(restoreBtn, cloneBtn, exportAnchor, deleteBtn);
            return actions;
        }).setHeader("").setFlexGrow(0).setWidth("380px");

        mainLayout.add(strategyForm, toolbar, grid);
        mainLayout.setFlexGrow(1, grid);

        container.add(loc.getValue(L.LABEL_BACKUPS), mainLayout);
        return mainLayout;
    }

    private void updateStrategyValueFieldState(BackupStrategy strategy) {
        if (strategy == null || strategy == BackupStrategy.DISABLED) {
            backupStrategyValueField.setVisible(false);
        } else if (strategy == BackupStrategy.AFTER_N_MESSAGES) {
            backupStrategyValueField.setVisible(true);
            backupStrategyValueField.setLabel(loc.getValue(L.LABEL_BACKUP_STRATEGY_VALUE_MESSAGES));
        } else if (strategy == BackupStrategy.AFTER_N_MINUTES) {
            backupStrategyValueField.setVisible(true);
            backupStrategyValueField.setLabel(loc.getValue(L.LABEL_BACKUP_STRATEGY_VALUE_MINUTES));
        }
        if (!perManuscriptCheckbox.getValue()) {
            backupStrategyValueField.setReadOnly(true);
        } else {
            backupStrategyValueField.setReadOnly(strategy == null);
        }
    }

    private void loadBackupStrategyUI() {
        loadingStrategy = true;
        try {
            Manuscript manuscript = parent.refreshManuscript();
            UserSetting userSetting = settingService.getOrCreate(UserSetting.class);

            boolean hasOverride = manuscript.getBackupStrategy() != null || manuscript.getBackupStrategyValue() != null;
            perManuscriptCheckbox.setValue(hasOverride);

            BackupStrategy strategy;
            String valStr;

            if (hasOverride) {
                strategy = manuscript.getBackupStrategy();
                valStr = manuscript.getBackupStrategyValue();
                backupStrategyCombo.setReadOnly(false);
            } else {
                strategy = userSetting.getBackupStrategy();
                valStr = userSetting.getBackupStrategyValue();
                backupStrategyCombo.setReadOnly(true);
            }

            backupStrategyCombo.setValue(strategy);
            if (StringUtils.isNotBlank(valStr) && StringUtils.isNumeric(valStr)) {
                backupStrategyValueField.setValue(Integer.parseInt(valStr));
            } else {
                backupStrategyValueField.setValue(1);
            }

            updateStrategyValueFieldState(strategy);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        } finally {
            loadingStrategy = false;
        }
    }

    private void saveManuscriptStrategy() {
        try {
            Manuscript manuscript = parent.refreshManuscript();
            BackupStrategy strategy = backupStrategyCombo.getValue();
            manuscript.setBackupStrategy(strategy);
            manuscript.setBackupStrategyValue(strategy != null && backupStrategyValueField.getValue() != null ? String.valueOf(backupStrategyValueField.getValue()) : null);
            parent.save();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private String getBackupFileName(ManuscriptBackup backup) {
        String name = backup.getManuscriptName();
        if (StringUtils.isBlank(name)) {
            name = "manuscript";
        }
        return name.replaceAll("[^a-zA-Z0-9.-]", "_") + "_backup.json";
    }

    private void takeBackup() {
        try {
            Manuscript manuscript = parent.refreshManuscript();
            backupService.takeBackup(manuscript);
            refreshBackups();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void openImportDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(loc.getValue(L.LABEL_IMPORT_BACKUP));
        dialog.setWidth("450px");

        InMemoryUploadHandler handler = new InMemoryUploadHandler((metadata, data) -> {
            try {
                Manuscript manuscript = parent.refreshManuscript();
                backupService.importBackup(manuscript, data);
                dialog.close();
                refreshBackups();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });

        Upload upload = new Upload(handler);
        upload.setAcceptedMimeTypes("application/json");

        Button cancelBtn = new Button(loc.getValue(L.LABEL_CANCEL), event -> dialog.close());

        VerticalLayout layout = new VerticalLayout(upload);
        layout.setPadding(true);

        dialog.add(layout);
        dialog.getFooter().add(cancelBtn);
        dialog.open();
    }

    private void openRestoreOptionsDialog(ManuscriptBackup backup) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(loc.getValue(L.LABEL_RESTORE_OPTION_TITLE));
        dialog.setWidth("450px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        Span msgSpan = new Span(loc.getValue(L.LABEL_RESTORE_OPTION_MSG));

        Button fullRestoreBtn = new Button(loc.getValue(L.LABEL_FULL_RESTORE), Solid.CHECK_DOUBLE.create(), event -> {
            dialog.close();
            performRestore(backup, false);
        });
        fullRestoreBtn.setThemeName("primary");
        fullRestoreBtn.setWidthFull();

        Button messagesOnlyBtn = new Button(loc.getValue(L.LABEL_MESSAGES_ONLY_RESTORE), Solid.COMMENT_ALT.create(), event -> {
            dialog.close();
            performRestore(backup, true);
        });
        messagesOnlyBtn.setWidthFull();

        layout.add(msgSpan, fullRestoreBtn, messagesOnlyBtn);
        dialog.add(layout);

        Button cancelBtn = new Button(loc.getValue(L.LABEL_CANCEL), event -> dialog.close());
        dialog.getFooter().add(cancelBtn);

        dialog.open();
    }

    private void performRestore(ManuscriptBackup backup, boolean messagesOnly) {
        try {
            Manuscript current = parent.refreshManuscript();
            Manuscript restored = backupService.applyBackup(current, backup, messagesOnly);
            Runnable onClose = parent.getOnClose();

            parent.close();

            Manuscript updated = manuscriptService.find(restored.getId());
            ManuscriptDialog newDialog = new ManuscriptDialog(updated);
            newDialog.setOnClose(onClose);
            newDialog.create();
            newDialog.open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void cloneBackup(ManuscriptBackup backup) {
        TextInputDialog dialog = new TextInputDialog.Builder(loc.getValue(L.LABEL_NEW_MANUSCRIPT_NAME), newName -> {
            try {
                Manuscript cloned = backupService.cloneBackup(backup, newName);
                Notification.success(loc.getValue(L.LABEL_CLONE_BACKUP) + ": " + cloned.getName());
                refreshBackups();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }).messageRequired().showCancel(true).build();
        dialog.open();
    }

    private void deleteBackup(ManuscriptBackup backup) {
        ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
            try {
                backupService.deleteBackup(backup);
                refreshBackups();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
    }

    private void refreshBackups() {
        try {
            Manuscript manuscript = parent.refreshManuscript();
            if (manuscript != null) {
                List<ManuscriptBackup> backups = backupService.getBackups(manuscript);
                grid.setItems(backups != null ? backups : new ArrayList<>());
            }
            loadBackupStrategyUI();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @Override
    public void setFrozen(boolean frozen) {
        // no need
    }

    @Override
    public void load(Manuscript manuscript) {
        refreshBackups();
    }

    @Override
    public void onTabLeave() throws Exception {

    }

    @Override
    public void onTabEnter() throws Exception {
        refreshBackups();
    }
}