package com.github.enerccio.marginalia.ui.workspace.parts.admin;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.service.DatabaseBackupService;
import com.github.enerccio.marginalia.domain.service.DatabaseBackupService.DatabaseBackup;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

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

        grid.addColumn(backup -> FileUtils.byteCountToDisplaySize(backup.getSize()))
                .setHeader(loc.getValue(L.LABEL_SIZE))
                .setFlexGrow(0)
                .setWidth("120px");

        grid.addComponentColumn(this::createActions)
                .setHeader("")
                .setFlexGrow(0)
                .setWidth("420px");

        layout.add(toolbar, pendingRestoreLayout, grid);
        layout.setFlexGrow(1, grid);

        refresh();

        return layout;
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
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
