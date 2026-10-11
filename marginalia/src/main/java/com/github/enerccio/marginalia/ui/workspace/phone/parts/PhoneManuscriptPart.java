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
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.widgets.BackendTableItem;
import com.github.enerccio.marginalia.ui.widgets.BackendTableProviderBase;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.github.enerccio.marginalia.ui.workspace.parts.NewBookAction;
import com.github.enerccio.marginalia.utils.UIUtils;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    public PhoneManuscriptPart(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getName() {
        return loc.getValue(L.LABEL_BOOKS);
    }

    @Override
    public Component create() throws Exception {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        Button addBookButton = new Button(loc.getValue(L.LABEL_ADD_BOOK), Solid.PLUS.create(),
                event -> new NewBookAction(this::refreshGrid).run());
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
        nameFilter.addValueChangeListener(event -> {
            filterValues.setName(event.getValue());
            refreshGrid();
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
        tagFilter.addValueChangeListener(event -> {
            filterValues.setTags(event.getValue().stream().map(Tag::getValue).sorted().toList());
            refreshGrid();
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
                manuscriptService.markOpened(wrapper.getManuscript());
                ManuscriptDialog dialog = new ManuscriptDialog(wrapper.getManuscript());
                dialog.setOnClose(this::refreshGrid);
                dialog.create();
                dialog.open();
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

    @Override
    public void refresh() throws Exception {
        refreshGrid();
    }

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

    @Override
    public void onTabSwitched() throws Exception {
        refresh();
    }

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
                    return "ERROR";
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
