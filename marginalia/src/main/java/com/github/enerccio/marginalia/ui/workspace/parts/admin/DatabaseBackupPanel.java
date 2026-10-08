package com.github.enerccio.marginalia.ui.workspace.parts.admin;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.service.CronSchedule;
import com.github.enerccio.marginalia.domain.service.DatabaseBackupService;
import com.github.enerccio.marginalia.domain.service.DatabaseBackupService.BackupSchedule;
import com.github.enerccio.marginalia.domain.service.DatabaseBackupService.DatabaseBackup;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Configurable
@Extendable
public class DatabaseBackupPanel {

    @Autowired
    private Localization loc;

    @Autowired
    private DatabaseBackupService databaseBackupService;

    private Span databaseSizeSpan;
    private HorizontalLayout pendingRestoreLayout;
    private Grid<DatabaseBackup> grid;
    private Checkbox scheduleEnabled;
    private TextField scheduleCron;
    private IntegerField scheduleKeep;
    private Span schedulePreview;
    private Span scheduleStatus;

    public Component create() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);

        Button createButton = new Button(loc.getValue(L.LABEL_CREATE_BACKUP), Solid.DATABASE.create(), e -> createBackup());
        createButton.setThemeName("primary");

        Upload upload = new Upload(UploadHandler.toTempFile((metadata, file) -> {
            try {
                databaseBackupService.importBackup(metadata.fileName(), file);
                refresh();
            } catch (IllegalArgumentException ex) {
                Notification.warning(loc.getValue(L.MSG_INVALID_DATABASE_FILE));
            } catch (Exception ex) {
                UIUtils.internalServerError(loc, ex);
            } finally {
                FileUtils.deleteQuietly(file);
            }
        }));
        upload.setAcceptedFileExtensions(".sqlite", ".db");
        upload.setUploadButton(new Button(loc.getValue(L.LABEL_UPLOAD_BACKUP), Solid.UPLOAD.create()));
        upload.setDropLabel(new Span());

        Button refreshButton = new Button(loc.getValue(L.LABEL_REFRESH), Solid.REFRESH.create(), e -> refresh());

        databaseSizeSpan = new Span();

        toolbar.add(createButton, upload, refreshButton, databaseSizeSpan);

        pendingRestoreLayout = new HorizontalLayout();
        pendingRestoreLayout.setWidthFull();
        pendingRestoreLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        pendingRestoreLayout.getStyle().set("background-color", "var(--lumo-warning-color-10pct)");
        pendingRestoreLayout.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        pendingRestoreLayout.getStyle().set("padding", "var(--lumo-space-s)");
        Span pendingSpan = new Span(loc.getValue(L.MSG_DATABASE_RESTORE_PENDING));
        Button cancelRestoreButton = new Button(loc.getValue(L.LABEL_CANCEL_RESTORE), Solid.TIMES.create(), e -> {
            try {
                databaseBackupService.cancelRestore();
                refresh();
            } catch (Exception ex) {
                UIUtils.internalServerError(loc, ex);
            }
        });
        cancelRestoreButton.setThemeName("small");
        pendingRestoreLayout.add(pendingSpan, cancelRestoreButton);
        pendingRestoreLayout.setFlexGrow(1, pendingSpan);
        pendingRestoreLayout.setVisible(false);

        grid = new Grid<>();
        grid.setSizeFull();

        grid.addColumn(DatabaseBackup::getName)
                .setHeader(loc.getValue(L.LABEL_NAME))
                .setFlexGrow(1);

        grid.addColumn(backup -> loc.getDateHourFormat().format(backup.getCreation()))
                .setHeader(loc.getValue(L.LABEL_CREATED))
                .setFlexGrow(0)
                .setWidth("200px");

        grid.addColumn(backup -> loc.getValue(backup.isScheduled() ? L.LABEL_BACKUP_SCHEDULED : L.LABEL_BACKUP_MANUAL))
                .setHeader(loc.getValue(L.LABEL_TYPE))
                .setFlexGrow(0)
                .setWidth("120px");

        grid.addColumn(backup -> FileUtils.byteCountToDisplaySize(backup.getSize()))
                .setHeader(loc.getValue(L.LABEL_SIZE))
                .setFlexGrow(0)
                .setWidth("120px");

        grid.addComponentColumn(this::createActions)
                .setHeader("")
                .setFlexGrow(0)
                .setWidth("420px");

        layout.add(toolbar, pendingRestoreLayout, createSchedule(), grid);
        layout.setFlexGrow(1, grid);

        refresh();
        try {
            loadSchedule();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }

        return layout;
    }

    private Component createSchedule() {
        scheduleEnabled = new Checkbox(loc.getValue(L.LABEL_BACKUP_SCHEDULE_ENABLED));

        scheduleCron = new TextField(loc.getValue(L.LABEL_BACKUP_SCHEDULE));
        scheduleCron.setHelperText(loc.getValue(L.HELP_BACKUP_SCHEDULE));
        scheduleCron.setWidth("26em");
        scheduleCron.setValueChangeMode(ValueChangeMode.LAZY);
        scheduleCron.addValueChangeListener(e -> previewSchedule());

        scheduleKeep = new IntegerField(loc.getValue(L.LABEL_BACKUP_KEEP));
        scheduleKeep.setHelperText(loc.getValue(L.HELP_BACKUP_KEEP));
        scheduleKeep.setMin(0);
        scheduleKeep.setStepButtonsVisible(true);
        scheduleKeep.setWidth("14em");

        Button saveButton = new Button(loc.getValue(L.LABEL_SAVE_SCHEDULE), Solid.SAVE.create(), e -> saveSchedule());
        saveButton.setThemeName("primary");

        HorizontalLayout fields = new HorizontalLayout(scheduleEnabled, scheduleCron, scheduleKeep, saveButton);
        fields.setAlignItems(FlexComponent.Alignment.BASELINE);
        fields.setWrap(true);

        schedulePreview = new Span();
        schedulePreview.getStyle().set("color", "var(--lumo-secondary-text-color)");
        scheduleStatus = new Span();

        VerticalLayout content = new VerticalLayout(fields, schedulePreview, scheduleStatus);
        content.setPadding(false);
        content.setSpacing(false);

        Details details = new Details(loc.getValue(L.LABEL_SCHEDULED_BACKUPS), content);
        details.setOpened(true);
        details.setWidthFull();
        return details;
    }

    private void loadSchedule() throws Exception {
        BackupSchedule schedule = databaseBackupService.getSchedule();
        scheduleEnabled.setValue(schedule.enabled());
        scheduleCron.setValue(schedule.cron());
        scheduleKeep.setValue(schedule.keep());
        previewSchedule();
        showScheduleStatus(schedule);
    }

    /**
     * Validates the typed expression and shows when it would run.
     */
    private boolean previewSchedule() {
        try {
            CronSchedule schedule = CronSchedule.parse(scheduleCron.getValue());
            List<ZonedDateTime> next = schedule.next(ZonedDateTime.now(schedule.getZone()), 3);
            scheduleCron.setInvalid(false);
            schedulePreview.setText(String.format(loc.getValue(L.MSG_BACKUP_SCHEDULE_NEXT), next.stream()
                    .map(t -> loc.getDateHourFormat().format(Date.from(t.toInstant())))
                    .collect(Collectors.joining(", "))));
            return true;
        } catch (IllegalArgumentException e) {
            scheduleCron.setErrorMessage(String.format(loc.getValue(L.MSG_BACKUP_SCHEDULE_INVALID), e.getMessage()));
            scheduleCron.setInvalid(true);
            schedulePreview.setText("");
            return false;
        }
    }

    private void showScheduleStatus(BackupSchedule schedule) {
        Date next = databaseBackupService.getNextScheduledBackup();
        if (!schedule.enabled() || next == null) {
            scheduleStatus.setText(loc.getValue(L.MSG_BACKUP_SCHEDULE_OFF));
            return;
        }
        String last = schedule.lastRun() == null ? loc.getValue(L.LABEL_NEVER) : loc.getDateHourFormat().format(schedule.lastRun());
        scheduleStatus.setText(String.format(loc.getValue(L.MSG_BACKUP_SCHEDULE_STATUS), last, loc.getDateHourFormat().format(next)));
    }

    private void saveSchedule() {
        if (!previewSchedule()) {
            return;
        }
        Integer keep = scheduleKeep.getValue();
        if (keep == null || keep < 0) {
            scheduleKeep.setInvalid(true);
            return;
        }
        scheduleKeep.setInvalid(false);
        try {
            showScheduleStatus(databaseBackupService.updateSchedule(scheduleEnabled.getValue(), scheduleCron.getValue(), keep));
            Notification.success(loc.getValue(L.MSG_BACKUP_SCHEDULE_SAVED));
        } catch (SecurityException e) {
            Notification.warning(loc.getValue(L.MSG_ADMIN_ONLY));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private Component createActions(DatabaseBackup backup) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Anchor downloadAnchor;
        try {
            downloadAnchor = new Anchor(DownloadHandler.forFile(databaseBackupService.getBackupFile(backup)), "");
        } catch (Exception e) {
            return actions;
        }
        downloadAnchor.getElement().setAttribute("download", true);
        Button downloadButton = new Button(loc.getValue(L.LABEL_DOWNLOAD), Solid.DOWNLOAD.create());
        downloadButton.setThemeName("small");
        downloadAnchor.add(downloadButton);

        Button restoreButton = new Button(loc.getValue(L.LABEL_RESTORE_ON_RESTART), Solid.UNDO.create(), e ->
                ConfirmDialog.show(String.format(loc.getValue(L.MSG_CONFIRM_DATABASE_RESTORE), backup.getName()), () -> {
                    try {
                        databaseBackupService.scheduleRestore(backup);
                        refresh();
                    } catch (Exception ex) {
                        UIUtils.internalServerError(loc, ex);
                    }
                }));
        restoreButton.setThemeName("small");

        Button deleteButton = new Button(Solid.TRASH.create(), e ->
                ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
                    try {
                        databaseBackupService.deleteBackup(backup);
                        refresh();
                    } catch (Exception ex) {
                        UIUtils.internalServerError(loc, ex);
                    }
                }));
        deleteButton.setThemeName("small error");

        actions.add(downloadAnchor, restoreButton, deleteButton);
        return actions;
    }

    private void createBackup() {
        try {
            databaseBackupService.createBackup();
            Notification.success(loc.getValue(L.MSG_DATABASE_BACKUP_CREATED));
            refresh();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    public void refresh() {
        if (grid == null) {
            return;
        }
        try {
            grid.setItems(databaseBackupService.getBackups());
            databaseSizeSpan.setText(String.format(loc.getValue(L.LABEL_DATABASE_SIZE),
                    FileUtils.byteCountToDisplaySize(databaseBackupService.getDatabaseSize())));
            pendingRestoreLayout.setVisible(databaseBackupService.isRestorePending());
            // fields keep unsaved edits, only the status (last/next run) is refreshed
            showScheduleStatus(databaseBackupService.getSchedule());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
