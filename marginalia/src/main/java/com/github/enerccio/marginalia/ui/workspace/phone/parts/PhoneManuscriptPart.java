package com.github.enerccio.marginalia.ui.workspace.phone.parts;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
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
import com.github.enerccio.marginalia.ui.RestorableState;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialog;
import com.github.enerccio.marginalia.ui.widgets.BackendTableItem;
import com.github.enerccio.marginalia.ui.widgets.BackendTableProviderBase;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.github.enerccio.marginalia.ui.workspace.parts.NewBookAction;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
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
 * refresh).
 */
@Configurable
@Extendable
public class PhoneManuscriptPart implements WorkspaceComponent {
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
    private TagRelationService tagRelationService;

    private final Workspace workspace;
    private Grid<ManuscriptWrapper> grid;

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
                    @APILocals(name = "headerLayout", type = HorizontalLayout.class),
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
                event -> new NewBookAction(this::refreshGrid, true).run());
        addBookButton.setThemeName("primary");

        Button refreshButton = new Button(Solid.REFRESH.create(), event -> refreshGrid());

        HorizontalLayout headerLayout = new HorizontalLayout(addBookButton, refreshButton);
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setFlexGrow(1, addBookButton);

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

        mainLayout.add(headerLayout, filtersLayout, grid);
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

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "wrapper", type = PhoneManuscriptPart.ManuscriptWrapper.class)},
            locals = {@APILocals(name = "textLayout", type = VerticalLayout.class),
                    @APILocals(name = "nameSpan", type = Span.class),
                    @APILocals(name = "tagsText", type = String.class),
                    @APILocals(name = "tagsSpan", type = Span.class),
                    @APILocals(name = "modificationSpan", type = Span.class),
                    @APILocals(name = "editButton", type = Button.class),
                    @APILocals(name = "deleteButton", type = Button.class),
                    @APILocals(name = "actions", type = HorizontalLayout.class),
                    @APILocals(name = "item", type = HorizontalLayout.class)})
    private Component createItem(ManuscriptWrapper wrapper) {
        VerticalLayout textLayout = new VerticalLayout();
        textLayout.setPadding(false);
        textLayout.setSpacing(false);
        textLayout.setWidthFull();
        textLayout.addClassName(CssStyles.MIN_WIDTH_0);

        Span nameSpan = new Span(wrapper.getName());
        nameSpan.addClassNames(CssStyles.TEXT_BOLD, CssStyles.TEXT_M, CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE);
        textLayout.add(nameSpan);

        String tagsText = wrapper.getTags();
        if (StringUtils.isNotBlank(tagsText)) {
            Span tagsSpan = new Span(tagsText);
            tagsSpan.addClassName(CssStyles.HINT_TEXT);
            textLayout.add(tagsSpan);
        }

        Span modificationSpan = new Span(wrapper.getModification());
        modificationSpan.addClassName(CssStyles.HINT_TEXT);
        textLayout.add(modificationSpan);

        Button editButton = new Button(Solid.PENCIL.create(), _ -> {
            try {
                openManuscript(wrapper.getManuscript());
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });

        Button deleteButton = new Button(Solid.TRASH.create(), _ ->
                ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
                    try {
                        manuscriptService.delete(wrapper.getManuscript(), false);
                        refreshGrid();
                    } catch (Exception e) {
                        UIUtils.internalServerError(loc, e);
                    }
                }));
        deleteButton.setThemeName("error tertiary");

        HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
        actions.setSpacing(false);
        actions.addClassName(CssStyles.FLEX_SHRINK_0);

        HorizontalLayout item = new HorizontalLayout(textLayout, actions);
        item.setWidthFull();
        item.setPadding(false);
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.setFlexGrow(1, textLayout);
        return item;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void refresh() throws Exception {
        refreshGrid();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "ids", type = List.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void refreshGrid() {
        if (grid == null) {
            return;
        }
        try {
            List<Long> ids = manuscriptService.searchManuscripts(filterValues,
                    Sorter.sorter(currentSortField, currentSortOrdering));
            grid.setItems(new ManuscriptBackendProvider(ids));
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
        if (nameFilterField != null && StringUtils.isNotBlank(nameFilterField.getValue())) {
            filters.addProperty("name", nameFilterField.getValue());
        }
        if (tagFilterField != null && !tagFilterField.getValue().isEmpty()) {
            filters.add("tags", RestorableState.array(tagFilterField.getValue().stream().map(Tag::getUuid).sorted().toList()));
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
                if (manuscript != null) {
                    openManuscript(manuscript).restoreState(dialogState);
                }
            }
        } catch (Exception e) {
            log.warn("Could not restore the state of the book list", e);
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
