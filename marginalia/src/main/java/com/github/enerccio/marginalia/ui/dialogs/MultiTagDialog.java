package com.github.enerccio.marginalia.ui.dialogs;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.stream.Stream;

/**
 * Puts one tag on many books, lorebooks or lore entries (or takes it off them) without opening each. The user picks the tag
 * (or types a new one), and the list shows every item with its tags; the items that have the picked tag come first, and the
 * tag is marked among theirs. Each row has buttons to apply or remove the tag for that item, the checkboxes and the buttons
 * at the bottom do the same for all the checked ones at once. An item an agent works on is locked: it is shown grayed out,
 * and nothing can be done with it.
 */
@Configurable
@Extendable
public class MultiTagDialog extends Dialog implements LLMInteract {

    private static final Logger log = LoggerFactory.getLogger(MultiTagDialog.class);

    /**
     * What the dialog tags.
     */
    public enum Kind {
        BOOKS(Manuscript.class, L.LABEL_TAG_BOOKS),
        LOREBOOKS(Lorebook.class, L.LABEL_TAG_LOREBOOKS),
        ENTRIES(LorebookEntry.class, L.LABEL_TAG_ENTRIES);

        private final Class<? extends BaseEntity> clazz;
        private final L title;

        Kind(Class<? extends BaseEntity> clazz, L title) {
            this.clazz = clazz;
            this.title = title;
        }
    }

    /**
     * One item of the list: what is tagged, how it is shown, whether it can be changed, its tags now and whether it is
     * checked.
     */
    static final class Row {
        private final BaseEntity entity;
        private final String name;
        private final boolean locked;
        private List<Tag> tags = new ArrayList<>();
        private boolean checked;

        private Row(BaseEntity entity, String name, boolean locked) {
            this.entity = entity;
            this.name = name;
            this.locked = locked;
        }

        BaseEntity entity() {
            return entity;
        }

        String name() {
            return name;
        }

        boolean locked() {
            return locked;
        }

        boolean checked() {
            return checked;
        }

        List<Tag> tags() {
            return tags;
        }
    }

    @Autowired
    private Localization loc;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private AgentRunService agentRunService;

    private final Kind kind;
    // the lorebook whose entries are tagged, null for the books and the lorebooks
    private final Lorebook lorebook;
    private final Runnable onChange;

    private ComboBox<Tag> tagBox;
    private Grid<Row> grid;
    private Checkbox allBox;
    private Button applyChecked;
    private Button removeChecked;
    private final List<Row> rows = new ArrayList<>();

    /**
     * Tags the books or the lorebooks of the user.
     *
     * @param onChange told after tags were changed, may be null
     */
    public MultiTagDialog(Kind kind, Runnable onChange) {
        this(kind, null, onChange);
    }

    /**
     * Tags the entries of the lorebook.
     */
    public MultiTagDialog(Lorebook lorebook, Runnable onChange) {
        this(Kind.ENTRIES, lorebook, onChange);
    }

    private MultiTagDialog(Kind kind, Lorebook lorebook, Runnable onChange) {
        this.kind = kind;
        this.lorebook = lorebook;
        this.onChange = onChange;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "hint", type = Span.class),
                    @APILocals(name = "close", type = Button.class),
                    @APILocals(name = "footer", type = HorizontalLayout.class),
                    @APILocals(name = "content", type = VerticalLayout.class)})
    public void create() {
        setHeaderTitle(loc.getValue(kind.title));
        setWidth("900px");
        setMaxWidth("96%");
        setHeight("80%");
        setModality(ModalityMode.STRICT);
        setCloseOnOutsideClick(false);
        UIUtils.fitPhone(this, true);

        Span hint = new Span(loc.getValue(L.HELP_MULTI_TAG));
        hint.addClassName(CssStyles.HINT_TEXT);

        tagBox = new ComboBox<>(loc.getValue(L.ENUM_DOMAIN_TAG));
        tagBox.setWidthFull();
        tagBox.setClearButtonVisible(true);
        tagBox.setAllowCustomValue(true);
        tagBox.setItemLabelGenerator(Tag::getValue);
        tagBox.setItems(query -> {
            try {
                return tagService.searchTagsForUser(query.getFilter().orElse(""), query.getOffset(), query.getLimit()).stream();
            } catch (Exception e) {
                log.error("Failed to fetch tags for filter: {}", query.getFilter().orElse(""), e);
                return Stream.empty();
            }
        });
        tagBox.addCustomValueSetListener(event -> createTag(event.getDetail()));
        tagBox.addValueChangeListener(event -> {
            sort();
            updateButtons();
        });

        allBox = new Checkbox();
        allBox.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                rows.stream().filter(row -> !row.locked).forEach(row -> row.checked = event.getValue());
                grid.getDataProvider().refreshAll();
                updateButtons();
            }
        });
        UIUtils.addTooltip(allBox, loc.getValue(L.LABEL_SELECT_ALL));

        grid = new Grid<>();
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.addComponentColumn(this::checkbox).setHeader(allBox).setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE);
        grid.addComponentColumn(this::nameCell).setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(2);
        grid.addComponentColumn(this::tagsCell).setHeader(loc.getValue(L.LABEL_TAGS)).setFlexGrow(3);
        grid.addComponentColumn(this::actions).setHeader("").setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE_HUGE);

        applyChecked = new Button(loc.getValue(L.LABEL_APPLY_TAG_TO_CHECKED), Solid.TAG.create(), event -> applyToChecked(true));
        applyChecked.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        removeChecked = new Button(loc.getValue(L.LABEL_REMOVE_TAG_FROM_CHECKED), Solid.TAG.create(), event -> applyToChecked(false));
        Button close = new Button(loc.getValue(L.LABEL_CLOSE), event -> close());
        HorizontalLayout footer = new HorizontalLayout(applyChecked, removeChecked);
        footer.setWidthFull();
        footer.setWrap(true);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.add(close);
        footer.expand(close);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        getFooter().add(footer);

        VerticalLayout content = new VerticalLayout(hint, tagBox, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.addClassName(CssStyles.MIN_HEIGHT_0);
        content.expand(grid);
        add(content);

        load();
        updateButtons();
    }

    /**
     * Reads the items and their tags again. The checks and the picked tag stay.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "loaded", type = List.class),
                    @APILocals(name = "book", type = Manuscript.class),
                    @APILocals(name = "book", type = Lorebook.class),
                    @APILocals(name = "entry", type = LorebookEntry.class),
                    @APILocals(name = "checked", type = Set.class),
                    @APILocals(name = "tags", type = Map.class),
                    @APILocals(name = "row", type = MultiTagDialog.Row.class),
                    @APILocals(name = "e", type = Exception.class)})
    public void load() {
        try {
            List<Row> loaded = new ArrayList<>();
            switch (kind) {
                case BOOKS -> {
                    for (Manuscript book : manuscriptService.findAllRegularForUser()) {
                        loaded.add(new Row(book, book.getName(), agentRunService.findLockingRun(book) != null));
                    }
                }
                case LOREBOOKS -> {
                    for (Lorebook book : lorebookService.findAllRegularForUser()) {
                        loaded.add(new Row(book, book.getName(), agentRunService.findLockingRun(book) != null));
                    }
                }
                case ENTRIES -> {
                    // the entries are locked with their lorebook
                    boolean locked = lorebook != null && agentRunService.findLockingRun(lorebook) != null;
                    for (LorebookEntry entry : lorebook == null ? List.<LorebookEntry>of() : lorebookEntryService.getEntriesForLorebook(lorebook)) {
                        loaded.add(new Row(entry, entry.getName(), locked));
                    }
                }
            }
            Set<Long> checked = new HashSet<>();
            rows.stream().filter(row -> row.checked).forEach(row -> checked.add(row.entity.getId()));
            Map<Long, List<Tag>> tags = tagRelationService.getTagsForObjects(
                    loaded.stream().map(row -> row.entity.getId()).toList(), kind.clazz);
            for (Row row : loaded) {
                row.tags = new ArrayList<>(tags.getOrDefault(row.entity.getId(), List.of()));
                row.tags.sort(Comparator.comparing(Tag::getValue, String.CASE_INSENSITIVE_ORDER));
                row.checked = !row.locked && checked.contains(row.entity.getId());
            }
            rows.clear();
            rows.addAll(loaded);
            sort();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * The items with the picked tag first, the others after them, each group by name.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "tag", type = Tag.class)})
    private void sort() {
        if (grid == null) {
            return;
        }
        Tag tag = tagBox.getValue();
        rows.sort(Comparator.comparing((Row row) -> tag == null || !has(row, tag))
                .thenComparing(row -> StringUtils.defaultString(row.name), String.CASE_INSENSITIVE_ORDER));
        grid.setItems(rows);
    }

    private static boolean has(Row row, Tag tag) {
        return row.tags.stream().anyMatch(t -> Objects.equals(t.getId(), tag.getId()));
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Checkbox.class),
            parameters = {@APIParameter(name = "row", type = MultiTagDialog.Row.class)},
            locals = {@APILocals(name = "box", type = Checkbox.class)})
    private Checkbox checkbox(Row row) {
        Checkbox box = new Checkbox(row.checked);
        box.setEnabled(!row.locked);
        dimmed(box, row);
        box.addValueChangeListener(event -> {
            row.checked = event.getValue();
            updateButtons();
        });
        return box;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Span.class),
            parameters = {@APIParameter(name = "row", type = MultiTagDialog.Row.class)},
            locals = {@APILocals(name = "name", type = Span.class)})
    private Span nameCell(Row row) {
        Span name = new Span(StringUtils.defaultString(row.name));
        if (row.locked) {
            UIUtils.addTooltip(name, loc.getValue(L.LABEL_LOCKED_BY_AGENT));
        }
        return dimmed(name, row);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Div.class),
            parameters = {@APIParameter(name = "row", type = MultiTagDialog.Row.class)},
            locals = {@APILocals(name = "picked", type = Tag.class),
                    @APILocals(name = "cell", type = Div.class),
                    @APILocals(name = "tag", type = Tag.class),
                    @APILocals(name = "chip", type = Span.class)})
    Div tagsCell(Row row) {
        Tag picked = tagBox.getValue();
        Div cell = new Div();
        for (Tag tag : row.tags) {
            Span chip = new Span(tag.getValue());
            chip.addClassName(CssStyles.MULTI_TAG_CHIP);
            if (picked != null && Objects.equals(picked.getId(), tag.getId())) {
                chip.addClassName(CssStyles.MULTI_TAG_CHIP_MARKED);
            }
            cell.add(chip);
        }
        return dimmed(cell, row);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = HorizontalLayout.class),
            parameters = {@APIParameter(name = "row", type = MultiTagDialog.Row.class)},
            locals = {@APILocals(name = "apply", type = Button.class),
                    @APILocals(name = "remove", type = Button.class)})
    private HorizontalLayout actions(Row row) {
        Button apply = new Button(Solid.PLUS.create(), event -> change(List.of(row), true));
        apply.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        apply.setEnabled(!row.locked);
        UIUtils.addTooltip(apply, loc.getValue(L.LABEL_APPLY_TAG));
        Button remove = new Button(Solid.MINUS.create(), event -> change(List.of(row), false));
        remove.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        remove.setEnabled(!row.locked);
        UIUtils.addTooltip(remove, loc.getValue(L.LABEL_REMOVE_TAG));
        return dimmed(new HorizontalLayout(apply, remove), row);
    }

    /**
     * A locked item is shown grayed out.
     */
    private static <T extends Component> T dimmed(T component, Row row) {
        if (row.locked) {
            component.addClassName(CssStyles.MULTI_TAG_LOCKED);
        }
        return component;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    private void updateButtons() {
        if (applyChecked == null) {
            return;
        }
        boolean enabled = tagBox.getValue() != null && rows.stream().anyMatch(row -> row.checked && !row.locked);
        applyChecked.setEnabled(enabled);
        removeChecked.setEnabled(enabled);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "apply", type = boolean.class)})
    private void applyToChecked(boolean apply) {
        change(rows.stream().filter(row -> row.checked).toList(), apply);
    }

    /**
     * Puts the picked tag on the items, or takes it off. An item that was locked meanwhile is left alone.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "targets", type = List.class),
                    @APIParameter(name = "apply", type = boolean.class)},
            locals = {@APILocals(name = "tag", type = Tag.class),
                    @APILocals(name = "row", type = MultiTagDialog.Row.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void change(List<Row> targets, boolean apply) {
        Tag tag = tagBox.getValue();
        if (tag == null) {
            Notification.warning(loc.getValue(L.MSG_TAG_PICK_FIRST));
            return;
        }
        if (targets.isEmpty()) {
            Notification.warning(loc.getValue(L.MSG_TAG_CHECK_FIRST));
            return;
        }
        int changed = 0;
        try {
            for (Row row : targets) {
                if (row.locked || isLockedNow(row)) {
                    continue;
                }
                if (apply) {
                    tagRelationService.createRelation(tag, row.entity.getId(), kind.clazz);
                } else {
                    tagRelationService.removeRelation(tag, row.entity.getId(), kind.clazz);
                }
                changed++;
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        load();
        updateButtons();
        Notification.success(String.format(loc.getValue(apply ? L.MSG_TAG_APPLIED : L.MSG_TAG_REMOVED), tag.getValue(), changed));
        if (changed > 0 && onChange != null) {
            onChange.run();
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = boolean.class),
            parameters = {@APIParameter(name = "row", type = MultiTagDialog.Row.class)})
    private boolean isLockedNow(Row row) throws Exception {
        return switch (kind) {
            case BOOKS -> agentRunService.findLockingRun((Manuscript) row.entity) != null;
            case LOREBOOKS -> agentRunService.findLockingRun((Lorebook) row.entity) != null;
            case ENTRIES -> lorebook != null && agentRunService.findLockingRun(lorebook) != null;
        };
    }

    /**
     * A tag typed in the box that does not exist yet is created (an existing one with that value, in any case, is used) and
     * picked.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "typed", type = String.class)},
            locals = {@APILocals(name = "tag", type = Tag.class),
                    @APILocals(name = "e", type = Exception.class)})
    void createTag(String typed) {
        try {
            Tag tag = tagService.getOrCreateForUser(typed);
            if (tag != null) {
                tagBox.getDataProvider().refreshAll();
                tagBox.setValue(tag);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // for the tests of the dialog

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = ComboBox.class))
    ComboBox<Tag> getTagBox() {
        return tagBox;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Grid.class))
    Grid<Row> getGrid() {
        return grid;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = List.class))
    List<Row> getRows() {
        return rows;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Checkbox.class))
    Checkbox getAllBox() {
        return allBox;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Button.class))
    Button getApplyChecked() {
        return applyChecked;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Button.class))
    Button getRemoveChecked() {
        return removeChecked;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "row", type = MultiTagDialog.Row.class),
                    @APIParameter(name = "checked", type = boolean.class)})
    void check(Row row, boolean checked) {
        row.checked = checked;
        updateButtons();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "row", type = MultiTagDialog.Row.class),
                    @APIParameter(name = "apply", type = boolean.class)})
    void applyTo(Row row, boolean apply) {
        change(List.of(row), apply);
    }
}
