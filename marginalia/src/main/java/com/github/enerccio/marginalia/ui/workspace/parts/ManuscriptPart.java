package com.github.enerccio.marginalia.ui.workspace.parts;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import com.github.enerccio.marginalia.domain.service.search.Sorter.Ordering;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.LLMInteraction;
import com.github.enerccio.marginalia.ui.RestorableState;
import com.github.enerccio.marginalia.ui.dialogs.*;
import com.github.enerccio.marginalia.ui.dialogs.agent.AgentLockCheck;
import com.github.enerccio.marginalia.ui.widgets.BackendTableItem;
import com.github.enerccio.marginalia.ui.widgets.BackendTableProviderBase;
import com.github.enerccio.marginalia.ui.workspace.ManuscriptSection;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceLLMSupport;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.stream.Stream;

@Configurable
@Extendable
public class ManuscriptPart implements ManuscriptSection {
    private static final Logger log = LoggerFactory.getLogger(ManuscriptPart.class);

    @Autowired
    private Localization loc;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    private final Workspace workspace;
    private Grid<ManuscriptWrapper> grid;
    private Button addBookButton;
    private Button tagBooksButton;
    private Button searchButton;
    private VerticalLayout mainLayout;

    private Column<ManuscriptWrapper> nameColumn;
    private TextField nameFilter;
    private Column<ManuscriptWrapper> tagsColumn;
    private MultiSelectComboBox<Tag> tagFilter;
    private Column<ManuscriptWrapper> creationColumn;
    private Column<ManuscriptWrapper> modificationColumn;

    private final List<GridSortOrder<ManuscriptWrapper>> sorters = new ArrayList<>();
    private final ManuscriptFilterValues filterValues = new ManuscriptFilterValues();

    // the book window that is open, remembered by the workspace autosave
    private ManuscriptDialog openDialog;

    public ManuscriptPart(Workspace workspace) {
        this.workspace = workspace;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class))
    public String getName() {
        return loc.getValue(L.LABEL_BOOKS);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "headerLayout", type = HorizontalLayout.class),
                    @APILocals(name = "titleSpan", type = Span.class),
                    @APILocals(name = "addBookButton", type = Button.class),
                    @APILocals(name = "importArchiveButton", type = Button.class),
                    @APILocals(name = "importSillyTavernButton", type = Button.class),
                    @APILocals(name = "massChangesBar", type = MenuBar.class),
                    @APILocals(name = "massChangesItem", type = MenuItem.class),
                    @APILocals(name = "toolColumn", type = Grid.Column.class),
                    @APILocals(name = "filterRow", type = HeaderRow.class),
                    @APILocals(name = "refresh", type = Button.class)})
    @Override
    public Component create() throws Exception {
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Span titleSpan = new Span(loc.getValue(L.LABEL_BOOKS));
        titleSpan.addClassName(CssStyles.SECTION_TITLE);

        Button addBookButton = new Button(loc.getValue(L.LABEL_ADD_BOOK),
                event -> new NewBookAction(this::openManuscript).run());
        addBookButton.setThemeName("primary");
        this.addBookButton = addBookButton;

        tagBooksButton = new Button(loc.getValue(L.LABEL_TAG_BOOKS), Solid.TAGS.create(), event -> openTagDialog());
        Button importArchiveButton = new Button(Solid.FILE_IMPORT.create(), event -> new NewBookAction(this::openManuscript).runFromArchive());
        importArchiveButton.setTooltipText(loc.getValue(L.LABEL_IMPORT_FROM_ARCHIVE));
        Button importSillyTavernButton = new Button(Solid.COMMENTS.create(), event -> new NewBookAction(this::openManuscript).runFromSillyTavern());
        importSillyTavernButton.setTooltipText(loc.getValue(L.LABEL_IMPORT_FROM_SILLYTAVERN));
        searchButton = new Button(loc.getValue(L.LABEL_SEARCH), Solid.SEARCH.create(), event -> openSearchDialog());
        MenuBar massChangesBar = new MenuBar();
        MenuItem massChangesItem = massChangesBar.addItem(loc.getValue(L.LABEL_MASS_CHANGES));
        massChangesItem.getSubMenu().addItem(loc.getValue(L.LABEL_MASS_CHANGE_MODEL_PROTOCOL), _ -> openMassChangeDialog());
        massChangesItem.getSubMenu().addItem(loc.getValue(L.LABEL_MASS_CHANGE_LANGUAGE), _ -> openMassChangeLanguageDialog());
        headerLayout.add(titleSpan, searchButton, tagBooksButton, massChangesBar, importArchiveButton, importSillyTavernButton, addBookButton);
        headerLayout.setFlexGrow(1, titleSpan);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        grid = new Grid<>();
        grid.setSizeFull();
        grid.setMultiSort(true);

        nameColumn = grid.addColumn(ManuscriptWrapper::getName)
                .setHeader(loc.getValue(L.LABEL_NAME))
                .setFlexGrow(4)
                .setSortProperty("name")
                .setSortable(true);
        nameFilter = new TextField();
        nameFilter.setWidthFull();
        nameFilter.setValueChangeMode(ValueChangeMode.TIMEOUT);
        nameFilter.setValueChangeTimeout(200);
        nameFilter.addValueChangeListener(event -> {
            filterValues.setName(event.getValue());
            refreshGrid();
            notifyStateChanged();
        });

        tagsColumn = grid.addColumn(ManuscriptWrapper::getTags)
                .setHeader(loc.getValue(L.LABEL_TAGS))
                .setFlexGrow(2)
                .setSortable(false);
        tagFilter = new MultiSelectComboBox<>();
        tagFilter.setWidthFull();
        tagFilter.setItemLabelGenerator(Tag::getValue);
        tagFilter.setClearButtonVisible(true);
        tagFilter.setItems(query -> {
            try {
                String filter = query.getFilter().orElse("");
                return tagService.searchTagsForUser(filter, query.getOffset(), query.getLimit()).stream();
            } catch (Exception e) {
                log.error("Failed to fetch tags for filter: {}", query.getFilter().orElse(""), e);
                return Stream.empty();
            }
        });
        tagFilter.addValueChangeListener(event -> {
            filterValues.setTags(event.getValue().stream().map(Tag::getValue).sorted().toList());
            refreshGrid();
            notifyStateChanged();
        });

        creationColumn = grid.addColumn(ManuscriptWrapper::getCreation)
                .setHeader(loc.getValue(L.LABEL_CREATED))
                .setSortProperty("creation")
                .setSortable(true);

        modificationColumn = grid.addColumn(ManuscriptWrapper::getModification)
                .setHeader(loc.getValue(L.LABEL_LAST_MODIFIED))
                .setSortProperty("modification")
                .setSortable(true);

        Column<ManuscriptWrapper> toolColumn = grid.addComponentColumn(manuscriptWrapper -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button editButton = new Button(Solid.PENCIL.create(), _ -> {
                try {
                    // a book an agent works on can't be changed, so there is nothing to do in its window
                    if (!new AgentLockCheck().refuses(manuscriptWrapper.getManuscript())) {
                        openManuscript(manuscriptWrapper.getManuscript());
                    }
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            });

            Button cloneButton = new Button(Solid.CLONE.create(), _ -> openCloneDialog(manuscriptWrapper.getManuscript()));
            cloneButton.setTooltipText(loc.getValue(L.LABEL_CLONE_BOOK));

            Button deleteButton = new Button(Solid.TRASH.create(), _ ->
                    ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
                        try {
                            manuscriptService.delete(manuscriptWrapper.getManuscript(), false);
                            refreshGrid();
                        } catch (Exception e) {
                            UIUtils.internalServerError(loc, e);
                        }
                    }));
            deleteButton.setThemeName("error tertiary");

            actions.add(editButton, cloneButton, deleteButton);
            return actions;
        }).setHeader("").setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE_GIANT);

        grid.sort(List.of(new GridSortOrder<>(modificationColumn, SortDirection.DESCENDING)));
        sorters.add(new GridSortOrder<>(modificationColumn, SortDirection.DESCENDING));

        grid.addSortListener(event -> {
            sorters.clear();
            sorters.addAll(event.getSortOrder());
            refreshGrid();
        });

        mainLayout.add(headerLayout, grid);
        mainLayout.setFlexGrow(1, grid);

        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(nameColumn).setComponent(nameFilter);
        filterRow.getCell(tagsColumn).setComponent(tagFilter);

        Button refresh = new Button(Solid.REFRESH.create());
        refresh.addClickListener(_ -> refreshGrid());
        filterRow.getCell(toolColumn).setComponent(refresh);
        refreshGrid();

        return mainLayout;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void refresh() throws Exception {
        refreshGrid();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "sortInfos", type = List.class),
                    @APILocals(name = "sortOrder", type = GridSortOrder.class),
                    @APILocals(name = "ids", type = List.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void refreshGrid() {
        if (grid == null) {
            return;
        }
        try {
            List<Sorter> sortInfos = new ArrayList<>();
            for (GridSortOrder<ManuscriptWrapper> sortOrder : sorters) {
                sortOrder.getSorted().getSortOrder(sortOrder.getDirection())
                        .forEach(so -> sortInfos.add(Sorter.sorter(so.getSorted(),
                                so.getDirection().equals(SortDirection.ASCENDING) ? Ordering.ASC : Ordering.DESC)));
            }
            List<Long> ids = manuscriptService.searchRegularManuscripts(filterValues, sortInfos.toArray(Sorter[]::new));
            grid.setItems(new ManuscriptBackendProvider(ids));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Opens the dialog that makes a new book of the book; the list shows the new book when it is closed.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)},
            locals = {@APILocals(name = "dialog", type = ManuscriptCloneDialog.class)})
    private void openCloneDialog(Manuscript manuscript) {
        ManuscriptCloneDialog dialog = new ManuscriptCloneDialog(manuscript);
        dialog.setOnClone(_ -> refreshGrid());
        dialog.create();
        dialog.open();
    }

    /**
     * Opens the advanced search of the books; a book chosen in the results opens in its window above the search.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = ManuscriptSearchDialog.class)})
    private void openSearchDialog() {
        ManuscriptSearchDialog dialog = new ManuscriptSearchDialog(this::openFound);
        dialog.open();
    }

    /**
     * Opens a book chosen in the results of the advanced search, unless an agent works on it.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)},
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void openFound(Manuscript manuscript) {
        try {
            if (!new AgentLockCheck().refuses(manuscript)) {
                openManuscript(manuscript);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Opens the dialog that puts a tag on many books at once; the list shows the new tags when it is closed.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = MultiTagDialog.class)})
    private void openTagDialog() {
        MultiTagDialog dialog = new MultiTagDialog(MultiTagDialog.Kind.BOOKS, this::refreshGrid);
        dialog.create();
        dialog.open();
    }

    /**
     * Opens the dialog that puts a model and a protocol on many books at once; the list shows the new ones when it is
     * closed.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = MassChangeModelProtocolDialog.class)})
    private void openMassChangeDialog() {
        MassChangeModelProtocolDialog dialog = new MassChangeModelProtocolDialog(this::refreshGrid);
        dialog.create();
        dialog.open();
    }

    /**
     * Opens the dialog that puts a language on many books at once; the list shows the new ones when it is closed.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = MassChangeLanguageDialog.class)})
    private void openMassChangeLanguageDialog() {
        MassChangeLanguageDialog dialog = new MassChangeLanguageDialog(this::refreshGrid);
        dialog.create();
        dialog.open();
    }

    /**
     * Opens the book window. The window tells the workspace autosave about its navigation, and about closing.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = ManuscriptDialog.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)},
            locals = {@APILocals(name = "dialog", type = ManuscriptDialog.class)})
    private ManuscriptDialog openManuscript(Manuscript manuscript) throws Exception {
        manuscriptService.markOpened(manuscript);
        ManuscriptDialog dialog = new ManuscriptDialog(manuscript);
        dialog.setOnClose(() -> {
            openDialog = null;
            refreshGrid();
            notifyStateChanged();
        });
        dialog.setOnStateChange(this::notifyStateChanged);
        dialog.create();
        openDialog = dialog;
        dialog.open();
        notifyStateChanged();
        return dialog;
    }

    /**
     * The book window that is open, null when none is.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = ManuscriptWindow.class))
    @Override
    public ManuscriptWindow getOpenWindow() {
        return openDialog;
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class))
    private void notifyStateChanged() {
        if (workspace != null) {
            workspace.autosave();
        }
    }

    /**
     * The filters of the list and the open book window.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = JsonObject.class),
            locals = {@APILocals(name = "state", type = JsonObject.class),
                    @APILocals(name = "filters", type = JsonObject.class)})
    @Override
    public JsonObject storeState() {
        JsonObject state = new JsonObject();
        JsonObject filters = new JsonObject();
        if (nameFilter != null && StringUtils.isNotBlank(nameFilter.getValue())) {
            filters.addProperty("name", nameFilter.getValue());
        }
        if (tagFilter != null && !tagFilter.getValue().isEmpty()) {
            filters.add("tags", RestorableState.array(tagFilter.getValue().stream().map(Tag::getUuid).sorted().toList()));
        }
        RestorableState.put(state, "filters", filters);
        if (openDialog != null) {
            RestorableState.put(state, "dialog", openDialog.storeState());
        }
        return state;
    }

    /**
     * Sets the stored filters (a tag that no longer exists is skipped) and opens the stored book, unless it no longer
     * exists.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "self", type = JsonObject.class)},
            locals = {@APILocals(name = "filters", type = JsonObject.class),
                    @APILocals(name = "tags", type = Set.class),
                    @APILocals(name = "uuid", type = String.class),
                    @APILocals(name = "tag", type = Tag.class),
                    @APILocals(name = "dialogState", type = JsonObject.class),
                    @APILocals(name = "manuscriptUuid", type = String.class),
                    @APILocals(name = "manuscript", type = Manuscript.class),
                    @APILocals(name = "e", type = Exception.class)})
    @Override
    public void restoreState(JsonObject self) {
        try {
            JsonObject filters = RestorableState.object(self, "filters");
            if (filters != null && nameFilter != null) {
                nameFilter.setValue(RestorableState.string(filters, "name", ""));
                Set<Tag> tags = new LinkedHashSet<>();
                for (String uuid : RestorableState.strings(filters, "tags")) {
                    Tag tag = tagService.findForUser(uuid);
                    if (tag != null) {
                        tags.add(tag);
                    }
                }
                tagFilter.setValue(tags);
            }

            JsonObject dialogState = RestorableState.object(self, "dialog");
            String manuscriptUuid = RestorableState.string(dialogState, "manuscript");
            if (manuscriptUuid != null && grid != null) {
                Manuscript manuscript = manuscriptService.findForUser(manuscriptUuid);
                if (manuscript != null && !new AgentLockCheck().refuses(manuscript)) {
                    openManuscript(manuscript).restoreState(dialogState);
                }
            }
        } catch (Exception e) {
            log.warn("Could not restore the state of the book list", e);
        }
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = String.class))
    @Override
    public String getLLMState() {
        return WorkspaceLLMSupport.describeTable("books", grid, addBookButton);
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = Map.class))
    @Override
    public Map<String, LLMInteraction> getInteraction() {
        return LLMInteract.dialogButtons(addBookButton);
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

    private class ManuscriptWrapper extends BackendTableItem {

        private final Manuscript manuscript;
        private List<String> tags;

        public ManuscriptWrapper(Manuscript manuscript) {
            super(manuscript.getId());
            this.manuscript = manuscript;
        }

        public Manuscript getManuscript() {
            return manuscript;
        }

        public String getName() {
            return manuscript.getName();
        }

        public String getCreation() {
            return loc.getDateHourFormat().format(manuscript.getCreation());
        }

        public String getModification() {
            return loc.getDateHourFormat().format(manuscript.getModification());
        }

        public String getTags() {
            if (tags == null) {
                try {
                    tags = tagRelationService.getTagsForObject(manuscript).stream().map(Tag::getValue).sorted().toList();
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                    return loc.getValue(L.LABEL_ERROR);
                }
            }
            return String.join(", ", tags);
        }

    }

    private class ManuscriptBackendProvider extends BackendTableProviderBase<ManuscriptWrapper, Manuscript, ManuscriptRepository> {

        public ManuscriptBackendProvider(List<Long> ids) {
            super();
            setIds(ids);
        }

        @Override
        protected ManuscriptWrapper entityToTableItem(Manuscript entity) throws Exception {
            return new ManuscriptWrapper(entity);
        }

    }
}