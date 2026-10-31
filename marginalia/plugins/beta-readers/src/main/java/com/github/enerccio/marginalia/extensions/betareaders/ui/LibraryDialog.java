package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.*;

/**
 * A list the user edits in a window: add, edit, copy, delete and reorder, applied with <i>Save</i> and thrown away with
 * <i>Cancel</i>. It works on copies of the items, so nothing the caller holds changes until <i>Save</i>; the caller gets the new
 * list and stores it. A subclass says how an item looks in the grid and how it is edited and calls {@link #build()} as the last
 * line of its constructor.
 *
 * @param <T> what the list holds
 */
public abstract class LibraryDialog<T> extends Dialog {

    private final List<T> items = new ArrayList<>();
    private final Consumer<List<T>> onApply;
    private final UnaryOperator<T> duplicate;
    private final Grid<T> grid = new Grid<>();
    private final HorizontalLayout toolbar = new HorizontalLayout();

    private Button editButton;
    private Button copyButton;
    private Button deleteButton;
    private Button upButton;
    private Button downButton;

    /**
     * @param snapshot  an independent copy of an item (the same identity), what the dialog edits
     * @param duplicate a copy of an item with an identity of its own, what <i>Copy</i> adds
     * @param onApply   told with the list after <i>Save</i>
     */
    protected LibraryDialog(String title, List<T> source, UnaryOperator<T> snapshot, UnaryOperator<T> duplicate,
                            Consumer<List<T>> onApply) {
        this.onApply = onApply;
        this.duplicate = duplicate;
        source.forEach(item -> items.add(snapshot.apply(item)));

        setHeaderTitle(title);
        setWidth("760px");
        setMaxWidth("96vw");
        setMaxHeight("92vh");
        setCloseOnOutsideClick(false);
    }

    /**
     * Fills the grid with the columns of the items.
     */
    protected abstract void addColumns(Grid<T> grid);

    /**
     * A new item that is not in the list yet.
     */
    protected abstract T create();

    /**
     * Opens the editor of the item; {@code done} is run after the user saved it there (the editor writes into the item).
     */
    protected abstract void edit(T item, boolean isNew, Runnable done);

    /**
     * What the user knows the item by, for the question before it is deleted.
     */
    protected abstract String labelOf(T item);

    /**
     * The items as they are now, for the editor to check a name against.
     */
    protected List<T> items() {
        return items;
    }

    /**
     * Another button of the row of buttons above the grid; {@code change} adds items to the list itself and tells the dialog.
     */
    protected void addToolbarButton(Button button) {
        toolbar.add(button);
    }

    /**
     * Shows the list again, after the subclass changed it.
     */
    protected void refresh() {
        grid.setItems(items);
        updateButtons();
    }

    /**
     * Builds the content; the last line of the constructor of a subclass.
     */
    protected final void build() {
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        grid.setWidthFull();
        grid.setHeight("360px");
        addColumns(grid);
        grid.getColumns().forEach(column -> column.setResizable(true));
        grid.addSelectionListener(_ -> updateButtons());
        grid.addItemDoubleClickListener(event -> editItem(event.getItem()));
        grid.setItems(items);

        Button add = new Button(T.localized(ADD), Solid.PLUS.create(), _ -> add());
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton = new Button(T.localized(EDIT), Solid.PEN.create(), _ -> selected(this::editItem));
        copyButton = new Button(T.localized(COPY), Solid.COPY.create(), _ -> selected(this::copyItem));
        deleteButton = new Button(T.localized(DELETE), Solid.TRASH.create(), _ -> selected(this::deleteItem));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        upButton = new Button(Solid.ARROW_UP.create(), _ -> selected(item -> move(item, -1)));
        upButton.setTooltipText(T.localized(MOVE_UP));
        upButton.setAriaLabel(T.localized(MOVE_UP));
        downButton = new Button(Solid.ARROW_DOWN.create(), _ -> selected(item -> move(item, 1)));
        downButton.setTooltipText(T.localized(MOVE_DOWN));
        downButton.setAriaLabel(T.localized(MOVE_DOWN));
        toolbar.addComponentAtIndex(0, add);
        toolbar.addComponentAtIndex(1, editButton);
        toolbar.addComponentAtIndex(2, copyButton);
        toolbar.addComponentAtIndex(3, deleteButton);
        toolbar.addComponentAtIndex(4, upButton);
        toolbar.addComponentAtIndex(5, downButton);
        toolbar.setWidthFull();
        toolbar.setWrap(true);

        VerticalLayout content = new VerticalLayout(toolbar, grid);
        content.setPadding(false);
        content.setWidthFull();
        content.setMinWidth("0");
        add(content);

        Button cancel = new Button(T.localized(CANCEL), _ -> close());
        Button save = new Button(T.localized(SAVE), _ -> {
            onApply.accept(new ArrayList<>(items));
            close();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(cancel, save);
        updateButtons();
    }

    private void add() {
        T item = create();
        edit(item, true, () -> {
            items.add(item);
            refresh();
            grid.select(item);
        });
    }

    private void editItem(T item) {
        edit(item, false, () -> {
            grid.getDataProvider().refreshItem(item);
            updateButtons();
        });
    }

    private void copyItem(T item) {
        T copy = duplicate.apply(item);
        items.add(items.indexOf(item) + 1, copy);
        refresh();
        grid.select(copy);
    }

    private void deleteItem(T item) {
        ConfirmDialog.show(T.localized(DELETE_S).formatted(labelOf(item)), () -> {
            items.remove(item);
            refresh();
        });
    }

    private void move(T item, int by) {
        int index = items.indexOf(item);
        int target = index + by;
        if (index >= 0 && target >= 0 && target < items.size()) {
            Collections.swap(items, index, target);
            refresh();
            grid.select(item);
        }
    }

    private void selected(Consumer<T> action) {
        grid.getSelectedItems().stream().findFirst().ifPresent(action);
    }

    private void updateButtons() {
        boolean any = !grid.getSelectedItems().isEmpty();
        T item = grid.getSelectedItems().stream().findFirst().orElse(null);
        int index = item == null ? -1 : items.indexOf(item);
        editButton.setEnabled(any);
        copyButton.setEnabled(any);
        deleteButton.setEnabled(any);
        upButton.setEnabled(any && index > 0);
        downButton.setEnabled(any && index >= 0 && index < items.size() - 1);
    }
}
