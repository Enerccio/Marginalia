package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.BackupService;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupExport;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.BackupService.ManuscriptBackup;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookDecision;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.*;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
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
import com.vaadin.flow.server.streams.InputStreamDownloadCallback;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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

        grid.addColumn(ManuscriptBackup::getImageCount)
                .setHeader(loc.getValue(L.LABEL_IMAGES))
                .setFlexGrow(0)
                .setWidth("100px");

        grid.addComponentColumn(backup -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button restoreBtn = new Button(loc.getValue(L.LABEL_RESTORE_BACKUP), Solid.UNDO.create(), event -> openRestoreOptionsDialog(backup));
            restoreBtn.setThemeName("small primary");

            Button cloneBtn = new Button(loc.getValue(L.LABEL_CLONE_BACKUP), Solid.COPY.create(), event -> cloneBackup(backup));
            cloneBtn.setThemeName("small");

            Component exportComponent = createExport(backup);

            Button deleteBtn = new Button(Solid.TRASH.create(), event -> deleteBackup(backup));
            deleteBtn.setThemeName("small error");

            actions.add(restoreBtn, cloneBtn, exportComponent, deleteBtn);
            return actions;
        }).setHeader("").setFlexGrow(0).setWidth("450px");

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

    /**
     * A backup without images is downloaded right away, one with images asks first whether to pack them.
     */
    private Component createExport(ManuscriptBackup backup) {
        if (backup.getImageCount() == 0) {
            Button exportBtn = new Button(loc.getValue(L.LABEL_EXPORT_BACKUP), Solid.DOWNLOAD.create());
            exportBtn.setThemeName("small");
            return downloadOf(backup, exportBtn);
        }
        Button exportBtn = new Button(loc.getValue(L.LABEL_EXPORT_BACKUP), Solid.DOWNLOAD.create(), event -> askAboutImages(backup));
        exportBtn.setThemeName("small");
        return exportBtn;
    }

    /**
     * @return an anchor around the button that downloads the JSON of the backup
     */
    private Anchor downloadOf(ManuscriptBackup backup, Button button) {
        Anchor anchor = new Anchor(new InputStreamDownloadHandler((InputStreamDownloadCallback) downloadEvent -> {
            try {
                // a copy of the stored file, deleted when it's downloaded, so a big backup is never in memory
                BackupExport export = backupService.exportBackup(backup, false, null);
                return new DownloadResponse(new TempFileInputStream(export.file()), export.fileName(), export.mimeType(), export.file().length());
            } catch (Exception e) {
                throw new IOException(e);
            }
        }), "");
        anchor.getElement().setAttribute("download", true);
        anchor.add(button);
        return anchor;
    }

    private void askAboutImages(ManuscriptBackup backup) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(loc.getValue(L.LABEL_EXPORT_BACKUP));
        dialog.setWidth("500px");

        Button withImages = new Button(loc.getValue(L.LABEL_WITH_IMAGES), Solid.IMAGES.create(), event -> {
            dialog.close();
            packWithImages(backup);
        });
        withImages.setThemeName("primary");

        Button without = new Button(loc.getValue(L.LABEL_WITHOUT_IMAGES), Solid.FILE_CODE.create());
        // the download starts with the click, the dialog closes with it
        Anchor withoutImages = downloadOf(backup, without);
        without.addClickListener(event -> dialog.close());

        VerticalLayout content = new VerticalLayout(new Span(String.format(loc.getValue(L.MSG_BACKUP_EXPORT_IMAGES), backup.getImageCount())));
        content.setPadding(false);
        dialog.add(content);
        dialog.getFooter().add(new Button(loc.getValue(L.LABEL_CANCEL), event -> dialog.close()), withoutImages, withImages);
        dialog.open();
    }

    /**
     * Packs the images into a file on disk, one image at a time, and offers it for download.
     */
    private void packWithImages(ManuscriptBackup backup) {
        AtomicReference<BackupExport> packed = new AtomicReference<>();
        AtomicReference<Exception> failure = new AtomicReference<>();

        ProgressBarDialog packing = new ProgressBarDialog(true);
        packing.setTitle(loc.getValue(L.MSG_PACKING_BACKUP));
        packing.setTotal((long) backup.getImageCount());
        packing.create();
        packing.setAction(dialog -> {
            try {
                packed.set(backupService.exportBackup(backup, true, dialog::updateProgress));
            } catch (Exception e) {
                failure.set(e);
            }
        });
        packing.setAfterAction(() -> {
            if (failure.get() != null) {
                UIUtils.internalServerError(loc, failure.get());
            } else {
                showPacked(packed.get());
            }
        });
        packing.open();
    }

    private void showPacked(BackupExport export) {
        Dialog result = new Dialog();
        result.setHeaderTitle(loc.getValue(L.LABEL_EXPORT_BACKUP));
        result.setCloseOnEsc(true);
        result.setCloseOnOutsideClick(false);
        result.setModality(ModalityMode.STRICT);

        Anchor download = new Anchor(new InputStreamDownloadHandler((InputStreamDownloadCallback) event ->
                new DownloadResponse(new FileInputStream(export.file()), export.fileName(), export.mimeType(), export.file().length())), "");
        download.getElement().setAttribute("download", true);
        Button downloadButton = new Button(loc.getValue(L.LABEL_DOWNLOAD) + " " + export.fileName(), Solid.DOWNLOAD.create());
        downloadButton.setThemeName("primary");
        download.add(downloadButton);

        VerticalLayout content = new VerticalLayout(new Span(String.format(loc.getValue(L.MSG_BACKUP_READY), export.fileName())), download);
        if (export.missingImages() > 0) {
            Span missing = new Span(String.format(loc.getValue(L.MSG_BACKUP_IMAGES_MISSING), export.missingImages()));
            missing.getStyle().set("color", "var(--lumo-error-text-color)");
            content.add(missing);
        }
        content.setPadding(false);
        result.add(content);
        result.getFooter().add(new Button(loc.getValue(L.LABEL_CLOSE), event -> result.close()));
        // the file is kept only as long as the dialog is open
        result.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                FileUtils.deleteQuietly(export.file());
            }
        });
        result.open();
    }

    /**
     * Deletes the file when it has been read.
     */
    private static final class TempFileInputStream extends FileInputStream {
        private final File file;

        private TempFileInputStream(File file) throws IOException {
            super(file);
            this.file = file;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                FileUtils.deleteQuietly(file);
            }
        }
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

        // a backup can be big (and have images), it is read from a file and not held in memory
        Upload upload = new Upload(UploadHandler.toTempFile((metadata, file) -> {
            try {
                Manuscript manuscript = parent.refreshManuscript();
                backupService.importBackup(manuscript, file);
                dialog.close();
                refreshBackups();
            } catch (IllegalArgumentException e) {
                Notification.warning(e.getMessage());
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            } finally {
                FileUtils.deleteQuietly(file);
            }
        }));
        upload.setAcceptedFileExtensions(".json", ".zip");

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
            try {
                LorebookImportDialog.resolve(backupService.analyzeLorebooks(backup),
                        decisions -> performRestore(backup, false, decisions));
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
        fullRestoreBtn.setThemeName("primary");
        fullRestoreBtn.setWidthFull();

        Button messagesOnlyBtn = new Button(loc.getValue(L.LABEL_MESSAGES_ONLY_RESTORE), Solid.COMMENT_ALT.create(), event -> {
            dialog.close();
            performRestore(backup, true, null);
        });
        messagesOnlyBtn.setWidthFull();

        layout.add(msgSpan, fullRestoreBtn, messagesOnlyBtn);
        dialog.add(layout);

        Button cancelBtn = new Button(loc.getValue(L.LABEL_CANCEL), event -> dialog.close());
        dialog.getFooter().add(cancelBtn);

        dialog.open();
    }

    private void performRestore(ManuscriptBackup backup, boolean messagesOnly, Map<String, LorebookDecision> lorebookDecisions) {
        try {
            Manuscript current = parent.refreshManuscript();
            Manuscript restored = backupService.applyBackup(current, backup, messagesOnly, lorebookDecisions);
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
            if (backup.getImageCount() == 0) {
                cloneBackup(backup, newName, false);
                return;
            }
            ConfirmDialog.show(String.format(loc.getValue(L.MSG_CLONE_BACKUP_IMAGES), backup.getImageCount()),
                    loc.getValue(L.LABEL_WITH_IMAGES), loc.getValue(L.LABEL_WITHOUT_IMAGES),
                    () -> cloneBackup(backup, newName, true), () -> cloneBackup(backup, newName, false), true);
        }).messageRequired().showCancel(true).build();
        dialog.open();
    }

    private void cloneBackup(ManuscriptBackup backup, String newName, boolean withImages) {
        try {
            LorebookImportDialog.resolve(backupService.analyzeLorebooks(backup), decisions -> {
                try {
                    Manuscript cloned = backupService.cloneBackup(backup, newName, decisions, withImages);
                    Notification.success(loc.getValue(L.LABEL_CLONE_BACKUP) + ": " + cloned.getName());
                    refreshBackups();
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            });
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
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