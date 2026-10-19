package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.service.AgentVirtualFileSystemService;
import com.github.enerccio.marginalia.domain.service.agent.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.TextInputDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.InputStreamDownloadCallback;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Looks at the workspace of an agent, the files it keeps between its runs (its notes and reports): browse the folders,
 * read a text file, download a file, upload a text file or an image, create a folder, delete a file or a folder, or
 * clear the whole workspace to get rid of stale notes. Goes through the same cage as the agent's own file tools, so
 * there is nothing here but the workspace, and the same limits and rules of names hold for what the user puts in (an
 * uploaded file has to be text, as the agent can only read text, or an image the agent can be shown). Files can't be
 * changed while the agent works, the run would lose them under its hands.
 */
@Configurable
public class AgentWorkspaceBrowser extends VerticalLayout implements LLMInteract {

    /**
     * How much of a text file is shown, the rest is for the download.
     */
    static final int VIEW_LIMIT = 2_000;

    @Autowired
    private Localization loc;

    @Autowired
    private AgentVirtualFileSystemService fileSystemService;

    private final Agent agent;
    private final BooleanSupplier busy;

    private VirtualPath folder = VirtualPath.ROOT;
    private VirtualFileSystem files;

    private Span pathLabel;
    private Span usageLabel;
    private Span noteLabel;
    private Button upButton;
    private Button clearButton;
    private Button newFolderButton;
    private Upload upload;
    private Grid<VirtualEntry> grid;
    private TextArea viewer;

    /**
     * @param agent the agent whose workspace it is
     * @param busy  whether the agent works now, the files are then only to look at
     */
    /**
     * What the workspace may hold, with the quotas of the owner; the default limits when they can't be read (only the
     * size of the upload box depends on it, the file system checks every file).
     */
    private WorkspaceLimits limits() {
        try {
            return fileSystemService.getLimits(agent);
        } catch (Exception e) {
            return fileSystemService.getLimits();
        }
    }

    public AgentWorkspaceBrowser(Agent agent, BooleanSupplier busy) {
        this.agent = agent;
        this.busy = busy;
    }

    public void create() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        // the list and the viewer share the height of the tab and scroll by themselves, the tab does not scroll over them
        addClassNames(CssStyles.WORKSPACE_BROWSER, CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_AUTO);

        upButton = new Button(loc.getValue(L.LABEL_UP), Solid.ARROW_UP.create(), event -> {
            folder = folder.isRoot() ? folder : folder.getParent();
            viewer.clear();
            refresh();
        });
        Button refreshButton = new Button(loc.getValue(L.LABEL_REFRESH), Solid.ARROWS_ROTATE.create(), event -> refresh());
        clearButton = new Button(loc.getValue(L.LABEL_CLEAR), Solid.TRASH.create(), event ->
                ConfirmDialog.open(loc.getValue(L.MSG_AGENT_CONFIRM_CLEAR_WORKSPACE), this::clear, () -> {}));
        clearButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        newFolderButton = new Button(loc.getValue(L.LABEL_NEW_FOLDER), Solid.FOLDER_PLUS.create(), event ->
                new TextInputDialog.Builder(loc.getValue(L.LABEL_NEW_FOLDER), this::createFolder)
                        .inputMessageLabel(loc.getValue(L.LABEL_FOLDER_NAME)).messageRequired().showCancel(true)
                        .build().open());
        for (Button button : List.of(upButton, refreshButton, newFolderButton, clearButton)) {
            button.addThemeVariants(ButtonVariant.LUMO_SMALL);
        }
        Button uploadButton = new Button(loc.getValue(L.LABEL_UPLOAD_FILE), Solid.UPLOAD.create());
        uploadButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        upload = new Upload(new InMemoryUploadHandler((metadata, data) -> upload(metadata.fileName(), data)));
        WorkspaceLimits limits = limits();
        upload.setMaxFileSize((int) Math.min(Integer.MAX_VALUE, Math.max(limits.maxFileBytes(), limits.maxImageBytes())));
        upload.setUploadButton(uploadButton);
        upload.setDropLabel(new Span());
        upload.addFileRejectedListener(event -> Notification.warning(
                String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), event.getErrorMessage())));
        pathLabel = new Span();
        pathLabel.addClassNames(CssStyles.TEXT_MONOSPACE, CssStyles.WRAP_ANYWHERE);
        HorizontalLayout toolbar = new HorizontalLayout(upButton, refreshButton, pathLabel, newFolderButton, upload,
                clearButton);
        toolbar.setWidthFull();
        toolbar.setWrap(true);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.expand(pathLabel);

        grid = new Grid<>();
        grid.setWidthFull();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.addComponentColumn(this::nameCell).setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(3);
        grid.addColumn(entry -> entry.directory() ? "" : FileUtils.byteCountToDisplaySize(entry.size()))
                .setHeader(loc.getValue(L.LABEL_SIZE)).setFlexGrow(1);
        grid.addComponentColumn(this::actions).setHeader("").setFlexGrow(0).setWidth("120px");
        grid.addItemClickListener(event -> open(event.getItem()));

        noteLabel = new Span();
        noteLabel.addClassNames(CssStyles.HINT_TEXT);
        viewer = new TextArea();
        viewer.setReadOnly(true);
        viewer.setWidthFull();
        viewer.addClassName(CssStyles.TEXT_MONOSPACE);
        usageLabel = new Span();
        usageLabel.addClassNames(CssStyles.HINT_TEXT);

        add(toolbar, grid, noteLabel, viewer, usageLabel);
        refresh();
    }

    private Component nameCell(VirtualEntry entry) {
        Span name = new Span(entry.path().getName());
        name.addClassName(CssStyles.WRAP_ANYWHERE);
        HorizontalLayout cell = new HorizontalLayout((entry.directory() ? Solid.FOLDER : Solid.FILE).create(), name);
        cell.setAlignItems(FlexComponent.Alignment.CENTER);
        cell.setSpacing(true);
        return cell;
    }

    private Component actions(VirtualEntry entry) {
        Button deleteButton = new Button(Solid.TRASH.create(), event ->
                ConfirmDialog.open(loc.getValue(L.MSG_CONFIRM_DELETE), () -> delete(entry), () -> {}));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.setTooltipText(loc.getValue(busy.getAsBoolean() ? L.MSG_AGENT_WORKSPACE_BUSY : L.LABEL_DELETE));
        deleteButton.setEnabled(!busy.getAsBoolean());
        if (entry.directory()) {
            return deleteButton;
        }
        Button downloadButton = new Button(Solid.DOWNLOAD.create());
        downloadButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        downloadButton.setTooltipText(loc.getValue(L.LABEL_DOWNLOAD));
        HorizontalLayout actions = new HorizontalLayout(downloadAnchor(entry, downloadButton), deleteButton);
        actions.setSpacing(false);
        return actions;
    }

    private Anchor downloadAnchor(VirtualEntry entry, Button button) {
        Anchor anchor = new Anchor(new InputStreamDownloadHandler((InputStreamDownloadCallback) downloadEvent -> {
            try {
                byte[] content = fileSystemService.openWorkspace(agent).read(entry.path());
                return new DownloadResponse(new ByteArrayInputStream(content), entry.path().getName(),
                        "application/octet-stream", content.length);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }), "");
        anchor.getElement().setAttribute("download", true);
        anchor.add(button);
        return anchor;
    }

    /**
     * A folder is entered, a text file is shown.
     */
    void open(VirtualEntry entry) {
        if (entry.directory()) {
            folder = entry.path();
            viewer.clear();
            refresh();
            return;
        }
        try {
            byte[] content = files.read(entry.path());
            String text = new String(content, StandardCharsets.UTF_8);
            if (text.indexOf('�') >= 0 || text.indexOf('\0') >= 0) {
                viewer.clear();
                noteLabel.setText(loc.getValue(L.MSG_AGENT_FILE_NOT_TEXT));
                return;
            }
            boolean cut = text.length() > VIEW_LIMIT;
            viewer.setValue(cut ? text.substring(0, VIEW_LIMIT) : text);
            noteLabel.setText(cut ? String.format(loc.getValue(L.MSG_AGENT_FILE_CUT), VIEW_LIMIT, text.length()) : "");
        } catch (VirtualFileException e) {
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
            refresh();
        }
    }

    void delete(VirtualEntry entry) {
        try {
            files.delete(entry.path(), true);
            viewer.clear();
        } catch (VirtualFileException e) {
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
        }
        refresh();
    }

    /**
     * Creates a folder in the open one; the name follows the rules of the names of the agent.
     */
    void createFolder(String name) {
        if (isBusy()) {
            return;
        }
        try {
            files.createFolder(folder.resolve(name.trim()));
        } catch (VirtualFileException e) {
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
        }
        refresh();
    }

    /**
     * Puts an uploaded file in the open folder. It is text (what the agent reads) or an image (what it can be shown),
     * within the limits of the workspace, and a file of that name is not replaced.
     */
    void upload(String fileName, byte[] content) {
        if (isBusy()) {
            return;
        }
        try {
            VirtualPath target = folder.resolve(fileName);
            if (files.stat(target) != null) {
                throw new VirtualFileException(target + " exists already; delete it first or rename the file");
            }
            if (ImageFiles.isImage(content)) {
                files.writeImage(target, content);
                return;
            }
            String text;
            try {
                text = VirtualFileSystem.decodeText(content, target);
            } catch (VirtualFileException e) {
                Notification.warning(String.format(loc.getValue(L.MSG_AGENT_UPLOAD_NOT_TEXT), fileName));
                return;
            }
            files.writeText(target, text, false);
        } catch (VirtualFileException e) {
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
        } finally {
            upload.clearFileList();
            refresh();
        }
    }

    private boolean isBusy() {
        if (busy.getAsBoolean()) {
            Notification.warning(loc.getValue(L.MSG_AGENT_WORKSPACE_BUSY));
            refresh();
            return true;
        }
        return false;
    }

    void clear() {
        try {
            fileSystemService.clearWorkspace(agent);
            folder = VirtualPath.ROOT;
            viewer.clear();
        } catch (IllegalStateException | VirtualFileException e) {
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        refresh();
    }

    /**
     * Reads the folder again, and the buttons that depend on whether the agent works.
     */
    public void refresh() {
        if (grid == null) {
            return;
        }
        try {
            files = fileSystemService.openWorkspace(agent);
            List<VirtualEntry> entries;
            try {
                entries = files.list(folder);
            } catch (VirtualFileException e) {
                // the folder is gone (deleted here, or by the agent meanwhile)
                folder = VirtualPath.ROOT;
                entries = files.list(folder);
            }
            grid.setItems(entries);
            pathLabel.setText("/" + String.join("/", folder.getSegments()));
            upButton.setEnabled(!folder.isRoot());
            clearButton.setEnabled(!busy.getAsBoolean());
            clearButton.setTooltipText(busy.getAsBoolean() ? loc.getValue(L.MSG_AGENT_WORKSPACE_BUSY) : null);
            newFolderButton.setEnabled(!busy.getAsBoolean());
            newFolderButton.setTooltipText(busy.getAsBoolean() ? loc.getValue(L.MSG_AGENT_WORKSPACE_BUSY) : null);
            upload.setEnabled(!busy.getAsBoolean());
            WorkspaceUsage usage = files.getUsage();
            usageLabel.setText(entries.isEmpty() && usage.files() == 0
                    ? loc.getValue(L.MSG_AGENT_WORKSPACE_EMPTY)
                    : String.format(loc.getValue(L.MSG_AGENT_WORKSPACE_USAGE), usage.files(),
                    FileUtils.byteCountToDisplaySize(usage.bytes()),
                    FileUtils.byteCountToDisplaySize(files.getLimits().maxTotalBytes())));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // for the tests of the browser

    Grid<VirtualEntry> getGrid() {
        return grid;
    }

    TextArea getViewer() {
        return viewer;
    }

    Span getUsageLabel() {
        return usageLabel;
    }

    Button getClearButton() {
        return clearButton;
    }

    Button getNewFolderButton() {
        return newFolderButton;
    }

    Upload getUpload() {
        return upload;
    }

    Button getUpButton() {
        return upButton;
    }

    VirtualPath getFolder() {
        return folder;
    }
}
