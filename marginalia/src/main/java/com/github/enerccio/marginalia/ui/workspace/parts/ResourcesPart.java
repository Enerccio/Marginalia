package com.github.enerccio.marginalia.ui.workspace.parts;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.repository.ResourceRepository.ResourceGroup;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.domain.service.ResourceService.ResourceLink;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.MessageImages;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.InputStreamDownloadCallback;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * All files of the user (pictures of the parts for now) in a lazy table grouped by the object that uses them (Used by):
 * a row of a group opens to the resources of the object. Download, replace the content, delete; ticking a group deletes
 * all its resources. A deleted resource is only marked as deleted, cleanup removes it later and the file stays in the
 * data folder.
 */
@Configurable
@Extendable
public class ResourcesPart implements WorkspaceComponent {

    @Autowired
    private Localization loc;

    @Autowired
    private ResourceService resourceService;

    private final Workspace workspace;
    // the phone layout: one card for each resource (picture, name, details, buttons) next to the check box
    private final boolean phone;
    private TreeGrid<Row> grid;
    private Button deleteSelectedButton;
    private MessageImages thumbnails;

    /**
     * A row of the table: a group of the resources of one object (the resource is null) or one resource of it.
     *
     * @param group the group the row is or belongs to
     */
    record Row(ResourceGroup group, Resource resource) {
        boolean isGroup() {
            return resource == null;
        }

        String id() {
            return resource != null ? resource.getUuid() : "group:" + group.clazz() + ":" + group.objectId();
        }
    }

    public ResourcesPart(Workspace workspace) {
        this(workspace, false);
    }

    public ResourcesPart(Workspace workspace, boolean phone) {
        this.workspace = workspace;
        this.phone = phone;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class))
    public String getName() {
        return loc.getValue(L.LABEL_RESOURCES);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "mainLayout", type = VerticalLayout.class),
                    @APILocals(name = "headerLayout", type = HorizontalLayout.class),
                    @APILocals(name = "titleSpan", type = Span.class),
                    @APILocals(name = "refreshButton", type = Button.class),
                    @APILocals(name = "buttons", type = HorizontalLayout.class),
                    @APILocals(name = "selection", type = GridMultiSelectionModel.class)})
    @Override
    public Component create() throws Exception {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        thumbnails = new MessageImages(List.of());

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        if (phone) {
            headerLayout.addClassName(CssStyles.FLEX_WRAP);
        }

        Span titleSpan = new Span(loc.getValue(L.LABEL_RESOURCES));
        titleSpan.addClassName(CssStyles.SECTION_TITLE);

        deleteSelectedButton = new Button(loc.getValue(L.LABEL_DELETE_SELECTED), Solid.TRASH.create(), event -> deleteSelected());
        deleteSelectedButton.setThemeName("error");
        deleteSelectedButton.setEnabled(false);

        Button refreshButton = new Button(loc.getValue(L.LABEL_REFRESH), Solid.SYNC.create(), event -> refreshGrid());

        HorizontalLayout buttons = new HorizontalLayout(deleteSelectedButton, refreshButton);
        headerLayout.add(titleSpan, buttons);
        headerLayout.setFlexGrow(1, titleSpan);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        grid = new TreeGrid<>();
        grid.setSizeFull();
        GridMultiSelectionModel<Row> selection = (GridMultiSelectionModel<Row>) grid.setSelectionMode(Grid.SelectionMode.MULTI);
        selection.setSelectAllCheckboxVisibility(GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
        grid.setEmptyStateText(loc.getValue(L.MSG_RESOURCES_EMPTY));
        grid.setMultiSort(false);
        // selection survives loading other pages of the lazy table
        grid.setDataProvider(new RowProvider());
        grid.addSelectionListener(event -> deleteSelectedButton.setEnabled(!event.getAllSelectedItems().isEmpty()));

        if (phone) {
            grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
            grid.addComponentHierarchyColumn(this::cardOf).setFlexGrow(1);
            mainLayout.add(headerLayout, grid);
            mainLayout.setFlexGrow(1, grid);
            return mainLayout;
        }

        // the group (the object that uses the resources) opens to its resources: the picture and name of each
        grid.addComponentHierarchyColumn(this::nameOf)
                .setHeader(loc.getValue(L.LABEL_LINKED_OBJECT)).setSortProperty("originalName").setFlexGrow(3);

        grid.addColumn(row -> row.isGroup() ? "" : StringUtils.defaultString(row.resource().getMimeType()))
                .setHeader(loc.getValue(L.LABEL_TYPE)).setSortProperty("mimeType").setFlexGrow(1);

        grid.addColumn(row -> FileUtils.byteCountToDisplaySize(row.isGroup() ? row.group().size() : row.resource().getSize()))
                .setHeader(loc.getValue(L.LABEL_SIZE)).setSortProperty("size").setFlexGrow(0).setWidth("110px");

        grid.addColumn(row -> loc.getDateHourFormat().format(row.isGroup() ? row.group().newest() : row.resource().getCreation()))
                .setHeader(loc.getValue(L.LABEL_CREATED)).setSortProperty("creation").setFlexGrow(1);

        grid.addComponentColumn(row -> row.isGroup() ? new Span() : actionsOf(row.resource()))
                .setHeader("").setFlexGrow(0).setWidth("190px");

        mainLayout.add(headerLayout, grid);
        mainLayout.setFlexGrow(1, grid);
        return mainLayout;
    }

    /**
     * The first column: the object that uses the resources of a group with the number of them, or the picture (if it
     * is one) and the name of a resource.
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "row", type = Row.class)},
            locals = {@APILocals(name = "resource", type = Resource.class),
                    @APILocals(name = "name", type = Span.class),
                    @APILocals(name = "layout", type = HorizontalLayout.class)})
    private Component nameOf(Row row) {
        if (row.isGroup()) {
            return groupOf(row.group());
        }
        Resource resource = row.resource();
        Span name = new Span(StringUtils.defaultString(resource.getOriginalName()));
        name.addClassName(CssStyles.WRAP_ANYWHERE);
        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setPadding(false);
        if (isImage(resource)) {
            layout.add(thumbnails.figure(resource, "", "48px"));
        }
        layout.add(name);
        return layout;
    }

    private static boolean isImage(Resource resource) {
        return resource.getMimeType() != null && resource.getMimeType().startsWith("image/");
    }

    /**
     * The object that uses the resources of the group and how many there are; nothing for the resources that are
     * not used, in red the object that is gone (the resources are probably not needed any more).
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "group", type = ResourceGroup.class)},
            locals = {@APILocals(name = "title", type = Span.class),
                    @APILocals(name = "link", type = ResourceLink.class),
                    @APILocals(name = "e", type = Exception.class),
                    @APILocals(name = "count", type = Span.class),
                    @APILocals(name = "layout", type = HorizontalLayout.class)})
    private Component groupOf(ResourceGroup group) {
        Span title;
        try {
            ResourceLink link = resourceService.describeGroup(group);
            if (link == null) {
                title = new Span(loc.getValue(L.MSG_RESOURCE_NOT_LINKED));
                title.addClassName(CssStyles.TEXT_SECONDARY);
            } else if (link.present()) {
                title = new Span(link.description());
            } else {
                title = new Span(String.format(loc.getValue(L.MSG_RESOURCE_LINK_MISSING), link.description()));
                title.addClassName(CssStyles.TEXT_ERROR);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            title = new Span();
        }
        title.addClassNames(CssStyles.TEXT_SEMIBOLD, CssStyles.WRAP_ANYWHERE);
        Span count = new Span("(" + group.count() + ")");
        count.addClassName(CssStyles.TEXT_SECONDARY);
        HorizontalLayout layout = new HorizontalLayout(title, count);
        layout.setPadding(false);
        return layout;
    }

    /**
     * A row on a phone: a group as the object with the number and size of its resources, a resource as the picture
     * (if it is one) and the name with the type, size and date below it, and the buttons in a row under them.
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "row", type = Row.class)},
            locals = {@APILocals(name = "resource", type = Resource.class),
                    @APILocals(name = "name", type = Span.class),
                    @APILocals(name = "details", type = Span.class),
                    @APILocals(name = "texts", type = VerticalLayout.class),
                    @APILocals(name = "top", type = HorizontalLayout.class),
                    @APILocals(name = "card", type = VerticalLayout.class)})
    private Component cardOf(Row row) {
        if (row.isGroup()) {
            return groupOf(row.group());
        }
        Resource resource = row.resource();
        Span name = new Span(StringUtils.defaultString(resource.getOriginalName()));
        name.addClassNames(CssStyles.TEXT_SEMIBOLD, CssStyles.WRAP_ANYWHERE);
        Span details = new Span(StringUtils.defaultString(resource.getMimeType()) + " · "
                + FileUtils.byteCountToDisplaySize(resource.getSize()) + " · "
                + loc.getDateHourFormat().format(resource.getCreation()));
        details.addClassNames(CssStyles.TEXT_SECONDARY, CssStyles.WRAP_ANYWHERE);
        VerticalLayout texts = new VerticalLayout(name, details);
        texts.setPadding(false);
        texts.setSpacing(false);
        texts.addClassName(CssStyles.MIN_WIDTH_0);
        HorizontalLayout top = new HorizontalLayout();
        top.setWidthFull();
        if (isImage(resource)) {
            top.add(thumbnails.figure(resource, "", "48px"));
        }
        top.add(texts);
        top.setFlexGrow(1, texts);
        VerticalLayout card = new VerticalLayout(top, actionsOf(resource));
        card.setPadding(false);
        card.setSpacing(false);
        card.setWidthFull();
        return card;
    }

    /**
     * The groups of the table and the resources of each, sorted as the user clicked; without a sort the newest are
     * first.
     */
    private final class RowProvider extends AbstractBackEndHierarchicalDataProvider<Row, Void> {

        @Override
        public int getChildCount(HierarchicalQuery<Row, Void> query) {
            try {
                Row parent = query.getParent();
                long count = parent == null ? resourceService.countGroupsForUser()
                        : resourceService.countOfGroupForUser(parent.group());
                return (int) Math.min(Integer.MAX_VALUE, count);
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
                return 0;
            }
        }

        @Override
        protected Stream<Row> fetchChildrenFromBackEnd(HierarchicalQuery<Row, Void> query) {
            try {
                List<QuerySortOrder> orders = query.getSortOrders() == null ? List.of() : query.getSortOrders();
                String property = orders.isEmpty() ? "creation" : orders.getFirst().getSorted();
                boolean ascending = !orders.isEmpty() && orders.getFirst().getDirection() == SortDirection.ASCENDING;
                Row parent = query.getParent();
                if (parent == null) {
                    return resourceService.findGroupPageForUser(query.getOffset(), query.getLimit(), property, ascending)
                            .stream().map(group -> new Row(group, null));
                }
                return resourceService.findPageOfGroupForUser(parent.group(), query.getOffset(), query.getLimit(), property, ascending)
                        .stream().map(resource -> new Row(parent.group(), resource));
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
                return Stream.empty();
            }
        }

        @Override
        public boolean hasChildren(Row row) {
            return row.isGroup();
        }

        @Override
        public Object getId(Row row) {
            return row.id();
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "resource", type = Resource.class)},
            locals = {@APILocals(name = "fileName", type = String.class),
                    @APILocals(name = "download", type = Anchor.class),
                    @APILocals(name = "downloadButton", type = Button.class),
                    @APILocals(name = "replaceButton", type = Button.class),
                    @APILocals(name = "deleteButton", type = Button.class),
                    @APILocals(name = "actions", type = HorizontalLayout.class)})
    private Component actionsOf(Resource resource) {
        String fileName = StringUtils.defaultIfBlank(resource.getOriginalName(), resource.getUuid())
                .replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_");
        Anchor download = new Anchor(new InputStreamDownloadHandler((InputStreamDownloadCallback) event -> {
            byte[] data;
            try {
                data = resourceService.getResourceData(resource);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
            return new DownloadResponse(new ByteArrayInputStream(data), fileName,
                    StringUtils.defaultIfBlank(resource.getMimeType(), "application/octet-stream"), data.length);
        }), "");
        download.getElement().setAttribute("download", true);
        Button downloadButton = new Button(Solid.DOWNLOAD.create());
        downloadButton.setThemeName("tertiary");
        UIUtils.addTooltip(downloadButton, loc.getValue(L.LABEL_DOWNLOAD));
        download.add(downloadButton);

        Button replaceButton = new Button(Solid.UPLOAD.create(), event -> openReplaceDialog(resource));
        replaceButton.setThemeName("tertiary");
        UIUtils.addTooltip(replaceButton, loc.getValue(L.LABEL_REPLACE_CONTENT));

        Button deleteButton = new Button(Solid.TRASH.create(), event ->
                ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> delete(List.of(resource))));
        deleteButton.setThemeName("error tertiary");
        UIUtils.addTooltip(deleteButton, loc.getValue(L.LABEL_DELETE));

        HorizontalLayout actions = new HorizontalLayout(download, replaceButton, deleteButton);
        actions.setSpacing(false);
        return actions;
    }

    /**
     * Deletes the ticked resources and all the resources of the ticked groups.
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "selected", type = Set.class),
                    @APILocals(name = "row", type = Row.class),
                    @APILocals(name = "e", type = Exception.class),
                    @APILocals(name = "resources", type = List.class)})
    private void deleteSelected() {
        Set<Resource> selected = new LinkedHashSet<>();
        try {
            for (Row row : grid.getSelectedItems()) {
                if (row.isGroup()) {
                    selected.addAll(resourceService.findPageOfGroupForUser(row.group(), 0, Integer.MAX_VALUE, "creation", false));
                } else {
                    selected.add(row.resource());
                }
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        if (selected.isEmpty()) {
            return;
        }
        List<Resource> resources = List.copyOf(selected);
        ConfirmDialog.show(String.format(loc.getValue(L.MSG_CONFIRM_DELETE_RESOURCES), resources.size()), () -> delete(resources));
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "resources", type = List.class)},
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void delete(List<Resource> resources) {
        try {
            resourceService.softDelete(resources.stream().map(Resource::getUuid).toList());
            grid.deselectAll();
            refreshGrid();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "resource", type = Resource.class)},
            locals = {@APILocals(name = "dialog", type = Dialog.class),
                    @APILocals(name = "handler", type = InMemoryUploadHandler.class),
                    @APILocals(name = "upload", type = Upload.class),
                    @APILocals(name = "content", type = VerticalLayout.class)})
    private void openReplaceDialog(Resource resource) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(loc.getValue(L.LABEL_REPLACE_CONTENT));
        dialog.setWidth("450px");

        boolean image = resource.getMimeType() != null && resource.getMimeType().startsWith("image/");
        InMemoryUploadHandler handler = new InMemoryUploadHandler((metadata, data) -> {
            try {
                resourceService.replace(resource, metadata.fileName(), data, metadata.contentType());
                dialog.close();
                Notification.success(loc.getValue(L.MSG_RESOURCE_REPLACED));
                refreshGrid();
            } catch (IllegalArgumentException e) {
                Notification.error(loc.getValue(L.ERROR_INVALID_IMAGE));
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
        Upload upload = new Upload(handler);
        if (image) {
            upload.setAcceptedMimeTypes("image/png", "image/jpeg", "image/gif", "image/webp");
            upload.setMaxFileSize(ResourceService.MAX_IMAGE_BYTES);
        }
        upload.setUploadButton(new Button(loc.getValue(L.LABEL_REPLACE_CONTENT), Solid.UPLOAD.create()));

        VerticalLayout content = new VerticalLayout(upload);
        content.setPadding(false);
        dialog.add(content);
        dialog.getFooter().add(new Button(loc.getValue(L.LABEL_CLOSE), event -> dialog.close()));
        dialog.open();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void refresh() throws Exception {
        refreshGrid();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    private void refreshGrid() {
        if (grid != null) {
            grid.getDataProvider().refreshAll();
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabSwitched() throws Exception {
        refresh();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabClosed() throws Exception {

    }
}
