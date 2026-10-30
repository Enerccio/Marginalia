package com.github.enerccio.marginalia.ui.main;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.bound.ApplicationPoint;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import com.github.enerccio.marginalia.domain.service.search.Sorter.Ordering;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.components.MessageImages;
import com.github.enerccio.marginalia.ui.dialogs.story.ReaderLazyLoader;
import com.github.enerccio.marginalia.ui.dialogs.story.StorySupport;
import com.github.enerccio.marginalia.ui.widgets.FolderBookProvider;
import com.github.enerccio.marginalia.ui.widgets.FolderBookRow;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Route("view")
@Configurable(preConstruction = true)
@Extendable
public class Viewer extends LoginCheckRoute implements HasUrlParameter<String> {

    private static final Logger log = LoggerFactory.getLogger(Viewer.class);

    @Autowired
    private User user;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private ApplicationPoint applicationPoint;

    @Autowired
    private OsgiService osgiService;

    private CssRegistry cssRegistry;

    private String manuscriptParam;
    private boolean isUserLoggedIn = false;

    // the value of the cookie of the list when the list shows the favorites; a folder is stored as its uuid
    private static final String FAVORITES_MARKER = "favorites";

    private Grid<FolderBookRow> grid;
    private TextField nameFilter;
    private MultiSelectComboBox<Tag> tagFilter;
    private FlexLayout breadcrumb;
    private Checkbox includeSubfolders;

    // the folder the list shows, null is the top level
    private Folder currentFolder;
    // the list shows the favorite books (the virtual folder at the top level) instead of a folder
    private boolean inFavorites;

    private String currentSortField = "lastOpened";
    private Ordering currentSortOrdering = Ordering.DESC;
    private final ManuscriptFilterValues filterValues = new ManuscriptFilterValues();

    public Viewer() {
        showLogin();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class))
    @Override
    protected String getAppTitle() {
        return loc.getValue(L.LABEL_VIEWER_TITLE);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class))
    @Override
    protected String getAppDescription() {
        return loc.getValue(L.LABEL_VIEWER_DESC);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = boolean.class),
            parameters = {@APIParameter(name = "userName", type = String.class),
                    @APIParameter(name = "password", type = String.class)})
    @Override
    protected boolean authenticate(String userName, String password) throws Exception {
        return userService.authenticate(userName, password, clientAddress());
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "attachEvent", type = AttachEvent.class)})
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // called also by the login, before the route is attached, so the registry is created on demand
        if (cssRegistry == null) {
            cssRegistry = new CssRegistry(this, osgiService);
        }
        cssRegistry.attach(attachEvent.getUI());
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "detachEvent", type = DetachEvent.class)})
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (cssRegistry != null) {
            cssRegistry.detach();
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "username", type = String.class)},
            locals = {@APILocals(name = "u", type = User.class),
                    @APILocals(name = "referenceCopy", type = User.class),
                    @APILocals(name = "e", type = Exception.class)})
    @Override
    protected void proceedWithLogin(String username) {
        try {
            User u = userService.findByName(username);

            user.setId(u.getId());
            user.setLogin(u.getLogin());
            user.setFullName(u.getFullName());

            isUserLoggedIn = true;
            sessionManager.userLoggedIn(user, VaadinSession.getCurrent());
            User referenceCopy = new User();
            referenceCopy.setId(u.getId());
            referenceCopy.setLogin(u.getLogin());
            referenceCopy.setFullName(u.getFullName());
            applicationPoint.register(UI.getCurrent(), null, referenceCopy);
            renderContent();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "event", type = BeforeEvent.class),
                    @APIParameter(name = "parameter", type = String.class)})
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        this.manuscriptParam = parameter;
        if (isUserLoggedIn) {
            renderContent();
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    private void renderContent() {
        removeAll();

        if (StringUtils.isBlank(manuscriptParam)) {
            renderGridView();
        } else {
            renderManuscriptReadingView(manuscriptParam);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "mainLayout", type = VerticalLayout.class),
                    @APILocals(name = "headerLayout", type = HorizontalLayout.class),
                    @APILocals(name = "titleSpan", type = Span.class),
                    @APILocals(name = "filtersLayout", type = VerticalLayout.class),
                    @APILocals(name = "sortButtonsLayout", type = HorizontalLayout.class),
                    @APILocals(name = "sortDateBtn", type = Button.class),
                    @APILocals(name = "sortNameBtn", type = Button.class)})
    private void renderGridView() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Span titleSpan = new Span(loc.getValue(L.LABEL_BOOKS));
        titleSpan.addClassName(CssStyles.SECTION_TITLE);

        headerLayout.add(titleSpan);

        // Top Filters Layout (stacked nicely for mobile screens)
        VerticalLayout filtersLayout = new VerticalLayout();
        filtersLayout.setWidthFull();
        filtersLayout.setPadding(false);
        filtersLayout.setSpacing(true);

        // where the list is: the path of the folder to tap on, and whether the folders below it are listed too
        breadcrumb = new FlexLayout();
        breadcrumb.setAlignItems(FlexComponent.Alignment.CENTER);
        breadcrumb.addClassNames(CssStyles.FLEX_WRAP, CssStyles.GAP_S);
        includeSubfolders = new Checkbox(loc.getValue(L.LABEL_INCLUDE_SUBFOLDERS));
        includeSubfolders.addValueChangeListener(event -> refreshGrid());

        nameFilter = new TextField();
        nameFilter.setPlaceholder(loc.getValue(L.LABEL_NAME));
        nameFilter.setWidthFull();
        nameFilter.setClearButtonVisible(true);
        nameFilter.setValueChangeMode(ValueChangeMode.TIMEOUT);
        nameFilter.setValueChangeTimeout(200);
        nameFilter.addValueChangeListener(event -> {
            filterValues.setName(event.getValue());
            refreshGrid();
        });

        tagFilter = new MultiSelectComboBox<>();
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
        tagFilter.addValueChangeListener(event -> {
            filterValues.setTags(event.getValue().stream().map(Tag::getValue).sorted().toList());
            refreshGrid();
        });

        // Sort buttons
        HorizontalLayout sortButtonsLayout = new HorizontalLayout();
        sortButtonsLayout.setWidthFull();
        sortButtonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Button sortDateBtn = new Button(loc.getValue(L.LABEL_LAST_OPENED), Solid.SORT_AMOUNT_DOWN.create());
        sortDateBtn.setThemeName("small primary");

        Button sortNameBtn = new Button(loc.getValue(L.LABEL_NAME), Solid.SORT_ALPHA_DOWN.create());
        sortNameBtn.setThemeName("small tertiary");

        sortDateBtn.addClickListener(e -> {
            if ("lastOpened".equals(currentSortField)) {
                currentSortOrdering = (currentSortOrdering == Ordering.DESC) ? Ordering.ASC : Ordering.DESC;
            } else {
                currentSortField = "lastOpened";
                currentSortOrdering = Ordering.DESC;
            }
            sortDateBtn.setThemeName("small primary");
            sortNameBtn.setThemeName("small tertiary");
            refreshGrid();
        });

        sortNameBtn.addClickListener(e -> {
            if ("name".equals(currentSortField)) {
                currentSortOrdering = (currentSortOrdering == Ordering.ASC) ? Ordering.DESC : Ordering.ASC;
            } else {
                currentSortField = "name";
                currentSortOrdering = Ordering.ASC;
            }
            sortNameBtn.setThemeName("small primary");
            sortDateBtn.setThemeName("small tertiary");
            refreshGrid();
        });

        sortButtonsLayout.add(sortDateBtn, sortNameBtn);

        filtersLayout.add(breadcrumb, includeSubfolders, nameFilter, tagFilter, sortButtonsLayout);

        // Single Column Grid
        grid = new Grid<>();
        grid.setSizeFull();
        // rows grow with their content, so a long title breaks into lines
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addComponentColumn(this::createItem).setFlexGrow(1);

        grid.addItemClickListener(event -> {
            FolderBookRow row = event.getItem();
            if (row == null) {
                return;
            }
            if (row.isFavorites()) {
                openFavorites();
            } else if (row.isFolder()) {
                openFolder(row.getFolder());
            } else if (row.getBook() != null) {
                Manuscript manuscript = row.getBook();
                try {
                    manuscriptService.markOpened(manuscript);
                } catch (Exception e) {
                    log.warn("Failed to mark manuscript {} as opened", manuscript.getUuid(), e);
                }
                UI.getCurrent().navigate(Viewer.class, manuscript.getUuid());
            }
        });

        mainLayout.add(headerLayout, filtersLayout, grid);
        mainLayout.setFlexGrow(1, grid);
        add(mainLayout);

        // the list opens where it was left, which the browser remembers (a cookie, never the server)
        JavascriptLibrary.readViewerFolder(UI.getCurrent().getPage()).then(String.class,
                saved -> {
                    restoreLocation(saved);
                    refreshGrid();
                },
                error -> refreshGrid());
    }

    /**
     * The row of the list: a folder (or the favorites) with the number of its books, or a book with its tags and when it was
     * opened last.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "row", type = FolderBookRow.class)},
            locals = {@APILocals(name = "itemLayout", type = VerticalLayout.class),
                    @APILocals(name = "nameSpan", type = Span.class),
                    @APILocals(name = "nameLine", type = HorizontalLayout.class),
                    @APILocals(name = "subRow", type = HorizontalLayout.class),
                    @APILocals(name = "booksSpan", type = Span.class),
                    @APILocals(name = "tagsText", type = String.class),
                    @APILocals(name = "tagsSpan", type = Span.class),
                    @APILocals(name = "book", type = Manuscript.class),
                    @APILocals(name = "date", type = Date.class),
                    @APILocals(name = "modSpan", type = Span.class),
                    @APILocals(name = "pathSpan", type = Span.class)})
    private Component createItem(FolderBookRow row) {
        VerticalLayout itemLayout = new VerticalLayout();
        itemLayout.setPadding(false);
        itemLayout.setSpacing(false);
        itemLayout.setWidthFull();

        Span nameSpan = new Span(row.getName());
        nameSpan.addClassNames(CssStyles.TEXT_BOLD, CssStyles.TEXT_M, CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE);
        if (row.isFavorites() || row.isFolder()) {
            HorizontalLayout nameLine = new HorizontalLayout(
                    (row.isFavorites() ? VaadinIcon.STAR : VaadinIcon.FOLDER).create(), nameSpan);
            nameLine.setPadding(false);
            nameLine.setSpacing(true);
            nameLine.setAlignItems(FlexComponent.Alignment.CENTER);
            itemLayout.add(nameLine);
        } else {
            itemLayout.add(nameSpan);
        }

        HorizontalLayout subRow = new HorizontalLayout();
        subRow.setWidthFull();
        subRow.setSpacing(true);
        subRow.addClassName(CssStyles.LIST_ITEM_SUBROW);

        if (row.isFavorites() || row.isFolder()) {
            Span booksSpan = new Span(loc.getValue(L.LABEL_BOOKS) + ": " + row.getBooks());
            subRow.add(booksSpan);
        }

        String tagsText = String.join(", ", row.getTags());
        if (StringUtils.isNotBlank(tagsText)) {
            Span tagsSpan = new Span(tagsText);
            subRow.add(tagsSpan);
        }

        if (!row.isFavorites() && !row.isFolder()) {
            Manuscript book = row.getBook();
            Date date = book.getLastOpened() != null ? book.getLastOpened() : book.getModification();
            if (date != null) {
                Span modSpan = new Span(loc.getDateHourFormat().format(date));
                modSpan.addClassName(CssStyles.MARGIN_LEFT_AUTO);
                subRow.add(modSpan);
            }
        }
        if (subRow.getComponentCount() > 0) {
            itemLayout.add(subRow);
        }

        // the folder the row is in, while the list is not of one folder (the subfolders, the favorites)
        if ((includeSubfolders.getValue() || inFavorites) && StringUtils.isNotBlank(row.getPath())) {
            Span pathSpan = new Span(row.getPath());
            pathSpan.addClassNames(CssStyles.HINT_TEXT, CssStyles.WRAP_ANYWHERE);
            itemLayout.add(pathSpan);
        }
        return itemLayout;
    }

    /**
     * Goes into the folder (null is the top level); the filters stay. The place is remembered by the browser.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "folder", type = Folder.class)})
    private void openFolder(Folder folder) {
        inFavorites = false;
        currentFolder = folder;
        refreshGrid();
        rememberLocation();
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
        rememberLocation();
    }

    /**
     * Puts the place of the list into the cookie: the uuid of the folder, the marker of the favorites, empty for the top level.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "value", type = String.class)})
    private void rememberLocation() {
        String value = inFavorites ? FAVORITES_MARKER : currentFolder == null ? "" : currentFolder.getUuid();
        JavascriptLibrary.rememberViewerFolder(UI.getCurrent().getPage(), value);
    }

    /**
     * Sets the place of the list from what the cookie held. A folder that is gone, or is not the user's (another user used the
     * browser before), is the top level.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "saved", type = String.class)},
            locals = {@APILocals(name = "folder", type = Folder.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void restoreLocation(String saved) {
        inFavorites = FAVORITES_MARKER.equals(saved);
        currentFolder = null;
        if (StringUtils.isBlank(saved) || inFavorites) {
            return;
        }
        try {
            Folder folder = folderService.findForUser(saved);
            currentFolder = folder == null || folder.isDeleted() ? null : folder;
        } catch (Exception e) {
            log.debug("Could not restore the folder of the viewer", e);
        }
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
        Button top = new Button(loc.getValue(L.LABEL_BOOKS), VaadinIcon.HOME.create(), event -> openFolder(null));
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
            Button button = new Button(above.getName(), event -> openFolder(above));
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            breadcrumb.add(button);
        }
        breadcrumb.add(new Span("/"));
        Span current = new Span(currentFolder.getName());
        current.addClassName(CssStyles.TEXT_BOLD);
        breadcrumb.add(current);
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "stored", type = Folder.class),
                    @APILocals(name = "sorters", type = List.class),
                    @APILocals(name = "folderSorter", type = Sorter.class),
                    @APILocals(name = "favorites", type = List.class),
                    @APILocals(name = "folders", type = List.class),
                    @APILocals(name = "ids", type = List.class),
                    @APILocals(name = "favoritesLabel", type = String.class),
                    @APILocals(name = "all", type = ManuscriptFilterValues.class),
                    @APILocals(name = "scope", type = ManuscriptFilterValues.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void refreshGrid() {
        if (grid == null) {
            return;
        }
        try {
            if (currentFolder != null) {
                // the folder may have been renamed or deleted since the list was shown
                Folder stored = folderService.find(currentFolder.getId());
                currentFolder = stored == null || stored.isDeleted() ? null : stored;
            }
            List<Sorter> sorters = new ArrayList<>();
            sorters.add(Sorter.sorter(currentSortField, currentSortOrdering));
            if ("lastOpened".equals(currentSortField)) {
                // never opened books have no lastOpened, order them by modification
                sorters.add(Sorter.sorter("modification", currentSortOrdering));
            }
            // a folder has no last opened time, its last modification is what its books say
            Sorter folderSorter = Sorter.sorter("name".equals(currentSortField) ? "name" : "modification", currentSortOrdering);

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
                ids = manuscriptService.searchRegularManuscripts(all, sorters.toArray(Sorter[]::new));
            } else {
                ManuscriptFilterValues scope = filterValues.inFolder(currentFolder, includeSubfolders.getValue());
                folders = folderService.search(scope, folderSorter);
                ids = manuscriptService.searchRegularManuscripts(scope, sorters.toArray(Sorter[]::new));
                boolean filtered = StringUtils.isNotEmpty(filterValues.getName())
                        || (filterValues.getTags() != null && !filterValues.getTags().isEmpty());
                if (!favorites.isEmpty() && currentFolder == null && !includeSubfolders.getValue() && !filtered) {
                    favoritesLabel = loc.getValue(L.LABEL_FAVORITES);
                }
            }
            grid.setItems(new FolderBookProvider(folders, ids, folderService.countBooksByFolder(), folderService.getPaths(),
                    favorites, favoritesLabel));
            includeSubfolders.setVisible(!inFavorites);
            updateBreadcrumb();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "param", type = String.class)},
            locals = {@APILocals(name = "manuscript", type = Manuscript.class),
                    @APILocals(name = "notFoundLayout", type = VerticalLayout.class),
                    @APILocals(name = "errorSpan", type = Span.class),
                    @APILocals(name = "backButton", type = Button.class),
                    @APILocals(name = "container", type = VerticalLayout.class),
                    @APILocals(name = "scrollPanel", type = ScrollPanel.class),
                    @APILocals(name = "contentWrapper", type = VerticalLayout.class),
                    @APILocals(name = "branch", type = List.class),
                    @APILocals(name = "emptyLabel", type = Span.class),
                    @APILocals(name = "manuscriptUuid", type = String.class),
                    @APILocals(name = "loader", type = ReaderLazyLoader.class),
                    @APILocals(name = "limit", type = Integer.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void renderManuscriptReadingView(String param) {
        Manuscript manuscript = findManuscript(param);

        if (manuscript == null) {
            VerticalLayout notFoundLayout = new VerticalLayout();
            notFoundLayout.setSizeFull();
            notFoundLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            notFoundLayout.setAlignItems(FlexComponent.Alignment.CENTER);

            Span errorSpan = new Span(loc.getValue(L.MSG_MANUSCRIPT_NOT_FOUND));
            Button backButton = new Button(Solid.ARROW_LEFT.create(), event -> UI.getCurrent().navigate(Viewer.class));
            backButton.setThemeName("primary");

            notFoundLayout.add(errorSpan, backButton);
            add(notFoundLayout);
            return;
        }

        VerticalLayout container = new VerticalLayout();
        container.setSizeFull();
        container.setPadding(false);
        container.setSpacing(false);
        container.addClassName(CssStyles.POSITION_RELATIVE);

        // Always apply reading style
        container.addClassName(CssStyles.MARKDOWN_MANUSCRIPT_STYLES);

        // Simple floating back button optimized for phone/touch
        Button backButton = new Button(Solid.ARROW_LEFT.create(), event -> UI.getCurrent().navigate(Viewer.class));
        backButton.setThemeName("primary icon");
        backButton.addClassName(CssStyles.FLOATING_BACK_BUTTON);

        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.setSizeFull();
        scrollPanel.addClassName(CssStyles.READER_SCROLL_PANEL);

        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setWidthFull();
        contentWrapper.setMaxWidth("800px");
        contentWrapper.addClassName(CssStyles.READER_CONTENT);
        contentWrapper.setPadding(false);
        contentWrapper.setSpacing(true);

        try {
            // lang drives the browser's hyphenation dictionary (see hyphens in shared-styles.css)
            container.getElement().setAttribute("lang", manuscriptService.getLanguage(manuscript).getCode());

            List<ChatMessage> branch = (manuscript.getActiveLeaf() != null)
                    ? chatMessageService.getBranchFromLeaf(manuscript.getActiveLeaf())
                    : Collections.emptyList();

            if (branch.isEmpty()) {
                Span emptyLabel = new Span(loc.getValue(L.MSG_NO_ACTIVE_BRANCH));
                contentWrapper.add(UIUtils.centerComponent(emptyLabel));
            } else {
                // the parts around where the reader stopped last time (the browser remembers it, never the server)
                // or the first ones, the others are loaded as the reader scrolls
                String manuscriptUuid = String.valueOf(manuscript.getUuid());
                ReaderLazyLoader loader = new ReaderLazyLoader(scrollPanel, contentWrapper, this::createPart, loc);
                Integer limit = new StorySupport().partsLimit();
                JavascriptLibrary.rememberReaderPosition(scrollPanel.getElement(), contentWrapper.getElement(), manuscriptUuid);
                JavascriptLibrary.readReaderPosition(UI.getCurrent().getPage(), manuscriptUuid).then(String.class,
                        saved -> loader.start(branch, limit, parseMessageId(saved)),
                        error -> loader.start(branch, limit, null));
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }

        scrollPanel.add(contentWrapper);
        container.add(backButton, scrollPanel);
        container.setFlexGrow(1, scrollPanel);

        add(container);
    }

    /**
     * The component of a part of the book: its text and images, {@code null} when it has neither.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "msg", type = ChatMessage.class)},
            locals = {@APILocals(name = "part", type = VerticalLayout.class),
                    @APILocals(name = "markdown", type = Markdown.class)})
    private Component createPart(ChatMessage msg) {
        VerticalLayout part = new VerticalLayout();
        part.setWidthFull();
        part.setPadding(false);
        part.setSpacing(true);
        if (StringUtils.isNotBlank(msg.getResponse())) {
            Markdown markdown = new Markdown();
            markdown.addClassName(CssStyles.CHAT_MESSAGE_MARKDOWN);
            markdown.setContent(msg.getResponse());

            part.add(markdown);
        }
        if (!msg.getImages().isEmpty()) {
            part.add(new MessageImages(msg.getImages()));
        }
        return part.getComponentCount() == 0 ? null : part;
    }

    /**
     * The id of the part the browser remembered, {@code null} when there is none.
     */
    private static Long parseMessageId(String saved) {
        try {
            return saved == null ? null : Long.valueOf(saved);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Only uuid is accepted, manuscripts that are not published are visible only to their owner. Anything else
     * looks the same as nonexistent manuscript.
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Manuscript.class),
            parameters = {@APIParameter(name = "param", type = String.class)},
            locals = {@APILocals(name = "e", type = Exception.class)})
    private Manuscript findManuscript(String param) {
        try {
            return manuscriptService.findViewable(param);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return null;
        }
    }
}
