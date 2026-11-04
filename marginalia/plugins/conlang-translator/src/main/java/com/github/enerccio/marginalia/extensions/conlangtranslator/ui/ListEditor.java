package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * A list that is edited in place: add, edit, delete and reorder. The list given is changed (it belongs to the thing that is being edited,
 * which is a copy until its window is saved); {@code onChange} is told after every change. A subclass says the columns and how one item is
 * edited, and calls {@link #build()} as the last line of its constructor.
 *
 * @param <T> what the list holds
 */
public abstract class ListEditor<T> extends VerticalLayout {

    private final List<T> items;
    private final Runnable onChange;
    private final Grid<T> grid = new Grid<>();
    private final HorizontalLayout toolbar = new HorizontalLayout();

    private Button editButton;
    private Button deleteButton;
    private Button upButton;
    private Button downButton;

    protected ListEditor(List<T> items, Runnable onChange) {
        this.items = items;
        this.onChange = onChange;
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        setMinWidth("0");
    }

    protected abstract void addColumns(Grid<T> grid);

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
     * Takes the item out of the list; a subclass whose items are referred to from elsewhere cleans the references too.
     */
    protected void remove(T item) {
        items.remove(item);
    }

    protected List<T> items() {
        return items;
    }

    /** The item that is selected, null for none. */
    public T selected() {
        return grid.getSelectedItems().stream().findFirst().orElse(null);
    }

    protected Grid<T> grid() {
        return grid;
    }

    /**
     * Another button in the row above the grid.
     */
    protected void addToolbarButton(Button button) {
        toolbar.add(button);
    }

    /**
     * Shows the list again after something changed it.
     */
    public void refresh() {
        grid.setItems(items);
        updateButtons();
    }

    /**
     * Builds the content; the last line of the constructor of a subclass.
     */
    protected final void build() {
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        grid.setWidthFull();
        grid.setHeight("100%");
        addColumns(grid);
        grid.getColumns().forEach(column -> column.setResizable(true));
        grid.addSelectionListener(_ -> updateButtons());
        grid.addItemDoubleClickListener(event -> editItem(event.getItem()));
        grid.setItems(items);

        Button add = new Button(T.localized(ADD), Solid.PLUS.create(), _ -> add());
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton = new Button(T.localized(EDIT), Solid.PEN.create(), _ -> selected(this::editItem));
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
        toolbar.addComponentAtIndex(2, deleteButton);
        toolbar.addComponentAtIndex(3, upButton);
        toolbar.addComponentAtIndex(4, downButton);
        toolbar.setWidthFull();
        toolbar.setWrap(true);

        add(toolbar, grid);
        setFlexGrow(1, grid);
        updateButtons();
    }

    private void add() {
        T item = create();
        edit(item, true, () -> {
            items.add(item);
            changed();
            grid.select(item);
        });
    }

    private void editItem(T item) {
        edit(item, false, () -> {
            grid.getDataProvider().refreshItem(item);
            onChange.run();
            updateButtons();
        });
    }

    private void deleteItem(T item) {
        ConfirmDialog.show(T.localized(DELETE_S).formatted(labelOf(item)), () -> {
            remove(item);
            changed();
        });
    }

    private void move(T item, int by) {
        int index = items.indexOf(item);
        int target = index + by;
        if (index >= 0 && target >= 0 && target < items.size()) {
            Collections.swap(items, index, target);
            changed();
            grid.select(item);
        }
    }

    private void changed() {
        refresh();
        onChange.run();
    }

    private void selected(Consumer<T> action) {
        grid.getSelectedItems().stream().findFirst().ifPresent(action);
    }

    private void updateButtons() {
        T item = grid.getSelectedItems().stream().findFirst().orElse(null);
        int index = item == null ? -1 : items.indexOf(item);
        editButton.setEnabled(item != null);
        deleteButton.setEnabled(item != null);
        upButton.setEnabled(item != null && index > 0);
        downButton.setEnabled(item != null && index >= 0 && index < items.size() - 1);
    }
}
