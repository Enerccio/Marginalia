package com.github.enerccio.marginalia.extensions.loregenerator.ui;

import com.github.enerccio.marginalia.CssStyles;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Chooses the categories of the name generator the model may ask names of. There are many of them: a lazy grid with a
 * filter field, where the checked categories stay in the grid whatever the filter says.
 */
public class NameToolsDialog extends Dialog {

    private final List<String> available;
    private final TextField filter = new TextField();
    private final Grid<String> categories = new Grid<>(String.class, false);
    private final Span count = new Span();

    /**
     * @param available all the categories of the name generator
     * @param checked   the categories checked now (the ones not in {@code available} are ignored)
     * @param onSave    gets the checked categories when the user saves
     */
    public NameToolsDialog(List<String> available, List<String> checked, Consumer<List<String>> onSave) {
        this.available = available;
        setHeaderTitle("Name tools");
        // the category names are long: wide enough for them, and the table takes the height
        setWidth("min(720px, 95vw)");
        setHeight("80vh");

        Span hint = new Span("The model can ask for names of the checked categories. None checked: no tools.");
        hint.addClassName(CssStyles.HINT_TEXT);

        filter.setPlaceholder("Filter categories");
        filter.setClearButtonVisible(true);
        filter.setPrefixComponent(VaadinIcon.SEARCH.create());
        filter.setValueChangeMode(ValueChangeMode.LAZY);
        filter.addClassName(CssStyles.FULL_WIDTH);
        // only the filter changes the rows: an item unchecked while it does not match stays until the next change of the filter
        filter.addValueChangeListener(e -> categories.getDataProvider().refreshAll());

        GridMultiSelectionModel<String> selection = (GridMultiSelectionModel<String>) categories.setSelectionMode(Grid.SelectionMode.MULTI);
        selection.setSelectAllCheckboxVisibility(GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
        categories.addColumn(c -> c).setHeader("Category");
        categories.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        categories.setSizeFull();
        categories.addClassName(CssStyles.MIN_HEIGHT_0);
        categories.setItems(
                query -> shown().skip(query.getOffset()).limit(query.getLimit()),
                query -> (int) shown().count());
        categories.addSelectionListener(e -> showCount(e.getAllSelectedItems().size()));

        Set<String> saved = new LinkedHashSet<>(checked == null ? List.of() : checked);
        saved.retainAll(available);
        saved.forEach(categories::select);
        showCount(saved.size());
        count.addClassName(CssStyles.HINT_TEXT);

        VerticalLayout content = new VerticalLayout(hint, filter, categories, count);
        content.setPadding(false);
        content.setSizeFull();
        content.setFlexGrow(1, categories);
        add(content);

        Button save = new Button("Save", e -> {
            onSave.accept(new ArrayList<>(categories.getSelectedItems()));
            close();
        });
        save.setThemeName("primary");
        getFooter().add(new Button("Cancel", e -> close()), save);
    }

    private void showCount(int checked) {
        count.setText(checked + " checked of " + available.size());
    }

    /** The categories to list: the ones containing the filter text (any case) and the checked ones, in the order they come. */
    private Stream<String> shown() {
        String needle = filter.getValue() == null ? "" : filter.getValue().strip().toLowerCase(Locale.ROOT);
        Set<String> checked = categories.getSelectedItems();
        return available.stream().filter(c -> needle.isEmpty() || checked.contains(c) || c.toLowerCase(Locale.ROOT).contains(needle));
    }
}
