package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.service.AgentVirtualFileSystemService;
import com.github.enerccio.marginalia.domain.service.agent.*;
import com.github.enerccio.marginalia.domain.traits.*;
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
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * Looks at the workspace of an agent, the files it keeps between its runs (its notes and reports): browse the folders,
 * read a text file, download a file, upload a text file or an image, create a folder, delete a file or a folder, or
 * clear the whole workspace to get rid of stale notes. Goes through the same cage as the agent's own file tools, so
 * there is nothing here but the workspace, and the same limits and rules of names hold for what the user puts in (an
 * uploaded file has to be text, as the agent can only read text, an image the agent can be shown, or a zip file the
 * agent can unpack). Files can't be
 * changed while the agent works, the run would lose them under its hands.
 */
@Configurable
@Extendable
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
    // a phone has the buttons of the toolbar as icons and the size of a file under its name
    private boolean phone = UIUtils.isPhone();
    // what the grid shows now, to leave it alone while nothing changed
    private String shownRows;
    private TextArea viewer;

    /**
     * @param agent the agent whose workspace it is
     * @param busy  whether the agent works now, the files are then only to look at
     */
    /**
     * What the workspace may hold, with the quotas of the owner; the default limits when they can't be read (only the
     * size of the upload box depends on it, the file system checks every file).
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = WorkspaceLimits.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
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

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "refreshButton", type = Button.class),
                    @APILocals(name = "button", type = Button.class),
                    @APILocals(name = "uploadButton", type = Button.class),
                    @APILocals(name = "limits", type = WorkspaceLimits.class),
                    @APILocals(name = "entry", type = Map.Entry.class),
                    @APILocals(name = "toolbar", type = HorizontalLayout.class)})
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
        upload.setMaxFileSize((int) Math.min(Integer.MAX_VALUE, limits.maxAnyFileBytes()));
        upload.setUploadButton(uploadButton);
        upload.setDropLabel(new Span());
        upload.addFileRejectedListener(event -> Notification.warning(
                String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), event.getErrorMessage())));
        pathLabel = new Span();
        pathLabel.addClassNames(CssStyles.TEXT_MONOSPACE, CssStyles.WRAP_ANYWHERE);
        if (phone) {
            for (Map.Entry<Button, L> entry : Map.of(upButton, L.LABEL_UP, refreshButton, L.LABEL_REFRESH,
                    newFolderButton, L.LABEL_NEW_FOLDER, uploadButton, L.LABEL_UPLOAD_FILE, clearButton, L.LABEL_CLEAR).entrySet()) {
                entry.getKey().setText("");
                entry.getKey().addThemeVariants(ButtonVariant.LUMO_ICON);
                entry.getKey().setAriaLabel(loc.getValue(entry.getValue()));
                entry.getKey().setTooltipText(loc.getValue(entry.getValue()));
            }
        }
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
        if (!phone) {
            grid.addColumn(this::sizeText).setHeader(loc.getValue(L.LABEL_SIZE)).setFlexGrow(1);
        }
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

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "entry", type = VirtualEntry.class)})
    private String sizeText(VirtualEntry entry) {
        return entry.directory() ? "" : FileUtils.byteCountToDisplaySize(entry.size());
    }

    /**
     * Whether to make the browser of a phone: the default is the screen of the user, set it before {@link #create()}.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "phone", type = boolean.class)})
    void setPhone(boolean phone) {
        this.phone = phone;
    }

    // package visible for the tests
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "entry", type = VirtualEntry.class)},
            locals = {@APILocals(name = "name", type = Span.class),
                    @APILocals(name = "cell", type = HorizontalLayout.class),
                    @APILocals(name = "size", type = Span.class)})
    Component nameCell(VirtualEntry entry) {
        Span name = new Span(entry.path().getName());
        name.addClassName(CssStyles.WRAP_ANYWHERE);
        HorizontalLayout cell = new HorizontalLayout((entry.directory() ? Solid.FOLDER : Solid.FILE).create(), name);
        cell.setAlignItems(FlexComponent.Alignment.CENTER);
        cell.setSpacing(true);
        if (phone && !entry.directory()) {
            Span size = new Span(sizeText(entry));
            size.addClassName(CssStyles.HINT_TEXT);
            cell.add(size);
        }
        return cell;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "entry", type = VirtualEntry.class)},
            locals = {@APILocals(name = "deleteButton", type = Button.class),
                    @APILocals(name = "downloadButton", type = Button.class),
                    @APILocals(name = "actions", type = HorizontalLayout.class)})
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

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Anchor.class),
            parameters = {@APIParameter(name = "entry", type = VirtualEntry.class),
                    @APIParameter(name = "button", type = Button.class)},
            locals = {@APILocals(name = "anchor", type = Anchor.class)})
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
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "entry", type = VirtualEntry.class)},
            locals = {@APILocals(name = "content", type = byte[].class),
                    @APILocals(name = "text", type = String.class),
                    @APILocals(name = "e", type = VirtualFileException.class)})
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

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "entry", type = VirtualEntry.class)},
            locals = {@APILocals(name = "e", type = VirtualFileException.class)})
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
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "name", type = String.class)},
            locals = {@APILocals(name = "e", type = VirtualFileException.class)})
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
     * Puts an uploaded file in the open folder. It is text (what the agent reads), an image (what it can be shown) or a
     * zip file (which the agent unpacks), within the limits of the workspace, and a file of that name is not replaced.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "fileName", type = String.class),
                    @APIParameter(name = "content", type = byte[].class)},
            locals = {@APILocals(name = "target", type = VirtualPath.class),
                    @APILocals(name = "text", type = String.class),
                    @APILocals(name = "e", type = VirtualFileException.class)})
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
            if (ArchiveFiles.isZip(content)) {
                // kept as it is, the agent unpacks it with unzip_file
                files.writeArchive(target, content);
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

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = boolean.class))
    private boolean isBusy() {
        if (busy.getAsBoolean()) {
            Notification.warning(loc.getValue(L.MSG_AGENT_WORKSPACE_BUSY));
            refresh();
            return true;
        }
        return false;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = RuntimeException.class),
                    @APILocals(name = "e", type = Exception.class)})
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
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "entries", type = List.class),
                    @APILocals(name = "e", type = VirtualFileException.class),
                    @APILocals(name = "shown", type = String.class),
                    @APILocals(name = "usage", type = WorkspaceUsage.class),
                    @APILocals(name = "e", type = Exception.class)})
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
            // the same rows again would replace them, and the selection of the user in them; the buttons of a row depend on busy
            String shown = busy.getAsBoolean() + ":" + entries;
            if (!shown.equals(shownRows)) {
                shownRows = shown;
                grid.setItems(entries);
            }
            UIUtils.setTextIfChanged(pathLabel, "/" + String.join("/", folder.getSegments()));
            upButton.setEnabled(!folder.isRoot());
            clearButton.setEnabled(!busy.getAsBoolean());
            clearButton.setTooltipText(busy.getAsBoolean() ? loc.getValue(L.MSG_AGENT_WORKSPACE_BUSY) : null);
            newFolderButton.setEnabled(!busy.getAsBoolean());
            newFolderButton.setTooltipText(busy.getAsBoolean() ? loc.getValue(L.MSG_AGENT_WORKSPACE_BUSY) : null);
            upload.setEnabled(!busy.getAsBoolean());
            WorkspaceUsage usage = files.getUsage();
            UIUtils.setTextIfChanged(usageLabel, entries.isEmpty() && usage.files() == 0
                    ? loc.getValue(L.MSG_AGENT_WORKSPACE_EMPTY)
                    : String.format(loc.getValue(L.MSG_AGENT_WORKSPACE_USAGE), usage.files(),
                    FileUtils.byteCountToDisplaySize(usage.bytes()),
                    FileUtils.byteCountToDisplaySize(files.getLimits().maxTotalBytes())));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // for the tests of the browser

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Grid.class))
    Grid<VirtualEntry> getGrid() {
        return grid;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = TextArea.class))
    TextArea getViewer() {
        return viewer;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Span.class))
    Span getUsageLabel() {
        return usageLabel;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Button.class))
    Button getClearButton() {
        return clearButton;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Button.class))
    Button getNewFolderButton() {
        return newFolderButton;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Upload.class))
    Upload getUpload() {
        return upload;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Button.class))
    Button getUpButton() {
        return upButton;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = VirtualPath.class))
    VirtualPath getFolder() {
        return folder;
    }
}
