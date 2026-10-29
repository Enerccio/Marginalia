package com.github.enerccio.marginalia.ui.workspace.parts;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.ChatMessageService.BranchSize;
import com.github.enerccio.marginalia.domain.service.FolderService;
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
import com.github.enerccio.marginalia.ui.widgets.FolderBookProvider;
import com.github.enerccio.marginalia.ui.widgets.FolderBookRow;
import com.github.enerccio.marginalia.ui.workspace.ManuscriptSection;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceLLMSupport;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
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

import java.text.NumberFormat;
import java.util.*;
import java.util.function.ToLongFunction;
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
    private FolderService folderService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    private final Workspace workspace;
    private Grid<FolderBookRow> grid;
    private Button addBookButton;
    private Button newFolderButton;
    private FlexLayout breadcrumb;
    private Checkbox includeSubfolders;

    // the folder the list shows, null is the top level
    private Folder currentFolder;
    // the folder the right pane of the Organise window was on when it was closed; only meaningful when organiseTargetKnown (null is the top level)
    private Folder organiseTarget;
    private boolean organiseTargetKnown;
    // the list shows the favorite books (the virtual folder at the top level) instead of a folder
    private boolean inFavorites;
    private Button tagBooksButton;
    private Button searchButton;
    private VerticalLayout mainLayout;

    private Column<FolderBookRow> nameColumn;
    private TextField nameFilter;
    private Column<FolderBookRow> folderColumn;
    private Column<FolderBookRow> tagsColumn;
    private Column<FolderBookRow> booksColumn;
    private Column<FolderBookRow> wordsColumn;
    private Column<FolderBookRow> tokensColumn;
    private MultiSelectComboBox<Tag> tagFilter;
    private Column<FolderBookRow> creationColumn;
    private Column<FolderBookRow> modificationColumn;

    private final List<GridSortOrder<FolderBookRow>> sorters = new ArrayList<>();
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

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "headerLayout", type = HorizontalLayout.class),
                    @APILocals(name = "titleSpan", type = Span.class),
                    @APILocals(name = "addBookButton", type = Button.class),
                    @APILocals(name = "organiseButton", type = Button.class),
                    @APILocals(name = "importArchiveButton", type = Button.class),
                    @APILocals(name = "importSillyTavernButton", type = Button.class),
                    @APILocals(name = "massChangesBar", type = MenuBar.class),
                    @APILocals(name = "massChangesItem", type = MenuItem.class),
                    @APILocals(name = "navigationLayout", type = HorizontalLayout.class),
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
                event -> new NewBookAction(this::openManuscript, currentFolder).run());
        addBookButton.setThemeName("primary");
        this.addBookButton = addBookButton;

        newFolderButton = new Button(loc.getValue(L.LABEL_NEW_FOLDER), Solid.FOLDER_PLUS.create(),
                event -> new FolderActions().create(currentFolder, _ -> refreshGrid()));
        Button organiseButton = new Button(loc.getValue(L.LABEL_ORGANISE), Solid.COLUMNS.create(), event -> openOrganiseDialog());
        tagBooksButton = new Button(loc.getValue(L.LABEL_TAG_BOOKS), Solid.TAGS.create(), event -> openTagDialog());
        Button importArchiveButton = new Button(Solid.FILE_IMPORT.create(),
                event -> new NewBookAction(this::openManuscript, currentFolder).runFromArchive());
        importArchiveButton.setTooltipText(loc.getValue(L.LABEL_IMPORT_FROM_ARCHIVE));
        Button importSillyTavernButton = new Button(Solid.COMMENTS.create(),
                event -> new NewBookAction(this::openManuscript, currentFolder).runFromSillyTavern());
        importSillyTavernButton.setTooltipText(loc.getValue(L.LABEL_IMPORT_FROM_SILLYTAVERN));
        searchButton = new Button(loc.getValue(L.LABEL_SEARCH), Solid.SEARCH.create(), event -> openSearchDialog());
        MenuBar massChangesBar = new MenuBar();
        MenuItem massChangesItem = massChangesBar.addItem(loc.getValue(L.LABEL_MASS_CHANGES));
        massChangesItem.getSubMenu().addItem(loc.getValue(L.LABEL_MASS_CHANGE_MODEL_PROTOCOL), _ -> openMassChangeDialog());
        massChangesItem.getSubMenu().addItem(loc.getValue(L.LABEL_MASS_CHANGE_LANGUAGE), _ -> openMassChangeLanguageDialog());
        massChangesItem.getSubMenu().addItem(loc.getValue(L.LABEL_TAG_FOLDERS), _ -> openTagFoldersDialog());
        headerLayout.add(titleSpan, searchButton, organiseButton, tagBooksButton, massChangesBar, importArchiveButton, importSillyTavernButton,
                newFolderButton, addBookButton);
        headerLayout.setFlexGrow(1, titleSpan);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // where the list is: the path of the folder to click on, and whether the folders below it are listed too
        breadcrumb = new FlexLayout();
        breadcrumb.setAlignItems(FlexComponent.Alignment.CENTER);
        breadcrumb.addClassNames(CssStyles.FLEX_WRAP, CssStyles.GAP_S);
        includeSubfolders = new Checkbox(loc.getValue(L.LABEL_INCLUDE_SUBFOLDERS));
        includeSubfolders.addValueChangeListener(_ -> {
            refreshGrid();
            notifyStateChanged();
        });
        HorizontalLayout navigationLayout = new HorizontalLayout(breadcrumb, includeSubfolders);
        navigationLayout.setWidthFull();
        navigationLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        navigationLayout.setFlexGrow(1, breadcrumb);

        grid = new Grid<>();
        grid.setSizeFull();
        grid.setMultiSort(true);

        nameColumn = grid.addComponentColumn(this::nameCell)
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

        // the folder of the row, shown while the folders below the current one are listed too
        folderColumn = grid.addColumn(FolderBookRow::getPath)
                .setHeader(loc.getValue(L.LABEL_FOLDER))
                .setFlexGrow(2)
                .setSortable(false);
        folderColumn.setVisible(false);

        tagsColumn = grid.addColumn(row -> String.join(", ", row.getTags()))
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

        // the total number of books of a folder, the size of the active branch of a book
        booksColumn = grid.addColumn(row -> row.isFolder() ? NumberFormat.getIntegerInstance().format(row.getBooks()) : "")
                .setHeader(loc.getValue(L.LABEL_BOOKS))
                .setFlexGrow(0)
                .setWidth(UIConstants.TOOL_COLUMN_SIZE_LARGE)
                .setSortable(false);
        wordsColumn = grid.addColumn(row -> size(row, BranchSize::words))
                .setHeader(loc.getValue(L.LABEL_WORDS))
                .setFlexGrow(0)
                .setWidth(UIConstants.TOOL_COLUMN_SIZE_LARGE)
                .setSortable(false);
        tokensColumn = grid.addColumn(row -> size(row, BranchSize::tokens))
                .setHeader(loc.getValue(L.LABEL_TOKENS))
                .setFlexGrow(0)
                .setWidth(UIConstants.TOOL_COLUMN_SIZE_LARGE)
                .setSortable(false);

        creationColumn = grid.addColumn(row -> row.isFavorites() ? "" : loc.getDateHourFormat().format(row.isFolder() ? row.getFolder().getCreation() : row.getBook().getCreation()))
                .setHeader(loc.getValue(L.LABEL_CREATED))
                .setSortProperty("creation")
                .setSortable(true);

        modificationColumn = grid.addColumn(row -> row.isFavorites() ? "" : loc.getDateHourFormat().format(row.isFolder() ? row.getFolder().getModification() : row.getBook().getModification()))
                .setHeader(loc.getValue(L.LABEL_LAST_MODIFIED))
                .setSortProperty("modification")
                .setSortable(true);

        Column<FolderBookRow> toolColumn = grid.addComponentColumn(row -> row.isFavorites() ? new Span() : row.isFolder() ? folderTools(row.getFolder()) : bookTools(row.getBook(), row.isFavorite()))
                .setHeader("").setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE_FIVE);

        grid.sort(List.of(new GridSortOrder<>(modificationColumn, SortDirection.DESCENDING)));
        sorters.add(new GridSortOrder<>(modificationColumn, SortDirection.DESCENDING));

        grid.addSortListener(event -> {
            sorters.clear();
            sorters.addAll(event.getSortOrder());
            refreshGrid();
        });

        mainLayout.add(headerLayout, navigationLayout, grid);
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

    /**
     * The name of a row: a folder is a button that goes into it, a book is its name.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "row", type = FolderBookRow.class)},
            locals = {@APILocals(name = "open", type = Button.class),
                    @APILocals(name = "name", type = HorizontalLayout.class)})
    private Component nameCell(FolderBookRow row) {
        if (row.isFavorites()) {
            Button open = new Button(row.getName(), VaadinIcon.STAR.create(), _ -> openFavorites());
            open.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            return open;
        }
        if (row.isFolder()) {
            Button open = new Button(row.getName(), VaadinIcon.FOLDER.create(), _ -> openFolder(row.getFolder()));
            open.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            return open;
        }
        HorizontalLayout name = new HorizontalLayout(VaadinIcon.BOOK.create(), new Span(row.getName()));
        name.setSpacing(true);
        name.setPadding(false);
        name.setAlignItems(FlexComponent.Alignment.CENTER);
        return name;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "row", type = FolderBookRow.class),
                    @APIParameter(name = "part", type = ToLongFunction.class)})
    private String size(FolderBookRow row, ToLongFunction<BranchSize> part) {
        return row.getSize() == null ? "" : NumberFormat.getIntegerInstance().format(part.applyAsLong(row.getSize()));
    }

    /**
     * The buttons of a folder row: rename, settings and delete (the folder opens from its name).
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "folder", type = Folder.class)},
            locals = {@APILocals(name = "actions", type = HorizontalLayout.class),
                    @APILocals(name = "renameButton", type = Button.class),
                    @APILocals(name = "settingsButton", type = Button.class),
                    @APILocals(name = "deleteButton", type = Button.class)})
    private Component folderTools(Folder folder) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button renameButton = new Button(Solid.PENCIL.create(), _ -> new FolderActions().rename(folder, _ -> refreshGrid()));
        renameButton.setTooltipText(loc.getValue(L.LABEL_RENAME));

        Button settingsButton = new Button(Solid.COG.create(), _ -> new FolderDialog(folder, _ -> refreshGrid()).show());
        settingsButton.setTooltipText(loc.getValue(L.LABEL_FOLDER_SETTINGS));

        Button deleteButton = new Button(Solid.TRASH.create(), _ -> new FolderActions().delete(folder, this::refreshGrid));
        deleteButton.setThemeName("error tertiary");
        deleteButton.setTooltipText(loc.getValue(L.LABEL_DELETE_FOLDER));

        actions.add(renameButton, settingsButton, deleteButton);
        return actions;
    }

    /**
     * The buttons of a book row: open, clone, move to a folder and delete.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class),
                    @APIParameter(name = "favorite", type = boolean.class)},
            locals = {@APILocals(name = "actions", type = HorizontalLayout.class),
                    @APILocals(name = "starButton", type = Button.class),
                    @APILocals(name = "editButton", type = Button.class),
                    @APILocals(name = "cloneButton", type = Button.class),
                    @APILocals(name = "moveButton", type = Button.class),
                    @APILocals(name = "deleteButton", type = Button.class)})
    private Component bookTools(Manuscript manuscript, boolean favorite) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button starButton = new Button((favorite ? VaadinIcon.STAR : VaadinIcon.STAR_O).create(), _ -> {
            try {
                manuscriptService.setFavorite(manuscript, !favorite);
                refreshGrid();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
        starButton.setTooltipText(loc.getValue(L.LABEL_FAVORITES));

        Button editButton = new Button(Solid.PENCIL.create(), _ -> {
            try {
                // a book an agent works on can't be changed, so there is nothing to do in its window
                if (!new AgentLockCheck().refuses(manuscript)) {
                    openManuscript(manuscript);
                }
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });

        Button cloneButton = new Button(Solid.CLONE.create(), _ -> openCloneDialog(manuscript));
        cloneButton.setTooltipText(loc.getValue(L.LABEL_CLONE_BOOK));

        Button moveButton = new Button(Solid.FOLDER_OPEN.create(), _ -> {
            if (!new AgentLockCheck().refuses(manuscript)) {
                new FolderActions().moveBooks(List.of(manuscript), manuscript.getFolder(), this::refreshGrid);
            }
        });
        moveButton.setTooltipText(loc.getValue(L.LABEL_MOVE_TO_FOLDER));

        Button deleteButton = new Button(Solid.TRASH.create(), _ ->
                ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
                    try {
                        manuscriptService.delete(manuscript, false);
                        refreshGrid();
                    } catch (Exception e) {
                        UIUtils.internalServerError(loc, e);
                    }
                }));
        deleteButton.setThemeName("error tertiary");

        actions.add(starButton, editButton, cloneButton, moveButton, deleteButton);
        return actions;
    }

    /**
     * Goes into the folder (null is the top level); the filters stay.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "folder", type = Folder.class)})
    private void openFolder(Folder folder) {
        inFavorites = false;
        currentFolder = folder;
        refreshGrid();
        notifyStateChanged();
    }

    /**
     * Shows the favorite books, the virtual folder of the top level.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class))
    private void openFavorites() {
        inFavorites = true;
        currentFolder = null;
        refreshGrid();
        notifyStateChanged();
    }

    /**
     * The path to the current folder as buttons, the folder itself last.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "top", type = Button.class),
                    @APILocals(name = "current", type = Span.class),
                    @APILocals(name = "above", type = Folder.class),
                    @APILocals(name = "button", type = Button.class)})
    private void updateBreadcrumb() throws Exception {
        breadcrumb.removeAll();
        Button top = new Button(loc.getValue(L.LABEL_BOOKS), VaadinIcon.HOME.create(), _ -> openFolder(null));
        top.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        breadcrumb.add(top);
        if (inFavorites) {
            breadcrumb.add(new Span("/"));
            Span current = new Span(loc.getValue(L.LABEL_FAVORITES));
            current.addClassName(CssStyles.TEXT_BOLD);
            breadcrumb.add(current);
            return;
        }
        if (currentFolder == null) {
            return;
        }
        for (Folder above : folderService.getAncestors(currentFolder)) {
            breadcrumb.add(new Span("/"));
            Button button = new Button(above.getName(), _ -> openFolder(above));
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            breadcrumb.add(button);
        }
        breadcrumb.add(new Span("/"));
        Span current = new Span(currentFolder.getName());
        current.addClassName(CssStyles.TEXT_BOLD);
        breadcrumb.add(current);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void refresh() throws Exception {
        refreshGrid();
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "stored", type = Folder.class),
                    @APILocals(name = "sortInfos", type = List.class),
                    @APILocals(name = "sortOrder", type = GridSortOrder.class),
                    @APILocals(name = "sorter", type = Sorter[].class),
                    @APILocals(name = "favorites", type = List.class),
                    @APILocals(name = "folders", type = List.class),
                    @APILocals(name = "ids", type = List.class),
                    @APILocals(name = "favoritesLabel", type = String.class),
                    @APILocals(name = "all", type = ManuscriptFilterValues.class),
                    @APILocals(name = "scope", type = ManuscriptFilterValues.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void refreshGrid() {
        if (grid == null || includeSubfolders == null) {
            return;
        }
        try {
            if (currentFolder != null) {
                // the folder may have been renamed or deleted since the list was shown
                Folder stored = folderService.find(currentFolder.getId());
                currentFolder = stored == null || stored.isDeleted() ? null : stored;
            }
            List<Sorter> sortInfos = new ArrayList<>();
            for (GridSortOrder<FolderBookRow> sortOrder : sorters) {
                sortOrder.getSorted().getSortOrder(sortOrder.getDirection())
                        .forEach(so -> sortInfos.add(Sorter.sorter(so.getSorted(),
                                so.getDirection().equals(SortDirection.ASCENDING) ? Ordering.ASC : Ordering.DESC)));
            }
            Sorter[] sorter = sortInfos.toArray(Sorter[]::new);
            List<String> favorites = manuscriptService.getFavorites();
            if (favorites.isEmpty()) {
                inFavorites = false;
            }
            List<Folder> folders;
            List<Long> ids;
            String favoritesLabel = null;
            if (inFavorites) {
                // the favorites are a view of books of any folder: the filters and sorters work, there is no scope
                ManuscriptFilterValues all = new ManuscriptFilterValues();
                all.setName(filterValues.getName());
                all.setTags(filterValues.getTags());
                all.setUuids(favorites);
                folders = List.of();
                ids = manuscriptService.searchRegularManuscripts(all, sorter);
            } else {
                ManuscriptFilterValues scope = filterValues.inFolder(currentFolder, includeSubfolders.getValue());
                folders = folderService.search(scope, sorter);
                ids = manuscriptService.searchRegularManuscripts(scope, sorter);
                boolean filtered = StringUtils.isNotEmpty(filterValues.getName())
                        || (filterValues.getTags() != null && !filterValues.getTags().isEmpty());
                if (!favorites.isEmpty() && currentFolder == null && !includeSubfolders.getValue() && !filtered) {
                    favoritesLabel = loc.getValue(L.LABEL_FAVORITES);
                }
            }
            grid.setItems(new FolderBookProvider(folders, ids, folderService.countBooksByFolder(), folderService.getPaths(),
                    favorites, favoritesLabel));
            // a folder column tells where the books are when the list is not of one folder
            folderColumn.setVisible(inFavorites || includeSubfolders.getValue());
            includeSubfolders.setVisible(!inFavorites);
            newFolderButton.setEnabled(!inFavorites);
            addBookButton.setEnabled(!inFavorites);
            updateBreadcrumb();
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
        MultiTagDialog dialog = new MultiTagDialog(MultiTagDialog.Kind.BOOKS, this::refreshGrid, currentFolder);
        dialog.create();
        dialog.open();
    }

    /**
     * Opens the two-pane dialog for the folders: the left pane on the current folder, the right one on the folder above it. The
     * list is read again when it is closed.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "target", type = Folder.class),
                    @APILocals(name = "dialog", type = OrganiseDialog.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void openOrganiseDialog() {
        try {
            Folder target = organiseTarget();
            OrganiseDialog dialog = new OrganiseDialog(currentFolder, target);
            dialog.setOnShowInList(this::openFolder);
            dialog.setOnTarget(folder -> {
                organiseTarget = folder;
                organiseTargetKnown = true;
                notifyStateChanged();
            });
            // the dialog changed folders and books: the list shows them again, and the top level when the current folder is gone
            dialog.addOpenedChangeListener(event -> {
                if (!event.isOpened()) {
                    refreshGrid();
                    notifyStateChanged();
                }
            });
            dialog.create();
            dialog.open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * The folder the right pane of the Organise window starts in: the one it was left on the last time, or the folder above the
     * current one. Not the current folder itself (nothing can be copied or moved there), and not a folder that is gone.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Folder.class),
            locals = {@APILocals(name = "last", type = Folder.class)})
    private Folder organiseTarget() throws Exception {
        if (organiseTargetKnown) {
            Folder last = organiseTarget == null ? null : folderService.find(organiseTarget.getId());
            boolean gone = organiseTarget != null && (last == null || last.isDeleted());
            boolean same = organiseTarget == null ? currentFolder == null : currentFolder != null && organiseTarget.getId().equals(currentFolder.getId());
            if (!gone && !same) {
                return last;
            }
        }
        return currentFolder == null ? null : folderService.find(currentFolder.getId()).getParent();
    }

    /**
     * Opens the dialog that puts a tag on the folders of the current folder; the list shows the new tags when it is closed.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = MultiTagDialog.class)})
    private void openTagFoldersDialog() {
        MultiTagDialog dialog = new MultiTagDialog(MultiTagDialog.Kind.FOLDERS, this::refreshGrid, currentFolder);
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
        MassChangeModelProtocolDialog dialog = new MassChangeModelProtocolDialog(this::refreshGrid, currentFolder);
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
        MassChangeLanguageDialog dialog = new MassChangeLanguageDialog(this::refreshGrid, currentFolder);
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
        if (currentFolder != null) {
            state.addProperty("folder", currentFolder.getUuid());
        }
        if (inFavorites) {
            state.addProperty("favorites", true);
        }
        if (includeSubfolders != null && includeSubfolders.getValue()) {
            state.addProperty("subfolders", true);
        }
        if (organiseTargetKnown) {
            // the top level has no uuid
            state.addProperty("organiseTarget", organiseTarget == null ? "top" : organiseTarget.getUuid());
        }
        if (openDialog != null) {
            RestorableState.put(state, "dialog", openDialog.storeState());
        }
        return state;
    }

    /**
     * Sets the stored filters (a tag that no longer exists is skipped) and opens the stored book, unless it no longer
     * exists.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "self", type = JsonObject.class)},
            locals = {@APILocals(name = "folderUuid", type = String.class),
                    @APILocals(name = "folder", type = Folder.class),
                    @APILocals(name = "filters", type = JsonObject.class),
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
            String folderUuid = RestorableState.string(self, "folder");
            Folder folder = folderUuid == null ? null : folderService.findForUser(folderUuid);
            if (folderUuid != null || includeSubfolders != null) {
                // a folder that is gone is the top level
                currentFolder = folder == null || folder.isDeleted() ? null : folder;
                // the favorites view is dropped by the refresh when there is no favorite any more
                inFavorites = self.has("favorites") && self.get("favorites").getAsBoolean();
                if (includeSubfolders != null) {
                    includeSubfolders.setValue(self.has("subfolders") && self.get("subfolders").getAsBoolean());
                }
                refreshGrid();
            }

            restoreOrganiseTarget(self);

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

    /**
     * Takes the folder the right pane of the Organise window was left on from the stored state; one that is gone is forgotten.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "self", type = JsonObject.class)},
            locals = {@APILocals(name = "uuid", type = String.class),
                    @APILocals(name = "folder", type = Folder.class)})
    private void restoreOrganiseTarget(JsonObject self) throws Exception {
        String uuid = RestorableState.string(self, "organiseTarget");
        organiseTargetKnown = false;
        organiseTarget = null;
        if (uuid == null) {
            return;
        }
        if (uuid.equals("top")) {
            organiseTargetKnown = true;
            return;
        }
        Folder folder = folderService.findForUser(uuid);
        if (folder != null && !folder.isDeleted()) {
            organiseTarget = folder;
            organiseTargetKnown = true;
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
}
