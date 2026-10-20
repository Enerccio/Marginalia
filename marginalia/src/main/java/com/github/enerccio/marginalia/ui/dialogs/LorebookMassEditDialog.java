package com.github.enerccio.marginalia.ui.dialogs;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.TagMultiComboBox;
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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

/**
 * Edits all the entries of a lorebook at once: the entries of the lorebook and of its subbooks (however deep) in one grid,
 * by their order. The enabled box, the name, the order and the tags of an entry are changed in place, the text is only
 * shown, shortened; the lorebook an entry is in is a box in its row, which moves it to the lorebook itself or to one of its
 * subbooks. The entries that are checked can be moved together. A lorebook is locked while an agent works on a book that
 * uses it: its entries are shown grayed out, nothing can be done with them, and nothing can be moved into it.
 */
@Configurable
public class LorebookMassEditDialog extends Dialog {

    /**
     * How many characters of the text of an entry the grid shows.
     */
    static final int CONTENT_PREVIEW_LENGTH = 300;

    /**
     * One entry of the grid, with the lorebook it is in, whether that lorebook is locked and whether it is checked.
     */
    static final class Row {
        private final LorebookEntry entry;
        private final Lorebook book;
        private final boolean locked;
        private boolean checked;

        private Row(LorebookEntry entry, Lorebook book, boolean locked) {
            this.entry = entry;
            this.book = book;
            this.locked = locked;
        }

        LorebookEntry entry() {
            return entry;
        }

        Lorebook book() {
            return book;
        }

        boolean locked() {
            return locked;
        }

        boolean checked() {
            return checked;
        }
    }

    @Autowired
    private Localization loc;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private AgentRunService agentRunService;

    private final Lorebook root;
    private final Runnable onChange;

    private Grid<Row> grid;
    private Checkbox allBox;
    private ComboBox<Lorebook> targetBox;
    private Button moveChecked;
    private Span lockedHint;

    // the lorebook and its subbooks, the lorebook first; the ones an agent works on are in lockedBooks
    private final List<Lorebook> books = new ArrayList<>();
    private final Set<Long> lockedBooks = new HashSet<>();
    private final List<Row> rows = new ArrayList<>();
    private boolean changed;

    /**
     * @param root     the lorebook whose entries, and those of its subbooks, are edited
     * @param onChange told when the dialog is closed after something was changed, may be null
     */
    public LorebookMassEditDialog(Lorebook root, Runnable onChange) {
        this.root = root;
        this.onChange = onChange;
    }

    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_MASS_EDIT) + ": " + StringUtils.defaultString(root.getName()));
        setWidth("1400px");
        setMaxWidth("96%");
        setHeight("85%");
        setModality(ModalityMode.STRICT);
        setCloseOnOutsideClick(false);
        UIUtils.fitPhone(this, true);

        Span hint = new Span(loc.getValue(L.HELP_LOREBOOK_MASS_EDIT));
        hint.addClassName(CssStyles.HINT_TEXT);

        lockedHint = new Span(loc.getValue(L.LABEL_LOCKED_BY_AGENT));
        lockedHint.addClassName(CssStyles.HINT_TEXT);
        lockedHint.setVisible(false);

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
        grid.addComponentColumn(this::enabledBox).setHeader("").setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE);
        grid.addComponentColumn(this::nameField).setHeader(loc.getValue(L.LABEL_ENTRY_NAME)).setFlexGrow(2);
        grid.addComponentColumn(this::orderField).setHeader(loc.getValue(L.LABEL_ORDER)).setFlexGrow(0)
                .setWidth(UIConstants.TOOL_COLUMN_SIZE_LARGE);
        grid.addComponentColumn(this::bookBox).setHeader(loc.getValue(L.LABEL_LOREBOOK)).setFlexGrow(2);
        grid.addComponentColumn(this::tagsBox).setHeader(loc.getValue(L.LABEL_TAGS)).setFlexGrow(2);
        grid.addComponentColumn(this::contentCell).setHeader(loc.getValue(L.LABEL_CONTENT)).setFlexGrow(4);

        targetBox = new ComboBox<>(loc.getValue(L.LABEL_MOVE_TO_LOREBOOK));
        targetBox.setItemLabelGenerator(book -> StringUtils.defaultString(book.getName()));
        targetBox.addValueChangeListener(event -> updateButtons());
        moveChecked = new Button(loc.getValue(L.LABEL_MOVE_CHECKED), Solid.EXCHANGE_ALT.create(), event -> moveChecked());
        moveChecked.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button refresh = new Button(loc.getValue(L.LABEL_REFRESH), Solid.REFRESH.create(), event -> reload());
        Button close = new Button(loc.getValue(L.LABEL_CLOSE), event -> close());
        HorizontalLayout moveLayout = new HorizontalLayout(targetBox, moveChecked, refresh);
        moveLayout.setAlignItems(FlexComponent.Alignment.END);
        moveLayout.setWrap(true);
        HorizontalLayout footer = new HorizontalLayout(moveLayout, close);
        footer.setWidthFull();
        footer.setAlignItems(FlexComponent.Alignment.END);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footer.setWrap(true);
        getFooter().add(footer);

        VerticalLayout content = new VerticalLayout(hint, lockedHint, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.addClassName(CssStyles.MIN_HEIGHT_0);
        content.expand(grid);
        add(content);

        addOpenedChangeListener(event -> {
            if (!event.isOpened() && changed && onChange != null) {
                onChange.run();
            }
        });

        load();
        updateButtons();
    }

    /**
     * Reads the lorebooks, the locks and the entries again. The checks stay.
     */
    public void load() {
        try {
            List<Lorebook> loadedBooks = lorebookService.getLorebookTree(root);
            Set<Long> locked = new HashSet<>();
            for (Lorebook book : loadedBooks) {
                if (agentRunService.findLockingRun(book) != null) {
                    locked.add(book.getId());
                }
            }
            Set<Long> checked = new HashSet<>();
            rows.stream().filter(row -> row.checked).forEach(row -> checked.add(row.entry.getId()));
            List<Row> loaded = new ArrayList<>();
            for (Lorebook book : loadedBooks) {
                boolean isLocked = locked.contains(book.getId());
                for (LorebookEntry entry : lorebookEntryService.getEntriesForLorebook(book.getId())) {
                    Row row = new Row(entry, book, isLocked);
                    row.checked = !isLocked && checked.contains(entry.getId());
                    loaded.add(row);
                }
            }
            books.clear();
            books.addAll(loadedBooks);
            lockedBooks.clear();
            lockedBooks.addAll(locked);
            rows.clear();
            rows.addAll(loaded);
            sort();
            List<Lorebook> targets = books.stream().filter(book -> !lockedBooks.contains(book.getId())).toList();
            targetBox.setItems(targets);
            if (targetBox.getValue() != null && targets.stream().noneMatch(book -> Objects.equals(book.getId(), targetBox.getValue().getId()))) {
                targetBox.clear();
            }
            lockedHint.setVisible(!lockedBooks.isEmpty());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * By the order of the entries, the ones with the same order by lorebook, then by name.
     */
    private void sort() {
        Comparator<Row> byBook = Comparator.comparingInt(row -> indexOf(row.book));
        rows.sort(Comparator.comparingInt((Row row) -> row.entry.getOrder())
                .thenComparing(byBook)
                .thenComparing(row -> StringUtils.defaultString(row.entry.getName()), String.CASE_INSENSITIVE_ORDER));
        grid.setItems(rows);
    }

    private int indexOf(Lorebook book) {
        for (int i = 0; i < books.size(); i++) {
            if (Objects.equals(books.get(i).getId(), book.getId())) {
                return i;
            }
        }
        return books.size();
    }

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

    private Checkbox enabledBox(Row row) {
        Checkbox box = new Checkbox(row.entry.isEnabled());
        box.setEnabled(!row.locked);
        box.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                row.entry.setEnabled(Boolean.TRUE.equals(event.getValue()));
                save(row);
            }
        });
        return dimmed(box, row);
    }

    private TextField nameField(Row row) {
        TextField field = new TextField();
        field.setWidthFull();
        field.setValue(StringUtils.defaultString(row.entry.getName()));
        field.setReadOnly(row.locked);
        field.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                row.entry.setName(event.getValue());
                save(row);
            }
        });
        return dimmed(field, row);
    }

    private IntegerField orderField(Row row) {
        IntegerField field = new IntegerField();
        field.setWidth("100px");
        field.setValue(row.entry.getOrder());
        field.setReadOnly(row.locked);
        field.addValueChangeListener(event -> {
            if (event.isFromClient() && event.getValue() != null) {
                row.entry.setOrder(event.getValue());
                if (save(row)) {
                    // the entry has its place by the new order
                    sort();
                }
            }
        });
        return dimmed(field, row);
    }

    private ComboBox<Lorebook> bookBox(Row row) {
        ComboBox<Lorebook> box = new ComboBox<>();
        box.setWidthFull();
        box.setItemLabelGenerator(book -> StringUtils.defaultString(book.getName()));
        // the book of a locked entry is shown, nothing else can be picked
        List<Lorebook> items = row.locked ? List.of(row.book)
                : books.stream().filter(book -> !lockedBooks.contains(book.getId())).toList();
        box.setItems(items);
        items.stream().filter(book -> Objects.equals(book.getId(), row.book.getId())).findFirst().ifPresent(box::setValue);
        box.setEnabled(!row.locked);
        box.addValueChangeListener(event -> {
            if (event.isFromClient() && event.getValue() != null) {
                move(List.of(row), event.getValue());
            }
        });
        return dimmed(box, row);
    }

    private TagMultiComboBox tagsBox(Row row) {
        TagMultiComboBox box = new TagMultiComboBox();
        box.setWidthFull();
        box.setForEntity(row.entry);
        box.setEnabled(!row.locked);
        return dimmed(box, row);
    }

    private Div contentCell(Row row) {
        String text = StringUtils.defaultString(row.entry.getPayload());
        Div cell = new Div(new Span(StringUtils.abbreviate(text, CONTENT_PREVIEW_LENGTH)));
        cell.addClassName(CssStyles.MASS_EDIT_CONTENT);
        return dimmed(cell, row);
    }

    /**
     * An entry of a locked lorebook is shown grayed out.
     */
    private <T extends Component> T dimmed(T component, Row row) {
        if (row.locked) {
            component.addClassName(CssStyles.MULTI_TAG_LOCKED);
            UIUtils.addTooltip(component, loc.getValue(L.LABEL_LOCKED_BY_AGENT));
        }
        return component;
    }

    private void updateButtons() {
        if (moveChecked != null) {
            moveChecked.setEnabled(targetBox.getValue() != null && rows.stream().anyMatch(row -> row.checked && !row.locked));
        }
    }

    /**
     * Saves the changed fields of the entry; false when it could not be saved (the lorebook was locked meanwhile), the
     * grid is then read again from the database.
     */
    private boolean save(Row row) {
        try {
            lorebookEntryService.save(row.entry);
            changed = true;
            return true;
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            reload();
            return false;
        }
    }

    private void moveChecked() {
        Lorebook target = targetBox.getValue();
        List<Row> checked = rows.stream().filter(row -> row.checked).toList();
        if (target == null || checked.isEmpty()) {
            Notification.warning(loc.getValue(L.MSG_MOVE_PICK_FIRST));
            return;
        }
        move(checked, target);
    }

    /**
     * Moves the entries to the lorebook. The ones that are in it already, and the ones of a lorebook an agent works on by
     * now, stay; a lorebook an agent works on by now takes nothing.
     */
    private void move(List<Row> targets, Lorebook target) {
        int moved = 0;
        try {
            for (Row row : targets) {
                if (row.locked || Objects.equals(row.book.getId(), target.getId())) {
                    continue;
                }
                lorebookEntryService.move(row.entry, target);
                moved++;
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        if (moved > 0) {
            changed = true;
            Notification.success(String.format(loc.getValue(L.MSG_ENTRIES_MOVED), moved, StringUtils.defaultString(target.getName())));
        }
        reload();
    }

    private void reload() {
        load();
        updateButtons();
    }

    // for the tests of the dialog

    Grid<Row> getGrid() {
        return grid;
    }

    List<Row> getRows() {
        return rows;
    }

    List<Lorebook> getBooks() {
        return books;
    }

    ComboBox<Lorebook> getTargetBox() {
        return targetBox;
    }

    Button getMoveChecked() {
        return moveChecked;
    }

    void check(Row row, boolean checked) {
        row.checked = checked;
        updateButtons();
    }

    void moveTo(Row row, Lorebook target) {
        move(List.of(row), target);
    }

    void setOrder(Row row, int order) {
        row.entry.setOrder(order);
        if (save(row)) {
            sort();
        }
    }
}
