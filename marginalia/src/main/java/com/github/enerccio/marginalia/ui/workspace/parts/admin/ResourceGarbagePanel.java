package com.github.enerccio.marginalia.ui.workspace.parts.admin;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.ResourceGarbageService;
import com.github.enerccio.marginalia.domain.service.ResourceGarbageService.DeleteResult;
import com.github.enerccio.marginalia.domain.service.ResourceGarbageService.StoredFile;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.streams.DownloadHandler;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Garbage collection of the files of resources ({@link ResourceGarbageService}): the scan lists the files that no resource
 * points to, all checked, to download as a ZIP and to delete from the disk.
 */
@Configurable
@Extendable
public class ResourceGarbagePanel implements LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private ResourceGarbageService resourceGarbageService;

    private Grid<StoredFile> grid;
    private Span summary;
    private Anchor zipAnchor;
    private Button zipButton;
    private Button deleteButton;
    private Button selectAllButton;
    private Button selectNoneButton;
    private Button closeButton;
    private List<StoredFile> unused = List.of();
    private int scanned;
    private String found = "";

    /**
     * The checked files for the download, which runs in a request of its own and must not touch the grid.
     */
    private volatile List<StoredFile> checked = List.of();

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "layout", type = VerticalLayout.class),
                    @APILocals(name = "description", type = Span.class),
                    @APILocals(name = "toolbar", type = HorizontalLayout.class),
                    @APILocals(name = "scanButton", type = Button.class),
                    @APILocals(name = "selection", type = GridMultiSelectionModel.class)})
    public Component create() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        Span description = new Span(loc.getValue(L.MSG_RESOURCE_FILES_DESCRIPTION));
        description.addClassName(CssStyles.TEXT_SECONDARY);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);

        Button scanButton = new Button(loc.getValue(L.LABEL_SCAN_FILES), Solid.SEARCH.create(), e -> scan());
        scanButton.setThemeName("primary");

        selectAllButton = new Button(loc.getValue(L.LABEL_SELECT_ALL), e -> grid.asMultiSelect().select(unused));
        selectNoneButton = new Button(loc.getValue(L.LABEL_SELECT_NONE), e -> grid.asMultiSelect().deselectAll());

        zipButton = new Button(loc.getValue(L.LABEL_DOWNLOAD_AS_ZIP), Solid.FILE_ARCHIVE.create());
        zipAnchor = new Anchor(zipHandler(), "");
        zipAnchor.getElement().setAttribute("download", true);
        zipAnchor.add(zipButton);

        deleteButton = new Button(loc.getValue(L.LABEL_DELETE_SELECTED), Solid.TRASH.create(), e -> deleteSelected());
        deleteButton.setThemeName("primary error");

        closeButton = new Button(loc.getValue(L.LABEL_CLOSE), e -> show(List.of(), 0));

        summary = new Span();
        summary.addClassName(CssStyles.TEXT_SECONDARY);

        toolbar.add(scanButton, selectAllButton, selectNoneButton, zipAnchor, deleteButton, closeButton, summary);

        grid = new Grid<>();
        grid.setSizeFull();
        GridMultiSelectionModel<StoredFile> selection = (GridMultiSelectionModel<StoredFile>) grid.setSelectionMode(Grid.SelectionMode.MULTI);
        selection.setSelectAllCheckboxVisibility(GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
        grid.addSelectionListener(e -> selectionChanged());

        grid.addColumn(StoredFile::path)
                .setHeader(loc.getValue(L.LABEL_FILE))
                .setFlexGrow(1);
        grid.addColumn(file -> FileUtils.byteCountToDisplaySize(file.size()))
                .setHeader(loc.getValue(L.LABEL_SIZE))
                .setComparator(StoredFile::size)
                .setFlexGrow(0)
                .setWidth("120px");
        grid.addColumn(file -> new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(file.lastModified())))
                .setHeader(loc.getValue(L.LABEL_LAST_MODIFIED))
                .setComparator(StoredFile::lastModified)
                .setFlexGrow(0)
                .setWidth("180px");
        grid.addComponentColumn(this::createDownload)
                .setHeader("")
                .setFlexGrow(0)
                .setWidth("100px");

        layout.add(description, toolbar, grid);
        layout.setFlexGrow(1, grid);
        show(List.of(), 0);
        return layout;
    }

    /**
     * The ZIP of the checked files, made while it is streamed.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = DownloadHandler.class))
    private DownloadHandler zipHandler() {
        return event -> {
            event.setFileName("unused-resource-files-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".zip");
            event.setContentType("application/zip");
            try {
                resourceGarbageService.writeZip(checked, event.getOutputStream());
            } catch (java.io.IOException e) {
                throw e;
            } catch (Exception e) {
                throw new java.io.IOException(e);
            }
        };
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "file", type = StoredFile.class)},
            locals = {@APILocals(name = "ioFile", type = File.class),
                    @APILocals(name = "e", type = Exception.class),
                    @APILocals(name = "anchor", type = Anchor.class),
                    @APILocals(name = "button", type = Button.class)})
    private Component createDownload(StoredFile file) {
        File ioFile;
        try {
            ioFile = resourceGarbageService.getFile(file);
        } catch (Exception e) {
            return new Span();
        }
        Anchor anchor = new Anchor(DownloadHandler.forFile(ioFile), "");
        anchor.getElement().setAttribute("download", true);
        Button button = new Button(Solid.DOWNLOAD.create());
        button.setThemeName("tertiary");
        UIUtils.addTooltip(button, loc.getValue(L.LABEL_DOWNLOAD));
        anchor.add(button);
        return anchor;
    }

    /**
     * Reads the folders of all users in a progress dialog, then asks which of the files nothing points to.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "folders", type = List.class),
                    @APILocals(name = "e", type = Exception.class),
                    @APILocals(name = "result", type = AtomicReference.class),
                    @APILocals(name = "count", type = AtomicInteger.class),
                    @APILocals(name = "failure", type = AtomicReference.class),
                    @APILocals(name = "progress", type = ProgressBarDialog.class)})
    private void scan() {
        List<String> folders;
        try {
            folders = resourceGarbageService.userFolders();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        AtomicReference<List<StoredFile>> result = new AtomicReference<>();
        AtomicInteger count = new AtomicInteger();
        AtomicReference<Exception> failure = new AtomicReference<>();

        ProgressBarDialog progress = new ProgressBarDialog(true, true);
        progress.setTitle(loc.getValue(L.MSG_SCANNING_RESOURCE_FILES));
        progress.setTotal((long) folders.size() + 1);
        progress.create();
        progress.setAction(dialog -> {
            try {
                List<StoredFile> all = new ArrayList<>();
                for (String folder : folders) {
                    if (dialog.isCancelled()) {
                        return;
                    }
                    all.addAll(resourceGarbageService.listFiles(folder));
                    dialog.updateProgress();
                }
                result.set(resourceGarbageService.findUnused(all));
                count.set(all.size());
                dialog.updateProgress();
            } catch (Exception e) {
                failure.set(e);
            }
        });
        progress.setAfterAction(() -> {
            if (failure.get() != null) {
                UIUtils.internalServerError(loc, failure.get());
            } else if (result.get() != null) {
                show(result.get(), count.get());
            }
        });
        progress.open();
    }

    /**
     * Puts the files into the table, all checked.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "files", type = List.class),
                    @APIParameter(name = "scanned", type = int.class)})
    private void show(List<StoredFile> files, int scanned) {
        unused = files;
        this.scanned = scanned;
        grid.setItems(files);
        grid.asMultiSelect().select(files);
        closeButton.setEnabled(!files.isEmpty());
        selectAllButton.setEnabled(!files.isEmpty());
        selectNoneButton.setEnabled(!files.isEmpty());
        found = files.isEmpty() && scanned == 0 ? "" : String.format(loc.getValue(L.MSG_RESOURCE_FILES_FOUND),
                files.size(), scanned, FileUtils.byteCountToDisplaySize(sum(files)));
        selectionChanged();
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "selected", type = Set.class)})
    private void selectionChanged() {
        Set<StoredFile> selected = grid.getSelectedItems();
        // the order of the table, not the one of the set
        checked = unused.stream().filter(selected::contains).toList();
        boolean any = !checked.isEmpty();
        zipAnchor.setEnabled(any);
        zipButton.setEnabled(any);
        deleteButton.setEnabled(any);
        summary.setText(found.isEmpty() ? "" : any ? found + " " + String.format(loc.getValue(L.MSG_RESOURCE_FILES_SELECTED),
                checked.size(), FileUtils.byteCountToDisplaySize(sum(checked))) : found);
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "files", type = List.class)})
    private void deleteSelected() {
        List<StoredFile> files = checked;
        if (files.isEmpty()) {
            return;
        }
        ConfirmDialog.show(String.format(loc.getValue(L.MSG_CONFIRM_DELETE_RESOURCE_FILES),
                files.size(), FileUtils.byteCountToDisplaySize(sum(files))), () -> doDelete(files));
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "files", type = List.class)},
            locals = {@APILocals(name = "result", type = DeleteResult.class),
                    @APILocals(name = "remaining", type = List.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void doDelete(List<StoredFile> files) {
        try {
            DeleteResult result = resourceGarbageService.delete(files);
            Notification.success(String.format(loc.getValue(L.MSG_RESOURCE_FILES_DELETED),
                    result.deleted(), FileUtils.byteCountToDisplaySize(result.bytes())));
            if (result.skipped() > 0) {
                Notification.warning(String.format(loc.getValue(L.MSG_RESOURCE_FILES_SKIPPED), result.skipped()));
            }
            // what was kept is not listed either: it is in use or gone, a new scan tells more
            List<StoredFile> remaining = unused.stream().filter(file -> !files.contains(file)).toList();
            show(remaining, scanned);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private static long sum(List<StoredFile> files) {
        long total = 0;
        for (StoredFile file : files) {
            total += file.size();
        }
        return total;
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class))
    public void refresh() {
        // reading all the folders is expensive, it runs only on demand
    }
}
