package com.github.enerccio.marginalia.ui.workspace.parts;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import com.github.enerccio.marginalia.domain.service.search.Sorter.Ordering;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.TextInputDialog;
import com.github.enerccio.marginalia.ui.widgets.BackendTableItem;
import com.github.enerccio.marginalia.ui.widgets.BackendTableProviderBase;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Configurable
@Extendable
public class ManuscriptPart implements WorkspaceComponent {
    private static final Logger log = LoggerFactory.getLogger(ManuscriptPart.class);

    @Autowired
    private Localization loc;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    private final Workspace workspace;
    private Grid<ManuscriptWrapper> grid;
    private VerticalLayout mainLayout;

    private Column<ManuscriptWrapper> nameColumn;
    private TextField nameFilter;
    private Column<ManuscriptWrapper> tagsColumn;
    private MultiSelectComboBox<Tag> tagFilter;
    private Column<ManuscriptWrapper> creationColumn;
    private Column<ManuscriptWrapper> modificationColumn;

    private final List<GridSortOrder<ManuscriptWrapper>> sorters = new ArrayList<>();
    private final ManuscriptFilterValues filterValues = new ManuscriptFilterValues();

    public ManuscriptPart(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getName() {
        return loc.getValue(L.LABEL_BOOKS);
    }

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
        titleSpan.getStyle().set("font-size", "var(--lumo-font-size-l)");
        titleSpan.getStyle().set("font-weight", "bold");

        Button addBookButton = new Button(loc.getValue(L.LABEL_ADD_BOOK), event -> {
            TextInputDialog dialog = new TextInputDialog.Builder(loc.getValue(L.LABEL_NEW_BOOK), name -> {
                try {
                    Manuscript manuscript = new Manuscript();
                    manuscript.setName(name);

                    UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
                    Long defaultModelId = userSetting.getDefaultModel();
                    if (defaultModelId != null) {
                        AI ai = aiService.find(defaultModelId);
                        if (ai != null && !ai.isDeleted()) {
                            manuscript.setAi(ai);
                        }
                    }
                    Long defaultProtocolId = userSetting.getDefaultProtocol();
                    if (defaultProtocolId != null) {
                        Protocol protocol = protocolService.find(defaultProtocolId);
                        if (protocol != null && !protocol.isDeleted()) {
                            manuscript.setProtocol(protocol);
                        }
                    }

                    manuscript = manuscriptService.save(manuscript);
                    ManuscriptDialog manuscriptDialog = new ManuscriptDialog(manuscript);
                    manuscriptDialog.setOnClose(this::refreshGrid);
                    manuscriptDialog.create();
                    manuscriptDialog.open();
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            }).messageRequired().showCancel(true).build();
            dialog.open();
        });
        addBookButton.setThemeName("primary");

        headerLayout.add(titleSpan, addBookButton);
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
                    ManuscriptDialog dialog = new ManuscriptDialog(manuscriptWrapper.getManuscript());
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
                            manuscriptService.delete(manuscriptWrapper.getManuscript(), false);
                            refreshGrid();
                        } catch (Exception e) {
                            UIUtils.internalServerError(loc, e);
                        }
                    }));
            deleteButton.setThemeName("error tertiary");

            actions.add(editButton, deleteButton);
            return actions;
        }).setHeader("").setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE_HUGE);

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

    @Override
    public void refresh() throws Exception {
        refreshGrid();
    }

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
            List<Long> ids = manuscriptService.searchManuscripts(filterValues, sortInfos.toArray(Sorter[]::new));
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