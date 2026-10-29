package com.github.enerccio.marginalia.ui.dialogs;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptSearchCriteria;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptSearchCriteria.TagMatch;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import com.github.enerccio.marginalia.domain.service.search.Sorter.Ordering;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * The advanced search of the book list (the desktop and the phone one): the name, the tags the book has to carry and the
 * ones it must not (all or any of them), when it was created and last modified, the number of its parts and the text in
 * it (in the branch the book is at, or in all of them). The books found are listed below with their tags, and can be
 * opened from the list. The search itself is {@link ManuscriptService#searchAdvanced}.
 * <p>
 * It can be limited to a folder (with the books below it on request), and then a result can be shown in its folder instead of opened.
 */
@Configurable(preConstruction = true)
public class ManuscriptSearchDialog extends Dialog {
    private static final Logger log = LoggerFactory.getLogger(ManuscriptSearchDialog.class);

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

    // null: the book can't be opened from here
    private final Consumer<Manuscript> onOpen;
    // null: no result can be shown in its folder
    private final Consumer<Manuscript> onShow;
    // the search is limited to this folder (and below), when scoped
    private final Folder folder;
    private final boolean scoped;
    final Checkbox subfolders = new Checkbox();

    final TextField name = new TextField();
    final MultiSelectComboBox<Tag> includeTags = new MultiSelectComboBox<>();
    final ComboBox<TagMatch> includeMatch = new ComboBox<>();
    final MultiSelectComboBox<Tag> excludeTags = new MultiSelectComboBox<>();
    private final ComboBox<TagMatch> excludeMatch = new ComboBox<>();
    private final DatePicker createdFrom = new DatePicker();
    final DatePicker createdTo = new DatePicker();
    private final DatePicker modifiedFrom = new DatePicker();
    private final DatePicker modifiedTo = new DatePicker();
    final IntegerField partsFrom = new IntegerField();
    private final IntegerField partsTo = new IntegerField();
    final TextField fulltext = new TextField();
    final Checkbox allBranches = new Checkbox();
    private final Span summary = new Span();
    private final Grid<Row> results = new Grid<>();

    /**
     * @param onOpen opens the book chosen in the list of the results
     */
    public ManuscriptSearchDialog(Consumer<Manuscript> onOpen) {
        this.onOpen = onOpen;
        this.onShow = null;
        this.folder = null;
        this.scoped = false;
        build();
    }

    /**
     * Searches the books of a folder only.
     *
     * @param folder the folder, null is the top level (with the checkbox ticked: every book)
     * @param onOpen opens the book chosen in the list of the results, null: a result has no button for it
     * @param onShow shows the book in its folder, null: a result has no button for it
     */
    public ManuscriptSearchDialog(Folder folder, Consumer<Manuscript> onOpen, Consumer<Manuscript> onShow) {
        this.onOpen = onOpen;
        this.onShow = onShow;
        this.folder = folder;
        this.scoped = true;
        // opened over the Organise window, which is modal
        setModality(ModalityMode.STRICT);
        build();
    }

    private void build() {
        setHeaderTitle(loc.getValue(L.LABEL_ADVANCED_SEARCH) + (scoped ? ": " + scopeName() : ""));
        setWidth("min(1000px, 95vw)");

        subfolders.setLabel(loc.getValue(L.LABEL_INCLUDE_SUBFOLDERS));
        subfolders.setVisible(scoped);

        name.setLabel(loc.getValue(L.LABEL_NAME));
        name.setClearButtonVisible(true);
        name.addKeyDownListener(Key.ENTER, e -> search());

        tags(includeTags, L.LABEL_TAGS_INCLUDE);
        tagMatch(includeMatch, TagMatch.ALL);
        tags(excludeTags, L.LABEL_TAGS_EXCLUDE);
        tagMatch(excludeMatch, TagMatch.ANY);

        date(createdFrom, L.LABEL_CREATED_FROM);
        date(createdTo, L.LABEL_CREATED_TO);
        date(modifiedFrom, L.LABEL_MODIFIED_FROM);
        date(modifiedTo, L.LABEL_MODIFIED_TO);

        count(partsFrom, L.LABEL_PARTS_FROM);
        count(partsTo, L.LABEL_PARTS_TO);

        fulltext.setLabel(loc.getValue(L.LABEL_TEXT));
        fulltext.setClearButtonVisible(true);
        fulltext.addKeyDownListener(Key.ENTER, e -> search());
        allBranches.setLabel(loc.getValue(L.LABEL_SEARCH_IN_ALL_BRANCHES));
        allBranches.setValue(false);
        // the fields share rows and wrap when the dialog is narrow
        name.setMinWidth("12em");
        fulltext.setMinWidth("14em");
        FlexLayout textRow = row(name, fulltext, allBranches);
        textRow.setFlexGrow(0, allBranches);

        FlexLayout tagsRow = row(group(includeTags, includeMatch), group(excludeTags, excludeMatch));

        FlexLayout rangeRow = row(createdFrom, createdTo, modifiedFrom, modifiedTo, partsFrom, partsTo);

        VerticalLayout form = new VerticalLayout(subfolders, textRow, tagsRow, rangeRow);
        form.setPadding(false);
        form.setSpacing(false);
        form.addClassName(CssStyles.GAP_S);
        form.setWidthFull();

        Button search = new Button(loc.getValue(L.LABEL_SEARCH), Solid.SEARCH.create(), e -> search());
        search.setThemeName("primary");
        Button clear = new Button(loc.getValue(L.LABEL_CLEAR), e -> clear());
        HorizontalLayout buttons = new HorizontalLayout(search, clear, summary);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        summary.addClassName(CssStyles.HINT_TEXT);

        results.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        results.addColumn(Row::name).setHeader(loc.getValue(L.LABEL_NAME)).setSortable(true)
                .setComparator(Comparator.comparing(Row::name, String.CASE_INSENSITIVE_ORDER)).setFlexGrow(2);
        results.addColumn(Row::tags).setHeader(loc.getValue(L.LABEL_TAGS)).setFlexGrow(2);
        results.addComponentColumn(this::tools).setHeader("").setFlexGrow(0).setAutoWidth(true);
        results.setHeight("40vh");
        results.setWidthFull();
        results.setVisible(false);

        VerticalLayout content = new VerticalLayout(form, buttons, results);
        content.setPadding(false);
        content.setWidthFull();
        add(content);

        getFooter().add(new Button(loc.getValue(L.LABEL_CLOSE), e -> close()));
    }

    private void tags(MultiSelectComboBox<Tag> box, L label) {
        box.setLabel(loc.getValue(label));
        box.setMinWidth("8em");
        box.setItemLabelGenerator(Tag::getValue);
        box.setClearButtonVisible(true);
        box.setItems(query -> {
            try {
                return tagService.searchTagsForUser(query.getFilter().orElse(""), query.getOffset(), query.getLimit()).stream();
            } catch (Exception e) {
                log.error("Failed to fetch tags for filter: {}", query.getFilter().orElse(""), e);
                return Stream.empty();
            }
        });
    }

    private void tagMatch(ComboBox<TagMatch> box, TagMatch value) {
        box.setItems(TagMatch.values());
        box.setItemLabelGenerator(match -> loc.getValue(match == TagMatch.ALL ? L.ENUM_TAG_MATCH_ALL : L.ENUM_TAG_MATCH_ANY));
        box.setAllowCustomValue(false);
        box.setValue(value);
        box.setWidth("10em");
        box.addValueChangeListener(e -> {
            if (e.getValue() == null) {
                box.setValue(e.getOldValue());
            }
        });
    }

    /**
     * The tags and the choice of all or any of them side by side.
     */
    private FlexLayout group(MultiSelectComboBox<Tag> tags, ComboBox<TagMatch> match) {
        FlexLayout group = new FlexLayout(tags, match);
        group.setAlignItems(FlexComponent.Alignment.END);
        group.setFlexGrow(1, tags);
        group.setMinWidth("22em");
        group.addClassName(CssStyles.GAP_S);
        return group;
    }

    /**
     * A row of fields that grow to fill it and wrap when it is too narrow.
     */
    private FlexLayout row(Component... fields) {
        FlexLayout row = new FlexLayout(fields);
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.END);
        row.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        row.setFlexGrow(1, fields);
        row.addClassName(CssStyles.GAP_S);
        return row;
    }

    private void date(DatePicker picker, L label) {
        picker.setLabel(loc.getValue(label));
        picker.setLocale(loc.getLocale());
        picker.setClearButtonVisible(true);
        picker.setMinWidth("9em");
    }

    private void count(IntegerField field, L label) {
        field.setLabel(loc.getValue(label));
        field.setMin(0);
        field.setStepButtonsVisible(true);
        field.setClearButtonVisible(true);
        field.setMinWidth("8em");
        field.addKeyDownListener(Key.ENTER, e -> search());
    }

    private String scopeName() {
        try {
            return folder == null ? loc.getValue(L.LABEL_TOP_LEVEL) : folderService.getPath(folder);
        } catch (Exception e) {
            return folder.getName();
        }
    }

    private HorizontalLayout tools(Row row) {
        HorizontalLayout tools = new HorizontalLayout();
        tools.setSpacing(false);
        if (onShow != null) {
            Button show = new Button(Solid.FOLDER.create(), e -> onShow.accept(row.manuscript));
            show.setTooltipText(loc.getValue(L.LABEL_SHOW_IN_FOLDER));
            tools.add(show);
        }
        if (onOpen != null) {
            Button open = new Button(Solid.FOLDER_OPEN.create(), e -> onOpen.accept(row.manuscript));
            open.setTooltipText(loc.getValue(L.LABEL_OPEN));
            tools.add(open);
        }
        return tools;
    }

    private void clear() {
        name.clear();
        includeTags.clear();
        includeMatch.setValue(TagMatch.ALL);
        excludeTags.clear();
        excludeMatch.setValue(TagMatch.ANY);
        createdFrom.clear();
        createdTo.clear();
        modifiedFrom.clear();
        modifiedTo.clear();
        partsFrom.clear();
        partsTo.clear();
        fulltext.clear();
        allBranches.setValue(false);
        subfolders.setValue(false);
        results.setVisible(false);
        summary.setText("");
    }

    /**
     * What the fields ask for; the day of a "to" date is included.
     */
    ManuscriptSearchCriteria criteria() {
        ManuscriptSearchCriteria criteria = new ManuscriptSearchCriteria();
        criteria.setName(StringUtils.trimToNull(name.getValue()));
        criteria.setIncludeTags(includeTags.getValue().stream().map(Tag::getValue).sorted().toList());
        criteria.setIncludeMatch(includeMatch.getValue());
        criteria.setExcludeTags(excludeTags.getValue().stream().map(Tag::getValue).sorted().toList());
        criteria.setExcludeMatch(excludeMatch.getValue());
        criteria.setCreatedFrom(startOf(createdFrom.getValue()));
        criteria.setCreatedTo(startOf(createdTo.getValue() == null ? null : createdTo.getValue().plusDays(1)));
        criteria.setModifiedFrom(startOf(modifiedFrom.getValue()));
        criteria.setModifiedTo(startOf(modifiedTo.getValue() == null ? null : modifiedTo.getValue().plusDays(1)));
        criteria.setMinParts(partsFrom.getValue());
        criteria.setMaxParts(partsTo.getValue());
        criteria.setFulltext(StringUtils.trimToNull(fulltext.getValue()));
        criteria.setAllBranches(allBranches.getValue());
        return criteria;
    }

    private static Date startOf(LocalDate day) {
        return day == null ? null : Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Searches with the fields and lists the books found, the last modified first.
     */
    void search() {
        try {
            List<Long> ids = manuscriptService.searchAdvanced(criteria(), Sorter.sorter("modification", Ordering.DESC));
            Map<Long, Manuscript> books = new HashMap<>();
            for (Manuscript book : scoped ? manuscriptService.findRegularInFolder(folder, subfolders.getValue())
                    : manuscriptService.findAllRegularForUser()) {
                books.put(book.getId(), book);
            }
            Map<Long, List<Tag>> tags = tagRelationService.getTagsForObjects(ids, Manuscript.class);

            List<Row> rows = new ArrayList<>();
            for (Long id : ids) {
                Manuscript book = books.get(id);
                if (book != null) {
                    rows.add(new Row(book, tags.getOrDefault(id, List.of()).stream().map(Tag::getValue).sorted().toList()));
                }
            }
            results.setItems(rows);
            results.setVisible(true);
            summary.setText(rows.isEmpty() ? loc.getValue(L.MSG_SEARCH_NO_RESULTS) : String.format(loc.getValue(L.MSG_BOOKS_FOUND), rows.size()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * The books listed as the results (for tests).
     */
    List<String> resultNames() {
        return results.isVisible()
                ? results.getListDataView().getItems().map(Row::name).toList()
                : List.of();
    }

    private record Row(Manuscript manuscript, List<String> tagValues) {
        String name() {
            return manuscript.getName();
        }

        String tags() {
            return String.join(", ", tagValues);
        }
    }
}
