package com.github.enerccio.marginalia.ui.workspace.phone.parts;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
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
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialog;
import com.github.enerccio.marginalia.ui.widgets.FolderBookProvider;
import com.github.enerccio.marginalia.ui.widgets.FolderBookRow;
import com.github.enerccio.marginalia.ui.workspace.ManuscriptSection;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceLLMSupport;
import com.github.enerccio.marginalia.ui.workspace.parts.NewBookAction;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.stream.Stream;

/**
 * The book list for phones. Rendered like the reader's book list in the Viewer (stacked filters, sort buttons and one
 * card-like column instead of a multi column grid) with the controls of the desktop book list (add, edit, delete,
 * refresh) and its folders: the path of the current folder to go back on, the folders above the books, the favorites
 * as a folder of the top level, the books of everything below. A folder is only navigated here: no folder is made,
 * renamed or deleted on the phone; a book is moved to another folder from its row.
 */
@Configurable
@Extendable
public class PhoneManuscriptPart implements ManuscriptSection {
    private static final Logger log = LoggerFactory.getLogger(PhoneManuscriptPart.class);

    private static final String SORT_MODIFICATION = "modification";
    private static final String SORT_CREATION = "creation";
    private static final String SORT_NAME = "name";

    @Autowired
    private Localization loc;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private TagService tagService;

    @Autowired
    private FolderService folderService;

    private final Workspace workspace;
    private Grid<FolderBookRow> grid;
    private Button addBookButton;
    private Button searchButton;
    private FlexLayout breadcrumb;
    private Checkbox includeSubfolders;

    // the folder the list shows, null is the top level
    private Folder currentFolder;
    // the list shows the favorite books (the virtual folder at the top level) instead of a folder
    private boolean inFavorites;

    private String currentSortField = SORT_MODIFICATION;
    private Ordering currentSortOrdering = Ordering.DESC;
    private final Map<String, Button> sortButtons = new LinkedHashMap<>();
    private final ManuscriptFilterValues filterValues = new ManuscriptFilterValues();

    // the filter fields (locals of create) and the book window that is open, remembered by the workspace autosave
    private TextField nameFilterField;
    private MultiSelectComboBox<Tag> tagFilterField;
    private PhoneManuscriptDialog openDialog;

    public PhoneManuscriptPart(Workspace workspace) {
        this.workspace = workspace;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class))
    public String getName() {
        return loc.getValue(L.LABEL_BOOKS);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "mainLayout", type = VerticalLayout.class),
                    @APILocals(name = "addBookButton", type = Button.class),
                    @APILocals(name = "refreshButton", type = Button.class),
                    @APILocals(name = "importArchiveButton", type = Button.class),
                    @APILocals(name = "importSillyTavernButton", type = Button.class),
                    @APILocals(name = "headerLayout", type = HorizontalLayout.class),
                    @APILocals(name = "navigationLayout", type = VerticalLayout.class),
                    @APILocals(name = "filtersLayout", type = VerticalLayout.class),
                    @APILocals(name = "nameFilter", type = TextField.class),
                    @APILocals(name = "tagFilter", type = MultiSelectComboBox.class),
                    @APILocals(name = "sortLayout", type = FlexLayout.class)})
    @Override
    public Component create() throws Exception {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        Button addBookButton = new Button(loc.getValue(L.LABEL_ADD_BOOK), Solid.PLUS.create(),
                event -> new NewBookAction(this::openManuscript, currentFolder).run());
        addBookButton.setThemeName("primary");
        this.addBookButton = addBookButton;

        Button refreshButton = new Button(Solid.REFRESH.create(), event -> refreshGrid());

        Button importArchiveButton = new Button(Solid.FILE_IMPORT.create(), event -> new NewBookAction(this::openManuscript, currentFolder).runFromArchive());
        importArchiveButton.setTooltipText(loc.getValue(L.LABEL_IMPORT_FROM_ARCHIVE));
        Button importSillyTavernButton = new Button(Solid.COMMENTS.create(), event -> new NewBookAction(this::openManuscript, currentFolder).runFromSillyTavern());
        importSillyTavernButton.setTooltipText(loc.getValue(L.LABEL_IMPORT_FROM_SILLYTAVERN));

        searchButton = new Button(Solid.SEARCH.create(), event -> openSearchDialog());
        searchButton.setTooltipText(loc.getValue(L.LABEL_ADVANCED_SEARCH));

        HorizontalLayout headerLayout = new HorizontalLayout(addBookButton, searchButton, importArchiveButton, importSillyTavernButton, refreshButton);
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setFlexGrow(1, addBookButton);

        // where the list is: the path of the folder to tap on, and whether the folders below it are listed too
        breadcrumb = new FlexLayout();
        breadcrumb.setAlignItems(FlexComponent.Alignment.CENTER);
        breadcrumb.addClassNames(CssStyles.FLEX_WRAP, CssStyles.GAP_S);
        includeSubfolders = new Checkbox(loc.getValue(L.LABEL_INCLUDE_SUBFOLDERS));
        includeSubfolders.addValueChangeListener(_ -> {
            refreshGrid();
            notifyStateChanged();
        });
        VerticalLayout navigationLayout = new VerticalLayout(breadcrumb, includeSubfolders);
        navigationLayout.setWidthFull();
        navigationLayout.setPadding(false);
        navigationLayout.setSpacing(false);

        VerticalLayout filtersLayout = new VerticalLayout();
        filtersLayout.setWidthFull();
        filtersLayout.setPadding(false);
        filtersLayout.setSpacing(true);

        TextField nameFilter = new TextField();
        nameFilter.setPlaceholder(loc.getValue(L.LABEL_NAME));
        nameFilter.setWidthFull();
        nameFilter.setClearButtonVisible(true);
        nameFilter.setValueChangeMode(ValueChangeMode.TIMEOUT);
        nameFilter.setValueChangeTimeout(200);
        nameFilterField = nameFilter;
        nameFilter.addValueChangeListener(event -> {
            filterValues.setName(event.getValue());
            refreshGrid();
            notifyStateChanged();
        });

        MultiSelectComboBox<Tag> tagFilter = new MultiSelectComboBox<>();
        tagFilter.setPlaceholder(loc.getValue(L.LABEL_TAGS));
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
        tagFilterField = tagFilter;
        tagFilter.addValueChangeListener(event -> {
            filterValues.setTags(event.getValue().stream().map(Tag::getValue).sorted().toList());
            refreshGrid();
            notifyStateChanged();
        });

        FlexLayout sortLayout = new FlexLayout();
        sortLayout.setWidthFull();
        sortLayout.addClassNames(CssStyles.FLEX_WRAP, CssStyles.GAP_S);
        addSortButton(sortLayout, SORT_MODIFICATION, loc.getValue(L.LABEL_LAST_MODIFIED), Ordering.DESC);
        addSortButton(sortLayout, SORT_CREATION, loc.getValue(L.LABEL_CREATED), Ordering.DESC);
        addSortButton(sortLayout, SORT_NAME, loc.getValue(L.LABEL_NAME), Ordering.ASC);
        updateSortButtons();

        filtersLayout.add(nameFilter, tagFilter, sortLayout);

        grid = new Grid<>();
        grid.setSizeFull();
        // rows grow with their content, so a long title breaks into lines instead of running under the buttons
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addComponentColumn(this::createItem).setFlexGrow(1);

        mainLayout.add(headerLayout, navigationLayout, filtersLayout, grid);
        mainLayout.setFlexGrow(1, grid);

        refreshGrid();
        return mainLayout;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "layout", type = FlexLayout.class),
                    @APIParameter(name = "field", type = String.class),
                    @APIParameter(name = "label", type = String.class),
                    @APIParameter(name = "defaultOrdering", type = Sorter.Ordering.class)},
            locals = {@APILocals(name = "button", type = Button.class)})
    private void addSortButton(FlexLayout layout, String field, String label, Ordering defaultOrdering) {
        Button button = new Button(label);
        button.addClickListener(event -> {
            if (field.equals(currentSortField)) {
                currentSortOrdering = currentSortOrdering == Ordering.ASC ? Ordering.DESC : Ordering.ASC;
            } else {
                currentSortField = field;
                currentSortOrdering = defaultOrdering;
            }
            updateSortButtons();
            refreshGrid();
        });
        sortButtons.put(field, button);
        layout.add(button);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    private void updateSortButtons() {
        sortButtons.forEach((field, button) -> {
            boolean active = field.equals(currentSortField);
            button.setThemeName(active ? "small primary" : "small tertiary");
            if (active) {
                button.setIcon((currentSortOrdering == Ordering.ASC ? Solid.ARROW_UP : Solid.ARROW_DOWN).create());
            } else {
                button.setIcon(null);
            }
        });
    }

    /**
     * A row of the list: a folder (or the favorites) opens when tapped, a book has its buttons.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "row", type = FolderBookRow.class)},
            locals = {@APILocals(name = "manuscript", type = Manuscript.class),
                    @APILocals(name = "textLayout", type = VerticalLayout.class),
                    @APILocals(name = "nameSpan", type = Span.class),
                    @APILocals(name = "tagsText", type = String.class),
                    @APILocals(name = "tagsSpan", type = Span.class),
                    @APILocals(name = "pathSpan", type = Span.class),
                    @APILocals(name = "modificationSpan", type = Span.class),
                    @APILocals(name = "editButton", type = Button.class),
                    @APILocals(name = "cloneButton", type = Button.class),
                    @APILocals(name = "deleteButton", type = Button.class),
                    @APILocals(name = "starButton", type = Button.class),
                    @APILocals(name = "moveButton", type = Button.class),
                    @APILocals(name = "firstRow", type = HorizontalLayout.class),
                    @APILocals(name = "secondRow", type = HorizontalLayout.class),
                    @APILocals(name = "actions", type = VerticalLayout.class),
                    @APILocals(name = "item", type = HorizontalLayout.class)})
    private Component createItem(FolderBookRow row) {
        if (row.isFavorites() || row.isFolder()) {
            return createFolderItem(row);
        }
        Manuscript manuscript = row.getBook();
        VerticalLayout textLayout = new VerticalLayout();
        textLayout.setPadding(false);
        textLayout.setSpacing(false);
        textLayout.setWidthFull();
        textLayout.addClassName(CssStyles.MIN_WIDTH_0);

        Span nameSpan = new Span(row.getName());
        nameSpan.addClassNames(CssStyles.TEXT_BOLD, CssStyles.TEXT_M, CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE);
        textLayout.add(nameSpan);

        String tagsText = String.join(", ", row.getTags());
        if (StringUtils.isNotBlank(tagsText)) {
            Span tagsSpan = new Span(tagsText);
            tagsSpan.addClassName(CssStyles.HINT_TEXT);
            textLayout.add(tagsSpan);
        }

        // the folder of the book, while the list is not of one folder
        if (StringUtils.isNotBlank(row.getPath())) {
            Span pathSpan = new Span(row.getPath());
            pathSpan.addClassNames(CssStyles.HINT_TEXT, CssStyles.WRAP_ANYWHERE);
            textLayout.add(pathSpan);
        }

        Span modificationSpan = new Span(loc.getDateHourFormat().format(manuscript.getModification()));
        modificationSpan.addClassName(CssStyles.HINT_TEXT);
        textLayout.add(modificationSpan);

        Button editButton = new Button(Solid.PENCIL.create(), _ -> {
            try {
                if (!new AgentLockCheck().refuses(manuscript)) {
                    openManuscript(manuscript);
                }
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });

        Button cloneButton = new Button(Solid.CLONE.create(), _ -> {
            ManuscriptCloneDialog dialog = new ManuscriptCloneDialog(manuscript);
            dialog.setOnClone(_ -> refreshGrid());
            dialog.create();
            dialog.open();
        });
        cloneButton.setTooltipText(loc.getValue(L.LABEL_CLONE_BOOK));

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

        Button starButton = new Button((row.isFavorite() ? VaadinIcon.STAR : VaadinIcon.STAR_O).create(), _ -> {
            try {
                manuscriptService.setFavorite(manuscript, !row.isFavorite());
                refreshGrid();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
        starButton.setTooltipText(loc.getValue(L.LABEL_FAVORITES));

        // the quick move: pick the folder, nothing else of the folder is edited on the phone
        Button moveButton = new Button(Solid.FOLDER_OPEN.create(), _ -> {
            if (!new AgentLockCheck().refuses(manuscript)) {
                new FolderActions().moveBooks(List.of(manuscript), manuscript.getFolder(), this::refreshGrid);
            }
        });
        moveButton.setTooltipText(loc.getValue(L.LABEL_MOVE_TO_FOLDER));

        // two short rows keep the text wide enough for a phone
        HorizontalLayout firstRow = new HorizontalLayout(editButton, cloneButton, deleteButton);
        firstRow.setSpacing(false);
        HorizontalLayout secondRow = new HorizontalLayout(starButton, moveButton);
        secondRow.setSpacing(false);
        VerticalLayout actions = new VerticalLayout(firstRow, secondRow);
        actions.setPadding(false);
        actions.setSpacing(false);
        actions.setWidth(null);
        actions.addClassName(CssStyles.FLEX_SHRINK_0);

        HorizontalLayout item = new HorizontalLayout(textLayout, actions);
        item.setWidthFull();
        item.setPadding(false);
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.setFlexGrow(1, textLayout);
        return item;
    }

    /**
     * The row of a folder or of the favorites: its name and how many books are beneath it, tapping it goes in.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "row", type = FolderBookRow.class)},
            locals = {@APILocals(name = "textLayout", type = VerticalLayout.class),
                    @APILocals(name = "nameSpan", type = Span.class),
                    @APILocals(name = "nameLine", type = HorizontalLayout.class),
                    @APILocals(name = "booksSpan", type = Span.class),
                    @APILocals(name = "tagsText", type = String.class),
                    @APILocals(name = "tagsSpan", type = Span.class),
                    @APILocals(name = "pathSpan", type = Span.class)})
    private Component createFolderItem(FolderBookRow row) {
        VerticalLayout textLayout = new VerticalLayout();
        textLayout.setPadding(false);
        textLayout.setSpacing(false);
        textLayout.setWidthFull();
        textLayout.addClassNames(CssStyles.MIN_WIDTH_0, CssStyles.CURSOR_POINTER);

        Span nameSpan = new Span(row.getName());
        nameSpan.addClassNames(CssStyles.TEXT_BOLD, CssStyles.TEXT_M, CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE);
        HorizontalLayout nameLine = new HorizontalLayout((row.isFavorites() ? VaadinIcon.STAR : VaadinIcon.FOLDER).create(), nameSpan);
        nameLine.setPadding(false);
        nameLine.setSpacing(true);
        nameLine.setAlignItems(FlexComponent.Alignment.CENTER);
        textLayout.add(nameLine);

        Span booksSpan = new Span(loc.getValue(L.LABEL_BOOKS) + ": " + row.getBooks());
        booksSpan.addClassName(CssStyles.HINT_TEXT);
        textLayout.add(booksSpan);

        String tagsText = String.join(", ", row.getTags());
        if (StringUtils.isNotBlank(tagsText)) {
            Span tagsSpan = new Span(tagsText);
            tagsSpan.addClassName(CssStyles.HINT_TEXT);
            textLayout.add(tagsSpan);
        }

        // the folder above it, while the list is not of one folder
        if (row.isFolder() && StringUtils.isNotBlank(row.getPath())) {
            Span pathSpan = new Span(row.getPath());
            pathSpan.addClassNames(CssStyles.HINT_TEXT, CssStyles.WRAP_ANYWHERE);
            textLayout.add(pathSpan);
        }

        textLayout.addClickListener(_ -> {
            if (row.isFavorites()) {
                openFavorites();
            } else {
                openFolder(row.getFolder());
            }
        });
        return textLayout;
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
                    @APILocals(name = "sorter", type = Sorter.class),
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
            Sorter sorter = Sorter.sorter(currentSortField, currentSortOrdering);
            List<String> favorites = manuscriptService.getFavorites();
            if (favorites.isEmpty()) {
                inFavorites = false;
            }
            List<Folder> folders;
            List<Long> ids;
            String favoritesLabel = null;
            if (inFavorites) {
                // the favorites are a view of books of any folder: the filters work, there is no scope
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
            includeSubfolders.setVisible(!inFavorites);
            addBookButton.setEnabled(!inFavorites);
            updateBreadcrumb();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
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
     * Opens the book window. The window tells the workspace autosave about its navigation, and about closing.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = PhoneManuscriptDialog.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)},
            locals = {@APILocals(name = "dialog", type = PhoneManuscriptDialog.class)})
    private PhoneManuscriptDialog openManuscript(Manuscript manuscript) throws Exception {
        manuscriptService.markOpened(manuscript);
        PhoneManuscriptDialog dialog = new PhoneManuscriptDialog(manuscript);
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
     * The folder and the filters of the list and the open book window.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = JsonObject.class),
            locals = {@APILocals(name = "state", type = JsonObject.class),
                    @APILocals(name = "filters", type = JsonObject.class)})
    @Override
    public JsonObject storeState() {
        JsonObject state = new JsonObject();
        JsonObject filters = new JsonObject();
        if (nameFilterField != null && StringUtils.isNotBlank(nameFilterField.getValue())) {
            filters.addProperty("name", nameFilterField.getValue());
        }
        if (tagFilterField != null && !tagFilterField.getValue().isEmpty()) {
            filters.add("tags", RestorableState.array(tagFilterField.getValue().stream().map(Tag::getUuid).sorted().toList()));
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

            JsonObject filters = RestorableState.object(self, "filters");
            if (filters != null && nameFilterField != null) {
                nameFilterField.setValue(RestorableState.string(filters, "name", ""));
                Set<Tag> tags = new LinkedHashSet<>();
                for (String uuid : RestorableState.strings(filters, "tags")) {
                    Tag tag = tagService.findForUser(uuid);
                    if (tag != null) {
                        tags.add(tag);
                    }
                }
                tagFilterField.setValue(tags);
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
}
